package org.icatproject.core.manager;

public class ScoredEntityBaseBean implements HasEntityId {

	private Long id;
	private float score;

	public ScoredEntityBaseBean(Long id, float score) {
		this.id = id;
		this.score = score;
	}

	public Long getId() {
		return id;
	}

	public float getScore() {
		return score;
	}

}
