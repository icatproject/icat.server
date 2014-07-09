package org.icatproject.exposed.importParser;

public class Token {

	public enum Type {
		// ICAT Tokens
		STRING, NAME, OPENPAREN, CLOSEPAREN, INTEGER, COMMA, COLON, TIMESTAMP, REAL, NULL, BOOLEAN, QMARK;

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
