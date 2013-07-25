package org.icatproject.core.parser;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;

public class SearchQuery {

	// SearchQuery ::= ( [ "DISTINCT" ] name ) |
	// ( "MIN" | "MAX" | "AVG" | "COUNT" | "SUM" "(" name ")" )
	// from_clause [where_clause] [other_jpql_clauses]
	// (([include_clause] [limit_clause]) | ([limit_clause] [include_clause]))

	static Logger logger = Logger.getLogger(SearchQuery.class);
	// private final static Set<String> rootUserNames = PropertyHandler.getInstance()
	// .getRootUserNames();

	private String idVar;

	private String string;

	private FromClause fromClause;

	private WhereClause whereClause;

	private OtherJpqlClauses otherJpqlClauses;

	private IncludeClause includeClause;

	private LimitClause limitClause;

	public SearchQuery(Input input) throws ParserException, IcatException {

		input.consume(Token.Type.SELECT);
		StringBuilder sb = new StringBuilder("SELECT ");

		Token t = input.consume(Token.Type.NAME, Token.Type.DISTINCT, Token.Type.COUNT,
				Token.Type.MAX, Token.Type.MIN, Token.Type.AVG, Token.Type.SUM);
		Token result;
		String resultValue;
		if (t.getType() == Token.Type.COUNT || t.getType() == Token.Type.MAX
				|| t.getType() == Token.Type.MIN || t.getType() == Token.Type.AVG
				|| t.getType() == Token.Type.SUM) {
			sb.append(t.getValue() + "(");
			input.consume(Token.Type.OPENPAREN);
			t = input.peek(0);
			if (t.getType() == Token.Type.DISTINCT) {
				input.consume(Token.Type.DISTINCT);
				sb.append("DISTINCT ");
			}
			result = input.consume(Token.Type.NAME);
			resultValue = result.getValue();
			input.consume(Token.Type.CLOSEPAREN);
			sb.append(resultValue + ")");
		} else {
			if (t.getType() == Token.Type.DISTINCT) {
				sb.append("DISTINCT ");
				resultValue = input.consume(Token.Type.NAME).getValue();
			} else {
				resultValue = t.getValue();
			}
			sb.append(resultValue);
		}
		idVar = resultValue.split("\\.")[0];
		string = sb.toString();
		logger.debug(string + " gives " + idVar);
		fromClause = new FromClause(input, idVar);
		t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input);
			t = input.peek(0);
		}
		if (t != null
				&& (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING || t
						.getType() == Token.Type.ORDER)) {
			otherJpqlClauses = new OtherJpqlClauses(input);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(input);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.LIMIT) {
			limitClause = new LimitClause(input);
			t = input.peek(0);

		}
		if (includeClause == null && t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(input);
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
		if (otherJpqlClauses != null) {
			sb.append(" " + otherJpqlClauses.toString());
		}
		if (includeClause != null) {
			sb.append(" " + includeClause.toString());
		}
		if (limitClause != null) {
			sb.append(" " + limitClause.toString());
		}
		return sb.toString();
	}

	public String getJPQL(String userId, EntityManager manager) {
		StringBuilder sb = new StringBuilder(string);
		sb.append(" " + fromClause.toString());
		if (whereClause != null) {
			sb.append(" " + whereClause.toString());
		}
		if (otherJpqlClauses != null) {
			sb.append(" " + otherJpqlClauses.toString());
		}
		// TODO add authz
		return sb.toString();
	}

	public Integer getOffset() {
		return limitClause == null ? null : limitClause.getOffset();
	}

	public Integer getNumber() {
		return limitClause == null ? null : limitClause.getNumber();
	}

	public Class<? extends EntityBaseBean> getBean() {
		return fromClause.getBean();
	}

}
