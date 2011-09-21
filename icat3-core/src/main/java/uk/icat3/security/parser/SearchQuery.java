package uk.icat3.security.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Rule;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.security.DagHandler;
import uk.icat3.security.DagHandler.Step;
import uk.icat3.security.EntityInfoHandler;

public class SearchQuery {

	// Query ::= [ "DISTINCT" ] name Include Order ( "[" SearchCondition "]" )? ( ("<->")? name ( "["
	// SearchCondition "]") ? )*

	public class TableAndSearchCondition {

		private String entityName;
		private SearchCondition searchCondition;

		public TableAndSearchCondition(String entityName, SearchCondition searchCondition) {
			this.entityName = entityName;
			this.searchCondition = searchCondition;
		}

	}

	static Logger logger = Logger.getLogger(SearchQuery.class);

	private boolean distinct;
	private Order order;
	private Token result;
	private SearchCondition searchCondition;
	private List<TableAndSearchCondition> tableAndSearchConditions = new ArrayList<TableAndSearchCondition>();

	private Include include;

	public SearchQuery(Input input) throws ParserException, BadParameterException {
		Token t = input.consume(Token.Type.NAME, Token.Type.DISTINCT);
		if (t.getType() == Token.Type.DISTINCT) {
			this.distinct = true;
			this.result = input.consume(Token.Type.NAME);
		} else {
			this.result = t;
		}

		this.order = new Order(input);
		this.include = new Include(input);

		t = input.peek(0);
		if (t != null) {
			if (t.getType() == Token.Type.BRA) {
				input.consume();
				this.searchCondition = new SearchCondition(input);
				input.consume(Token.Type.KET);
			}
		}
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.ENTSEP) {
				input.consume();
			}
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

	private Class<? extends EntityBaseBean> getFirstEntity() throws BadParameterException {
		return EntityInfoHandler.getClass(this.result.getValue().split("\\.")[0]);
	}

	public String getJPQL(String userId, EntityManager manager) throws BadParameterException, IcatInternalException,
			InsufficientPrivilegesException {
		Set<Class<? extends EntityBaseBean>> es = this.getRelatedEntities();

		Class<? extends EntityBaseBean> bean = this.getFirstEntity();

		String beanName = bean.getSimpleName();
		TypedQuery<Rule> query = manager.createNamedQuery(Rule.SEARCH_QUERY, Rule.class).setParameter("member", userId)
				.setParameter("what", beanName);

		List<Rule> rules = query.getResultList();
		SearchQuery.logger.debug("Got " + rules.size() + " authz queries for search by " + userId + " to a " + beanName);
		StringBuilder restriction = new StringBuilder();
		boolean first = true;
		for (Rule r : rules) {
			String rBeans = r.getBeans();
			SearchQuery.logger.debug("Restriction: " + r.getRestriction());
			SearchQuery.logger.debug("JPQL: " + r.getSearchJPQL());
			SearchQuery.logger.debug("Related beans: " + rBeans);
			if (r.getRestriction() == null) {
				SearchQuery.logger.info("Null restriction => Operation permitted");
				restriction = null;
				break;
			}
			if (!rBeans.isEmpty()) {
				for (String b : rBeans.split(" ")) {
					es.add(EntityInfoHandler.getClass(b));
				}
			}
			if (first) {
				first = false;
			} else {
				restriction.append(" OR ");
			}
			restriction.append("(" + r.getSearchJPQL().replace(":user", "'" + userId + "'") + ")");
		}

		Step step = DagHandler.fixes(this.getFirstEntity(), es);
		StringBuilder sb = this.getSelect(step);
		sb.append(' ').append(this.getWhere());
		if (restriction != null) {
			if (restriction.length() == 0) {
				throw new InsufficientPrivilegesException("Read access to this " + beanName + " is not allowed.");
			}
			sb.append(" AND(").append(restriction).append(")");
		}
		sb.append(' ').append(this.getOrderBy());
		return sb.toString();
	}

	private StringBuilder getOrderBy() throws BadParameterException {
		StringBuilder sb = new StringBuilder();
		StringBuilder orderBy = this.order.getOrderBy(this.getFirstEntity());
		if (orderBy != null) {
			sb.append("ORDER BY " + orderBy);
		}
		return sb;
	}

	private Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws BadParameterException {
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
		sb.append(resultName + " FROM " + beanName + " AS " + beanName + "$ " + step.join());

		return sb;
	}

	private StringBuilder getWhere() throws BadParameterException {
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
		sb.append(this.result);
		
		sb.append(this.include);

		sb.append(this.order);

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

	public Set<Class<? extends EntityBaseBean>> getIncludes() throws BadParameterException {
		return this.include.getBeans();
	}

}
