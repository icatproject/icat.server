package uk.icat3.security.parser;

import java.util.ArrayList;
import java.util.List;

import uk.icat3.exceptions.BadParameterException;

public class ComparisonPredicate {

	// ComparisonPredicate ::= value ( COMPOP value ) | ( "IN" "(" value ("," value)* ")" ) | (
	// "BETWEEN" value "AND" value )

	private Token value1;
	private Token compop;
	private Token value2;
	private Token value3;
	private List<Token> inValues;

	public ComparisonPredicate(Input input) throws ParserException {
		value1 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
				Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		compop = input.consume(Token.Type.COMPOP, Token.Type.IN, Token.Type.BETWEEN);
		if (compop.getType() == Token.Type.COMPOP) {
			value2 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
					Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		} else if (compop.getType() == Token.Type.IN) {
			input.consume(Token.Type.OPENPAREN);
			inValues = new ArrayList<Token>();
			inValues.add(input.consume(Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL, Token.Type.PARAMETER,
					Token.Type.TIMESTAMP));
			while (input.consume(Token.Type.COMMA, Token.Type.CLOSEPAREN).getType().equals(Token.Type.COMMA)) {
				inValues.add(input.consume(Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
						Token.Type.PARAMETER, Token.Type.TIMESTAMP));
			}
		} else {
			value2 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
					Token.Type.PARAMETER, Token.Type.TIMESTAMP);
			input.consume(Token.Type.AND);
			value3 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
					Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		}
	}

	public StringBuilder getWhere(Class<?> tb) throws BadParameterException {
		StringBuilder sb = new StringBuilder();
		if (compop.getType() == Token.Type.COMPOP) {
			boolean one = value1.getType() == Token.Type.NAME && value2.getType() != Token.Type.NAME;
			boolean two = value2.getType() == Token.Type.NAME && value1.getType() != Token.Type.NAME;
			if (!one && !two) {
				throw new BadParameterException("Attribute comparisons require one attribute name and one value: "
						+ value1 + " " + compop + " " + value2);
			}
			if (one) {
				sb.append(getName(value1, tb));
			} else {
				sb.append(getValue(value1));
			}

			sb.append(" " + compop.getValue() + " ");

			if (two) {
				sb.append(getName(value2, tb));
			} else {
				sb.append(getValue(value2));
			}
		} else if (compop.getType() == Token.Type.IN) {
			if (value1.getType() != Token.Type.NAME) {
				throw new BadParameterException("\"IN\" comparisons require a name on the LHS rather than "
						+ Tokenizer.getTypeToPrint(value1.getType()));
			}
			sb.append(getName(value1, tb));
			sb.append(" IN (");
			boolean first = true;
			for (Token token : inValues) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(getValue(token));
			}
			sb.append(")");
		} else {
			if (value1.getType() != Token.Type.NAME) {
				throw new BadParameterException("\"BETWEEN\" comparisons require a name on the LHS rather than "
						+ Tokenizer.getTypeToPrint(value1.getType()));
			}
			sb.append(getName(value1, tb)).append(" BETWEEN ").append(getValue(value2)).append(" AND ")
					.append(getValue(value3));
		}
		return sb;
	}

	private String getValue(Token value) {
		String val = value.getValue();
		if (value.getType() == Token.Type.STRING) {
			val = "'" + val.replace("'", "''") + "'";
		} else if (value.getType() == Token.Type.TIMESTAMP) {
			val =":" + value.getValue().replace(" ", "").replace(":", "").replace("-", "").replace("{", "").replace("}", "");
		}
		return val;
	}

	private String getName(Token value, Class<?> tb) throws BadParameterException {
		String val = value.getValue();
		try {
			tb.getDeclaredField(val.split("\\.")[0]);
		} catch (NoSuchFieldException e) {
			throw new BadParameterException("Field " + val + " of " + tb + " does not exist");
		}
		val = tb.getSimpleName() + "$." + val;
		return val;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value1);
		sb.append(" " + compop + " ");
		if (compop.getType() == Token.Type.COMPOP) {
			sb.append(value2);
		} else if (compop.getType() == Token.Type.IN) {
			sb.append(compop.getValue() + "(");
			boolean first = false;
			for (Token token : inValues) {
				if (first) {
					first = false;
				} else {
					sb.append(compop.getValue() + ", ");
				}
				sb.append(getValue(token));
			}
		} else {
			sb.append(value2).append(" AND ").append(value3);
		}
		return sb.toString();
	}
}
