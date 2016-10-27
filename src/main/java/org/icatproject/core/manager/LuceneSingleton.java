package org.icatproject.core.manager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@DependsOn("LoggingConfigurator")
@Singleton
@LocalBean
@Remote(Lucene.class)
public class LuceneSingleton implements Lucene {

	public class IndexSome implements Callable<Void> {

		private List<Long> ids;
		private EntityManager manager;
		private Class<?> klass;
		private IndexWriter iwriter;

		public IndexSome(String entityName, List<Long> ids, EntityManagerFactory entityManagerFactory)
				throws IcatException {
			try {
				iwriter = indexWriters.get(entityName);
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
			try {
				for (Long id : ids) {
					EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
					if (bean != null) {
						try {
							iwriter.addDocument(bean.getDoc());
						} catch (IOException e) {
							throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
						}
					}
				}
				manager.close();
				return null;
			} catch (Exception e) {
				logger.error("About to throw internal exception because of", e);
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			}
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
						indexWriters.get(populatingClassName).deleteAll();
						Long start = -1L;

						logger.info("Lucene Populating " + populatingClassName);

						CompletionService<Void> threads = new ExecutorCompletionService<>(executorService);
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
									.setMaxResults(luceneCommitCount).getResultList();
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

						/* Process the ones that came in while populating */
						Class<?> klass = Class.forName(Constants.ENTITY_PREFIX + populatingClassName);
						IndexWriter iwriter = indexWriters.get(populatingClassName);
						for (Long id : idsToCheck) {
							EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
							if (bean != null) {
								Document doc = bean.getDoc();
								iwriter.updateDocument(new Term("id", id.toString()), doc);
							} else {
								iwriter.deleteDocuments(new Term("id", id.toString()));
							}
						}

						/*
						 * Commit the changes and ensure that the changes are
						 * seen
						 */
						iwriter.commit();
						searcherManagers.get(populatingClassName).maybeRefreshBlocking();
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

	class ScoredResult {

		private String result;
		private float score;

		private ScoredResult(String result, float score) {
			this.result = result;
			this.score = score;
		}

		public String getResult() {
			return result;
		}

		public float getScore() {
			return score;
		}

	}

	final static Logger logger = LoggerFactory.getLogger(LuceneSingleton.class);
	final static Marker fatal = MarkerFactory.getMarker("FATAL");

	final static SearcherFactory searcherFactory = new SearcherFactory();

	private String populatingClassName;

	@PersistenceUnit(unitName = "icat")
	private EntityManagerFactory entityManagerFactory;

	private boolean active;

	private int luceneCommitCount;

	private String luceneDirectory;

	private long luceneRefreshSeconds;

	private StandardQueryParser parser;

	/**
	 * The Set of classes for which population is requested
	 */
	private SortedSet<String> populateList = new ConcurrentSkipListSet<>();

	/** The thread which does the population */
	private PopulateThread populateThread;

	@EJB
	PropertyHandler propertyHandler;
	private Timer timer;
	private ExecutorService executorService;
	private int maxThreads;
	private Map<String, FSDirectory> directories = new HashMap<>();
	private Map<String, IndexWriter> indexWriters = new HashMap<>();

	private Map<String, SearcherManager> searcherManagers = new HashMap<>();
	private Map<Long, Map<String, IndexSearcher>> buckets = new ConcurrentHashMap<>();

	private Set<Long> idsToCheck = new HashSet<>();

	private AtomicLong bucketNum = new AtomicLong();

	private PopState popState = PopState.STOPPED;

	@Override
	public void addDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		IndexWriter indexWriter = indexWriters.get(entityName);
		if (indexWriter != null) {
			if (entityName.equals(populatingClassName)) {
				idsToCheck.add(bean.getId());
				logger.trace("Will add to {} lucene index later", entityName);
			} else {
				try {
					indexWriter.addDocument(bean.getDoc());
					logger.trace("Added to {} lucene index", entityName);
				} catch (IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}
		}
	}

	private Query buildDatafileQuery(String userName, String text, String lower, String upper,
			List<ParameterPOJO> parms, Map<String, IndexSearcher> bucket) throws IOException, QueryNodeException {
		logger.debug("Lucene Datafile search user:" + userName + " text:" + text + " lower:" + lower + " upper:" + upper
				+ " parameters: " + parms);
		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();

		if (userName != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", userName)), getIndexSearcher(bucket, "InvestigationUser"),
					ScoreMode.None);

			Query invQuery = JoinUtil.createJoinQuery("id", false, "investigation", iuQuery,
					getIndexSearcher(bucket, "Investigation"), ScoreMode.None);

			Query dsQuery = JoinUtil.createJoinQuery("id", false, "dataset", invQuery,
					getIndexSearcher(bucket, "Dataset"), ScoreMode.None);

			theQuery.add(dsQuery, Occur.MUST);
		}

		if (text != null) {
			theQuery.add(parser.parse(text, "text"), Occur.MUST);
		}

		if (lower != null && upper != null) {
			theQuery.add(new TermRangeQuery("date", new BytesRef(lower), new BytesRef(upper), true, true), Occur.MUST);
			theQuery.add(new TermRangeQuery("date", new BytesRef(lower), new BytesRef(upper), true, true), Occur.MUST);
		}

		if (!parms.isEmpty()) {
			IndexSearcher datafileParameterSearcher = getIndexSearcher(bucket, "DatafileParameter");
			for (ParameterPOJO parameter : parms) {
				BooleanQuery.Builder paramQuery = new BooleanQuery.Builder();
				if (parameter.getName() != null) {
					paramQuery.add(new WildcardQuery(new Term("name", parameter.getName())), Occur.MUST);
				}
				if (parameter.getUnits() != null) {
					paramQuery.add(new WildcardQuery(new Term("units", parameter.getUnits())), Occur.MUST);
				}
				if (parameter.getStringValue() != null) {
					paramQuery.add(new WildcardQuery(new Term("stringValue", parameter.getStringValue())), Occur.MUST);
				} else if (parameter.getLowerDateValue() != null && parameter.getUpperDateValue() != null) {
					paramQuery.add(new TermRangeQuery("dateTimeValue", new BytesRef(parameter.getLowerDateValue()),
							new BytesRef(upper), true, true), Occur.MUST);

				} else if (parameter.getLowerNumericValue() != null && parameter.getUpperNumericValue() != null) {
					paramQuery.add(NumericRangeQuery.newDoubleRange("numericValue", parameter.getLowerNumericValue(),
							parameter.getUpperNumericValue(), true, true), Occur.MUST);
				}
				Query toQuery = JoinUtil.createJoinQuery("datafile", false, "id", paramQuery.build(),
						datafileParameterSearcher, ScoreMode.None);
				theQuery.add(toQuery, Occur.MUST);
			}
		}
		return maybeEmptyQuery(theQuery);
	}

	private Query buildDatasetQuery(String userName, String text, String lower, String upper, List<ParameterPOJO> parms,
			Map<String, IndexSearcher> bucket) throws IOException, QueryNodeException {
		logger.debug("Lucene Dataset search user:" + userName + " text:" + text + " lower:" + lower + " upper:" + upper
				+ " parameters: " + parms);
		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();

		if (userName != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", userName)), getIndexSearcher(bucket, "InvestigationUser"),
					ScoreMode.None);

			Query invQuery = JoinUtil.createJoinQuery("id", false, "investigation", iuQuery,
					getIndexSearcher(bucket, "Investigation"), ScoreMode.None);

			theQuery.add(invQuery, Occur.MUST);
		}

		if (text != null) {
			theQuery.add(parser.parse(text, "text"), Occur.MUST);
		}

		if (lower != null && upper != null) {
			theQuery.add(new TermRangeQuery("startDate", new BytesRef(lower), new BytesRef(upper), true, true),
					Occur.MUST);
			theQuery.add(new TermRangeQuery("endDate", new BytesRef(lower), new BytesRef(upper), true, true),
					Occur.MUST);
		}

		if (!parms.isEmpty()) {
			IndexSearcher datasetParameterSearcher = getIndexSearcher(bucket, "DatasetParameter");
			for (ParameterPOJO parameter : parms) {
				BooleanQuery.Builder paramQuery = new BooleanQuery.Builder();
				if (parameter.getName() != null) {
					paramQuery.add(new WildcardQuery(new Term("name", parameter.getName())), Occur.MUST);
				}
				if (parameter.getUnits() != null) {
					paramQuery.add(new WildcardQuery(new Term("units", parameter.getUnits())), Occur.MUST);
				}
				if (parameter.getStringValue() != null) {
					paramQuery.add(new WildcardQuery(new Term("stringValue", parameter.getStringValue())), Occur.MUST);
				} else if (parameter.getLowerDateValue() != null && parameter.getUpperDateValue() != null) {
					paramQuery.add(new TermRangeQuery("dateTimeValue", new BytesRef(parameter.getLowerDateValue()),
							new BytesRef(upper), true, true), Occur.MUST);

				} else if (parameter.getLowerNumericValue() != null && parameter.getUpperNumericValue() != null) {
					paramQuery.add(NumericRangeQuery.newDoubleRange("numericValue", parameter.getLowerNumericValue(),
							parameter.getUpperNumericValue(), true, true), Occur.MUST);
				}
				Query toQuery = JoinUtil.createJoinQuery("dataset", false, "id", paramQuery.build(),
						datasetParameterSearcher, ScoreMode.None);
				theQuery.add(toQuery, Occur.MUST);
			}
		}
		return maybeEmptyQuery(theQuery);

	}

