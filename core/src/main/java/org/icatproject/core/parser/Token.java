package org.icatproject.core.parser;

public class Token {

	public enum Type {
		// ICAT Tokens
		STRING, NAME, OPENPAREN, CLOSEPAREN, COMPOP, INTEGER, PARAMETER, COMMA,

		// ICAT Keywords
		INCLUDE, LIMIT, TIMESTAMP,

		// Keywords
		ABS, ALL, AND, ANY, AS, ASC, AVG, BETWEEN, BOTH, BY, CONCAT, COUNT, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP,

		DESC, DISTINCT, DIV, EMPTY, FALSE, FETCH, FROM, GROUP, HAVING, IN, INNER, IS, JOIN, LEADING, LEFT, LENGTH, LOCATE,

		LOWER, MAX, MEMBER, MIN, MINUS, MOD, MULT, NOT, NULL, OR, ORDER, OUTER, PLUS, REAL, SELECT, SIZE, SQRT, SUBSTRING,

		SUM, TRAILING, TRIM, TRUE, UPPER, WHERE;

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
