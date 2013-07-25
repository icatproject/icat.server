package org.icatproject.core.parser;

import org.apache.log4j.Logger;

public class SubSelectClause {

	static Logger logger = Logger.getLogger(SubSelectClause.class);

	private String string;

	public SubSelectClause(Input input) throws ParserException {
		StringBuilder sb = new StringBuilder();
		// input.consume(Token.Type.WHERE);
		// sb.append("WHERE");
		// Token t = input.peek(0);
		// while (t != null && t.getType() != Token.Type.GROUP && t.getType() != Token.Type.HAVING
		// && t.getType() != Token.Type.ORDER && t.getType() != Token.Type.INCLUDE
		// && t.getType() != Token.Type.LIMIT) {
		// t = input.consume();
		// if (t.getType() == Token.Type.OPENPAREN && input.peek(0).getType()== Token.Type.SELECT) {
		// subSelectClause = new SubSelectClause(input);
		// t = input.consume(Token.Type.CLOSEPAREN);
		// }
		// t = input.peek(0);
		// }
		string = sb.toString();
		logger.debug(string);
	}

	@Override
	public String toString() {
		return string;
	}

}
