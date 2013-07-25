package org.icatproject.core.parser;

public class Token {

	enum Type {
		// ICAT Tokens
		STRING, NAME, OPENPAREN, CLOSEPAREN, COMPOP, INTEGER, PARAMETER, COMMA,

		// Keywords
		ALL, ANY, AND, AS, ASC, BETWEEN, BOTH, BY, DESC, DISTINCT, DIV, EMPTY, FETCH, FROM, GROUP, HAVING, IN, INNER,

		IS, JOIN, LEADING, LEFT, MEMBER, MINUS, MULT, NOT, NULL, OR, ORDER, OUTER, PLUS, REAL, SELECT, TRAILING, WHERE,

		// ICAT Keywords
		INCLUDE, LIMIT, TIMESTAMP,

		// Aggregate functions
		AVG, COUNT, MAX, MIN, SUM,

		// String Expressions
		CONCAT, LENGTH, LOCATE, SUBSTRING, TRIM,

		// Arithmetic Expressions
		ABS, MOD, SQRT, SIZE,

		// Date/Time Expressions
		CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP;

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
