package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class Constraint {

	private List<String> fieldNames = new ArrayList<String>();

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

}
