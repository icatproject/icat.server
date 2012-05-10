package uk.icat3.parser;

import java.util.HashSet;
import java.util.Set;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.manager.EntityInfoHandler;

public class Include {

	// Include ::= ( "INCLUDE" Name|1 ("," Name|1 )* ) ?
	
	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private Set<Class<? extends EntityBaseBean>> includes = new HashSet<Class<? extends EntityBaseBean>>();

	private boolean one;

	public Include(Input input) throws ParserException, BadParameterException {

		Token t = null;

		if ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.INCLUDE) {
				input.consume();
				processNameOr1(input);
				while ((t = input.peek(0)) != null && t.getType() == Token.Type.COMMA) {
					input.consume();
					processNameOr1(input);
				}
			}
		}
	}

	private void processNameOr1(Input input) throws ParserException, BadParameterException {
		Token name = input.consume(Token.Type.NAME, Token.Type.INTEGER);
		String value = name.getValue();
		if (name.getType() == Token.Type.NAME) {
			this.includes.add(EntityInfoHandler.getClass(value));
		} else if (value.equals("1")) {
			one = true;
		} else {
			throw new BadParameterException("Only integer value allowed in the INCLUDE list is 1");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!includes.isEmpty() || one) {
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
		}
		return sb.toString();
	}

	public Set<Class<? extends EntityBaseBean>> getBeans(Class<? extends EntityBaseBean> bean) throws IcatInternalException {
		if (! one) {
		return includes;
		} else {
			Set<Class<? extends EntityBaseBean>> res = new HashSet<Class<? extends EntityBaseBean>>(includes);
			res.addAll(eiHandler.getOnes(bean));
			for (Class<? extends EntityBaseBean> inc : includes) {
				res.addAll(eiHandler.getOnes(inc));
			}
			res.remove(bean);
			return res;
		}
	}
}
