package uk.icat3.security.parser;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;

public class BooleanFactor {

	private boolean not;
	private ComparisonPredicate predicate;
	private SearchCondition searchCondition;

	// BooleanFactor ::= ("NOT")? Predicate | ( "(" SearchCondition ")" )

	public BooleanFactor(Input input) throws ParserException {
		Token t = input.peek(0);
		if (t.getType() == Token.Type.NOT) {
			input.consume();
			this.not = true;
		}

		t = input.peek(0);
		if (t.getType() == Token.Type.OPENPAREN) {
			input.consume();
			this.searchCondition = new SearchCondition(input);
			input.consume(Token.Type.CLOSEPAREN);
		} else {
			this.predicate = new ComparisonPredicate(input);
		}
	}

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws BadParameterException {
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
