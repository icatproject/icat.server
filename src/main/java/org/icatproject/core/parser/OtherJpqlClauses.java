package org.icatproject.core.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherJpqlClauses {

	static Logger logger = LoggerFactory.getLogger(OtherJpqlClauses.class);

	private String string;

	public OtherJpqlClauses(Input input) throws ParserException {
		StringBuilder sb = new StringBuilder();
		Token t = input.peek(0);
		while (t != null && t.getType() != Token.Type.INCLUDE && t.getType() != Token.Type.LIMIT) {
			input.consume();
			String val = t.getValue();
			sb.append(" " + val);
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
