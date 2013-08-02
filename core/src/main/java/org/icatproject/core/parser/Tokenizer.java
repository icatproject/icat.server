package org.icatproject.core.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

	enum State {
		RESET, NONE, INQUOTES, CLOSEDQUOTES, LT, NAME, INTEGER, REAL, NOT, PARAMETER, TIMESTAMP, GT
	}

	private final static Pattern tsRegExp = Pattern
			.compile("\\{\\s*ts\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2})\\s*\\}");

	private final static Set<String> boolops = new HashSet<String>(
			Arrays.asList("AND", "OR", "NOT"));
	private final static Set<String> keyWords = new HashSet<String>(Arrays.asList("ALL", "ANY",
			"AND", "AS", "ASC", "BETWEEN", "BOTH", "BY", "DESC", "DISTINCT", "DIV", "EMPTY", "FALSE",
			"FETCH", "FROM", "GROUP", "HAVING", "IN", "INNER", "IS", "JOIN", "LEADING", "LEFT",
			"MEMBER", "MINUS", "MULT", "NOT", "NULL", "OR", "ORDER", "OUTER", "PLUS", "REAL",
			"SELECT", "TRAILING", "WHERE", "INCLUDE", "LIMIT", "TIMESTAMP", "TRUE"));
	private final static Set<String> aggFunctions = new HashSet<String>(Arrays.asList("MIN", "MAX",
			"AVG", "COUNT", "SUM"));

	private final static Set<String> otherFunctions = new HashSet<String>(Arrays.asList("CONCAT",
			"LENGTH", "LOCATE", "SUBSTRING", "TRIM", "ABS", "MOD", "SQRT", "SIZE", "CURRENT_DATE",
			"CURRENT_TIME", "CURRENT_TIMESTAMP"));

	public static List<Token> getTokens(String input) throws LexerException {
		List<Token> tokens = new ArrayList<Token>();
		State state = State.NONE;
		int start = 0;
		char ch = ' ';
		for (int i = 0; i < input.length() + 1; i++) {
			if (state == State.RESET) {
				i--;
				state = State.NONE;
			} else if (i < input.length()) {
				ch = input.charAt(i);
			} else {
				ch = 0;
			}
			if (state == State.NONE) {
				if (ch == ' ' || ch == 0) {
					// Ignore
				} else if (Character.isLetter(ch) || ch == '$' || ch == '_') {
					state = State.NAME;
					start = i;
				} else if (Character.isDigit(ch)) {
					state = State.INTEGER;
					start = i;
				} else if (ch == ':') {
					state = State.PARAMETER;
					start = i;
				} else if (ch == '\'') {
					state = State.INQUOTES;
					start = i;
				} else if (ch == '(') {
					tokens.add(new Token(Token.Type.OPENPAREN, ch));
				} else if (ch == ')') {
					tokens.add(new Token(Token.Type.CLOSEPAREN, ch));
				} else if (ch == ',') {
					tokens.add(new Token(Token.Type.COMMA, ch));
				} else if (ch == '<') {
					state = State.LT;
				} else if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, ch));
				} else if (ch == '>') {
					state = State.GT;
				} else if (ch == '!') {
					state = State.NOT;
				} else if (ch == '{') {
					state = State.TIMESTAMP;
					start = i;
				} else if (ch == '+') {
					tokens.add(new Token(Token.Type.PLUS, ch));
				} else if (ch == '-') {
					tokens.add(new Token(Token.Type.MINUS, ch));
				} else if (ch == '*') {
					tokens.add(new Token(Token.Type.MULT, ch));
				} else if (ch == '/') {
					tokens.add(new Token(Token.Type.DIV, ch));
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.INQUOTES) {
				if (ch == '\'') {
					state = State.CLOSEDQUOTES;
				} else if (ch == 0) {
					reportError(ch, state, i, input);
				}
			} else if (state == State.CLOSEDQUOTES) {
				if (ch == '\'') {
					state = State.INQUOTES;
				} else {
					tokens.add(new Token(Token.Type.STRING, input.substring(start + 1, i - 1)
							.replace("''", "'")));
					state = State.RESET;
				}
			} else if (state == State.GT) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, ">="));
					state = State.NONE;
				} else {
					tokens.add(new Token(Token.Type.COMPOP, ">"));
					state = State.RESET;
				}
			} else if (state == State.LT) {
				if (ch == '>') {
					tokens.add(new Token(Token.Type.COMPOP, "<>"));
					state = State.NONE;
				} else if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, "<="));
					state = State.NONE;
				} else {
					tokens.add(new Token(Token.Type.COMPOP, "<"));
					state = State.RESET;
				}
			} else if (state == State.NAME) {
				if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '.' && ch != '$') {
					String name = input.substring(start, i);
					String nameUp = name.toUpperCase();
					if (boolops.contains(nameUp) || keyWords.contains(nameUp)
							|| aggFunctions.contains(nameUp) || otherFunctions.contains(nameUp)) {
						tokens.add(new Token(Token.Type.valueOf(nameUp), nameUp));
					} else if (nameUp.equals("LIKE")) {
						tokens.add(new Token(Token.Type.COMPOP, "LIKE"));
					} else {
						tokens.add(new Token(Token.Type.NAME, name));
					}
					state = State.RESET;
				}
			} else if (state == State.INTEGER) {
				if (ch == 'e' || ch == 'E' || ch == '.') {
					state = State.REAL;
				} else if (!Character.isDigit(ch)) {
					tokens.add(new Token(Token.Type.INTEGER, input.substring(start, i)));
					state = State.RESET;
				}
			} else if (state == State.REAL) {
				if (!Character.isDigit(ch) && ch != 'e' && ch != 'E' && ch != '.' && ch != '+'
						&& ch != '-') {
					Double d = null;
					try {
						d = Double.parseDouble(input.substring(start, i));
					} catch (NumberFormatException e) {
						reportError(ch, state, i, input);
					}
					tokens.add(new Token(Token.Type.REAL, d.toString()));
					state = State.RESET;
				}
			} else if (state == State.NOT) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, "!="));
					state = State.NONE;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.PARAMETER) {
				if (!Character.isLetterOrDigit(ch) && ch != '_') {
					tokens.add(new Token(Token.Type.PARAMETER, input.substring(start, i)));
					state = State.RESET;
				}
			} else if (state == State.TIMESTAMP) {
				if (ch == '}') {
					String ts = input.substring(start, i) + "}";
					Matcher matcher = tsRegExp.matcher(ts);
					if (!matcher.matches()) {
						throw new LexerException("Timestamp " + ts
								+ " is not of form {ts yyyy-mm-dd hh:mm:ss}.");
					}
					tokens.add(new Token(Token.Type.TIMESTAMP, "{ts " + matcher.group(1) + " "
							+ matcher.group(2) + "}"));
					state = State.NONE;
				}
			}
		}

		return tokens;
	}

	private static void reportError(char ch, State state, int i, String input)
			throws LexerException {
		int i1 = Math.max(0, i - 4);
		int i2 = Math.min(i + 5, input.length());
		if (ch != 0) {
			throw new LexerException("Unexpected character '" + ch + "' near \""
					+ input.substring(i1, i2) + "\" in state " + state + " for string: " + input);
		} else {
			throw new LexerException("Unexpected end of string in state " + state + " for string: "
					+ input);
		}
	}

	public static String getTypeToPrint(Token.Type type) {
		if (type == Token.Type.COMPOP) {
			return ">, <, !=, =, <>, >=, <=";
		} else if (type == Token.Type.OPENPAREN) {
			return "(";
		} else if (type == Token.Type.CLOSEPAREN) {
			return ")";
		}
		return type.name();
	}

}
