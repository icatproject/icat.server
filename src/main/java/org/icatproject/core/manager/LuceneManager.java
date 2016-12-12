package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
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

	final static Logger logger = LoggerFactory.getLogger(LuceneManager.class);
	final static Marker fatal = MarkerFactory.getMarker("FATAL");

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
					luceneApi.checkStatus(response);
				}
			}
			return null;
		}
	}

	private enum PopState {
		POPULATING, STOPPING, STOPPED
	}

	@EJB
	PropertyHandler propertyHandler;

	private Set<Long> idsToCheck = new HashSet<>();

	private PopState popState = PopState.STOPPED;

	private ExecutorService populateExecutor;
	private int maxThreads;

	private LuceneApi luceneApi;

	public class PopulateThread extends Thread {
		// TODO make it send a request to the server to stop commits and restart
		// them at the end. There is still a problem with multiple ICATs - but
		// would people be so unwise?

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
						luceneApi.deleteAll(populatingClassName);

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
						 * Commit the changes
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

	@PostConstruct
	private void init() {
		logger.info("Initialising LuceneManager");
		try {
			luceneApi = new LuceneApi(new URI(propertyHandler.getLuceneUrl().toString()));
			lucenePopulateBlockSize = propertyHandler.getLucenePopulateBlockSize();
			maxThreads = Runtime.getRuntime().availableProcessors();
			populateExecutor = Executors.newWorkStealingPool(maxThreads);
			getBeanDocExecutor = Executors.newCachedThreadPool();
			logger.info("Initialised LuceneManager");

		} catch (Exception e) {
			logger.error(fatal, "Problem setting up LuceneManager", e);
			throw new IllegalStateException("Problem setting up LuceneManager");
		}
	}

	@PreDestroy
	public void exit() {
		logger.info("Closing down LuceneManager");
		try {
			populateExecutor.shutdown();
			getBeanDocExecutor.shutdown();
			logger.info("Closed down LuceneManager");
		} catch (Exception e) {
			logger.error(fatal, "Problem closing down LuceneManager", e);
		}
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

		for (String name : EntityInfoHandler.getEntityNamesList()) {
			Class<EntityBaseBean> klass = EntityInfoHandler.getClass(name);
			try {
				klass.getDeclaredMethod("getDoc");
				luceneApi.deleteAll(name);
			} catch (NoSuchMethodException e) {
				// There is no getDoc method so not interested
			}
			luceneApi.commit();
		}
		logger.info("Lucene clear completed");
	}

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		Long id = bean.getId();
		// TODO this must be moved to the server
		if (entityName.equals(populatingClassName)) {
			idsToCheck.add(id);
			logger.trace("Will delete {} from {} lucene index later", id, entityName);
		} else {
			luceneApi.delete(entityName, id);
		}
	}

	public LuceneSearchResult investigations(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int blockSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public LuceneSearchResult investigationsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int blockSize,
			LuceneSearchResult last) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		if (entityName.equals(populatingClassName)) {
			idsToCheck.add(bean.getId());
			logger.trace("Will add to {} lucene index later", entityName);
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				gen.writeStartArray();
				bean.getDoc(gen);
				gen.writeEnd();
			}
			luceneApi.addDocument(entityName, baos.toString());

			logger.trace("Added to {} lucene index", entityName);

		}
	}

	public LuceneSearchResult datasets(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int blockSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public LuceneSearchResult datasetsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int blockSize, LuceneSearchResult last) {
		// TODO Auto-generated method stub
		return null;
	}

	public void freeSearcher(LuceneSearchResult last) {
		luceneApi.freeSearcher(last);
	}

	public boolean isActive() {
		return true;
	}

	public LuceneSearchResult datafiles(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int blockSize) throws IcatException {
		return luceneApi.datafiles(user, text, lower, upper, parms, blockSize, null);
	}

	public LuceneSearchResult datafilesAfter(long uid, int blockSize) throws IcatException {
		return luceneApi.datafiles(uid, blockSize);
	}

	public List<String> getPopulating() {
		return new ArrayList<>(populateList);
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

	public void updateDocument(EntityBaseBean entityBaseBean) {
		// TODO Auto-generated method stub

	}

	public void commit() throws IcatException {
		luceneApi.commit();
	}

}