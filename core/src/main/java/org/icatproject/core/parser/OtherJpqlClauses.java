package org.icatproject.core.parser;

import org.apache.log4j.Logger;

public class OtherJpqlClauses {

	static Logger logger = Logger.getLogger(OtherJpqlClauses.class);

	private String string;

	public OtherJpqlClauses(Input input) throws ParserException {
		StringBuilder sb = new StringBuilder();
		Token t = input.consume();
		sb.append(t.getValue());
		t = input.peek(0);
		while (t != null && t.getType() != Token.Type.INCLUDE && t.getType() != Token.Type.LIMIT) {
			if (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING) {
				throw new ParserException("GROUP BY and HAVING keywords are not compatible with returned result");
			}
			t = input.consume();
			sb.append(" " + t.getValue());
			t = input.peek(0);
		}
		string = sb.toString();
		logger.debug(string);
	}

	@Override
	public String toString() {
		return string;
	}

}
