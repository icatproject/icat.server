package org.icatproject.core.manager.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds information for a single faceted dimension, or field.
 * Each dimension will have a list of FacetLabels, and to prevent ambiguity is
 * associated with the target entity that was faceted. For example, both a
 * Dataset and a DatasetParameter might have the "type.name" dimension.
 */
public class FacetDimension {

	private String target;
	private String dimension;
	private List<FacetLabel> facets = new ArrayList<>();

	public FacetDimension(String target, String dimension) {
		this.target = target;
		this.dimension = dimension;
	}

	public FacetDimension(String target, String dimension, FacetLabel... labels) {
		this.target = target;
		this.dimension = dimension;
		Collections.addAll(facets, labels);
	}

	public List<FacetLabel> getFacets() {
		return facets;
	}

	public String getDimension() {
		return dimension;
	}

	public String getTarget() {
		return target;
	}

}
