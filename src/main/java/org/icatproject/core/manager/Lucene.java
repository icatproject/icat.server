package org.icatproject.core.manager;

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.search.ScoreDoc;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;

public interface Lucene {

	@SuppressWarnings("serial")
	public class ParameterPOJO implements Serializable {

		private String name;
		private String units;
		private String stringValue;
		private String lowerDateValue;
		private String upperDateValue;
		private Double lowerNumericValue;
		private Double upperNumericValue;

		public ParameterPOJO(String name, String units, String stringValue) {
			this.name = name;
			this.units = units;
			this.stringValue = stringValue;
		}

		public ParameterPOJO(String name, String units, String lower, String upper) {
			this.name = name;
			this.units = units;
			lowerDateValue = lower;
			upperDateValue = upper;
		}

		public ParameterPOJO(String name, String units, double lower, double upper) {
			this.name = name;
			this.units = units;
			lowerNumericValue = lower;
			upperNumericValue = upper;
		}

		public String getLowerDateValue() {
			return lowerDateValue;
		}

		public String getUpperDateValue() {
			return upperDateValue;
		}

		public Double getLowerNumericValue() {
			return lowerNumericValue;
		}

		public Double getUpperNumericValue() {
			return upperNumericValue;
		}

		public String getName() {
			return name;
		}

		public String getUnits() {
			return units;
		}

		public String getStringValue() {
			return stringValue;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("name:" + name);
			if (units != null) {
				sb.append(" units:" + units);
			}
			if (stringValue != null) {
				sb.append(" stringValue:" + stringValue);
			} else if (lowerDateValue != null) {
				sb.append(" lowerDateValue:" + lowerDateValue + " upperDateValue:" + upperDateValue);
			} else if (lowerNumericValue != null) {
				sb.append(", lowerNumericValue:" + lowerNumericValue + " upperNumericValue:"
						+ upperNumericValue);
			}
			return sb.toString();
		}

	}

	@SuppressWarnings("serial")
	public class LuceneSearchResult implements Serializable {

		private List<String> results;
		private int doc;
		private int shardIndex;
		private float score;
		private boolean scoreDocExists;

		public LuceneSearchResult(List<String> results, ScoreDoc lastDoc) {
			this.results = results;
			if (lastDoc != null) {
				this.doc = lastDoc.doc;
				this.shardIndex = lastDoc.shardIndex;
				this.score = lastDoc.score;
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

	LuceneSearchResult investigations(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxResults)
			throws IcatException;

	LuceneSearchResult investigationsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int blockSize,
			LuceneSearchResult last) throws IcatException;

	LuceneSearchResult datasets(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults) throws IcatException;

	LuceneSearchResult datasetsAfter(String user, String text, String lower, String upper,
			List<ParameterPOJO> parms, int maxResults, LuceneSearchResult last)
			throws IcatException;

}
