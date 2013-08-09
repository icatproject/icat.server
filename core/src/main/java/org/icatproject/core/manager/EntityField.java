package org.icatproject.core.manager;

public class EntityField {

	public enum RelType {
		ATTRIBUTE, MANY, ONE
	}

	private String comment;
	private String name;
	private boolean notNullable;
	private RelType relType;
	private Integer stringLength;
	private String type;

	public String getComment() {
		return comment;
	}

	public String getName() {
		return name;
	}

	public RelType getRelType() {
		return relType;
	}

	public Integer getStringLength() {
		return stringLength;
	}

	public String getType() {
		return type;
	}

	// TODO get rid of this
	public Boolean isCascaded() {
		return relType == RelType.MANY;
	}

	public boolean isNotNullable() {
		return notNullable;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNotNullable(boolean notNullable) {
		this.notNullable = notNullable;
	}

	public void setRelType(RelType relType) {
		this.relType = relType;
	}

	public void setStringLength(Integer stringLength) {
		this.stringLength = stringLength;
	}

	public void setType(String type) {
		this.type = type;
	}

}
