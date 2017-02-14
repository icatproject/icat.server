package org.icatproject.core.manager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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

@Startup
@Singleton
public class LuceneManager {

	public enum Operation {
		ADD, UPDATE, DELETE
	}

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	public class IndexSome implements Callable<Long> {

		private List<Long> ids;
		private EntityManager manager;
		private Class<? extends EntityBaseBean> klass;
		private String entityName;
		private long start;

		@SuppressWarnings("unchecked")
		public IndexSome(String entityName, List<Long> ids, EntityManagerFactory entityManagerFactory, long start)
				throws IcatException {
			try {
				logger.debug("About to index {} {} records", ids.size(), entityName);
				this.entityName = entityName;
				klass = (Class<? extends EntityBaseBean>) Class.forName(Constants.ENTITY_PREFIX + entityName);
				this.ids = ids;
				manager = entityManagerFactory.createEntityManager();
				this.start = start;
			} catch (Exception e) {
				logger.error("About to throw internal exception because of", e);
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			}
		}

		@Override
		public Long call() throws Exception {
			if (eiHandler.hasLuceneDoc(klass)) {

				URI uri = new URIBuilder(luceneApi.server).setPath(LuceneApi.basePath + "/addNow/" + entityName)
						.build();
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
									gen.writeStartArray();
									bean.getDoc(gen);
									gen.writeEnd();
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
			}
			return start;
		}
	}

	private enum PopState {
		STOPPING, STOPPED
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
				while (!populateMap.isEmpty()) {

					populatingClassEntry = populateMap.firstEntry();

					if (populatingClassEntry != null) {
						luceneApi.lock(populatingClassEntry.getKey());

						Long start = populatingClassEntry.getValue();

						logger.info("Lucene Populating " + populatingClassEntry);

						CompletionService<Long> threads = new ExecutorCompletionService<>(populateExecutor);
						SortedSet<Long> tasks = new ConcurrentSkipListSet<>();

						while (true) {

							if (popState == PopState.STOPPING) {
								logger.info("PopulateThread stopping as flag was set");
								break;
							}
							/* Get next block of ids */
							List<Long> ids = manager
									.createQuery("SELECT e.id from " + populatingClassEntry.getKey()
											+ " e WHERE e.id > " + start + " ORDER BY e.id", Long.class)
									.setMaxResults(lucenePopulateBlockSize).getResultList();
							if (ids.size() == 0) {
								break;
							}

							Future<Long> fut;
							/* Remove any completed ones */
							while ((fut = threads.poll()) != null) {
								Long s = fut.get();
								if (s.equals(tasks.first())) {
//									populatingClassEntry.setValue(s);
								}
								tasks.remove(s);
							}

							/* If full then wait */
							if (tasks.size() == maxThreads) {
								fut = threads.take();
								Long s = fut.get();
								if (s.equals(tasks.first())) {
//									populatingClassEntry.setValue(s);
								}
								tasks.remove(s);
							}

							logger.debug("About to submit " + ids.size() + " " + populatingClassEntry + " documents");
							threads.submit(
									new IndexSome(populatingClassEntry.getKey(), ids, entityManagerFactory, start));
							tasks.add(start);
							start = ids.get(ids.size() - 1);

							manager.clear();
						}

						/* Wait for the last few to finish */
						Future<Long> fut;
						while (tasks.size() > 0) {
							fut = threads.take();
							Long s = fut.get();
							if (s.equals(tasks.first())) {
//								populatingClassEntry.setValue(s);
							}
							tasks.remove(s);
						}

						/*
						 * Unlock and commit the changes
						 */
						luceneApi.unlock(populatingClassEntry.getKey());
						populateMap.remove(populatingClassEntry.getKey());
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
	private ConcurrentSkipListMap<String, Long> populateMap = new ConcurrentSkipListMap<>();

	/** The thread which does the population */
	private PopulateThread populateThread;
	private Entry<String, Long> populatingClassEntry;

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
		if (eiHandler.hasLuceneDoc(bean.getClass())) {
			String entityName = bean.getClass().getSimpleName();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				gen.writeStartArray();
				bean.getDoc(gen);
				gen.writeEnd();
			}
			try {
				luceneApi.addDocument(entityName, baos.toString());
				logger.trace("Added to {} lucene index", entityName);
			} catch (IcatException e) {
				holdLuceneOp(Operation.ADD, entityName, bean.getId());
				logger.trace("Will add to {} lucene index when server is back", entityName);
			}

		}
	}

	private Long lock = 0L;

	private Timer timer;

	/*
	 * This writes to a file rather than the database because of difficulties
	 * with transactions. It is simple and safe if not elegant.
	 */
	private static String fname = "fred";

