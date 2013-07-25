package org.icatproject.core.parser;

import org.apache.log4j.Logger;

public class LimitClause {

	static Logger logger = Logger.getLogger(LimitClause.class);

	private int offset;

	private Integer number;

	public LimitClause(Input input) throws ParserException {
		input.consume(Token.Type.LIMIT);
		Token t = input.consume(Token.Type.INTEGER);
		offset = Integer.parseInt(t.getValue());
		input.consume(Token.Type.COMMA);
		t = input.consume(Token.Type.INTEGER, Token.Type.MULT);
		if (t.getType() == Token.Type.INTEGER) {
			number = Integer.parseInt(t.getValue());
		}
		logger.debug(this);
	}

	@Override
	public String toString() {
		return "LIMIT " + offset + "," + (number == null?"*":number);
	}

	public int getOffset() {
		return offset;
	}

	public Integer getNumber() {
		return number;
	}

}
