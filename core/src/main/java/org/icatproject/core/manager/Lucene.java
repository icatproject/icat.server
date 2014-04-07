package org.icatproject.core.manager;

import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;

public interface Lucene {

	public class LuceneSearchResult {

		// TODO non-private?
		Query query;
		private List<String> results;
		ScoreDoc scoreDoc;

		public LuceneSearchResult(List<String> results, ScoreDoc scoreDoc, Query query) {
			this.results = results;
			this.scoreDoc = scoreDoc;
			this.query = query;
		}

		public List<String> getResults() {
			return results;
		}

	}

	LuceneSearchResult search(String query, int blockSize, String entityName) throws IcatException;

	void populate(Class<?> klass);

	void clear() throws IcatException;

	void commit() throws IcatException;

	List<String> getPopulating();

	LuceneSearchResult searchAfter(LuceneSearchResult last, int blockSize) throws IcatException;

	void addDocument(EntityBaseBean entityBaseBean) throws IcatException;

	void deleteDocument(EntityBaseBean entityBaseBean) throws IcatException;

	void updateDocument(EntityBaseBean entityBaseBean) throws IcatException;

	boolean getActive();

}
