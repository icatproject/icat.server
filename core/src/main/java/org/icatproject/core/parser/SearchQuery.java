package org.icatproject.core.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.parser.Token.Type;

public class SearchQuery {

	// SearchQuery ::= ( [ "DISTINCT" ] name ) |
	// ( "MIN" | "MAX" | "AVG" | "COUNT" | "SUM" "(" name ")" )
	// from_clause [where_clause] [other_jpql_clauses]
	// (([include_clause] [limit_clause]) | ([limit_clause] [include_clause]))

	private static Logger logger = Logger.getLogger(SearchQuery.class);

	private final static Set<String> rootUserNames = PropertyHandler.getInstance()
			.getRootUserNames();

	private String idVar;

	private String string;

	private FromClause fromClause;

	private WhereClause whereClause;

	private OtherJpqlClauses otherJpqlClauses;

	private IncludeClause includeClause;

	private LimitClause limitClause;

	private int varCount;

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
			int dot = resultValue.indexOf('.');
			input.consume(Token.Type.CLOSEPAREN);
			sb.append("$0$");
			if (dot > 0) {
				sb.append(resultValue.substring(dot));
			}
			sb.append(")");
		} else {
			if (t.getType() == Token.Type.DISTINCT) {
				sb.append("DISTINCT ");
				resultValue = input.consume(Token.Type.NAME).getValue();
			} else {
				resultValue = t.getValue();
			}
			int dot = resultValue.indexOf('.');
			sb.append("$0$");
			if (dot > 0) {
				sb.append(resultValue.substring(dot));
			}
		}
		idVar = resultValue.split("\\.")[0].toUpperCase();
		string = sb.toString();
		Map<String, Integer> idVarMap = new HashMap<>();
		idVarMap.put(idVar, 0);
		boolean isQuery = true;
		fromClause = new FromClause(input, idVar, idVarMap, isQuery);
		t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null
				&& (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING || t
						.getType() == Token.Type.ORDER)) {
			otherJpqlClauses = new OtherJpqlClauses(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.LIMIT) {
			limitClause = new LimitClause(input);
			t = input.peek(0);

		}
		if (includeClause == null && t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null) {
			throw new ParserException(input, new Type[0]);
		}
		varCount = idVarMap.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(string);
		sb.append(" FROM" + fromClause.toString());
		if (whereClause != null) {
			sb.append(" WHERE" + whereClause.toString());
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
		logger.debug("Processing " + this);
		StringBuilder sb = new StringBuilder(string);
		sb.append(" FROM" + fromClause.toString());
		String beanName = fromClause.getBean().getSimpleName();

		boolean restricted;
		List<Rule> rules = null;
		if (rootUserNames.contains(userId) && GateKeeper.rootSpecials.contains(beanName)) {
			logger.info("\"Root\" user " + userId + " is allowed READ to " + beanName);
			restricted = false;
		} else {
			TypedQuery<Rule> query = manager.createNamedQuery(Rule.SEARCH_QUERY, Rule.class)
					.setParameter("member", userId).setParameter("bean", beanName);
			rules = query.getResultList();
			SearchQuery.logger.debug("Got " + rules.size() + " authz queries for search by "
					+ userId + " to a " + beanName);
			if (rules.size() == 0) {
				return null;
			}
			restricted = true;

			for (Rule r : rules) {
				if (!r.isRestricted()) {
					logger.info("Null restriction => Operation permitted");
					restricted = false;
					break;
				}
			}
		}

		StringBuilder ruleWhere = new StringBuilder();

		if (restricted) {
			/* Can only get here if rules has been set */
			for (Rule r : rules) {
				String jpql = r.getFromJPQL();
				String jwhere = r.getWhereJPQL();
				logger.info("Include rule " + r.getWhat() + " FROM: " + jpql + " WHERE: " + jwhere);
				int n = varCount;

				for (int i = 1; i < r.getVarCount(); i++) {
					jpql = jpql.replace("$" + i + "$", "$" + i + n + "$");
					jwhere = jwhere.replace("$" + i + "$", "$" + i + n + "$");
				}

				sb.append(" " + jpql);
				if (!jwhere.isEmpty()) {
					if (ruleWhere.length() > 0) {
						ruleWhere.append(" OR ");
					}
					ruleWhere.append("(" + jwhere + ")");
				}
			}
		}

		if (whereClause != null || ruleWhere.length() > 0) {
			sb.append(" WHERE ");
		}
		if (whereClause != null) {
			sb.append(whereClause);
		}
		if (whereClause != null && ruleWhere.length() > 0) {
			sb.append(" AND ");
		}
		if (ruleWhere.length() > 0) {
			sb.append(ruleWhere);
		}

		if (otherJpqlClauses != null) {
			sb.append(" " + otherJpqlClauses.toString());
		}
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
