package org.icatproject.core.oldparser;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;


public class BooleanFactor {

	private boolean not;
	private ComparisonPredicate predicate;
	private SearchCondition searchCondition;

	// BooleanFactor ::= ("NOT")? Predicate | ( "(" SearchCondition ")" )

	public BooleanFactor(OldInput input) throws OldParserException {
		OldToken t = input.peek(0);
		if (t.getType() == OldToken.Type.NOT) {
			input.consume();
			this.not = true;
		}

		t = input.peek(0);
		if (t.getType() == OldToken.Type.OPENPAREN) {
			input.consume();
			this.searchCondition = new SearchCondition(input);
			input.consume(OldToken.Type.CLOSEPAREN);
		} else {
			this.predicate = new ComparisonPredicate(input);
		}
	}

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws IcatException  {
		StringBuilder sb = new StringBuilder();
		if (this.not) {
			sb.append("NOT ");
		}
		if (this.predicate != null) {
			sb.append(this.predicate.getWhere(tb));
		} else {
			sb.append("(");
			sb.append(this.searchCondition.getWhere(tb));
			sb.append(")");
		}
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.not) {
			sb.append("NOT ");
		}
		if (this.predicate != null) {
			sb.append(this.predicate);
		} else {
			sb.append("(");
			sb.append(this.searchCondition);
			sb.append(")");
		}
		return sb.toString();
	}
}
