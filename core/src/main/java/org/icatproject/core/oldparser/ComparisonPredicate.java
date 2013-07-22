package org.icatproject.core.oldparser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.oldparser.Token.Type;

public class ComparisonPredicate {

	// ComparisonPredicate ::= value ( COMPOP value ) | ( "IN" "(" value (","
	// value)* ")" ) | (
	// "BETWEEN" value "AND" value )

	private Token value1;
	private Token compop;
	private Token value2;
	private Token value3;
	private List<Token> inValues;

	private static final Set<String> beanFields = new HashSet<String>(Arrays.asList("createTime",
			"createId", "modTime", "modId"));

	private static final Set<String> booleanLiterals = new HashSet<String>(Arrays.asList("TRUE",
			"FALSE"));

	public ComparisonPredicate(Input input) throws ParserException {
		value1 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER,
				Token.Type.REAL, Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		compop = input.consume(Token.Type.COMPOP, Token.Type.IN, Token.Type.BETWEEN);
		if (compop.getType() == Token.Type.COMPOP) {
			value2 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER,
					Token.Type.REAL, Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		} else if (compop.getType() == Token.Type.IN) {
			input.consume(Token.Type.OPENPAREN);
			inValues = new ArrayList<Token>();
			inValues.add(input.consume(Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
					Token.Type.PARAMETER, Token.Type.TIMESTAMP));
			while (input.consume(Token.Type.COMMA, Token.Type.CLOSEPAREN).getType()
					.equals(Token.Type.COMMA)) {
				inValues.add(input.consume(Token.Type.STRING, Token.Type.INTEGER, Token.Type.REAL,
						Token.Type.PARAMETER, Token.Type.TIMESTAMP));
			}
		} else {
			value2 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER,
					Token.Type.REAL, Token.Type.PARAMETER, Token.Type.TIMESTAMP);
			input.consume(Token.Type.AND);
			value3 = input.consume(Token.Type.NAME, Token.Type.STRING, Token.Type.INTEGER,
					Token.Type.REAL, Token.Type.PARAMETER, Token.Type.TIMESTAMP);
		}
	}

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (compop.getType() == Token.Type.COMPOP) {
			Token nameToken = null;
			Token valueToken = null;
			String name = getName(value1, tb);
			if (name != null) {
				nameToken = value1;
				valueToken = value2;
			} else if ((name = getName(value2, tb)) != null) {
				nameToken = value2;
				valueToken = value1;
			} else {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
						"Attribute comparisons require one attribute name and one value: " + value1
								+ " " + compop + " " + value2);
			}
			Type valueType = valueToken.getType();
			sb.append(name);
			sb.append(" " + compop.getValue() + " ");
			if (valueType != Token.Type.NAME
					|| booleanLiterals.contains(valueToken.getValue().toUpperCase())) {
				sb.append(getValue(valueToken));
			} else {
				Class<?> klass = tb;
				String[] levels = nameToken.getValue().split("\\.");
				Field nextField = null;
				for (String level : levels) {
					try {
						nextField = klass.getDeclaredField(level);
					} catch (NoSuchFieldException e) {
						try {
							nextField = tb.getSuperclass().getDeclaredField(level);
						} catch (NoSuchFieldException e1) {
							throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
									"Field " + level + " not found in class " + klass);
						}
					}
					klass = nextField.getType();
				}
				sb.append(nextField.getType().getCanonicalName() + "." + valueToken.getValue());
			}
		} else if (compop.getType() == Token.Type.IN) {
			if (value1.getType() != Token.Type.NAME) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
						"\"IN\" comparisons require a name on the LHS rather than "
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
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
						"\"BETWEEN\" comparisons require a name on the LHS rather than "
								+ Tokenizer.getTypeToPrint(value1.getType()));
			}
			sb.append(getName(value1, tb)).append(" BETWEEN ").append(getValue(value2))
					.append(" AND ").append(getValue(value3));
		}
		return sb;
	}

	private String getValue(Token value) {
		String val = value.getValue();
		if (value.getType() == Token.Type.STRING) {
			val = "'" + val.replace("'", "''") + "'";
		} else if (value.getType() == Token.Type.TIMESTAMP) {
			val = ":"
					+ value.getValue().replace(" ", "").replace(":", "").replace("-", "")
							.replace("{", "").replace("}", "");
		}
		return val;
	}

	private String getName(Token value, Class<?> tb) throws IcatException {
		String val = value.getValue();
		if (!beanFields.contains(val)) {
			try {
				tb.getDeclaredField(val.split("\\.")[0]);
			} catch (NoSuchFieldException e) {
				try {
					tb.getSuperclass().getDeclaredField(val.split("\\.")[0]);
				} catch (NoSuchFieldException e1) {
					return null;
				}
			}
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
			sb.append("(");
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
			sb.append(value2).append(" AND ").append(value3);
		}
		return sb.toString();
	}
}
