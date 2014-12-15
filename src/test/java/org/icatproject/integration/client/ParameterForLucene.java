package org.icatproject.integration.client;

import java.util.Date;

public class ParameterForLucene {

	private String name;
	private String units;
	private String stringValue;
	private Date lowerDateValue;
	private Date upperDateValue;
	private Double lowerNumericValue;
	private Double upperNumericValue;

	public ParameterForLucene(String name, String units, String stringValue) {
		this.name = name;
		this.units = units;
		this.stringValue = stringValue;
	}

	public ParameterForLucene(String name, String units, Date lower, Date upper) {
		this.name = name;
		this.units = units;
		lowerDateValue = lower;
		upperDateValue = upper;
	}

	public ParameterForLucene(String name, String units, double lower, double upper) {
		this.name = name;
		this.units = units;
		lowerNumericValue = lower;
		upperNumericValue = upper;
	}

	public Date getLowerDateValue() {
		return lowerDateValue;
	}

	public Date getUpperDateValue() {
		return upperDateValue;
	}

	public Double getLowerNumericValue() {
		return lowerNumericValue;
	}

	public Double getUpperNumericValue() {
		return upperNumericValue;
	}

	public String getName() {
		return name;
	}

	public String getUnits() {
		return units;
	}

	public String getStringValue() {
		return stringValue;
	}

}
