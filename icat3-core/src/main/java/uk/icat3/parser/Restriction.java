package uk.icat3.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.manager.DagHandler;
import uk.icat3.manager.EntityInfoHandler;
import uk.icat3.manager.DagHandler.Step;

public class Restriction {

	// Restriction ::= ( "[" SearchCondition "]" )? ( ("<->")? name "["
	// SearchCondition "]" )*

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

	public String getQuery(String tableName) throws BadParameterException, IcatInternalException {
		Set<Class<? extends EntityBaseBean>> es = getRelatedEntities();
		Step step = DagHandler.fixes(EntityInfoHandler.getClass(tableName), es);
		StringBuilder sb = getSelect(tableName, step);
		sb.append(' ');
		sb.append(getWhere(tableName));
		return sb.toString();
	}

	public Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws BadParameterException {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		for (TableAndSearchCondition ts : this.tableAndSearchConditions) {
			es.add(EntityInfoHandler.getClass(ts.entityName));
		}
		return es;
	}

	private StringBuilder getWhere(String tableName) throws BadParameterException {
		Class<? extends EntityBaseBean> bean = EntityInfoHandler.getClass(tableName);

		StringBuilder sb = new StringBuilder("WHERE (");
		int c = 0;
		for (Field field : bean.getDeclaredFields()) {
			if (field.getAnnotation(Id.class) != null) {
				sb.append(tableName + "$." + field.getName() + " = :pkid");
				c++;
			}
		}
		for (Field field : bean.getDeclaredFields()) {
			if (field.getAnnotation(EmbeddedId.class) != null) {
				int n = 0;
				for (Field ifield : field.getType().getDeclaredFields()) {
					if ((ifield.getModifiers() & Modifier.STATIC) == 0) {
						if (n != 0) {
							sb.append(" AND ");
						}
						sb.append(tableName + "$." + field.getName() + "." + ifield.getName() + " = :pkid" + n++);
					}
				}
				c++;
			}
		}
		if (c != 1) {
			throw new BadParameterException("Unable to determine key for " + tableName);
		}
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
		return new StringBuilder("SELECT COUNT(" + tableName + "$) FROM " + tableName + " AS " + tableName + "$ "
				+ step.join());
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

	public String getSearchWhere(String beanName) throws BadParameterException {
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
