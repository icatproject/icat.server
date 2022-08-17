package org.icatproject.core.manager.search;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonValue;

/**
 * Represents the results from a single search performed against the engine.
 * Stores a list of ScoredEntityBaseBean, FacetDimension, and a JsonValue
 * representing the last document returned if appropriate.
 */
public class SearchResult {

	private JsonValue searchAfter;
	private List<ScoredEntityBaseBean> results = new ArrayList<>();
	private List<FacetDimension> dimensions;

	public SearchResult() {
	}

	public SearchResult(JsonValue searchAfter, List<ScoredEntityBaseBean> results, List<FacetDimension> dimensions) {
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

	public JsonValue getSearchAfter() {
		return searchAfter;
	}

	public void setSearchAfter(JsonValue searchAfter) {
		this.searchAfter = searchAfter;
	}

}
