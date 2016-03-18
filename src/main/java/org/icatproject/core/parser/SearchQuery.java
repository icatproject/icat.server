package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.parser.Token.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchQuery {

	// SearchQuery ::= select_clause from_clause [where_clause]
	// [other_jpql_clauses]
	// (([include_clause] [limit_clause]) | ([limit_clause] [include_clause]))

	private static Logger logger = LoggerFactory.getLogger(SearchQuery.class);

	private SelectClause selectClause;

	private FromClause fromClause;

	private WhereClause whereClause;

	private OtherJpqlClauses otherJpqlClauses;

	private IncludeClause includeClause;

	private LimitClause limitClause;

	private GateKeeper gateKeeper;

	private Pattern idSearch = Pattern.compile("^\\s?\\$0\\$.id = [0-9]+s?$");

	private List<Object> noAuthzResult = new ArrayList<>(1);

	public SearchQuery(Input input, GateKeeper gateKeeper, String userId) throws ParserException, IcatException {
		this.gateKeeper = gateKeeper;

		selectClause = new SelectClause(input);
		if (selectClause.isCount()) {
			noAuthzResult.add(0L);
		}

		fromClause = new FromClause(input, selectClause.getIdPaths());
		Token t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input);
			t = input.peek(0);
		}

		if (t != null && (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING
				|| t.getType() == Token.Type.ORDER)) {
			otherJpqlClauses = new OtherJpqlClauses(input);
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			if (selectClause.getIdPaths().size() > 1) {
				throw new ParserException("INCLUDE is only valid for one quantity in the SELECT clause");
			} else {
				String idv = selectClause.getIdPaths().iterator().next();
				Class<? extends EntityBaseBean> bean = fromClause.getAuthzMap().get(idv + ".id");
				includeClause = new IncludeClause(bean, input, idv.toUpperCase(), gateKeeper);
				t = input.peek(0);
			}

		}
		if (t != null && t.getType() == Token.Type.LIMIT) {
			limitClause = new LimitClause(input);
			t = input.peek(0);
		}

		if (includeClause == null && t != null && t.getType() == Token.Type.INCLUDE) {
			if (selectClause.getIdPaths().size() > 1) {
				throw new ParserException("INCLUDE is only valid for one quantity in the SELECT clause");
			} else {
				String idv = selectClause.getIdPaths().iterator().next();
				Class<? extends EntityBaseBean> bean = fromClause.getAuthzMap().get(idv + ".id");
				includeClause = new IncludeClause(bean, input, idv.toUpperCase(), gateKeeper);
				t = input.peek(0);
			}
		}
		if (t != null) {
			throw new ParserException(input, new Type[0]);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SELECT " + selectClause);
		sb.append(" FROM" + fromClause);
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

		// Trap case of selecting on id values where the LIMIT is ignored in the
		// eclipselink generated SQL
		if (limitClause != null && whereClause != null) {
			if (limitClause.getOffset() > 0 && idSearch.matcher(whereClause.toString()).matches()) {
				logger.debug("LIMIT offset is non-zero but can only return at most one entry");
				return null;
			}
		}

		StringBuilder sb = new StringBuilder("SELECT " + selectClause);
		sb.append(" FROM" + fromClause.toString());
		List<StringBuilder> whereBits = new ArrayList<>();
		if (whereClause != null) {
			whereBits.add(new StringBuilder(whereClause.toString()));
		}

		for (Entry<String, Class<? extends EntityBaseBean>> entry : fromClause.getAuthzMap().entrySet()) {
			String beanName = entry.getValue().getSimpleName();
			String path = entry.getKey();

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
				SearchQuery.logger
						.debug("Got " + rules.size() + " authz queries for search by " + userId + " to a " + beanName);
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

			if (restricted) {
				/* Can only get here if rules has been set for this bean */
				StringBuilder ruleWhere = new StringBuilder();
				for (Rule r : rules) {
					String jpql = r.getSearchJPQL();

					logger.info("Include authz rule {} for {}", jpql, beanName);

					if (ruleWhere.length() > 0) {
						ruleWhere.append(" OR ");
					}
					ruleWhere.append(path + " IN  (" + jpql + ")");
				}
				whereBits.add(ruleWhere);

			}
		}

		boolean first = true;
		for (StringBuilder ruleWhere : whereBits) {
			if (first) {
				sb.append(" WHERE ( ");
				first = false;
			} else {
				sb.append(" AND ( ");
			}
			sb.append(ruleWhere + " )");
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

	public IncludeClause getIncludeClause() {
		return includeClause;
	}

	public String typeQuery() {
		String s = selectClause.toString().substring(11, selectClause.toString().length() - 1).replace("DISTINCT ", "");
		return "SELECT " + s + " FROM" + fromClause + " WHERE " + s + " IS NOT NULL";
	}

	public List<Object> getNoAuthzResult() {
		return noAuthzResult;
	}

}
