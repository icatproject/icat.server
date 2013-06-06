package org.icatproject.core.manager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;

public class LuceneSingleton {

	private static LuceneSingleton instance;
	final static Logger logger = Logger.getLogger(LuceneSingleton.class);
	private static int users;

	public synchronized static LuceneSingleton getInstance() {
		if (instance == null) {
			instance = new LuceneSingleton();
		} else if (instance.directory == null) {
			instance.refresh();
			logger.debug("Refreshed LuceneSingleton");
		}
		users++;
		logger.debug("LuceneSingleton has " + users + " users.");
		return instance;
	}

	private String luceneDirectory;

	private void refresh() {
		try {
			directory = FSDirectory.open(new File(luceneDirectory));
			logger.debug("Opened FSDirectory with lockid " + directory.getLockID());
			Analyzer analyzer = new ESNAnalyzer(Version.LUCENE_40);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
			iwriter = new IndexWriter(directory, config);
			String[] files = directory.listAll();
			if (files.length == 1 && files[0].equals("write.lock")) {
				logger.debug("Directory only has the write.lock file so commit and reopen");
				iwriter.commit();
				iwriter.close();
				iwriter = new IndexWriter(directory, config);
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			parser = new QueryParser(Version.LUCENE_40, "all", analyzer);
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
	}

	private IndexWriter iwriter;
	private QueryParser parser;
	private FSDirectory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private EntityInfoHandler ei;

	private LuceneSingleton() {
		ei = EntityInfoHandler.getInstance();
		luceneDirectory = PropertyHandler.getInstance().getLuceneDirectory();
		refresh();
		logger.debug("Created LuceneSingleton");
	}

	private Document buildDoc(EntityBaseBean bean) throws IcatException {
		Map<Field, Integer> stringFields = ei.getStringFields(bean.getClass());
		StringBuilder sb = new StringBuilder();
		for (Entry<Field, Integer> item : stringFields.entrySet()) {
			Field field = item.getKey();
			Method getter = ei.getGetters(bean.getClass()).get(field);
			try {
				sb.append(((String) getter.invoke(bean)) + " ");
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
		doc.add(new TextField("all", sb.toString(), Store.NO));
		doc.add(new TextField(bean.getClass().getSimpleName(), sb.toString(), Store.NO));
		logger.debug("Created document " + sb.toString() + " to index for " + id);
		return doc;
	}

	public void addDocument(EntityBaseBean bean) throws IcatException {
		Document doc = buildDoc(bean);
		try {
			iwriter.addDocument(doc);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void commit() throws IcatException {
		try {
			iwriter.commit();
			logger.debug("Now have " + iwriter.numDocs() + " products indexed");
		} catch (IOException e) {
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

	public void deleteDocument(EntityBaseBean bean) throws IcatException {
		String id = bean.getClass().getSimpleName() + ":" + bean.getId();
		try {
			iwriter.deleteDocuments(new Term("id", id));
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void clear() throws IcatException {
		try {
			iwriter.deleteAll();
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public synchronized void close() {
		users--;
		if (users == 0) {
			try {
				logger.debug("Closing IndexWriter for directory lockid " + directory.getLockID());
				iwriter.commit();
				iwriter.close();
				iwriter = null;
				logger.debug("IndexWriter closed for directory lockid " + directory.getLockID());
				ireader.close();
				logger.debug("IndexReader closed for directory lockid " + directory.getLockID());
				directory.close();
				directory = null;
				logger.debug("Directory closed");
			} catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				logger.fatal(errors.toString());
			}
		} else {
			logger.debug("LuceneSingleton still has " + users + " users after close.");
		}
	}

	public List<String> search(String queryString, int offset, int count) throws IcatException {
		try {
			List<String> results = new ArrayList<String>();
			ScoreDoc[] hits = null;
			Query query = parser.parse(queryString);
			hits = isearcher.search(query, offset + count).scoreDocs;
			for (int i = offset; i < hits.length; i++) {
				results.add(isearcher.doc(hits[i].doc).get("id"));
				logger.debug("Retrieved " + isearcher.doc(hits[i].doc));
			}
			return results;
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public int getCount(String queryString) throws IcatException {
		try {
			TotalHitCountCollector counter = new TotalHitCountCollector();
			isearcher.search(parser.parse(queryString), counter);
			return counter.getTotalHits();
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public synchronized int getInstanceCount() {
		return users;
	}
}
