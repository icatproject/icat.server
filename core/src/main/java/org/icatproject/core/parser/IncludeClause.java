package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class IncludeClause {

	static Logger logger = Logger.getLogger(IncludeClause.class);

	private class Step {

		private String path;
		private String var;

		public Step(String path, String var) {
			this.path = path;
			this.var = var;
		}

	}

	private boolean one;
	private List<Step> steps = new ArrayList<Step>();

	public IncludeClause(Input input, Map<String, Integer> idVarMap) throws ParserException {
		input.consume(Token.Type.INCLUDE);
		Token t = input.peek(0);
		if (t.getValue().equals("1")) {
			input.consume(Token.Type.INTEGER);
			one = true;
		} else {
			processStep(input, idVarMap);
		}

		t = input.peek(0);
		while (t != null && t.getType() == Token.Type.COMMA) {
			t = input.consume(Token.Type.COMMA);
			processStep(input, idVarMap);
			t = input.peek(0);
		}
		logger.debug(this);
	}

	private void processStep(Input input, Map<String, Integer> idVarMap) throws ParserException {
		Token t = input.consume(Token.Type.NAME);
		String path = t.getValue();
		String var = null;
		t = input.peek(0);
		if (t != null && t.getType() == Token.Type.AS) {
			t = input.consume(Token.Type.AS);
			var = input.consume(Token.Type.NAME).getValue();
		} else {
			if (t != null && t.getType() == Token.Type.NAME) {
				var = input.consume(Token.Type.NAME).getValue();
			}
		}

		/* Check the path */
		int dot = path.indexOf('.');
		if (dot <= 0) {
			throw new ParserException("Path " + path + " must contain a '.'");
		}
		String idv = path.substring(0, dot).toUpperCase();
		Integer intVal = idVarMap.get(idv);
		if (intVal == null) {
			throw new ParserException("variable " + idv
					+ " mentioned in INCLUDE clause is not defined");
		}
		path = " $" + intVal + "$" + path.substring(dot);

		/* Check the var */
		if (var != null) {
			if (var.indexOf('.') >= 0) {
				throw new ParserException("Variable " + var + " must not contain a '.'");
			}
			idv = var.toUpperCase();
			intVal = idVarMap.get(idv);
			if (intVal != null) {
				throw new ParserException("variable " + idv
						+ " mentioned in INCLUDE clause is already defined");
			}
			intVal = idVarMap.size();
			idVarMap.put(idv, intVal);
			var = " $" + intVal + "$";
		}

		steps.add(new Step(path, var));

	}

	@Override
	public String toString() {
		if (one) {
			return ("INCLUDE 1");
		} else {
			StringBuilder sb = new StringBuilder();
			for (Step step : steps) {
				if (sb.length() == 0) {
					sb.append("INCLUDE ");
				} else {
					sb.append(", ");
				}
				sb.append(step.path);
				if (step.var != null) {
					sb.append(" " + step.var);
				}
			}
			return sb.toString();
		}

	}

}
