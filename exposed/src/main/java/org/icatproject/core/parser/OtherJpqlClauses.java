package org.icatproject.core.parser;

import java.util.Map;

import org.apache.log4j.Logger;

public class OtherJpqlClauses {

	static Logger logger = Logger.getLogger(OtherJpqlClauses.class);

	private String string;

	public OtherJpqlClauses(Input input, Map<String, Integer> idVarMap) throws ParserException {
		StringBuilder sb = new StringBuilder();
		Token t = input.peek(0);
		while (t != null && t.getType() != Token.Type.INCLUDE && t.getType() != Token.Type.LIMIT) {
			if (t.getType() == Token.Type.GROUP || t.getType() == Token.Type.HAVING) {
				throw new ParserException(
						"GROUP BY and HAVING keywords are not compatible with returned result");
			}
			input.consume();
			String val = t.getValue();
			if (t.getType() == Token.Type.NAME) {
				int dot = val.indexOf('.');
				if (dot < 0) {
					throw new ParserException("path " + val
							+ " mentioned in ORDER BY clause contains no dots");
				}
				String idv = val.substring(0, dot).toUpperCase();
				Integer intVal = idVarMap.get(idv);
				if (intVal == null) {
					throw new ParserException("variable " + idv
							+ " mentioned in ORDER BY clause is not defined");
				}
				sb.append(" $" + intVal + "$" + val.substring(dot));
			} else {
				sb.append(" " + val);
			}
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
