package uk.icat3.parser;

import java.util.ArrayList;
import java.util.List;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.parser.Token.Type;

public class BooleanTerm {

	// BooleanTerm ::= Factor ( "AND" Factor ) *

	private List<BooleanFactor> factors = new ArrayList<BooleanFactor>();

	public BooleanTerm(Input input) throws ParserException {
		this.factors.add(new BooleanFactor(input));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.AND) {
				input.consume();
				this.factors.add(new BooleanFactor(input));
			} else {
				return;
			}
		}
	}

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws BadParameterException {
		StringBuilder sb = new StringBuilder();
		sb.append(this.factors.get(0).getWhere(tb));
		for (int i = 1; i < this.factors.size(); i++) {
			sb.append(" AND ");
			sb.append(this.factors.get(i).getWhere(tb));
		}
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.factors.get(0));
		for (int i = 1; i < this.factors.size(); i++) {
			sb.append(" AND ");
			sb.append(this.factors.get(i));
		}
		return sb.toString();
	}

}
