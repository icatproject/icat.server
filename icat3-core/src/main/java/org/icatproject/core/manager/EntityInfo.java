package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.core.manager.EntityInfoHandler.KeyType;


public class EntityInfo {

	private String classComment;

	private List<Constraint> constraints = new ArrayList<Constraint>();

	private List<EntityField> fields = new ArrayList<EntityField>();

	private KeyType keyType;

	private String keyFieldname;

	public String getClassComment() {
		return classComment;
	}

	public void setClassComment(String classComment) {
		this.classComment = classComment;
	}

	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}

	public void setKeyFieldname(String keyFieldname) {
		this.keyFieldname = keyFieldname;

	}

	public void setFields(List<EntityField> fields) {
		this.fields = fields;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public KeyType getKeyType() {
		return keyType;
	}

	public String getKeyFieldname() {
		return keyFieldname;
	}

	public List<EntityField> getFields() {
		return fields;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

}