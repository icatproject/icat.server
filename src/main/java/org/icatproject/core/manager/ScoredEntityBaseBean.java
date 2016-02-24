package org.icatproject.core.manager;

public class ScoredEntityBaseBean {

	private long entityBaseBeanId;
	private float score;

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
