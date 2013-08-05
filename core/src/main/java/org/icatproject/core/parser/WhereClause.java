package org.icatproject.core.parser;

import java.util.Map;

import org.icatproject.core.Constants;
import org.icatproject.core.manager.EntityInfoHandler;

public class WhereClause {
	// static Logger logger = Logger.getLogger(WhereClause.class);

	private String string;

	private SubSelectClause subSelectClause;

	public WhereClause(Input input, Map<String, Integer> idVarMap) throws ParserException {
		StringBuilder sb = new StringBuilder();
		input.consume(Token.Type.WHERE);
		Token t = input.peek(0);
		while (t != null && t.getType() != Token.Type.GROUP && t.getType() != Token.Type.HAVING
				&& t.getType() != Token.Type.ORDER && t.getType() != Token.Type.INCLUDE
				&& t.getType() != Token.Type.LIMIT) {
			t = input.consume();
			String val = t.getValue();
			if (t.getType() == Token.Type.OPENPAREN && input.peek(0).getType() == Token.Type.SELECT) {
				subSelectClause = new SubSelectClause(input);
				t = input.consume(Token.Type.CLOSEPAREN);
				sb.append("(" + subSelectClause + ")");
			} else if (t.getType() == Token.Type.NAME) {
				if (val.startsWith(Constants.ENUMPREFIX)) {
					int n = Constants.ENUMPREFIX.split("\\.").length;
					String vals[] = val.split("\\.");
					if (vals.length != n + 2) {
						throw new ParserException("Enum literal " + val + " must contain exactly "
								+ (n + 2) + " parts");
					}
					sb.append(" " + Constants.ENTITY_PREFIX
							+ t.getValue().substring(Constants.ENUMPREFIX.length()));
				} else if (EntityInfoHandler.getEntityNames().contains(val)) {
					sb.append(" " + val);
				} else {
					int dot = val.indexOf('.');
					if (dot < 0) {
						throw new ParserException("path " + val
								+ " mentioned in FROM clause contains no dots");
					}
					String idv = val.substring(0, dot).toUpperCase();
					Integer intVal = idVarMap.get(idv);
					if (intVal == null) {
						throw new ParserException("variable " + idv
								+ " mentioned in FROM clause is not defined");
					}
					sb.append(" $" + intVal + "$" + val.substring(dot));
				}
			} else {
				if (t.getType() == Token.Type.STRING) {
					val = "'" + val.replace("'", "''") + "'";
				} else if (t.getType() == Token.Type.TIMESTAMP) {
					val = ":"
							+ val.replace(" ", "").replace(":", "").replace("-", "")
									.replace("{", "").replace("}", "");
				}
				sb.append(" " + val);
			}
			t = input.peek(0);
		}
		string = sb.toString();
	}

	@Override
	public String toString() {
		return string;
	}

}
