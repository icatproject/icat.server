package org.icatproject.core.manager;

/**
 * Holds information for a single label value pair.
 * The value is the number of times the label is present in a particular facet
 * dimension.
 */
public class FacetLabel {

	private String label;
	private Long value;

	public FacetLabel(String label, Long value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public Long getValue() {
		return value;
	}

}
