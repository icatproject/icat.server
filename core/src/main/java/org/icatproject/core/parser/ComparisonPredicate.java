package org.icatproject.core.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.EntityInfoHandler;

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

	private static final EntityInfoHandler ei = EntityInfoHandler.getInstance();

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

	private static final Logger logger = Logger.getLogger(ComparisonPredicate.class);

	public StringBuilder getWhere(Class<? extends EntityBaseBean> tb) throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (compop.getType() == Token.Type.COMPOP) {
			boolean one = value1.getType() == Token.Type.NAME
					&& value2.getType() != Token.Type.NAME;
			boolean two = value2.getType() == Token.Type.NAME
					&& value1.getType() != Token.Type.NAME;
			if (!one && !two) {
				if (value1.getType() == Token.Type.NAME && value2.getType() == Token.Type.NAME) {
					Map<String, Field> enums = ei.getEnums(tb);
					one = enums.containsKey(value1.getValue());
					two = enums.containsKey(value2.getValue());
					if (!one && !two) {
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								"Attribute comparisons require one attribute name and one value: "
										+ value1 + " " + compop + " " + value2);
					}
					if (one) {
						sb.append(getName(value1, tb));
					} else {
						sb.append(enums.get(value2.getValue()).getType().getCanonicalName() + "."
								+ getValue(value1));
					}

					sb.append(" " + compop.getValue() + " ");

					if (two) {
						sb.append(getName(value2, tb));
					} else {
						sb.append(enums.get(value1.getValue()).getType().getCanonicalName() + "."
								+ getValue(value2));
					}
					return sb;
				} else {
					throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
							"Attribute comparisons require one attribute name and one value: "
									+ value1 + " " + compop + " " + value2);
				}

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
					throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, "Field "
							+ val + " of " + tb + " does not exist");
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
