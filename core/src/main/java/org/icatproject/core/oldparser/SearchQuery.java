package org.icatproject.core.oldparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.oldparser.DagHandler.Step;

public class SearchQuery {

	// Query ::= ( [ [num] "," [num] ] [ "DISTINCT" ] name Include Order ) |
	// ( "MIN" | "MAX" | "AVG" | "COUNT" | "SUM" "(" name ")" )
	// ( "[" SearchCondition "]" )? ( "<->" name ( "[" SearchCondition "]") ?
	// )*

	public class TableAndSearchCondition {

		private String entityName;
		private SearchCondition searchCondition;

		public TableAndSearchCondition(String entityName, SearchCondition searchCondition) {
			this.entityName = entityName;
			this.searchCondition = searchCondition;
		}

	}

	static Logger logger = Logger.getLogger(SearchQuery.class);
	private final static Set<String> rootUserNames = PropertyHandler.getInstance()
			.getRootUserNames();

	private boolean distinct;
	private Order order;
	private Token result;
	private SearchCondition searchCondition;
	private List<TableAndSearchCondition> tableAndSearchConditions = new ArrayList<TableAndSearchCondition>();

	private Include include;

	private Integer offset;

	private Integer number;

	private String aggFunction;

	private Class<? extends EntityBaseBean> firstBean;

	public SearchQuery(Input input) throws ParserException, IcatException {

		Token t = input.consume(Token.Type.NAME, Token.Type.DISTINCT, Token.Type.INTEGER,
				Token.Type.COMMA, Token.Type.COUNT, Token.Type.MAX, Token.Type.MIN, Token.Type.AVG,
				Token.Type.SUM);
		if (t.getType() == Token.Type.COUNT || t.getType() == Token.Type.MAX
				|| t.getType() == Token.Type.MIN || t.getType() == Token.Type.AVG
				|| t.getType() == Token.Type.SUM) {
			aggFunction = t.getValue();
			input.consume(Token.Type.OPENPAREN);
			this.result = input.consume(Token.Type.NAME);
			String resultValue = this.result.getValue();
			String[] eles = resultValue.split("\\.");
			this.firstBean = EntityInfoHandler.getClass(eles[0]);
			input.consume(Token.Type.CLOSEPAREN);
		} else {
			if (t.getType() == Token.Type.INTEGER) {
				this.offset = Integer.parseInt(t.getValue());
				input.consume(Token.Type.COMMA);
				t = input.consume(Token.Type.NAME, Token.Type.DISTINCT, Token.Type.INTEGER);
				if (t.getType() == Token.Type.INTEGER) {
					this.number = Integer.parseInt(t.getValue());
					t = input.consume(Token.Type.NAME, Token.Type.DISTINCT);
				}
			} else if (t.getType() == Token.Type.COMMA) {
				t = input.consume(Token.Type.NAME, Token.Type.DISTINCT, Token.Type.INTEGER);
				if (t.getType() == Token.Type.INTEGER) {
					this.number = Integer.parseInt(t.getValue());
					t = input.consume(Token.Type.NAME, Token.Type.DISTINCT);
				}
			}

			if (t.getType() == Token.Type.DISTINCT) {
				this.distinct = true;
				this.result = input.consume(Token.Type.NAME);
			} else {
				this.result = t;
			}

			String resultValue = this.result.getValue();
			String[] eles = resultValue.split("\\.");
			this.firstBean = EntityInfoHandler.getClass(eles[0]);
			boolean simple = eles.length == 1;
			t = input.peek(0);
			if (t != null) {
				if (t.getType() == Token.Type.ORDER) {
					this.order = new Order(input);
					if (simple) {
						t = input.peek(0);
						if (t != null && t.getType() == Token.Type.INCLUDE) {
							this.include = new Include(getFirstEntity(), input);
						}
					}
				} else {
					if (simple) {
						t = input.peek(0);
						if (t != null && t.getType() == Token.Type.INCLUDE) {
							this.include = new Include(getFirstEntity(), input);
						}
					}
					this.order = new Order(input);
				}
			}
		}

		t = input.peek(0);
		if (t != null) {
			if (t.getType() == Token.Type.BRA) {
				input.consume();
				this.searchCondition = new SearchCondition(input);
				input.consume(Token.Type.KET);
			}
		}
		while ((t = input.peek(0)) != null) {
			input.consume(Token.Type.ENTSEP);
			t = input.consume(Token.Type.NAME);
			String name = t.getValue();
			t = input.peek(0);
			SearchCondition sc = null;
			if (t != null) {
				if (t.getType() == Token.Type.BRA) {
					input.consume();
					sc = new SearchCondition(input);
					input.consume(Token.Type.KET);
				}
			}
			this.tableAndSearchConditions.add(new TableAndSearchCondition(name, sc));
		}
	}

	public Class<? extends EntityBaseBean> getFirstEntity() throws IcatException {
		return firstBean;
	}

