package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.parser.DagHandler.Step;

public class Restriction {

	// Restriction ::= ( "[" SearchCondition "]" )? ( "<->" name "[" SearchCondition "]" )*

	public class TableAndSearchCondition {

		private SearchCondition searchCondition;
		private String entityName;

		public TableAndSearchCondition(String entityName, SearchCondition searchCondition) {
			this.entityName = entityName;
			this.searchCondition = searchCondition;
		}
	}

	private SearchCondition searchCondition;
	private List<TableAndSearchCondition> tableAndSearchConditions = new ArrayList<TableAndSearchCondition>();

	public Restriction(Input input) throws ParserException {
		Token t = input.peek(0);
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

	public String getQuery(String tableName) throws IcatException {
		Set<Class<? extends EntityBaseBean>> es = getRelatedEntities();
		Step step = DagHandler.findSteps(EntityInfoHandler.getClass(tableName), es);
		StringBuilder sb = getSelect(tableName, step);
		sb.append(' ');
		sb.append(getWhere(tableName));
		return sb.toString();
	}

	public Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws IcatException {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			es.add(EntityInfoHandler.getClass(ts.entityName));
		}
		return es;
	}

	private StringBuilder getWhere(String tableName) throws IcatException {
		Class<? extends EntityBaseBean> bean = EntityInfoHandler.getClass(tableName);

		StringBuilder sb = new StringBuilder("WHERE (");
		sb.append(tableName + "$.id = :pkid");
		sb.append(')');

		if (this.searchCondition != null) {
			sb.append(" AND " + this.searchCondition.getWhere(bean));
		}
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			String beanName = ts.entityName;
			bean = EntityInfoHandler.getClass(beanName);
			if (ts.searchCondition != null) {
				sb.append(" AND " + ts.searchCondition.getWhere(bean));
			}
		}
		return sb;
	}

	private StringBuilder getSelect(String tableName, Step step) {
		return new StringBuilder("SELECT COUNT(" + tableName + "$) FROM " + tableName + " AS "
				+ tableName + "$ " + step.join());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
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

	public String getSearchWhere(String beanName) throws IcatException {
		Class<? extends EntityBaseBean> bean = EntityInfoHandler.getClass(beanName);

		StringBuilder sb = new StringBuilder();

		if (this.searchCondition != null) {
			sb.append(this.searchCondition.getWhere(bean));
		}
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			beanName = ts.entityName;
			bean = EntityInfoHandler.getClass(beanName);
			if (ts.searchCondition != null) {
				if (sb.length() > 0) {
					sb.append(" AND ");
				}
				sb.append(ts.searchCondition.getWhere(bean));
			}
		}
		return sb.toString();
	}

	public boolean isRestricted() {
		return searchCondition != null || !tableAndSearchConditions.isEmpty();
	}

}
