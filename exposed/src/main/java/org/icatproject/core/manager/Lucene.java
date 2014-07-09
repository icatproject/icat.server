package org.icatproject.core.manager;

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;

public interface Lucene {

	@SuppressWarnings("serial")
	public class LuceneSearchResult implements Serializable {

		private List<String> results;
		private int doc;
		private int shardIndex;
		private float score;
		private boolean scoreDocExists;

		public LuceneSearchResult(List<String> results, ScoreDoc scoreDoc, Query query) {
			this.results = results;
			if (scoreDoc != null) {
				this.doc = scoreDoc.doc;
				this.shardIndex = scoreDoc.shardIndex;
				this.score = scoreDoc.score;
				scoreDocExists = true;
			}
		}

		public List<String> getResults() {
			return results;
		}

		public ScoreDoc getScoreDoc() {
			return scoreDocExists ? new ScoreDoc(doc, score, shardIndex) : null;
		}

	}

	void addDocument(EntityBaseBean entityBaseBean) throws IcatException;

	void clear() throws IcatException;

	void commit() throws IcatException;

	void deleteDocument(EntityBaseBean entityBaseBean) throws IcatException;

	boolean getActive();

	List<String> getPopulating();

	void populate(Class<?> klass);

	LuceneSearchResult search(String query, int blockSize, String entityName) throws IcatException;

	LuceneSearchResult searchAfter(String query, int blockSize, String entityName,
			LuceneSearchResult last) throws IcatException;

	void updateDocument(EntityBaseBean entityBaseBean) throws IcatException;

}
