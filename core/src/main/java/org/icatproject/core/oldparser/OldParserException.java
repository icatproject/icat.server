package org.icatproject.core.oldparser;

import java.util.Arrays;
import java.util.List;

import org.icatproject.core.oldparser.OldToken.Type;



@SuppressWarnings("serial")
public class OldParserException extends Exception {

	private static String getMsg(OldInput input, List<Type> types) {
		StringBuilder sb = new StringBuilder();
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

		sb.append(input.peek(0).getValue()).append(" in ");
		for (int i = -2; i < 0; i++) {
			OldToken t = input.peek(i);
			if (t != null) {
				sb.append(t.getValue()).append(' ');
			}
		}
		sb.append("< " + input.peek(0).getValue() + " > ");
		for (int i = 1; i < 3; i++) {
			OldToken t = input.peek(i);
			if (t != null) {
				sb.append(t.getValue()).append(' ');
			}
		}
		return sb.toString();
	}

	public OldParserException(OldInput input, Type[] types) {
		super(getMsg(input, Arrays.asList(types)));
	}

	public OldParserException(String msg) {
		super(msg);
	}

}
