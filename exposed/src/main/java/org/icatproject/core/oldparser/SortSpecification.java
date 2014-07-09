package org.icatproject.core.oldparser;

import org.icatproject.core.oldparser.OldToken.Type;


public class SortSpecification {

	// SortSpecification ::= name ( "ASC" | "DESC" ) ?

	private String field;
	private boolean ascending = true;

	public SortSpecification(OldInput input) throws OldParserException {
		this.field = input.consume(OldToken.Type.NAME).getValue();
		OldToken t = input.peek(0);
		if (t != null) {
			Type tt = t.getType();
			if (tt == OldToken.Type.ASC || tt == OldToken.Type.DESC) {
				ascending = tt == OldToken.Type.ASC;
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
