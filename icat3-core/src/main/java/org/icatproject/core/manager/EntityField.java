package org.icatproject.core.manager;

public class EntityField {

	private String name;
	private String type;
	private boolean notNullable;
	private Integer stringLength;
	private String comment;

	public enum RelType {
		MANY, ONE, ATTRIBUTE
	}

	public void setName(String name) {
		this.name = name;
	}

	private RelType relType;
	private Boolean cascaded;

	public Boolean isCascaded() {
		return cascaded;
	}

	public RelType getRelType() {
		return relType;
	}

	public void setRelType(RelType relType) {
		this.relType = relType;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isNotNullable() {
		return notNullable;
	}

	public Integer getStringLength() {
		return stringLength;
	}

	public String getComment() {
		return comment;
	}

	public void setNotNullable(boolean notNullable) {
		this.notNullable = notNullable;
	}

	public void setStringLength(Integer stringLength) {
		this.stringLength = stringLength;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCascaded(Boolean cascaded) {
		this.cascaded = cascaded;

	}

}
