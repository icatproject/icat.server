package org.icatproject.core.manager;

public class ScoredEntityBaseBean {

	private long entityBaseBeanId;
	private float score;

	public ScoredEntityBaseBean(String id, double score) {
		this.entityBaseBeanId = Long.parseLong(id);
		this.score = (float) score;
	}

	public ScoredEntityBaseBean(long id, float score) {
		this.entityBaseBeanId = id;
		this.score = score;
	}

	public long getEntityBaseBeanId() {
		return entityBaseBeanId;
	}

	public float getScore() {
		return score;
	}

}
