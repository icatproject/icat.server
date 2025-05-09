package org.icatproject.core.manager.search;

import jakarta.json.JsonObject;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.manager.HasEntityId;

public class ScoredEntityBaseBean implements HasEntityId {

	private long id;
	private int shardIndex;
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
	 * @param shardIndex  The index of the shard that the entity was found on. This
	 *                    is only relevant when merging results with the icat.lucene
	 *                    component.
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
	public ScoredEntityBaseBean(int engineDocId, int shardIndex, float score, JsonObject source) throws IcatException {
		if (!source.containsKey("id")) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"Document source must have 'id' and the entityBaseBeanId as a key-value pair, but it was "
							+ source);
		}
		this.engineDocId = engineDocId;
		this.shardIndex = shardIndex;
		this.score = score;
		this.source = source;
		this.id = source.getJsonNumber("id").longValueExact();
	}

	public Long getId() {
		return id;
	}

	public int getEngineDocId() {
		return engineDocId;
	}

	public int getShardIndex() {
		return shardIndex;
	}

	public float getScore() {
		return score;
	}

	public JsonObject getSource() {
		return source;
	}

}
