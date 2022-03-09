package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information for a single facetable dimension, or field.
 * Each dimension will have a list of FacetLabels.
 */
public class FacetDimension {

	private String dimension;
	private List<FacetLabel> facets = new ArrayList<>();

	public FacetDimension(String dimension) {
		this.dimension = dimension;
	}

	public List<FacetLabel> getFacets() {
		return facets;
	}

	public String getDimension() {
		return dimension;
	}

}
