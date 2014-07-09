package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {

	private String classComment;

	private List<Constraint> constraints = new ArrayList<Constraint>();

	private List<EntityField> fields = new ArrayList<EntityField>();

	public String getClassComment() {
		return classComment;
	}

	public void setClassComment(String classComment) {
		this.classComment = classComment;
	}

	public void setFields(List<EntityField> fields) {
		this.fields = fields;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public List<EntityField> getFields() {
		return fields;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

}