package org.icatproject.core.manager;

import javax.json.JsonObject;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;

public class ScoredEntityBaseBean {

	private long entityBaseBeanId;
	private int engineDocId;
	private float score;
	private JsonObject source;

	/**
	 * Represents a single entity returned from a search, and relevant search engine
	 * information.
	 * 
	 * @param engineDocId The id of the search engine Document that represents this
	 *                    entity. This should not be confused with the
	 *                    entityBaseBeanId. This is needed in order to enable
	 *                    subsequent searches to "search after" Documents which have
	 *                    already been returned once.
	 * @param score       A float generated by the engine to indicate the relevance
	 *                    of the returned Document to the search term(s). Higher
	 *                    scores are more relevant. May be null if the results were
	 *                    not sorted by relevance.
	 * @param source      JsonObject containing the requested fields of the Document
	 *                    as key-value pairs. At the very least, this should contain
	 *                    the ICAT "id" of the entity.
	 * @throws IcatException If "id" and the corresponding entityBaseBeanId are not
	 *                       a key-value pair in the source JsonObject.
	 */
	public ScoredEntityBaseBean(int engineDocId, float score, JsonObject source) throws IcatException {
		if (!source.keySet().contains("id")) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"Document source must have 'id' and the entityBaseBeanId as a key-value pair.");
		}
		this.engineDocId = engineDocId;
		this.score = score;
		this.source = source;
		this.entityBaseBeanId = new Long(source.getString("id"));
	}

	public long getEntityBaseBeanId() {
		return entityBaseBeanId;
	}

	public int getEngineDocId() {
		return engineDocId;
	}

	public float getScore() {
		return score;
	}

	public JsonObject getSource() {
		return source;
	}

}
