package org.icatproject.core.parser;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;

public class RuleWhat {

	// RuleWhat ::= ( [ "DISTINCT" ] name ) from_clause [where_clause]

	static Logger logger = Logger.getLogger(RuleWhat.class);

	private String idVar;

	private String string;

	private FromClause fromClause;

	private WhereClause whereClause;

	public RuleWhat(Input input) throws ParserException, IcatException {

		input.consume(Token.Type.SELECT);
		StringBuilder sb = new StringBuilder("SELECT ");
		Token t = input.consume(Token.Type.NAME, Token.Type.DISTINCT);
		String resultValue;
		if (t.getType() == Token.Type.DISTINCT) {
			sb.append("DISTINCT ");
			resultValue = input.consume(Token.Type.NAME).getValue();
		} else {
			resultValue = t.getValue();
		}
		sb.append(resultValue);
		idVar = resultValue.split("\\.")[0];
		string = sb.toString();
		logger.debug(string + " gives " + idVar);
		fromClause = new FromClause(input, idVar);
		t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input);
			t = input.peek(0);
		}
		if (t != null) {
			throw new ParserException("Attempt to read beyond end");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(string);
		sb.append(" " + fromClause.toString());
		if (whereClause != null) {
			sb.append(" " + whereClause.toString());
		}
		return sb.toString();
	}

	public Class<? extends EntityBaseBean> getBean() {
		return fromClause.getBean();
	}

}
