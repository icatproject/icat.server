package org.icatproject.core.manager.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.PropertyHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.PropertyHandler.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Startup
@Singleton
public class SearchManager {

	public class EnqueuedSearchRequestHandler extends TimerTask {

		@Override
		public void run() {

			synchronized (queueFileLock) {
				if (queueFile.length() != 0) {
					logger.debug("Will attempt to process {}", queueFile);
					StringBuilder sb = new StringBuilder("[");
					try (BufferedReader reader = new BufferedReader(new FileReader(queueFile))) {
						String line;
						while ((line = reader.readLine()) != null) {
							if (sb.length() != 1) {
								sb.append(',');
							}
							sb.append(line);
						}
					} catch (IOException e) {
						logger.error("Problems reading from {} : {}", queueFile, e.getMessage());
						return;
					}
					sb.append(']');

					try {
						searchApi.modify(sb.toString());
					} catch (Exception e) {
						// Catch all exceptions so the Timer doesn't end unexpectedly
						// Record failures in a flat file to be examined periodically
						logger.error("Search engine failed to modify documents with error {} : {}", e.getClass(),
								e.getMessage());
						synchronized (backlogHandlerFileLock) {
							try {
								FileWriter output = new FileWriter(backlogHandlerFile, true);
								output.write(sb.toString() + "\n");
								output.close();
							} catch (IOException e2) {
								logger.error("Problems writing to {} : {}", backlogHandlerFile, e2.getMessage());
							}
						}
					} finally {
						queueFile.delete();
					}
				}
			}
		}
	}

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
			if (eiHandler.hasSearchDoc(klass)) {
				searchApi.addNow(entityName, ids, manager, klass, getBeanDocExecutor);
			}
			return start;
		}
	}

	private class PendingSearchRequestHandler extends TimerTask {

		@Override
		public void run() {
			synchronized (backlogHandlerFileLock) {
				if (backlogHandlerFile.length() != 0) {
					logger.debug("Will attempt to process {}", backlogHandlerFile);
					try (BufferedReader reader = new BufferedReader(new FileReader(backlogHandlerFile))) {
						String line;
						while ((line = reader.readLine()) != null) {
							searchApi.modify(line);
						}
						backlogHandlerFile.delete();
						logger.info("Pending search records now all inserted");
					} catch (IOException e) {
						logger.error("Problems reading from {} : {}", backlogHandlerFile, e.getMessage());
					} catch (IcatException e) {
						logger.error("Failed to put previously failed entries into search engine " + e.getMessage());
					} catch (Throwable e) {
						logger.error("Something unexpected happened " + e.getClass() + " " + e.getMessage());
					}
					logger.debug("finish processing");
				}
			}
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
						searchApi.lock(populatingClassEntry.getKey());

						Long start = populatingClassEntry.getValue();

						logger.info("Search engine populating " + populatingClassEntry);

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
									.setMaxResults(populateBlockSize).getResultList();
							if (ids.size() == 0) {
								break;
							}

							Future<Long> fut;
							/* Remove any completed ones */
							while ((fut = threads.poll()) != null) {
								Long s = fut.get();
								if (s.equals(tasks.first())) {
									populateMap.put(populatingClassEntry.getKey(), s);
								}
								tasks.remove(s);
							}

							/* If full then wait */
							if (tasks.size() == maxThreads) {
								fut = threads.take();
								Long s = fut.get();
								if (s.equals(tasks.first())) {
									populateMap.put(populatingClassEntry.getKey(), s);
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
								populateMap.put(populatingClassEntry.getKey(), s);
							}
							tasks.remove(s);
						}

						/*
						 * Unlock and commit the changes
						 */
						searchApi.unlock(populatingClassEntry.getKey());
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

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	final static Logger logger = LoggerFactory.getLogger(SearchManager.class);

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

	private int populateBlockSize;

	private ExecutorService getBeanDocExecutor;

	@EJB
	PropertyHandler propertyHandler;
	private PopState popState = PopState.STOPPED;

	private ExecutorService populateExecutor;

	private int maxThreads;

	private SearchApi searchApi;

	private boolean active;

	private Long backlogHandlerFileLock = 0L;

	private Long queueFileLock = 0L;

	private Timer timer;

	private Set<String> entitiesToIndex;

	private File backlogHandlerFile;

	private File queueFile;

	private SearchEngine searchEngine;

	private List<URL> urls;

	private static final Map<String, List<String>> publicSearchFields = new HashMap<>();

	/**
	 * Gets (and if necessary, builds) the fields which should be returned as part
	 * of the document source from a search.
	 * 
	 * @param gateKeeper GateKeeper instance.
	 * @param simpleName Name of the entity to get public fields for.
	 * @return List of fields which can be shown in search results provided the main
	 *         entity is authorised.
	 * @throws IcatException
	 */
	public static List<String> getPublicSearchFields(GateKeeper gateKeeper, String simpleName) throws IcatException {
		if (gateKeeper.getPublicSearchFieldsStale() || publicSearchFields.size() == 0) {
			logger.info("Building public search fields from public tables and steps");
			publicSearchFields.put("Datafile", buildPublicSearchFields(gateKeeper, Datafile.getDocumentFields()));
			publicSearchFields.put("Dataset", buildPublicSearchFields(gateKeeper, Dataset.getDocumentFields()));
			publicSearchFields.put("Investigation",
					buildPublicSearchFields(gateKeeper, Investigation.getDocumentFields()));
			gateKeeper.markPublicSearchFieldsFresh();
		}
		List<String> requestedFields = publicSearchFields.get(simpleName);
		logger.trace("{} has public fields {}", simpleName, requestedFields);
		return requestedFields;
	}

	public void addDocument(EntityBaseBean bean) throws IcatException {
		Class<? extends EntityBaseBean> klass = bean.getClass();
		if (eiHandler.hasSearchDoc(klass) && entitiesToIndex.contains(klass.getSimpleName())) {
			enqueue(SearchApi.encodeOperation("create", bean));
		}
	}

	public void enqueue(String json) throws IcatException {
		synchronized (queueFileLock) {
			try {
				FileWriter output = new FileWriter(queueFile, true);
				output.write(json + "\n");
				output.close();
			} catch (IOException e) {
				String msg = "Problems writing to " + queueFile + " " + e.getMessage();
				logger.error(msg);
				throw new IcatException(IcatExceptionType.INTERNAL, msg);
			}
		}

	}

	public void clear() throws IcatException {
		logger.info("Search engine clear called");
		popState = PopState.STOPPING;
		while (populateThread != null && populateThread.getState() != Thread.State.TERMINATED) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
		logger.debug("Search engine population terminated");
	}

	public void commit() throws IcatException {
		pushPendingCalls();
		searchApi.commit();
	}

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		if (eiHandler.hasSearchDoc(bean.getClass())) {
			enqueue(SearchApi.encodeDeletion(bean));
		}
	}

	/**
	 * Builds a JsonObject for performing faceting against results from a previous
	 * search.
	 * 
	 * @param results   List of results from a previous search, containing entity
	 *                  ids.
	 * @param idField   The field to perform id querying against.
	 * @param facetJson JsonObject containing the dimensions to facet.
	 * @return <code>{"query": {`idField`: [...]}, "dimensions": [...]}</code>
	 */
	public static JsonObject buildFacetQuery(List<ScoredEntityBaseBean> results, String idField, JsonObject facetJson) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		results.forEach(r -> arrayBuilder.add(Long.toString(r.getEntityBaseBeanId())));
		JsonObject terms = Json.createObjectBuilder().add(idField, arrayBuilder.build()).build();
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add("query", terms);
		if (facetJson.containsKey("dimensions")) {
			objectBuilder.add("dimensions", facetJson.getJsonArray("dimensions"));
		}
		return objectBuilder.build();
	}

	/**
	 * Builds a JsonObject for performing faceting against results from a previous
	 * search.
	 * 
	 * @param filterObject JsonObject to be used as a query.
	 * @param idField      The field to perform id querying against.
	 * @param facetJson    JsonObject containing the dimensions to facet.
	 * @return <code>{"query": `filterObject`, "dimensions": [...]}</code>
	 */
	public static JsonObject buildFacetQuery(JsonObject filterObject, String idField, JsonObject facetJson) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add("query", filterObject);
		if (facetJson.containsKey("dimensions")) {
			objectBuilder.add("dimensions", facetJson.getJsonArray("dimensions"));
		}
		return objectBuilder.build();
	}

	/**
	 * Checks if the underlying Relationship is allowed for a field on an entity.
	 * 
	 * @param gateKeeper GateKeeper instance.
	 * @param map        Map of fields to the Relationship that must be allowed in
	 *                   order to return the fields with search results for a
	 *                   particular entity.
	 * @return List of fields (keys) from map that have an allowed relationship
	 */
	private static List<String> buildPublicSearchFields(GateKeeper gateKeeper, Map<String, Relationship[]> map) {
		List<String> fields = new ArrayList<>();
		for (Entry<String, Relationship[]> entry : map.entrySet()) {
			Boolean includeField = true;
			if (entry.getValue() != null) {
				for (Relationship relationship : entry.getValue()) {
					if (!gateKeeper.allowed(relationship)) {
						includeField = false;
						logger.debug("Access to {} blocked by disallowed relationship between {} and {}",
								entry.getKey(),
								relationship.getOriginBean().getSimpleName(),
								relationship.getDestinationBean().getSimpleName());
						break;
					}
				}
			}
			if (includeField) {
				fields.add(entry.getKey());
			}
		}
		return fields;
	}

	/**
	 * Builds a Json representation of the final search result based on the sort
	 * criteria used. This allows future searches to efficiently "search after" this
	 * result.
	 * 
	 * @param lastBean The last ScoredEntityBaseBean of the current search results.
	 * @param sort     String representing a JsonObject of sort criteria.
	 * @return JsonValue representing the lastBean to allow future searches to
	 *         search after it.
	 * @throws IcatException If the score of the lastBean is NaN, or one of the sort
	 *                       fields is not present in the source of the lastBean.
	 */
	public JsonValue buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		return searchApi.buildSearchAfter(lastBean, sort);
	}

	private void pushPendingCalls() {
		timer.schedule(new EnqueuedSearchRequestHandler(), 0L);
		while (queueFile.length() != 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	@PreDestroy
	private void exit() {
		logger.info("Closing down SearchManager");
		if (active) {
			try {
				populateExecutor.shutdown();
				getBeanDocExecutor.shutdown();
				pushPendingCalls();
				timer.cancel();
				timer = null;
				logger.info("Closed down SearchManager");
			} catch (Exception e) {
				logger.error(fatal, "Problem closing down SearchManager", e);
			}
		}
	}

	/**
	 * Perform faceting on an entity/index. The query associated with the request
	 * should determine which Documents to consider, and optionally the dimensions
	 * to facet. If no dimensions are provided, "sparse" faceting is performed
	 * across relevant string fields (but no Range faceting occurs).
	 * 
	 * @param target     Name of the entity/index to facet on.
	 * @param facetQuery JsonObject containing the criteria to facet on.
	 * @param maxResults The maximum number of results to include in the returned
	 *                   Json.
	 * @param maxLabels  The maximum number of labels to return for each dimension
	 *                   of the facets.
	 * @return List of FacetDimensions that were collected for the query.
	 * @throws IcatException
	 */
	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, int maxResults, int maxLabels)
			throws IcatException {
		return searchApi.facetSearch(target, facetQuery, maxResults, maxLabels);
	}

	public List<String> getPopulating() {
		List<String> result = new ArrayList<>();
		for (Entry<String, Long> e : populateMap.entrySet()) {
			result.add(e.getKey() + " " + e.getValue());
		}
		return result;
	}


	/**
	 * Gets SearchResult for query without searchAfter (pagination).
	 * 
	 * @param query      JsonObject containing the criteria to search on.
	 * @param maxResults Maximum number of results to retrieve from the engine.
	 * @param sort       String of Json representing the sort criteria.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public SearchResult freeTextSearch(JsonObject query, int maxResults, String sort) throws IcatException {
		return searchApi.getResults(query, maxResults, sort);
	}

	/**
	 * Gets SearchResult for query.
	 * 
	 * @param query           JsonObject containing the criteria to search on.
	 * @param searchAfter     JsonValue representing the last result of a previous
	 *                        search in order to skip results that have already been
	 *                        returned.
	 * @param blockSize       Maximum number of results to retrieve from the engine.
	 * @param sort            String of Json representing the sort criteria.
	 * @param requestedFields List of fields to return in the document source.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public SearchResult freeTextSearch(JsonObject query, JsonValue searchAfter, int blockSize, String sort,
			List<String> requestedFields) throws IcatException {
		return searchApi.getResults(query, searchAfter, blockSize, sort, requestedFields);
	}

	@PostConstruct
	private void init() {
		searchEngine = propertyHandler.getSearchEngine();
		logger.info("Initialising SearchManager for engine {}", searchEngine);
		urls = propertyHandler.getSearchUrls();
		active = urls != null && urls.size() > 0;
		if (active) {
			try {
				if (searchEngine == SearchEngine.LUCENE) {
					searchApi = new LuceneApi(propertyHandler.getSearchUrls().get(0).toURI());
				} else if (searchEngine == SearchEngine.ELASTICSEARCH || searchEngine == SearchEngine.OPENSEARCH) {
					String unitAliasOptions = propertyHandler.getUnitAliasOptions();
					searchApi = new OpensearchApi(propertyHandler.getSearchUrls().get(0).toURI(), unitAliasOptions);
				} else {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Search engine {} not supported, must be one of " + SearchEngine.values());
				}

				populateBlockSize = propertyHandler.getSearchPopulateBlockSize();
				Path searchDirectory = propertyHandler.getSearchDirectory();
				backlogHandlerFile = searchDirectory.resolve("backLog").toFile();
				queueFile = searchDirectory.resolve("queue").toFile();
				maxThreads = Runtime.getRuntime().availableProcessors();
				populateExecutor = Executors.newWorkStealingPool(maxThreads);
				getBeanDocExecutor = Executors.newCachedThreadPool();
				timer = new Timer();
				timer.schedule(new PendingSearchRequestHandler(), 0L,
						propertyHandler.getSearchBacklogHandlerIntervalMillis());
				timer.schedule(new EnqueuedSearchRequestHandler(), 0L,
						propertyHandler.getSearchEnqueuedRequestIntervalMillis());
				entitiesToIndex = propertyHandler.getEntitiesToIndex();
				logger.info("Initialised SearchManager at {}", urls);
			} catch (Exception e) {
				logger.error(fatal, "Problem setting up SearchManager", e);
				throw new IllegalStateException("Problem setting up SearchManager");
			}
		} else {
			logger.info("SearchManager is inactive");
		}
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
			logger.debug("Search engine population of {} requested", entityName);
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
		Class<? extends EntityBaseBean> klass = bean.getClass();
		if (eiHandler.hasSearchDoc(klass) && entitiesToIndex.contains(klass.getSimpleName())) {
			enqueue(SearchApi.encodeOperation("update", bean));
		}
	}

}