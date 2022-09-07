package org.icatproject.core.manager.search;

import javax.json.JsonNumber;
import javax.json.JsonObject;

/**
 * Holds information for a single label value pair.
 * The value is the number of times the label is present in a particular facet
 * dimension.
 */
public class FacetLabel {

	private String label;
	private Long value;
	private JsonNumber from;
	private JsonNumber to;

	public FacetLabel(String label, Long value) {
		this.label = label;
		this.value = value;
	}

	public FacetLabel(JsonObject jsonObject) {
		label = jsonObject.getString("key");
		value = jsonObject.getJsonNumber("doc_count").longValueExact();
		if (jsonObject.containsKey("from")) {
			from = jsonObject.getJsonNumber("from");
		}
		if (jsonObject.containsKey("to")) {
			to = jsonObject.getJsonNumber("to");
		}
	}

	public FacetLabel(String label, JsonObject jsonObject) {
		this.label = label;
		value = jsonObject.getJsonNumber("doc_count").longValueExact();
		if (jsonObject.containsKey("from")) {
			from = jsonObject.getJsonNumber("from");
		}
		if (jsonObject.containsKey("to")) {
			to = jsonObject.getJsonNumber("to");
		}
	}

	public String getLabel() {
		return label;
	}

	public Long getValue() {
		return value;
	}

	public JsonNumber getFrom() {
		return from;
	}

	public JsonNumber getTo() {
		return to;
	}

	public String toString() {
		return label + ": " + value;
	}

}
