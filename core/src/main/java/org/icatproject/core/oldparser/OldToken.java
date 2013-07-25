package org.icatproject.core.oldparser;



public class OldToken {

	enum Type {
		 STRING, NAME, OPENPAREN, CLOSEPAREN, BRA, KET, COMPOP, ENTSEP, INTEGER, AND, OR, NOT, REAL, PARAMETER, DISTINCT, ORDER, BY, ASC, DESC, COMMA, IN, BETWEEN, TIMESTAMP, INCLUDE, MIN, MAX, AVG, COUNT, SUM;
		 
		 public String toString() {
			return OldTokenizer.getTypeToPrint(this);
		 }
	};
	
	private Type type;

	private String value;

	public String getValue() {
		return value;
	}

	OldToken(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	public OldToken(Type type, char value) {
		this.type = type;
		this.value = Character.toString(value);
	}

	public Type getType() {
		return type;
	}

	public String toString() {
			return this.value;
	}

}