	private Query buildInvestigationQuery(String userName, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, Map<String, IndexSearcher> bucket)
			throws QueryNodeException, IOException {
		logger.debug("Lucene Investigation search user:" + userName + " text:" + text + " lower:" + lower + " upper:"
				+ upper + " parameters: " + parms + " samples:" + samples + " userFullName:" + userFullName);

		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();

		if (userName != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", userName)), getIndexSearcher(bucket, "InvestigationUser"),
					ScoreMode.None);
			theQuery.add(iuQuery, Occur.MUST);
		}

		if (text != null) {
			theQuery.add(parser.parse(text, "text"), Occur.MUST);
		}

		if (lower != null && upper != null) {
			theQuery.add(new TermRangeQuery("startDate", new BytesRef(lower), new BytesRef(upper), true, true),
					Occur.MUST);
			theQuery.add(new TermRangeQuery("endDate", new BytesRef(lower), new BytesRef(upper), true, true),
					Occur.MUST);
		}

		if (!parms.isEmpty()) {
			IndexSearcher investigationParameterSearcher = getIndexSearcher(bucket, "InvestigationParameter");
			for (ParameterPOJO parameter : parms) {
				BooleanQuery.Builder paramQuery = new BooleanQuery.Builder();
				if (parameter.getName() != null) {
					paramQuery.add(new WildcardQuery(new Term("name", parameter.getName())), Occur.MUST);
				}
				if (parameter.getUnits() != null) {
					paramQuery.add(new WildcardQuery(new Term("units", parameter.getUnits())), Occur.MUST);
				}
				if (parameter.getStringValue() != null) {
					paramQuery.add(new WildcardQuery(new Term("stringValue", parameter.getStringValue())), Occur.MUST);
				}

				Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", paramQuery.build(),
						investigationParameterSearcher, ScoreMode.None);
				theQuery.add(toQuery, Occur.MUST);
			}
		}

