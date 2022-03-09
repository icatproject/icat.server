package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

	private String uid;
	private List<ScoredEntityBaseBean> results = new ArrayList<>();

	public List<ScoredEntityBaseBean> getResults() {
		return results;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

}