	public String getJPQL(String userId, EntityManager manager) throws IcatException {
		Set<Class<? extends EntityBaseBean>> es = this.getRelatedEntities();
		Class<? extends EntityBaseBean> bean = this.getFirstEntity();
		String beanName = bean.getSimpleName();

		StringBuilder restriction = null;
		if (rootUserNames.contains(userId) && GateKeeper.rootSpecials.contains(beanName)) {
			logger.info("\"Root\" user " + userId + " is allowed READ to " + beanName);
		} else {
			TypedQuery<Rule> query = manager.createNamedQuery(Rule.SEARCH_QUERY, Rule.class)
					.setParameter("member", userId).setParameter("bean", beanName);

			List<Rule> rules = query.getResultList();
			SearchQuery.logger.debug("Got " + rules.size() + " authz queries for search by "
					+ userId + " to a " + beanName);
			restriction = new StringBuilder();

			for (Rule r : rules) {
				if (!r.isRestricted()) {
					SearchQuery.logger.info("Null restriction => Operation permitted");
					restriction = null;
					break;
				}
			}

			if (restriction != null) {
				boolean first = true;
				for (Rule r : rules) {
					String rBeans = r.getBeans();
					String jpql = r.getSearchJPQL().replace(":user", "'" + userId + "'");
					SearchQuery.logger.debug("Substituted SearchJPQL: " + jpql);
					SearchQuery.logger.debug("Related beans: " + rBeans);
					// The complication here is because Oracle stores empty strings as null values
					// whereas MySQL gets it right
					if (rBeans != null && !rBeans.isEmpty()) {
						for (String b : rBeans.split(" ")) {
							es.add(EntityInfoHandler.getClass(b));
						}
					}
					if (first) {
						first = false;
					} else {
						restriction.append(" OR ");
					}
					restriction.append("(" + jpql + ")");
				}
			}
		}

		Step step = DagHandler.findSteps(this.getFirstEntity(), es);
		StringBuilder sb = this.getSelect(step);
		String where = this.getWhere().toString();
		boolean whereThere = !where.isEmpty();
		if (whereThere) {
			sb.append(' ').append(where.replace(":user", "'" + userId + "'"));
		}
		if (restriction != null) {
			if (restriction.length() == 0) {
				throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES,
						"Read access to this " + beanName + " is not allowed.");
			}
			if (whereThere) {
				sb.append(" AND(");
			} else {
				sb.append(" WHERE(");
			}
			sb.append(restriction).append(")");
		}
		sb.append(' ').append(this.getOrderBy());
		return sb.toString();
	}

	private StringBuilder getOrderBy() throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (this.order != null) {
			StringBuilder orderBy = this.order.getOrderBy(this.getFirstEntity());
			if (orderBy != null) {
				sb.append("ORDER BY " + orderBy);
			}
		}
		return sb;
	}

	Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws IcatException {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			es.add(EntityInfoHandler.getClass(ts.entityName));
		}
		return es;
	}

	private StringBuilder getSelect(Step step) {
		StringBuilder sb = new StringBuilder("SELECT ");
		if (this.distinct) {
			sb.append("DISTINCT ");
		}
		String resultName = this.result.getValue();
		int pos = resultName.indexOf(".");
		String beanName = null;
		if (pos >= 0) {
			beanName = resultName.substring(0, pos);
			resultName = resultName.substring(0, pos) + "$" + resultName.substring(pos);
		} else {
			beanName = resultName;
			resultName = resultName + "$";
		}

		if (aggFunction == null) {
			sb.append(resultName);
		} else {
			sb.append(aggFunction + "(" + resultName + ")");
		}
		sb.append(" FROM " + beanName + " AS " + beanName + "$ " + step.join());
		return sb;
	}

	private StringBuilder getWhere() throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (this.searchCondition != null) {
			StringBuilder where = this.searchCondition.getWhere(this.getFirstEntity());
			if (where != null) {
				sb.append("WHERE " + where);
			}
		}

		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			String beanName = ts.entityName;
			Class<? extends EntityBaseBean> bean = EntityInfoHandler.getClass(beanName);
			if (ts.searchCondition != null) {
				StringBuilder where = ts.searchCondition.getWhere(bean);
				if (where != null) {
					if (sb.length() == 0) {
						sb.append("WHERE ");
					} else {
						sb.append(" AND ");
					}
					sb.append(where);
				}
			}
		}
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.distinct) {
			sb.append("DISTINCT ");
		}

		if (aggFunction == null) {
			sb.append(result);
		} else {
			sb.append(aggFunction + "(" + result + ")");
		}

		if (this.include != null) {
			sb.append(this.include);
		}

		if (this.order != null) {
			sb.append(this.order);
		}

		if (this.searchCondition != null) {
			sb.append('[');
			sb.append(this.searchCondition);
			sb.append(']');
		}
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			sb.append(ts.entityName).append(" <-> ");
			if (ts.searchCondition != null) {
				sb.append('[');
				sb.append(ts.searchCondition);
				sb.append(']');
			}
		}
		return sb.toString();
	}

	public Include getInclude() throws IcatException {
		return this.include;
	}

	public Integer getOffset() {
		return this.offset;
	}

	public Integer getNumber() {
		return this.number;
	}

}
