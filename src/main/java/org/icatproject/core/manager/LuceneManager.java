package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Stateless
@Singleton
public class LuceneManager {

	public class IndexSome implements Callable<Void> {

		private List<Long> ids;
		private EntityManager manager;
		private Class<?> klass;
		private String entityName;

		public IndexSome(String entityName, List<Long> ids, EntityManagerFactory entityManagerFactory)
				throws IcatException {
			try {
				this.entityName = entityName;
				klass = Class.forName(Constants.ENTITY_PREFIX + entityName);
				this.ids = ids;
				manager = entityManagerFactory.createEntityManager();
			} catch (Exception e) {
				logger.error("About to throw internal exception because of", e);
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			}
		}

		@Override
		public Void call() throws Exception {

			URI uri = new URIBuilder(luceneApi.server).setPath(LuceneApi.basePath + "/addNow/" + entityName).build();
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(uri);
				PipedOutputStream beanDocs = new PipedOutputStream();
				httpPost.setEntity(new InputStreamEntity(new PipedInputStream(beanDocs)));
				getBeanDocExecutor.submit(() -> {
					try (JsonGenerator gen = Json.createGenerator(beanDocs)) {
						gen.writeStartArray();
						for (Long id : ids) {
							EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
							if (bean != null) {
								bean.getDoc(gen);
							}
						}
						gen.writeEnd();
						return null;
					} catch (Exception e) {
						logger.error("About to throw internal exception because of", e);
						throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
					} finally {
						manager.close();
					}
				});

				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					LuceneApi.checkStatus(response);
				}
			}
			return null;
		}
	}

	private enum PopState {
		POPULATING, STOPPING, STOPPED
	}

	public class PopulateThread extends Thread {

		private EntityManager manager;
		private EntityManagerFactory entityManagerFactory;

		public PopulateThread(EntityManagerFactory entityManagerFactory) {
			this.entityManagerFactory = entityManagerFactory;
			manager = entityManagerFactory.createEntityManager();
			logger.info("Start new populate thread");
		}

		@Override
		public void run() {

			try {
				while (!populateList.isEmpty()) {
					try {
						populatingClassName = populateList.first();
					} catch (NoSuchElementException e) {
					}

					if (populatingClassName != null) {
						luceneApi.lock(populatingClassName);

						Long start = -1L;

						logger.info("Lucene Populating " + populatingClassName);

						CompletionService<Void> threads = new ExecutorCompletionService<>(populateExecutor);
						int tasksIn = 0;

						while (true) {

							if (popState == PopState.STOPPING) {
								logger.info("PopulateThread stopping as flag was set");
								break;
							}
							/* Get next block of ids */
							List<Long> ids = manager
									.createQuery("SELECT e.id from " + populatingClassName + " e WHERE e.id > " + start
											+ " ORDER BY e.id", Long.class)
									.setMaxResults(lucenePopulateBlockSize).getResultList();
							if (ids.size() == 0) {
								break;
							}
							start = ids.get(ids.size() - 1);

							Future<Void> fut;
							/* Remove any completed ones */
							while ((fut = threads.poll()) != null) {
								tasksIn--;
								fut.get();
							}

							/* If full then wait */
							if (tasksIn == maxThreads) {
								fut = threads.take();
								tasksIn--;
								fut.get();
							}

							logger.debug("About to submit " + ids.size() + " " + populatingClassName + " documents");
							threads.submit(new IndexSome(populatingClassName, ids, entityManagerFactory));
							tasksIn++;

							manager.clear();
						}

						/* Wait for the last few to finish */
						Future<Void> fut;
						while (tasksIn > 0) {
							fut = threads.take();
							tasksIn--;
							fut.get();
						}

						/*
						 * Unlock and commit the changes
						 */
						luceneApi.unlock(populatingClassName);
						populateList.remove(populatingClassName);
					}
				}
			} catch (Throwable t) {
				logger.error("Problem encountered in", t);
			} finally {
				manager.close();
				popState = PopState.STOPPED;
			}
		}
	}

	final static Logger logger = LoggerFactory.getLogger(LuceneManager.class);

	final static Marker fatal = MarkerFactory.getMarker("FATAL");

	/**
	 * The Set of classes for which population is requested
	 */
	private SortedSet<String> populateList = new ConcurrentSkipListSet<>();

	/** The thread which does the population */
	private PopulateThread populateThread;
	private String populatingClassName;

	@PersistenceUnit(unitName = "icat")
	private EntityManagerFactory entityManagerFactory;

	private int lucenePopulateBlockSize;

	private ExecutorService getBeanDocExecutor;

	@EJB
	PropertyHandler propertyHandler;

	private PopState popState = PopState.STOPPED;
	private ExecutorService populateExecutor;

	private int maxThreads;

	private LuceneApi luceneApi;

	private boolean active;

	public void addDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			bean.getDoc(gen);
			gen.writeEnd();
		}
		luceneApi.addDocument(entityName, baos.toString());
		logger.trace("Added to {} lucene index", entityName);
	}

	public void clear() throws IcatException {
		logger.info("Lucene clear called");
		popState = PopState.STOPPING;
		while (populateThread != null && populateThread.getState() != Thread.State.TERMINATED) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
		logger.debug("Lucene population terminated");
		luceneApi.clear();
		logger.info("Lucene clear completed");
	}

	public void commit() throws IcatException {
		luceneApi.commit();
	}

	public LuceneSearchResult datafiles(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int blockSize) throws IcatException {
		return luceneApi.datafiles(user, text, lower, upper, parms, blockSize);
	}

	public LuceneSearchResult datafilesAfter(long uid, int blockSize) throws IcatException {
		return luceneApi.datafiles(uid, blockSize);
	}

	public LuceneSearchResult datasets(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int blockSize) throws IcatException {
		return luceneApi.datasets(user, text, lower, upper, parms, blockSize);
	}

	public LuceneSearchResult datasetsAfter(Long uid, int blockSize) throws IcatException {
		return luceneApi.datasets(uid, blockSize);
	}

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		Long id = bean.getId();
		luceneApi.delete(entityName, id);
	}

	@PreDestroy
	private void exit() {
		logger.info("Closing down LuceneManager");
		if (active) {
			try {
				populateExecutor.shutdown();
				getBeanDocExecutor.shutdown();
				logger.info("Closed down LuceneManager");
			} catch (Exception e) {
				logger.error(fatal, "Problem closing down LuceneManager", e);
			}
		}
	}

	public void freeSearcher(Long uid) throws IcatException {
		luceneApi.freeSearcher(uid);
	}

	public List<String> getPopulating() {
		return new ArrayList<>(populateList);
	}

	@PostConstruct
	private void init() {
		logger.info("Initialising LuceneManager");
		URL url = propertyHandler.getLuceneUrl();
		active = url != null;
		if (active) {
			try {
				luceneApi = new LuceneApi(new URI(propertyHandler.getLuceneUrl().toString()));
				lucenePopulateBlockSize = propertyHandler.getLucenePopulateBlockSize();
				maxThreads = Runtime.getRuntime().availableProcessors();
				populateExecutor = Executors.newWorkStealingPool(maxThreads);
				getBeanDocExecutor = Executors.newCachedThreadPool();
				logger.info("Initialised LuceneManager at {}", url);
			} catch (Exception e) {
				logger.error(fatal, "Problem setting up LuceneManager", e);
				throw new IllegalStateException("Problem setting up LuceneManager");
			}
		} else {
			logger.info("LuceneManager is inactive");
		}
	}

	public LuceneSearchResult investigations(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int blockSize) throws IcatException {
		return luceneApi.investigations(user, text, lower, upper, parms, samples, userFullName, blockSize);
	}

	public LuceneSearchResult investigationsAfter(Long uid, int blockSize) throws IcatException {
		return luceneApi.investigations(uid, blockSize);
	}

	public boolean isActive() {
		return active;
	}

	public void populate(String entityName) throws IcatException {
		if (popState == PopState.STOPPING) {
			while (populateThread != null && populateThread.getState() != Thread.State.TERMINATED) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
		if (populateList.add(entityName)) {
			logger.debug("Lucene population of {} requested", entityName);
		} else {
			throw new IcatException(IcatExceptionType.OBJECT_ALREADY_EXISTS,
					"population of " + entityName + " already requested");
		}
		if (populateThread == null || populateThread.getState() == Thread.State.TERMINATED) {
			populateThread = new PopulateThread(entityManagerFactory);
			populateThread.start();
		}
	}

	public void updateDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			bean.getDoc(gen);
			gen.writeEnd();
		}
		luceneApi.update(entityName, baos.toString(), bean.getId());
		logger.trace("Updated {} lucene index", entityName);
	}

}