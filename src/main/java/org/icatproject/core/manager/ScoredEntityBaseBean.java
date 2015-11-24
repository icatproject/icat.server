package org.icatproject.core.manager;

import org.icatproject.core.entity.EntityBaseBean;

public class ScoredEntityBaseBean {

	private EntityBaseBean entityBaseBean;
	private float score;

	public ScoredEntityBaseBean(EntityBaseBean eb, float score) {
		this.entityBaseBean = eb;
		this.score = score;
	}

	public EntityBaseBean getEntityBaseBean() {
		return entityBaseBean;
	}

	public float getScore() {
		return score;
	}

}
