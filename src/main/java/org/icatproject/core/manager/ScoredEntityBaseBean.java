package org.icatproject.core.manager;

import javax.json.JsonObject;

public class ScoredEntityBaseBean {

	private long entityBaseBeanId;
	private float score;
	private JsonObject source;

	public ScoredEntityBaseBean(String id, float score, JsonObject source) {
		this.entityBaseBeanId = Long.parseLong(id);
		this.score = score;
		this.source = source;
	}

	public ScoredEntityBaseBean(long id, float score, JsonObject source) {
		this.entityBaseBeanId = id;
		this.score = score;
		this.source = source;
	}

	public long getEntityBaseBeanId() {
		return entityBaseBeanId;
	}

	public float getScore() {
		return score;
	}

	public JsonObject getSource() {
		return source;
	}

	public void setSource(JsonObject source) {
		this.source = source;
	}

}
