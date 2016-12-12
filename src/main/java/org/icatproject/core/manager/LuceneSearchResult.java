package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class LuceneSearchResult {

	private Long uid;
	private List<ScoredEntityBaseBean> results = new ArrayList<>();

	public List<ScoredEntityBaseBean> getResults() {
		return results;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Long getUid() {
		return uid;
	}

}
