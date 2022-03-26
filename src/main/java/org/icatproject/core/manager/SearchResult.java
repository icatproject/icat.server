package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

	private String searchAfter;
	private List<ScoredEntityBaseBean> results = new ArrayList<>();

	public SearchResult() {}

	public SearchResult(String searchAfter, List<ScoredEntityBaseBean> results) {
		this.searchAfter = searchAfter;
		this.results = results;
	}

	public List<ScoredEntityBaseBean> getResults() {
		return results;
	}

	public String getSearchAfter() {
		return searchAfter;
	}

	public void setSearchAfter(String searchAfter) {
		this.searchAfter = searchAfter;
	}

}
