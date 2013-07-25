package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class IncludeClause {

	static Logger logger = Logger.getLogger(IncludeClause.class);

	private String string;

	private boolean one;
	private List<String> values = new ArrayList<String>();

	public IncludeClause(Input input) throws ParserException {
		input.consume(Token.Type.INCLUDE);
		Token t = input.consume(Token.Type.NAME, Token.Type.INTEGER);
		if (t.getValue().equals("1")) {
			one = true;
		} else {
			values.add(t.getValue());
		}

		t = input.peek(0);
		while (t != null && t.getType() == Token.Type.COMMA) {
			t = input.consume(Token.Type.COMMA);
			t = input.consume(Token.Type.NAME);
			values.add(t.getValue());
			t = input.peek(0);
		}
		logger.debug(this);
	}

	@Override
	public String toString() {
		if (one) {
			return ("INCLUDE 1");
		} else {
			StringBuilder sb = new StringBuilder();
			for (String value:values) {
				if (sb.length() == 0) {
					sb.append("INCLUDE " + value);
				} else {
					sb.append(", " + value);
				}
			}
			return sb.toString();
		}
		
	}

}
