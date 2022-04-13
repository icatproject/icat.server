package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

	private String searchAfter;
	private List<ScoredEntityBaseBean> results = new ArrayList<>();
	private List<FacetDimension> dimensions;

	public SearchResult() {}

	public SearchResult(String searchAfter, List<ScoredEntityBaseBean> results, List<FacetDimension> dimensions) {
		this.searchAfter = searchAfter;
		this.results = results;
		this.dimensions = dimensions;
	}

	public List<FacetDimension> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<FacetDimension> dimensions) {
		this.dimensions = dimensions;
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
