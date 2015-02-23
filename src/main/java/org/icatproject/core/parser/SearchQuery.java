package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
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

	private String idVar;

	private String string;

	private FromClause fromClause;

	private WhereClause whereClause;

	private OtherJpqlClauses otherJpqlClauses;

	private IncludeClause includeClause;

	private LimitClause limitClause;

	private int varCount;

	private GateKeeper gateKeeper;

	private static ArrayList<Object> aListWithZero = new ArrayList<>(1);
	static {
		aListWithZero.add(0L);
	}

	private List<Object> noAuthzResult = Collections.emptyList();

	private String relativePathToReturn;

	private Type aggregateFunctionToReturn;

	public SearchQuery(Input input, GateKeeper gateKeeper, String userId) throws ParserException, IcatException {
		/*
		 * The thing being selected (between the SELECT and the FROM) may
		 * include a path such as "SELECT inv.facility FROM Investigation inv".
		 * This is referred to below as a path target.
		 * 
		 * The authz rules can lead to multiple values being returned. If the
		 * query has a non-path target or the COUNT of a non-path target then we
		 * always add the DISTINCT keyword (if not present) to avoid duplicates.
		 * 
		 * If the DISTINCT keyword is not present and either a path target is
		 * requested or AVG, SUM or COUNT of a path target is requested we get
		 * the entities at the top of the path with DISTINCT in the query and
		 * manually extract the requested information.
		 * 
		 * All other cases are treated directly - including MAX and MIN
		 * aggregate functions.
		 */
		this.gateKeeper = gateKeeper;
		//TODO use rootUser as a short cut to evaluate query directly
		boolean rootUser = this.gateKeeper.getRootUserNames().contains(userId);
		input.consume(Token.Type.SELECT);
		StringBuilder sb = new StringBuilder("SELECT ");

		Token t = input.consume(Token.Type.NAME, Token.Type.DISTINCT, Token.Type.COUNT, Token.Type.MAX, Token.Type.MIN,
				Token.Type.AVG, Token.Type.SUM);
		String resultValue;

		if (t.getType() == Token.Type.COUNT || t.getType() == Token.Type.MAX || t.getType() == Token.Type.MIN
				|| t.getType() == Token.Type.AVG || t.getType() == Token.Type.SUM) {
			Token aggregateFunction = t;
			if (aggregateFunction.getType() == Token.Type.COUNT) {
				noAuthzResult = aListWithZero;
			}
			input.consume(Token.Type.OPENPAREN);
			t = input.peek(0);
			boolean distinct = t.getType() == Token.Type.DISTINCT;
			if (distinct) {
				input.consume(Token.Type.DISTINCT);
			}
			resultValue = input.consume(Token.Type.NAME).getValue();
			int dot = resultValue.indexOf('.');
			input.consume(Token.Type.CLOSEPAREN);

			if (dot > 0) {
				if (distinct || aggregateFunction.getType() == Token.Type.MAX
						|| aggregateFunction.getType() == Token.Type.MIN) {
					sb.append(aggregateFunction.getValue() + "(DISTINCT $0$");
					sb.append(resultValue.substring(dot));
					sb.append(")");
				} else { // COUNT, AVG or SUM
					sb.append("DISTINCT $0$");
					relativePathToReturn = resultValue.substring(dot + 1);
					aggregateFunctionToReturn = aggregateFunction.getType();
				}
			} else if (aggregateFunction.getType() == Token.Type.COUNT) { 
				sb.append("COUNT (DISTINCT $0$)");
			} else {
				throw new ParserException("Found aggregate function " + aggregateFunction.getValue()
						+ " where only COUNT works without attributes");
			}
		} else {
			sb.append("DISTINCT ");
			boolean distinct = t.getType() == Token.Type.DISTINCT;
			if (distinct) {
				resultValue = input.consume(Token.Type.NAME).getValue();
			} else {
				resultValue = t.getValue();
			}
			int dot = resultValue.indexOf('.');
			sb.append("$0$");
			if (dot > 0) {
				if (distinct) {
					sb.append(resultValue.substring(dot));
				} else {
					relativePathToReturn = resultValue.substring(dot + 1);
				}
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

		varCount = idVarMap.size() - 1; // variables up to end of where clause
										// without the $0
		if (t != null
				&& (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING || t.getType() == Token.Type.ORDER)) {
			otherJpqlClauses = new OtherJpqlClauses(input, idVarMap);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(getBean(), input, idVar, gateKeeper);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.LIMIT) {
			limitClause = new LimitClause(input);
			t = input.peek(0);

		}
		if (includeClause == null && t != null && t.getType() == Token.Type.INCLUDE) {
			includeClause = new IncludeClause(getBean(), input, idVar, gateKeeper);
			t = input.peek(0);
		}
		if (t != null) {
			throw new ParserException(input, new Type[0]);
		}
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
		logger.debug("Processing: " + this);
		logger.debug("=> fromClause: " + fromClause);
		logger.debug("=> whereClause: " + whereClause);
		StringBuilder sb = new StringBuilder(string);
		sb.append(" FROM" + fromClause.toString());
		String beanName = fromClause.getBean().getSimpleName();

		boolean restricted;
		List<Rule> rules = null;
		if (gateKeeper.getRootUserNames().contains(userId)) {
			logger.info("\"Root\" user " + userId + " is allowed READ to " + beanName);
			restricted = false;
		} else if (gateKeeper.getPublicTables().contains(beanName)) {
			logger.info("All are allowed READ to " + beanName);
			restricted = false;
		} else {
			TypedQuery<Rule> query = manager.createNamedQuery(Rule.SEARCH_QUERY, Rule.class)
					.setParameter("member", userId).setParameter("bean", beanName);
			rules = query.getResultList();
			SearchQuery.logger.debug("Got " + rules.size() + " authz queries for search by " + userId + " to a "
					+ beanName);
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
				if (jpql == null) {
					jpql = ""; // For Oracle
				}
				String jwhere = r.getWhereJPQL();
				logger.info("Include authz rule " + r.getWhat() + " FROM: " + jpql + " WHERE: " + jwhere);

				for (int i = r.getVarCount() - 1; i > 0; i--) {
					jpql = jpql.replace("$" + i + "$", "$" + (i + varCount) + "$");
					jwhere = jwhere.replace("$" + i + "$", "$" + (i + varCount) + "$");
				}

				sb.append(" " + jpql);
				if (!jwhere.isEmpty()) {
					if (ruleWhere.length() > 0) {
						ruleWhere.append(" OR ");
					}
					ruleWhere.append("(" + jwhere + ")");
				}
				varCount += r.getVarCount() - 1;
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
			sb.append("(" + ruleWhere + ")");
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

	public IncludeClause getIncludeClause() {
		return includeClause;
	}

	public List<?> noAuthzResult() {
		logger.debug("noAuthzResult is of length " + noAuthzResult.size());
		return noAuthzResult;
	}

	public String getRelativePathToReturn() throws IcatException {
		return relativePathToReturn;
	}

	public Type getAggregateFunctionToReturn() {
		return aggregateFunctionToReturn;
	}

}
