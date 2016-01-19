package org.icatproject.core.parser;

import org.icatproject.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubSelectClause {

	static Logger logger = LoggerFactory.getLogger(SubSelectClause.class);

	private String string;

	public SubSelectClause(Input input) throws ParserException {
		StringBuilder sb = new StringBuilder();
		Token t = input.peek(0);
		int level = 1; // Once we get back to the same level of brackets leave
		while (t != null && t.getType() != Token.Type.ORDER && t.getType() != Token.Type.INCLUDE
				&& t.getType() != Token.Type.LIMIT) {
			if (t.getType() == Token.Type.OPENPAREN) {
				level++;
			} else if (t.getType() == Token.Type.CLOSEPAREN) {
				level--;
				if (level == 0) {
					break;
				}
			}
			String val = t.getValue();
			if (t.getType() == Token.Type.NAME) {
				if (val.startsWith(Constants.ENUMPREFIX)) {
					int n = Constants.ENUMPREFIX.split("\\.").length;
					String vals[] = val.split("\\.");
					if (vals.length != n + 2) {
						throw new ParserException(
								"Enum literal " + val + " must contain exactly " + (n + 2) + " parts");
					}
					sb.append(" " + Constants.ENTITY_PREFIX + t.getValue().substring(Constants.ENUMPREFIX.length()));
				} else {
					sb.append(" " + val);
				}
			} else {
				if (t.getType() == Token.Type.STRING) {
					val = "'" + val.replace("'", "''") + "'";
				} else if (t.getType() == Token.Type.TIMESTAMP) {
					val = ":"
							+ val.replace(" ", "").replace(":", "").replace("-", "").replace("{", "").replace("}", "");
				}
				sb.append(" " + val);
			}
			input.consume();
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
