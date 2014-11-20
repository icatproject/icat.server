package org.icatproject.integration.client;

public class ParameterForLucene {

	private String name;
	private String units;
	private String stringValue;

	public ParameterForLucene(String name, String units, String stringValue) {
		this.name = name;
		this.units = units;
		this.stringValue = stringValue;
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