	private void holdLuceneOp(Operation operation, String entityName, Long id) {
		synchronized (lock) {
			try {
				FileWriter output = new FileWriter(fname, true);
				output.write(operation + " " + entityName + " " + id + "\n");
				output.close();
			} catch (IOException e) {
				logger.error("Problems writing to {} : {}", fname, e.getMessage());
			}
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
	}

	public void commit() throws IcatException {
		luceneApi.commit();
	}

	public LuceneSearchResult datafiles(String user, String text, Date lower, Date upper, List<ParameterPOJO> parms,
			int blockSize) throws IcatException {
		return luceneApi.datafiles(user, text, lower, upper, parms, blockSize);
	}

	public LuceneSearchResult datafilesAfter(long uid, int blockSize) throws IcatException {
		return luceneApi.datafiles(uid, blockSize);
	}

	public LuceneSearchResult datasets(String user, String text, Date lower, Date upper, List<ParameterPOJO> parms,
			int blockSize) throws IcatException {
		return luceneApi.datasets(user, text, lower, upper, parms, blockSize);
	}

	public LuceneSearchResult datasetsAfter(Long uid, int blockSize) throws IcatException {
		return luceneApi.datasets(uid, blockSize);
	}

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		if (eiHandler.hasLuceneDoc(bean.getClass())) {
			String entityName = bean.getClass().getSimpleName();
			Long id = bean.getId();
			try {
				luceneApi.delete(entityName, id);
			} catch (IcatException e) {
				holdLuceneOp(Operation.DELETE, entityName, id);
				logger.error("Added a DELETE plop");
			}
		}
	}

	@PreDestroy
	private void exit() {
		logger.info("Closing down LuceneManager");
		if (active) {
			try {
				populateExecutor.shutdown();
				getBeanDocExecutor.shutdown();
				timer.cancel();
				timer = null;
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
		List<String> result = new ArrayList<>();
		for (Entry<String, Long> e : populateMap.entrySet()) {
			result.add(e.getKey() + " " + e.getValue());
		}
		return result;
	}

	private class PendingLuceneRequestHandler extends TimerTask {

		private EntityManager manager;

		public PendingLuceneRequestHandler(EntityManagerFactory entityManagerFactory) {
			manager = entityManagerFactory.createEntityManager();
		}

		@Override
		public void run() {
			synchronized (lock) {
				File f = new File(fname);
				if (f.length() != 0) {
					logger.debug("Will attempt to process {}", f);
					try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
						String line;
						while ((line = reader.readLine()) != null) {
							String[] bits = line.split(" ");
							Operation operation = Operation.valueOf(bits[0]);
							String entityName = bits[1];
							@SuppressWarnings("unchecked")
							Class<? extends EntityBaseBean> klass = (Class<? extends EntityBaseBean>) Class
									.forName(Constants.ENTITY_PREFIX + entityName);
							Long id = Long.parseLong(bits[2]);
							EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);

							if (bean != null) {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								try (JsonGenerator gen = Json.createGenerator(baos)) {
									gen.writeStartArray();
									bean.getDoc(gen);
									gen.writeEnd();
								}
								if (operation == Operation.ADD) {
									luceneApi.addDocument(entityName, baos.toString());
								} else if (operation == Operation.UPDATE) {
									luceneApi.updateDocument(entityName, baos.toString(), id);
								}
							} else {
								if (operation == Operation.DELETE) {
									luceneApi.delete(entityName, id);
								}
							}
						}
						f.delete();
						logger.info("Pending lucene records now all inserted");
					} catch (IOException e) {
						logger.error("Problems reading from {} : {}", fname, e.getMessage());
					} catch (ClassNotFoundException e) {
						logger.error("Odd problem : {}", e.getMessage());
					} catch (IcatException e) {
						logger.error("Failed to put previously failed entries into lucene " + e.getMessage());
					} catch (Throwable e) {
						logger.error("Something unexpected happened " + e.getClass() + " " + e.getMessage());
					}
					logger.debug("finish processing");
				}
			}
		}
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
				timer = new Timer();
				timer.schedule(new PendingLuceneRequestHandler(entityManagerFactory), 0L, 60000L);
				logger.info("Initialised LuceneManager at {}", url);
			} catch (Exception e) {
				logger.error(fatal, "Problem setting up LuceneManager", e);
				throw new IllegalStateException("Problem setting up LuceneManager");
			}
		} else {
			logger.info("LuceneManager is inactive");
		}
	}

	public LuceneSearchResult investigations(String user, String text, Date lower, Date upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int blockSize) throws IcatException {
		return luceneApi.investigations(user, text, lower, upper, parms, samples, userFullName, blockSize);
	}

	public LuceneSearchResult investigationsAfter(Long uid, int blockSize) throws IcatException {
		return luceneApi.investigations(uid, blockSize);
	}

	public boolean isActive() {
		return active;
	}

	public void populate(String entityName, long minid) throws IcatException {
		if (popState == PopState.STOPPING) {
			while (populateThread != null && populateThread.getState() != Thread.State.TERMINATED) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
		if (populateMap.put(entityName, minid) == null) {
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
		if (eiHandler.hasLuceneDoc(bean.getClass())) {
			String entityName = bean.getClass().getSimpleName();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				gen.writeStartArray();
				bean.getDoc(gen);
				gen.writeEnd();
			}
			try {
				luceneApi.updateDocument(entityName, baos.toString(), bean.getId());
			} catch (IcatException e) {
				holdLuceneOp(Operation.UPDATE, entityName, bean.getId());
			}
			logger.trace("Updated {} lucene index", entityName);
		}
	}

}