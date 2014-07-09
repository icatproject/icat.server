package org.icatproject.core.parser;

import java.util.Map;

import org.apache.log4j.Logger;
import org.icatproject.core.Constants;
import org.icatproject.core.manager.EntityInfoHandler;

public class SubSelectClause {

	static Logger logger = Logger.getLogger(SubSelectClause.class);

	private String string;

	public SubSelectClause(Input input, Map<String, Integer> idVarMap) throws ParserException {
		StringBuilder sb = new StringBuilder();
		Token t = input.peek(0);
		int level = 1; // Once we get back to the same level of brackets leave
		while (t != null && t.getType() != Token.Type.ORDER && t.getType() != Token.Type.INCLUDE
				&& t.getType() != Token.Type.LIMIT) {
			if (t.getType() == Token.Type.OPENPAREN) {
				level++;
			} else if (t.getType() == Token.Type.CLOSEPAREN) {
				level--;
				if (level == 0) {
					break;
				}
			}
			String val = t.getValue();
			if (t.getType() == Token.Type.NAME) {
				if (val.startsWith(Constants.ENUMPREFIX)) {
					int n = Constants.ENUMPREFIX.split("\\.").length;
					String vals[] = val.split("\\.");
					if (vals.length != n + 2) {
						throw new ParserException("Enum literal " + val + " must contain exactly "
								+ (n + 2) + " parts");
					}
					sb.append(" " + Constants.ENTITY_PREFIX
							+ t.getValue().substring(Constants.ENUMPREFIX.length()));
				} else if (EntityInfoHandler.getAlphabeticEntityNames().contains(val)) {
					sb.append(" " + val);
				} else {
					int dot = val.indexOf('.');
					String idv = dot >= 0 ? val.substring(0, dot).toUpperCase() : val.toUpperCase();
					Integer intVal = idVarMap.get(idv);
					if (intVal == null) {
						intVal = idVarMap.size();
						idVarMap.put(idv, intVal);
					}
					sb.append(" $" + intVal + "$");
					if (dot >= 0) {
						sb.append(val.substring(dot));
					}
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
			input.consume();
			t = input.peek(0);
		}
		string = sb.toString();
		logger.debug(string);
	}

	@Override
	public String toString() {
		return string;
	}

}
