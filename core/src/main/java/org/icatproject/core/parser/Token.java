package org.icatproject.core.parser;



public class Token {

	enum Type {
		 STRING, NAME, OPENPAREN, CLOSEPAREN, BRA, KET, COMPOP, ENTSEP, INTEGER, AND, OR, NOT, REAL, PARAMETER, DISTINCT, ORDER, BY, ASC, DESC, COMMA, IN, BETWEEN, TIMESTAMP, INCLUDE, MIN, MAX, AVG, COUNT, SUM;
		 
		 public String toString() {
			return Tokenizer.getTypeToPrint(this);
		 }
	};
	
	private Type type;

	private String value;

	public String getValue() {
		return value;
	}

	Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	public Token(Type type, char value) {
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
