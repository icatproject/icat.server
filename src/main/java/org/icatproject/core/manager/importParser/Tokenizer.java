package org.icatproject.core.manager.importParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.icatproject.core.parser.LexerException;

public class Tokenizer {

	enum State {
		RESET, NONE, INQUOTES, CLOSEDQUOTES, NAME, INTEGER, TIMESTAMP, MINUS, REAL
	}

	private final static Pattern tsRegExp1 = Pattern
			.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z?");

	private final static Pattern tsRegExp2 = Pattern
			.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[+-]\\d{2}:\\d{2}");

	private final static Set<String> keyWords = new HashSet<String>(Arrays.asList("NULL"));
	private final static Set<String> booleans = new HashSet<String>(Arrays.asList("TRUE", "FALSE"));

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
			// System.out.println(state + " " + i + " " + ch);
			if (state == State.NONE) {
				if (ch == ' ' || ch == 0) {
					// Ignore
				} else if (Character.isLetter(ch) || ch == '$' || ch == '_') {
					state = State.NAME;
					start = i;
				} else if (Character.isDigit(ch)) {
					state = State.INTEGER;
					start = i;
				} else if (ch == '"') {
					state = State.INQUOTES;
					start = i;
				} else if (ch == '(') {
					tokens.add(new Token(Token.Type.OPENPAREN, ch));
				} else if (ch == ')') {
					tokens.add(new Token(Token.Type.CLOSEPAREN, ch));
				} else if (ch == ',') {
					tokens.add(new Token(Token.Type.COMMA, ch));
				} else if (ch == ':') {
					tokens.add(new Token(Token.Type.COLON, ch));
				} else if (ch == '?') {
					tokens.add(new Token(Token.Type.QMARK, ch));
				} else if (ch == '-') {
					state = State.MINUS;
					start = i;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.MINUS) {
				if (Character.isDigit(ch)) {
					state = State.INTEGER;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.INQUOTES) {
				if (ch == '"') {
					state = State.CLOSEDQUOTES;
				} else if (ch == 0) {
					reportError(ch, state, i, input);
				}
			} else if (state == State.CLOSEDQUOTES) {
				if (ch == '"') {
					state = State.INQUOTES;
				} else {
					tokens.add(new Token(Token.Type.STRING, input.substring(start + 1, i - 1)
							.replace("\"\"", "\"")));
					state = State.RESET;
				}
			} else if (state == State.NAME) {
				if (!Character.isLetterOrDigit(ch) && ch != '_') {
					String name = input.substring(start, i);
					String nameUp = name.toUpperCase();
					if (keyWords.contains(nameUp)) {
						tokens.add(new Token(Token.Type.valueOf(nameUp), nameUp));
					} else if (booleans.contains(nameUp)) {
						tokens.add(new Token(Token.Type.BOOLEAN, nameUp));
					} else {
						tokens.add(new Token(Token.Type.NAME, name));
					}
					state = State.RESET;
				}
			} else if (state == State.INTEGER) {
				if (ch == 'e' || ch == 'E' || ch == '.') {
					state = State.REAL;
				} else if (ch == '-') {
					state = State.TIMESTAMP;
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

			} else if (state == State.TIMESTAMP) {
				if (!Character.isDigit(ch) && ch != 'T' && ch != 'Z' && ch != '.' && ch != ':'
						&& ch != '+' && ch != '-') {
					String ts = input.substring(start, i);

					if (!tsRegExp1.matcher(ts).matches() && !tsRegExp2.matcher(ts).matches()) {
						throw new LexerException("Timestamp " + ts + " is not of required form");
					}
					tokens.add(new Token(Token.Type.TIMESTAMP, input.substring(start, i)));
					state = State.RESET;
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
		if (type == Token.Type.OPENPAREN) {
			return "(";
		} else if (type == Token.Type.CLOSEPAREN) {
			return ")";
		}
		return type.name();
	}

}
