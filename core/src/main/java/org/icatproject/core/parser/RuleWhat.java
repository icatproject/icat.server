package org.icatproject.core.parser;

import java.util.HashMap;
import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.parser.Token.Type;

public class RuleWhat {

	// RuleWhat ::= ( [ "DISTINCT" ] name ) from_clause [where_clause]

	private String idVar;

	private FromClause fromClause;

	private WhereClause whereClause;

	private int varCount;

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
		idVar = resultValue.split("\\.")[0].toUpperCase();

		Map<String, Integer> idVarMap = new HashMap<>();
		idVarMap.put(idVar, 0);
		boolean isQuery = false;
		fromClause = new FromClause(input, idVar, idVarMap, isQuery);
		t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null) {
			throw new ParserException(input, new Type[0]);
		}
		varCount = idVarMap.size();
	}

	public Class<? extends EntityBaseBean> getBean() {
		return fromClause.getBean();
	}

	public String getWhere() {
		return whereClause == null ? "" : whereClause.toString();
	}

	public String getFrom() {
		return fromClause == null ? "" : fromClause.toString();
	}

	public int getVarCount() {
		return varCount;
	}

}
