package org.icatproject.core.parser;

import org.icatproject.core.parser.Token.Type;


public class SortSpecification {

	// SortSpecification ::= name ( "ASC" | "DESC" ) ?

	private String field;
	private boolean ascending = true;

	public SortSpecification(Input input) throws ParserException {
		this.field = input.consume(Token.Type.NAME).getValue();
		Token t = input.peek(0);
		if (t != null) {
			Type tt = t.getType();
			if (tt == Token.Type.ASC || tt == Token.Type.DESC) {
				ascending = tt == Token.Type.ASC;
				input.consume();
			}
		}
	}

	@Override
	public String toString() {
		if (ascending) {
			return field;
		} else {
			return field + " DESC";
		}
	}

	public String getValue() {
		return toString();
	}

}
