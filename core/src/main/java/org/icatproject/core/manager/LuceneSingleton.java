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
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;

/* There is synchronization code in this class - please understand it before making any changes */
@DependsOn("LoggingConfigurator")
@Singleton
public class LuceneSingleton {

	@PersistenceUnit(unitName = "icat")
	private EntityManagerFactory entityManagerFactory;

	public class LuceneSearchResult {

		private Query query;
		private List<String> results;
		private ScoreDoc scoreDoc;

		public LuceneSearchResult(List<String> results, ScoreDoc scoreDoc, Query query) {
			this.results = results;
			this.scoreDoc = scoreDoc;
			this.query = query;
		}

		public List<String> getResults() {
			return results;
		}

	}

	public class PopulateThread extends Thread {

		private EntityManager manager;

		public PopulateThread(EntityManagerFactory entityManagerFactory) {
			manager = entityManagerFactory.createEntityManager();
			logger.debug("Start new populate thread");
		}

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

	public void addDocument(EntityBaseBean bean) throws IcatException {
		Document doc = buildDoc(bean);
		try {
			iwriter.addDocument(doc);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
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

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		try {
			iwriter.deleteDocuments(new Term("id", id));
			logger.debug("Deleted document: " + id);
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

	public boolean getActive() {
		return active;
	}

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
				Analyzer analyzer = new IcatAnalyzer(Version.LUCENE_43);
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
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

	public synchronized void populate(Class<?> klass) {
		populateList.add(klass);
		if (populateThread == null || (populateThread).getState() == Thread.State.TERMINATED) {
			populateThread = new PopulateThread(entityManagerFactory);
			populateThread.start();
		}
	}

	public LuceneSearchResult search(String queryString, int count, String entityName)
			throws IcatException {
		if (entityName != null) {
			try {
				Class<?> klass = Class.forName(Constants.ENTITY_PREFIX + entityName);
				System.out.println(klass);
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
			return new LuceneSearchResult(results, lastDoc, query);
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public LuceneSearchResult searchAfter(LuceneSearchResult last, int count) throws IcatException {
		try {
			List<String> results = new ArrayList<String>();
			Query query = last.query;
			ScoreDoc[] hits = isearcher.searchAfter(last.scoreDoc, last.query, count).scoreDocs;
			for (ScoreDoc hit : hits) {
				Document doc = isearcher.doc(hit.doc);
				results.add(doc.get("id"));
			}
			ScoreDoc lastDoc = results.isEmpty() ? null : hits[hits.length - 1];
			return new LuceneSearchResult(results, lastDoc, query);
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void updateDocument(EntityBaseBean bean) throws IcatException {
		Document doc = buildDoc(bean);
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		try {
			iwriter.updateDocument(new Term("id", id), doc);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}
}
