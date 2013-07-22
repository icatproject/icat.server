package org.icatproject.core.oldparser;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;


public class SearchCondition {

	private List<BooleanTerm> booleanTerms = new ArrayList<BooleanTerm>();

	// SearchCondition ::= BooleanTerm ( "OR" BooleanTerm ) *

	public SearchCondition(Input input) throws ParserException {
		this.booleanTerms.add(new BooleanTerm(input));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.OR) {
				input.consume();
				this.booleanTerms.add(new BooleanTerm(input));
			} else {
				return;
			}
		}
	}

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws IcatException  {
		StringBuilder sb = new StringBuilder("(");
		sb.append(this.booleanTerms.get(0).getWhere(tb));
		for (int i = 1; i < this.booleanTerms.size(); i++) {
			sb.append("OR ");
			sb.append(this.booleanTerms.get(i).getWhere(tb));
		}
		sb.append(')');
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.booleanTerms.get(0));
		for (int i = 1; i < this.booleanTerms.size(); i++) {
			sb.append("OR ");
			sb.append(this.booleanTerms.get(i));
		}
		return sb.toString();
	}

}