		if (!samples.isEmpty()) {
			IndexSearcher sampleSearcher = getIndexSearcher(bucket, "Sample");
			for (String sample : samples) {
				BooleanQuery.Builder sampleQuery = new BooleanQuery.Builder();
				sampleQuery.add(parser.parse(sample, "text"), Occur.MUST);
				Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", sampleQuery.build(),
						sampleSearcher, ScoreMode.None);
				theQuery.add(toQuery, Occur.MUST);
			}
		}

		if (userFullName != null) {
			BooleanQuery.Builder userFullNameQuery = new BooleanQuery.Builder();
			userFullNameQuery.add(parser.parse(userFullName, "text"), Occur.MUST);
			IndexSearcher investigationUserSearcher = getIndexSearcher(bucket, "InvestigationUser");
			Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", userFullNameQuery.build(),
					investigationUserSearcher, ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}
		return maybeEmptyQuery(theQuery);
	}

	@Override
	public void clear() throws IcatException {
		try {
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
			for (IndexWriter iwriter : indexWriters.values()) {
				iwriter.deleteAll();
				iwriter.commit();
			}
			logger.info("Lucene clear completed");
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public void commit() throws IcatException {
		try {
			for (Entry<String, IndexWriter> entry : indexWriters.entrySet()) {
				String name = entry.getKey();
				if (!name.equals(populatingClassName)) {
					IndexWriter iwriter = entry.getValue();
					int cached = iwriter.numRamDocs();
					iwriter.commit();
					if (cached != 0) {
						logger.debug("Synch has committed {} {} changes to Lucene - now have {} documents indexed",
								cached, name, iwriter.numDocs());
					}
					searcherManagers.get(name).maybeRefreshBlocking();
				}
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult datafiles(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int maxResults) throws IcatException {

		Long uid = null;
		try {
			uid = bucketNum.getAndIncrement();
			Map<String, IndexSearcher> bucket = new HashMap<>();
			buckets.put(uid, bucket);
			IndexSearcher datafileSearcher = getIndexSearcher(bucket, "Datafile");
			Query query = buildDatafileQuery(user, text, lower, upper, parms, bucket);
			return luceneSearchResult(datafileSearcher, query, maxResults, null, uid);
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult datafilesAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults, LuceneSearchResult last) throws IcatException {
		long uid = last.getUid();
		try {
			Map<String, IndexSearcher> bucket = buckets.get(uid);
			IndexSearcher datafileSearcher = bucket.get("Datafile");
			try {
				Query query = buildDatafileQuery(user, text, lower, upper, parms, bucket);
				return luceneSearchResult(datafileSearcher, query, maxResults, last.getScoreDoc(), uid);
			} catch (Exception e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult datasets(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int maxResults) throws IcatException {
		Long uid = null;
		try {
			uid = bucketNum.getAndIncrement();
			Map<String, IndexSearcher> bucket = new HashMap<>();
			buckets.put(uid, bucket);
			IndexSearcher datasetSearcher = getIndexSearcher(bucket, "Dataset");
			Query query = buildDatasetQuery(user, text, lower, upper, parms, bucket);
			return luceneSearchResult(datasetSearcher, query, maxResults, null, uid);
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult datasetsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults, LuceneSearchResult last) throws IcatException {
		long uid = last.getUid();
		Map<String, IndexSearcher> bucket = buckets.get(uid);
		IndexSearcher datasetSearcher = bucket.get("Dataset");
		try {
			Query query = buildDatasetQuery(user, text, lower, upper, parms, bucket);
			return luceneSearchResult(datasetSearcher, query, maxResults, last.getScoreDoc(), uid);
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		IndexWriter indexWriter = indexWriters.get(entityName);
		if (indexWriter != null) {
			Long id = bean.getId();
			if (entityName.equals(populatingClassName)) {
				idsToCheck.add(id);
				logger.trace("Will delete {} from {} lucene index later", id, entityName);
			} else {
				IndexWriter iwriter = indexWriters.get(entityName);
				try {
					iwriter.deleteDocuments(new Term("id", id.toString()));
					logger.trace("Deleted {} from {} lucene index", id, entityName);
				} catch (IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}
		}
	}

	@PreDestroy
	private void exit() {
		timer.cancel();
		try {
			logger.debug("Closing SearcherManagers");
			for (SearcherManager manager : searcherManagers.values()) {
				manager.close();
			}

			logger.debug("Closing IndexWriters");
			for (IndexWriter iwriter : indexWriters.values()) {
				iwriter.commit();
				iwriter.close();
			}

			logger.debug("Closing FSDirectories");
			for (FSDirectory directory : directories.values()) {
				directory.close();
				directory = null;
			}

			executorService.shutdown();
		} catch (Exception e) {
			logger.error(fatal, "Problem closing down LuceneSingleton", e);
		}
	}

	private void freeSearcher(Long uid) throws IcatException {
		if (uid != null) { // May not be set for internal calls
			Map<String, IndexSearcher> bucket = buckets.get(uid);
			for (Entry<String, IndexSearcher> entry : bucket.entrySet()) {
				String name = entry.getKey();
				IndexSearcher isearcher = entry.getValue();
				SearcherManager manager = searcherManagers.get(name);
				try {
					manager.release(isearcher);
				} catch (IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
				}
			}
			buckets.remove(uid);
		}
	}

	@Override
	public void freeSearcher(LuceneSearchResult last) throws IcatException {
		if (last != null) {
			long uid = last.getUid();
			freeSearcher(uid);
		}
	}

	@Override
	public boolean getActive() {
		return active;
	}

	private IndexSearcher getIndexSearcher(Map<String, IndexSearcher> bucket, String name) throws IOException {
		IndexSearcher isearcher = bucket.get(name);
		if (isearcher == null) {
			isearcher = searcherManagers.get(name).acquire();
			bucket.put(name, isearcher);
		}
		return isearcher;
	}

	@Override
	public List<String> getPopulating() {
		return new ArrayList<>(populateList);
	}

	@PostConstruct
	private void init() {

		luceneDirectory = propertyHandler.getLuceneDirectory();
		active = luceneDirectory != null;
		if (active) {
			luceneRefreshSeconds = propertyHandler.getLuceneRefreshSeconds() * 1000L;
			luceneCommitCount = propertyHandler.getLuceneCommitCount();

			try {
				Analyzer analyzer = new IcatAnalyzer();
				for (String name : EntityInfoHandler.getEntityNamesList()) {
					Class<EntityBaseBean> klass = EntityInfoHandler.getClass(name);
					try {
						klass.getDeclaredMethod("getDoc");
						FSDirectory directory = FSDirectory.open(Paths.get(luceneDirectory, name));
						directories.put(name, directory);
						logger.debug("Opened FSDirectory {}", directory);

						IndexWriterConfig config = new IndexWriterConfig(analyzer);
						IndexWriter iwriter = new IndexWriter(directory, config);
						String[] files = directory.listAll();
						if (files.length == 1 && files[0].equals("write.lock")) {
							logger.debug("Directory only has the write.lock file so store and delete a dummy document");
							Document doc = new Document();
							doc.add(new StringField("dummy", "dummy", Store.NO));
							iwriter.addDocument(doc);
							iwriter.commit();
							iwriter.deleteDocuments(new Term("dummy", "dummy"));
							iwriter.commit();
							logger.debug("Now have " + iwriter.numDocs() + " documents indexed");
						}
						indexWriters.put(name, iwriter);

						searcherManagers.put(name, new SearcherManager(iwriter, false, null));

						parser = new StandardQueryParser();
						StandardQueryConfigHandler qpConf = (StandardQueryConfigHandler) parser.getQueryConfigHandler();
						qpConf.set(ConfigurationKeys.ANALYZER, analyzer);
						qpConf.set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, true);
					} catch (NoSuchMethodException e) {
						// There is no getDoc method so not interested
					}
				}
			} catch (Exception e) {
				logger.error(fatal, "Problem setting up LuceneSingleton", e);
				throw new IllegalStateException("Problem setting up LuceneSingleton");
			}

			timer = new Timer("Lucene");
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						commit();
					} catch (IcatException e) {
						logger.error(e.getMessage());
					} catch (Throwable t) {
						logger.error(t.getMessage());
					}
				}
			}, luceneRefreshSeconds, luceneRefreshSeconds);

			maxThreads = Runtime.getRuntime().availableProcessors();
			executorService = Executors.newWorkStealingPool(maxThreads);

			logger.debug("Created LuceneSingleton");
		}

	}

	@Override
	public LuceneSearchResult investigations(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxResults) throws IcatException {
		Long uid = null;
		try {
			uid = bucketNum.getAndIncrement();
			Map<String, IndexSearcher> bucket = new HashMap<>();
			buckets.put(uid, bucket);
			IndexSearcher investigationSearcher = getIndexSearcher(bucket, "Investigation");
			Query query = buildInvestigationQuery(user, text, lower, upper, parms, samples, userFullName, bucket);
			return luceneSearchResult(investigationSearcher, query, maxResults, null, uid);
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult investigationsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxResults,
			LuceneSearchResult last) throws IcatException {
		long uid = last.getUid();
		Map<String, IndexSearcher> bucket = buckets.get(uid);
		IndexSearcher investigationSearcher = bucket.get("Investigation");
		try {
			Query query = buildInvestigationQuery(user, text, lower, upper, parms, samples, userFullName, bucket);
			return luceneSearchResult(investigationSearcher, query, maxResults, last.getScoreDoc(), uid);
		} catch (Exception e) {
			freeSearcher(uid);
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private LuceneSearchResult luceneSearchResult(IndexSearcher isearcher, Query query, int maxResults,
			ScoreDoc scoreDoc, long uid) throws IOException {
		TopDocs topDocs = scoreDoc == null ? isearcher.search(query, maxResults)
				: isearcher.searchAfter(scoreDoc, query, maxResults);
		ScoreDoc[] hits = topDocs.scoreDocs;
		logger.debug("Hits " + topDocs.totalHits + " maxscore " + topDocs.getMaxScore());
		List<ScoredResult> results = new ArrayList<>();
		for (ScoreDoc hit : hits) {
			Document doc = isearcher.doc(hit.doc);
			results.add(new ScoredResult(doc.get("id"), hit.score));
			logger.debug(doc + " -> " + hit);
		}
		ScoreDoc lastDoc = results.isEmpty() ? null : hits[hits.length - 1];
		return new LuceneSearchResult(results, lastDoc, uid);
	}

	private Query maybeEmptyQuery(Builder theQuery) {
		Query query = theQuery.build();
		if (query.toString().isEmpty()) {
			query = new MatchAllDocsQuery();
		}
		logger.debug("Lucene query {}", query);
		return query;
	}

	@Override
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

	@Override
	public void updateDocument(EntityBaseBean bean) throws IcatException {
		String entityName = bean.getClass().getSimpleName();
		IndexWriter indexWriter = indexWriters.get(entityName);
		if (indexWriter != null) {
			Long id = bean.getId();
			if (entityName.equals(populatingClassName)) {
				idsToCheck.add(id);
				logger.trace("Will update {} lucene index later", entityName);
			} else {
				try {
					indexWriters.get(bean.getClass().getSimpleName()).updateDocument(new Term("id", id.toString()),
							bean.getDoc());
					logger.trace("Updated {} lucene index", entityName);
				} catch (IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}

		}
	}

}
