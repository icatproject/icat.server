package uk.icat3.parser;

import java.util.HashSet;
import java.util.Set;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.manager.EntityInfoHandler;

public class Include {

	// Include ::= ( "INCLUDE" Name ("," Name )* ) ?

	private Set<Class<? extends EntityBaseBean>> includes = new HashSet<Class<? extends EntityBaseBean>>();

	public Include(Input input) throws ParserException, BadParameterException {

		Token t = null;

		if ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.INCLUDE) {
				input.consume();
				this.includes.add(EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue()));
				while ((t = input.peek(0)) != null && t.getType() == Token.Type.COMMA) {
					input.consume();
					this.includes.add(EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue()));
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!includes.isEmpty()) {
			sb.append(" INCLUDE ");
			boolean first = true;
			for (Class<? extends EntityBaseBean> bean : includes) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(bean.getSimpleName());
			}
		}
		return sb.toString();
	}

	public Set<Class<? extends EntityBaseBean>> getBeans() {
		return includes;
	}
}
