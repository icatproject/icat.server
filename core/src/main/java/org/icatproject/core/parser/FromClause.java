package org.icatproject.core.parser;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class FromClause {

	static Logger logger = Logger.getLogger(FromClause.class);

	private String string;
	private Class<? extends EntityBaseBean> bean;

	public FromClause(Input input, String idVar) throws ParserException, IcatException {
		StringBuilder sb = new StringBuilder();
		input.consume(Token.Type.FROM);
		sb.append("FROM");
		Token t = input.peek(0);
		while (t != null && t.getType() != Token.Type.WHERE && t.getType() != Token.Type.GROUP
				&& t.getType() != Token.Type.HAVING && t.getType() != Token.Type.ORDER
				&& t.getType() != Token.Type.INCLUDE && t.getType() != Token.Type.LIMIT) {
			t = input.consume();
			if (t.getType() != Token.Type.FETCH) {
				sb.append(" " + t.getValue());
			}
			Token next = input.peek(0);
			if (next != null && next.getType() == Token.Type.AS) {
				sb.append(" " + t.getValue());
				input.consume(Token.Type.AS);
				next = input.peek(0);
			}
			if (next != null && next.getType() == Token.Type.NAME && next.getValue().equals(idVar)) {
				if (bean != null) {
					throw new ParserException("Attempt to define " + idVar
							+ " more than once in FROM clause");
				}
				bean = EntityInfoHandler.getClass(t.getValue());
			}
			t = next;
		}
		if (bean == null) {
			throw new ParserException(idVar + " is not defined in the FROM clause");
		}
		string = sb.toString();
		logger.debug(string);
	}

	@Override
	public String toString() {
		return string;
	}

	public Class<? extends EntityBaseBean> getBean() {
		return bean;
	}

}
