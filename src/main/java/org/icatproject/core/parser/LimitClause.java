package org.icatproject.core.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitClause {

	static Logger logger = LoggerFactory.getLogger(LimitClause.class);

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
		logger.debug(this.toString());
	}

	@Override
	public String toString() {
		return "LIMIT " + offset + "," + (number == null ? "*" : number);
	}

	public int getOffset() {
		return offset;
	}

	public Integer getNumber() {
		return number;
	}

}
