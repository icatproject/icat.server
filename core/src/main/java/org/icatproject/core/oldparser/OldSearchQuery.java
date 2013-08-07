package org.icatproject.core.oldparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.oldparser.DagHandler.Step;

public class OldSearchQuery {

	// OldSearchQuery ::= ( [ [num] "," [num] ] [ "DISTINCT" ] name OldInclude Order ) |
	// ( "MIN" | "MAX" | "AVG" | "COUNT" | "SUM" "(" name ")" )
	// ( "[" SearchCondition "]" )? ( "<->" name ( "[" SearchCondition "]") ?
	// )*

	private class TableAndSearchCondition {

		private String entityName;
		private SearchCondition searchCondition;

		public TableAndSearchCondition(String entityName, SearchCondition searchCondition) {
			this.entityName = entityName;
			this.searchCondition = searchCondition;
		}

	}

	static Logger logger = Logger.getLogger(OldSearchQuery.class);

	private boolean distinct;
	private Order order;
	private OldToken result;
	private SearchCondition searchCondition;
	private List<TableAndSearchCondition> tableAndSearchConditions = new ArrayList<TableAndSearchCondition>();

	private OldInclude include;

	private Integer offset;

	private Integer number;

	private String aggFunction;

	private Class<? extends EntityBaseBean> firstBean;

	public OldSearchQuery(OldInput input) throws OldParserException, IcatException {

		OldToken t = input.consume(OldToken.Type.NAME, OldToken.Type.DISTINCT,
				OldToken.Type.INTEGER, OldToken.Type.COMMA, OldToken.Type.COUNT, OldToken.Type.MAX,
				OldToken.Type.MIN, OldToken.Type.AVG, OldToken.Type.SUM);
		if (t.getType() == OldToken.Type.COUNT || t.getType() == OldToken.Type.MAX
				|| t.getType() == OldToken.Type.MIN || t.getType() == OldToken.Type.AVG
				|| t.getType() == OldToken.Type.SUM) {
			aggFunction = t.getValue();
			input.consume(OldToken.Type.OPENPAREN);
			this.result = input.consume(OldToken.Type.NAME);
			String resultValue = this.result.getValue();
			String[] eles = resultValue.split("\\.");
			this.firstBean = EntityInfoHandler.getClass(eles[0]);
			input.consume(OldToken.Type.CLOSEPAREN);
		} else {
			if (t.getType() == OldToken.Type.INTEGER) {
				this.offset = Integer.parseInt(t.getValue());
				input.consume(OldToken.Type.COMMA);
				t = input
						.consume(OldToken.Type.NAME, OldToken.Type.DISTINCT, OldToken.Type.INTEGER);
				if (t.getType() == OldToken.Type.INTEGER) {
					this.number = Integer.parseInt(t.getValue());
					t = input.consume(OldToken.Type.NAME, OldToken.Type.DISTINCT);
				}
			} else if (t.getType() == OldToken.Type.COMMA) {
				t = input
						.consume(OldToken.Type.NAME, OldToken.Type.DISTINCT, OldToken.Type.INTEGER);
				if (t.getType() == OldToken.Type.INTEGER) {
					this.number = Integer.parseInt(t.getValue());
					t = input.consume(OldToken.Type.NAME, OldToken.Type.DISTINCT);
				}
			}

			if (t.getType() == OldToken.Type.DISTINCT) {
				this.distinct = true;
				this.result = input.consume(OldToken.Type.NAME);
			} else {
				this.result = t;
			}

			String resultValue = this.result.getValue();
			String[] eles = resultValue.split("\\.");
			this.firstBean = EntityInfoHandler.getClass(eles[0]);
			boolean simple = eles.length == 1;
			t = input.peek(0);
			if (t != null) {
				if (t.getType() == OldToken.Type.ORDER) {
					this.order = new Order(input);
					if (simple) {
						t = input.peek(0);
						if (t != null && t.getType() == OldToken.Type.INCLUDE) {
							this.include = new OldInclude(firstBean, input);
						}
					}
				} else {
					if (simple) {
						t = input.peek(0);
						if (t != null && t.getType() == OldToken.Type.INCLUDE) {
							this.include = new OldInclude(firstBean, input);
						}
					}
					this.order = new Order(input);
				}
			}
		}

		t = input.peek(0);
		if (t != null) {
			if (t.getType() == OldToken.Type.BRA) {
				input.consume();
				this.searchCondition = new SearchCondition(input);
				input.consume(OldToken.Type.KET);
			}
		}
		while ((t = input.peek(0)) != null) {
			input.consume(OldToken.Type.ENTSEP);
			t = input.consume(OldToken.Type.NAME);
			String name = t.getValue();
			t = input.peek(0);
			SearchCondition sc = null;
			if (t != null) {
				if (t.getType() == OldToken.Type.BRA) {
					input.consume();
					sc = new SearchCondition(input);
					input.consume(OldToken.Type.KET);
				}
			}
			this.tableAndSearchConditions.add(new TableAndSearchCondition(name, sc));
		}
	}

	public String getNewQuery() throws IcatException {
		Set<Class<? extends EntityBaseBean>> es = this.getRelatedEntities();
		Step step = DagHandler.findSteps(this.firstBean, es);
		StringBuilder sb = this.getSelect(step);
		if (sb.charAt(sb.length() - 1) != ' ') {
			sb.append(' ');
		}
		sb.append(this.getWhere().toString());
		sb.append(' ').append(this.getOrderBy());
		if (include != null) {
			if (sb.charAt(sb.length() - 1) != ' ') {
				sb.append(' ');
			}
			sb.append(include.getNewInclude(firstBean));
		}

		if (offset != null || number != null) {
			sb.append(" LIMIT");
			sb.append(offset == null ? " 0" : " " + offset);
			sb.append(",");
			sb.append(number == null ? "*" : number);
		}
		return sb.toString().trim();
	}

	private StringBuilder getOrderBy() throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (this.order != null) {
			StringBuilder orderBy = this.order.getOrderBy(this.firstBean);
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
			StringBuilder where = this.searchCondition.getWhere(firstBean);
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

}
