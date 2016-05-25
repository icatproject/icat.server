package org.icatproject.core.parser;

import java.util.HashSet;
import java.util.Set;

public class SelectClause {

	private Set<String> idPaths = new HashSet<>();

	private boolean count;

	private String clause;

	public SelectClause(Input input) throws ParserException {
		/*
		 * Identify the set of identification variables and paths that must be
		 * present in the from clause. Also see if COUNT is used.
		 */
		input.consume(Token.Type.SELECT);
		StringBuilder sb = new StringBuilder();

		Token t = input.peek(0);
		while (t != null && t.getType() != Token.Type.FROM) {
			input.consume();
			sb.append(" " + t.getValue());
			if (t.getType() == Token.Type.NAME) {
				idPaths.add(t.getValue());
			} else if (t.getType() == Token.Type.COUNT) {
				count = true;
			}
			t = input.peek(0);
		}
		clause = sb.toString();
	}

	/** Only interested to detect single count request */
	public boolean isCount() {
		return idPaths.size() == 1 && count;
	}

	public Set<String> getIdPaths() {
		return idPaths;
	}

	public String toString() {
		return clause;
	}

}
