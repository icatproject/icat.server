package org.icatproject.core.parser;

import java.util.Arrays;
import java.util.List;

import org.icatproject.core.parser.Token.Type;

@SuppressWarnings("serial")
public class ParserException extends Exception {

	private static String getMsg(Input input, List<Type> types) {
		StringBuilder sb = new StringBuilder();
		if (types.size() > 0) {
			sb.append("Expected token from types [");
			boolean first = true;
			for (Type type : types) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(type);
			}
			sb.append("] at token ");
		} else {
			sb.append("No more input expected at token ");
		}
		

		sb.append(input.peek(0).getValue()).append(" in ");
		for (int i = -2; i < 0; i++) {
			Token t = input.peek(i);
			if (t != null) {
				sb.append(t.getValue()).append(' ');
			}
		}
		sb.append("< " + input.peek(0).getValue() + " > ");
		for (int i = 1; i < 3; i++) {
			Token t = input.peek(i);
			if (t != null) {
				sb.append(t.getValue()).append(' ');
			}
		}
		return sb.toString();
	}

	public ParserException(Input input, Type[] types) {
		super(getMsg(input, Arrays.asList(types)));
	}

	public ParserException(String msg) {
		super(msg);
	}

}
