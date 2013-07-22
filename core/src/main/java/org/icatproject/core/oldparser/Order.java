package org.icatproject.core.oldparser;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.core.entity.EntityBaseBean;


public class Order {

	private List<SortSpecification> sortSpecifications = new ArrayList<SortSpecification>();

	// Order ::= ( "ORDER" "BY" SortSpecification ("," SortSpecification )* ) ?

	public Order(Input input) throws ParserException {
		Token t = null;

		if ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.ORDER) {
				input.consume();
				input.consume(Token.Type.BY);
				this.sortSpecifications.add(new SortSpecification(input));
				while ((t = input.peek(0)) != null && t.getType() == Token.Type.COMMA) {
					input.consume();
					this.sortSpecifications.add(new SortSpecification(input));
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!sortSpecifications.isEmpty()) {
			sb.append(" ORDER BY ");
			boolean first = true;
			for (SortSpecification sortSpecification : sortSpecifications) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(sortSpecification);
			}
		}
		return sb.toString();
	}

	public StringBuilder getOrderBy(Class<? extends EntityBaseBean> bean) {
		if (!sortSpecifications.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (SortSpecification ss : sortSpecifications) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(bean.getSimpleName() + "$." + ss.getValue());
			}

			return sb;
		} else {
			return null;
		}
	}

}
