package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
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
import org.apache.lucene.util.Version;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;

/* There is synchronization code in this class - please understand it before making any changes */
@DependsOn("LoggingConfigurator")
@Singleton
@LocalBean
@Remote(Lucene.class)
public class LuceneSingleton implements Lucene {

	@PersistenceUnit(unitName = "icat")
	private EntityManagerFactory entityManagerFactory;

	public class PopulateThread extends Thread {

		private EntityManager manager;

		public PopulateThread(EntityManagerFactory entityManagerFactory) {
			manager = entityManagerFactory.createEntityManager();
			logger.debug("Start new populate thread");
		}

		@Override
		public void run() {

			try {
				do {
					synchronized (LuceneSingleton.this) {
						Class<?> t = null;
						for (Class<?> klass : populateList) {
							t = klass;
							break;
						}
						populatingClass = t;
						if (populatingClass != null) {
							populateList.remove(populatingClass);
						}
					}
					if (populatingClass != null) {
						String entityName = populatingClass.getSimpleName();
						clear(entityName);
						int start = 0;
						List<Long> ids;
						logger.debug("Populating " + entityName);
						while (true) {
							synchronized (this) {
								if (stopPopulation) {
									logger.info("PopulateThread stopping as flag was set");
									return;
								}
							}
							/* Get next block of ids */
							ids = manager
									.createQuery(
											"SELECT e.id from " + entityName + " e ORDER BY e.id",
											Long.class).setFirstResult(start)
									.setMaxResults(luceneCommitCount).getResultList();
							if (ids.size() == 0) {
								break;
							}
							logger.debug("About to add " + ids.size() + " " + entityName
									+ " documents");
							try {
								for (Long id : ids) {
									EntityBaseBean bean = (EntityBaseBean) manager.find(
											populatingClass, id);
									if (bean != null) {
										addDocument(bean);
									}
								}
							} catch (Exception e) {
								throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
							}
							commit();
							start = start + ids.size();
						}
					}
				} while (populatingClass != null);
			} catch (Throwable t) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				t.printStackTrace(new PrintStream(baos));
				logger.error(baos);
			} finally {
				manager.close();
			}
		}
	}

	final static Logger logger = Logger.getLogger(LuceneSingleton.class);

	final static SearcherFactory searcherFactory = new SearcherFactory();

	private boolean active;

	private FSDirectory directory;
	private EntityInfoHandler ei;

	private IndexSearcher isearcher;

	private IndexWriter iwriter;
	private int luceneCommitCount;
	private String luceneDirectory;

	private long luceneRefreshSeconds;

	private StandardQueryParser parser;

	/** The Set of classes for which population is requested - access is synchronized on this */
	private Set<Class<?>> populateList = new HashSet<>();

	/** The thread which does the population - access is synchronized on this */
	private PopulateThread populateThread;

	/** The class which is currently being populated - may be null - access is synchronized on this */
	public Class<?> populatingClass;

	@EJB
	PropertyHandler propertyHandler;

	private SearcherManager searcherManager;

	/**
	 * A flag to stop population at the end of the current block of ids - access is synchronized on
	 * this
	 */
	private boolean stopPopulation;

	private Timer timer;

	@Override
	public void addDocument(EntityBaseBean bean) throws IcatException {
		Document doc = bean.getDoc();
		if (doc == null) {
			return;
		}
		buildDoc(doc, bean);
		try {
			iwriter.addDocument(doc);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	private void buildDoc(Document doc, EntityBaseBean bean) {
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		doc.add(new StringField("id", id, Store.YES));
		doc.add(new StringField("entity", bean.getClass().getSimpleName(), Store.NO));
		logger.debug("Created document '" + doc + "' to index for " + id);
	}

	private Document buildDoc(EntityBaseBean bean) throws IcatException {
		Map<Field, Integer> stringFields = ei.getStringFields(bean.getClass());
		StringBuilder sb = new StringBuilder();
		for (Entry<Field, Integer> item : stringFields.entrySet()) {
			Field field = item.getKey();
			Method getter = ei.getGetters(bean.getClass()).get(field);
			try {
				String text = (String) getter.invoke(bean);
				if (text != null) {
					if (sb.length() != 0) {
						sb.append(' ');
					}
					sb.append(text);
				}
			} catch (IllegalArgumentException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			} catch (IllegalAccessException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			} catch (InvocationTargetException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
			}
		}

		Document doc = new Document();
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		doc.add(new StringField("id", id, Store.YES));
		doc.add(new StringField("entity", bean.getClass().getSimpleName(), Store.NO));
		doc.add(new TextField("all", sb.toString(), Store.NO));
		logger.debug("Created document '" + sb.toString() + "' to index for " + id);
		return doc;
	}

	@Override
	public synchronized void clear() throws IcatException {
		try {
			populateList.clear();
			stopPopulation = true;
			while (populatingClass != null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
			stopPopulation = false;
			iwriter.deleteAll();
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	private void clear(String entityName) throws IcatException {
		try {
			commit();
			int n = iwriter.numDocs();
			iwriter.deleteDocuments(new Term("entity", entityName));
			commit();
			n -= iwriter.numDocs();
			logger.debug("Deleted " + n + " documents");
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public synchronized void commit() throws IcatException {
		try {
			int cached = iwriter.numRamDocs();
			iwriter.commit();
			if (cached != 0) {
				logger.debug("Synch has committed " + cached + " changes to Lucene - now have "
						+ iwriter.numDocs() + " documents indexed");
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		try {
			iwriter.deleteDocuments(new Term("id", id));
			// TODO logger.debug("Deleted document: " + id);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@PreDestroy
	private void exit() {
		timer.cancel();
		try {
			logger.debug("Closing IndexWriter for directory lockid " + directory.getLockID());
			iwriter.commit();
			iwriter.close();
			iwriter = null;
			logger.debug("IndexWriter closed for directory lockid " + directory.getLockID());
			searcherManager.close();
			logger.debug("SearcherManager closed for directory lockid " + directory.getLockID());
			directory.close();
			directory = null;
			logger.info("Directory closed");
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.fatal(errors.toString());
		}
	}

	@Override
	public boolean getActive() {
		return active;
	}

	@Override
	public synchronized List<String> getPopulating() {
		List<String> result = new ArrayList<>();

		if (populatingClass != null) {
			result.add(populatingClass.getSimpleName());
			for (Class<?> klass : populateList) {
				result.add(klass.getSimpleName());
			}
		}
		return result;
	}

	@PostConstruct
	private void init() {

		luceneDirectory = propertyHandler.getLuceneDirectory();
		active = luceneDirectory != null;
		if (active) {
			luceneRefreshSeconds = propertyHandler.getLuceneRefreshSeconds() * 1000L;
			luceneCommitCount = propertyHandler.getLuceneCommitCount();
			ei = EntityInfoHandler.getInstance();

			try {
				directory = FSDirectory.open(new File(luceneDirectory));
				logger.debug("Opened FSDirectory with lockid " + directory.getLockID());
				Analyzer analyzer = new IcatAnalyzer();
				IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
				iwriter = new IndexWriter(directory, config);
				String[] files = directory.listAll();
				if (files.length == 1 && files[0].equals("write.lock")) {
					logger.debug("Directory only has the write.lock file so commit and reopen");
					iwriter.commit();
					iwriter.close();
					iwriter = new IndexWriter(directory, config);
				}

				searcherManager = new SearcherManager(directory, new SearcherFactory());
				isearcher = searcherManager.acquire();
				logger.debug("Got a new IndexSearcher " + isearcher);

				parser = new StandardQueryParser();
				StandardQueryConfigHandler qpConf = (StandardQueryConfigHandler) parser
						.getQueryConfigHandler();
				qpConf.set(ConfigurationKeys.ANALYZER, analyzer);
				qpConf.set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, true);
			} catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				logger.fatal(errors.toString());
				if (directory != null) {
					try {
						String lockId = directory.getLockID();
						directory.clearLock(lockId);
						logger.warn("Cleared lock " + lockId);
					} catch (IOException e1) {
						// Ignore
					}
				}
				throw new IllegalStateException(errors.toString());
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

			logger.debug("Created LuceneSingleton");
		}

	}

	@Override
	public synchronized void populate(Class<?> klass) {
		populateList.add(klass);
		if (populateThread == null || (populateThread).getState() == Thread.State.TERMINATED) {
			populateThread = new PopulateThread(entityManagerFactory);
			populateThread.start();
		}
	}

	@Override
	public LuceneSearchResult search(String queryString, int count, String entityName)
			throws IcatException {
		if (entityName != null) {
			try {
				Class<?> klass = Class.forName(Constants.ENTITY_PREFIX + entityName);
				if (!EntityBaseBean.class.isAssignableFrom(klass)) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Invalid entity name "
							+ entityName);
				}
			} catch (ClassNotFoundException e) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Invalid entity name "
						+ entityName);
			}
		}
		try {

			if (!searcherManager.isSearcherCurrent()) {
				IndexSearcher oldSearcher = isearcher;
				searcherManager.maybeRefreshBlocking();
				isearcher = searcherManager.acquire();
				searcherManager.release(oldSearcher);
				logger.debug("Got a new IndexSearcher " + isearcher);
			}

			List<String> results = new ArrayList<String>();
			Query query = parser.parse(queryString, "all");
			if (entityName != null) {
				BooleanQuery bquery = new BooleanQuery();
				bquery.add(query, Occur.MUST);
				bquery.add(new TermQuery(new Term("entity", entityName)), Occur.MUST);
				query = bquery;
			}
			ScoreDoc[] hits = isearcher.search(query, count).scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document doc = isearcher.doc(hits[i].doc);
				results.add(doc.get("id"));
			}
			ScoreDoc lastDoc = results.isEmpty() ? null : hits[hits.length - 1];
			return new LuceneSearchResult(results, lastDoc);
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.error(baos.toString());
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult searchAfter(String queryString, int count, String entityName,
			LuceneSearchResult last) throws IcatException {
		try {
			List<String> results = new ArrayList<String>();
			Query query = parser.parse(queryString, "all");
			if (entityName != null) {
				BooleanQuery bquery = new BooleanQuery();
				bquery.add(query, Occur.MUST);
				bquery.add(new TermQuery(new Term("entity", entityName)), Occur.MUST);
				query = bquery;
			}
			ScoreDoc[] hits = isearcher.searchAfter(last.getScoreDoc(), query, count).scoreDocs;
			for (ScoreDoc hit : hits) {
				Document doc = isearcher.doc(hit.doc);
				results.add(doc.get("id"));
			}
			ScoreDoc lastDoc = results.isEmpty() ? null : hits[hits.length - 1];
			return new LuceneSearchResult(results, lastDoc);
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public void updateDocument(EntityBaseBean bean) throws IcatException {
		Document doc = buildDoc(bean);
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		try {
			iwriter.updateDocument(new Term("id", id), doc);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public LuceneSearchResult investigations(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxResults)
			throws IcatException {
		try {
			refreshSearcher();
			Query query = buildInvestigationQuery(user, text, lower, upper, parms, samples,
					userFullName);
			return luceneSearchResult(isearcher.search(query, maxResults));
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private LuceneSearchResult luceneSearchResult(TopDocs topDocs) throws IOException {
		ScoreDoc[] hits = topDocs.scoreDocs;
		logger.debug("Hits " + topDocs.totalHits + " maxscore " + topDocs.getMaxScore());
		for (ScoreDoc hit : hits) {
			logger.debug(hit + " " + isearcher.doc(hit.doc));
		}

		List<String> results = new ArrayList<String>();

		for (int i = 0; i < hits.length; i++) {
			Document doc = isearcher.doc(hits[i].doc);
			results.add(doc.get("id"));
		}
		ScoreDoc lastDoc = results.isEmpty() ? null : hits[hits.length - 1];
		return new LuceneSearchResult(results, lastDoc);
	}

	@Override
	public LuceneSearchResult investigationsAfter(String user, String text, String lower,
			String upper, List<ParameterPOJO> parms, List<String> samples, String userFullName,
			int maxResults, LuceneSearchResult last) throws IcatException {
		try {
			Query query = buildInvestigationQuery(user, text, lower, upper, parms, samples,
					userFullName);
			return luceneSearchResult(isearcher.searchAfter(last.getScoreDoc(), query, maxResults));
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private Query buildInvestigationQuery(String userName, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName)
			throws QueryNodeException, IOException {
		logger.debug("Lucene Investigation search user:" + userName + " text:" + text + " lower:"
				+ lower + " upper:" + upper + " parameters: " + parms + " samples:" + samples
				+ " userFullName:" + userFullName);
		BooleanQuery theQuery = new BooleanQuery();
		theQuery.add(new TermQuery(new Term("entity", "Investigation")), Occur.MUST);

		if (userName != null) {
			BooleanQuery userQuery = new BooleanQuery();
			userQuery.add(new TermQuery(new Term("entity", "InvestigationUser")), Occur.MUST);
			userQuery.add(new TermQuery(new Term("name", userName)), Occur.MUST);
			Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", userQuery,
					isearcher, ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}

		if (text != null) {
			theQuery.add(parser.parse(text, "text"), Occur.MUST);
		}

		if (lower != null && upper != null) {
			theQuery.add(new TermRangeQuery("startDate", new BytesRef(lower), new BytesRef(upper),
					true, true), Occur.MUST);
			theQuery.add(new TermRangeQuery("endDate", new BytesRef(lower), new BytesRef(upper),
					true, true), Occur.MUST);
		}

		for (ParameterPOJO parameter : parms) {
			BooleanQuery paramQuery = new BooleanQuery();
			paramQuery.add(new TermQuery(new Term("entity", "InvestigationParameter")), Occur.MUST);
			if (parameter.getName() != null) {
				paramQuery
						.add(new WildcardQuery(new Term("name", parameter.getName())), Occur.MUST);
			}
			if (parameter.getUnits() != null) {
				paramQuery.add(new WildcardQuery(new Term("units", parameter.getUnits())),
						Occur.MUST);
			}
			if (parameter.getStringValue() != null) {
				paramQuery.add(new WildcardQuery(
						new Term("stringValue", parameter.getStringValue())), Occur.MUST);
			}
			Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", paramQuery,
					isearcher, ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}

		for (String sample : samples) {
			BooleanQuery sampleQuery = new BooleanQuery();
			sampleQuery.add(new TermQuery(new Term("entity", "Sample")), Occur.MUST);
			sampleQuery.add(parser.parse(sample, "text"), Occur.MUST);
			Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", sampleQuery,
					isearcher, ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}

		if (userFullName != null) {
			BooleanQuery userFullNameQuery = new BooleanQuery();
			userFullNameQuery.add(new TermQuery(new Term("entity", "InvestigationUser")),
					Occur.MUST);
			userFullNameQuery.add(parser.parse(userFullName, "text"), Occur.MUST);
			Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					userFullNameQuery, isearcher, ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}

		return theQuery;

	}

	@Override
	public LuceneSearchResult datasets(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults) throws IcatException {
		try {
			refreshSearcher();
			Query query = buildDatasetQuery(user, text, lower, upper, parms);
			return luceneSearchResult(isearcher.search(query, maxResults));
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private Query buildDatasetQuery(String userName, String text, String lower, String upper,
			List<ParameterPOJO> parms) throws IOException, QueryNodeException {
		logger.debug("Lucene Dataset search user:" + userName + " text:" + text + " lower:" + lower
				+ " upper:" + upper + " parameters: " + parms);
		BooleanQuery theQuery = new BooleanQuery();
		theQuery.add(new TermQuery(new Term("entity", "Dataset")), Occur.MUST);

		// if (userName != null) {
		// BooleanQuery userQuery = new BooleanQuery();
		// userQuery.add(new TermQuery(new Term("entity", "InvestigationUser")), Occur.MUST);
		// userQuery.add(new TermQuery(new Term("name", userName)), Occur.MUST);
		// Query toQuery = JoinUtil.createJoinQuery("investigation", false, "id", userQuery,
		// isearcher, ScoreMode.None);
		// theQuery.add(toQuery, Occur.MUST);
		// }

		if (text != null) {
			theQuery.add(parser.parse(text, "text"), Occur.MUST);
		}

		if (lower != null && upper != null) {
			theQuery.add(new TermRangeQuery("startDate", new BytesRef(lower), new BytesRef(upper),
					true, true), Occur.MUST);
			theQuery.add(new TermRangeQuery("endDate", new BytesRef(lower), new BytesRef(upper),
					true, true), Occur.MUST);
		}

		for (ParameterPOJO parameter : parms) {
			BooleanQuery paramQuery = new BooleanQuery();
			paramQuery.add(new TermQuery(new Term("entity", "DatasetParameter")), Occur.MUST);
			if (parameter.getName() != null) {
				paramQuery
						.add(new WildcardQuery(new Term("name", parameter.getName())), Occur.MUST);
			}
			if (parameter.getUnits() != null) {
				paramQuery.add(new WildcardQuery(new Term("units", parameter.getUnits())),
						Occur.MUST);
			}
			if (parameter.getStringValue() != null) {
				paramQuery.add(new WildcardQuery(
						new Term("stringValue", parameter.getStringValue())), Occur.MUST);
			} else if (parameter.getLowerDateValue() != null
					&& parameter.getUpperDateValue() != null) {
				paramQuery
						.add(new TermRangeQuery("dateTimeValue", new BytesRef(parameter
								.getLowerDateValue()), new BytesRef(upper), true, true), Occur.MUST);

			} else if (parameter.getLowerNumericValue() != null
					&& parameter.getUpperNumericValue() != null) {
				paramQuery.add(NumericRangeQuery.newDoubleRange("numericValue",
						parameter.getLowerNumericValue(), parameter.getUpperNumericValue(), true,
						true), Occur.MUST);
			}
			Query toQuery = JoinUtil.createJoinQuery("dataset", false, "id", paramQuery, isearcher,
					ScoreMode.None);
			theQuery.add(toQuery, Occur.MUST);
		}

		return theQuery;

	}

	private void refreshSearcher() throws IOException {
		if (!searcherManager.isSearcherCurrent()) {
			IndexSearcher oldSearcher = isearcher;
			searcherManager.maybeRefreshBlocking();
			isearcher = searcherManager.acquire();
			searcherManager.release(oldSearcher);
			logger.debug("Got a new IndexSearcher " + isearcher);
		}

	}

	@Override
	public LuceneSearchResult datasetsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults, LuceneSearchResult last)
			throws IcatException {
		try {
			Query query = buildDatasetQuery(user, text, lower, upper, parms);
			return luceneSearchResult(isearcher.searchAfter(last.getScoreDoc(), query, maxResults));
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
