package org.icatproject.core.manager.search;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class ParameterPOJO implements Serializable {

	public String name;
	public String units;
	public String stringValue;
	public Date lowerDateValue;
	public Date upperDateValue;
	public Double lowerNumericValue;
	public Double upperNumericValue;

	public ParameterPOJO(String name, String units, String stringValue) {
		this.name = name;
		this.units = units;
		this.stringValue = stringValue;
	}

	public ParameterPOJO(String name, String units, Date lower, Date upper) {
		this.name = name;
		this.units = units;
		lowerDateValue = lower;
		upperDateValue = upper;
	}

	public ParameterPOJO(String name, String units, double lower, double upper) {
		this.name = name;
		this.units = units;
		lowerNumericValue = lower;
		upperNumericValue = upper;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("name:" + name);
		if (units != null) {
			sb.append(" units:" + units);
		}
		if (stringValue != null) {
			sb.append(" stringValue:" + stringValue);
		} else if (lowerDateValue != null) {
			sb.append(" lowerDateValue:" + lowerDateValue.getTime() + " upperDateValue:" + upperDateValue.getTime());
		} else if (lowerNumericValue != null) {
			sb.append(", lowerNumericValue:" + lowerNumericValue + " upperNumericValue:" + upperNumericValue);
		}
		return sb.toString();
	}

}
