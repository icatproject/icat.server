package org.icatproject.core.parser;

import java.util.HashSet;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class Include {

	// Include ::= "INCLUDE" "1" | (Name ("," Name )*)

	private Set<Class<? extends EntityBaseBean>> includes = new HashSet<Class<? extends EntityBaseBean>>();

	private boolean one;

	public Include(Class<? extends EntityBaseBean> bean, Input input) throws ParserException,
			IcatException {

		input.consume(Token.Type.INCLUDE);
		Token name = input.consume(Token.Type.NAME, Token.Type.INTEGER);
		String value = name.getValue();
		if (name.getType() == Token.Type.NAME) {
			this.includes.add(EntityInfoHandler.getClass(value));
		} else if (value.equals("1")) {
			one = true;
		} else {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Only integer value allowed in the INCLUDE list is 1");
		}
		Token t;
		while ((t = input.peek(0)) != null && t.getType() == Token.Type.COMMA) {
			input.consume();
			name = input.consume(Token.Type.NAME);
			value = name.getValue();
			this.includes.add(EntityInfoHandler.getClass(value));
		}

		DagHandler.checkIncludes(bean, getBeans());
	}

	@Override
	public String toString() {

		if (one) {
			return " INCLUDE 1";
		}

		if (includes.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(" INCLUDE ");
		boolean first = true;
		if (one) {
			sb.append("1 ");
			first = false;
		}
		for (Class<? extends EntityBaseBean> bean : includes) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(bean.getSimpleName());
		}
		return sb.toString();
	}

	public Set<Class<? extends EntityBaseBean>> getBeans() {
		return includes;
	}

	public boolean isOne() throws IcatException {
		return one;
	}
}
