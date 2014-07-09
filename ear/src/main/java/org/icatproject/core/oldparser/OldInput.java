package org.icatproject.core.oldparser;

import java.util.Arrays;
import java.util.List;

import org.icatproject.core.oldparser.OldToken.Type;
import org.icatproject.core.oldparser.OldParserException;

public class OldInput {

	private List<OldToken> tokens;
	private int pos;

	public OldInput(List<OldToken> tokens) {
		this.tokens = tokens;
		this.pos = 0;
	}

	public OldToken consume(Type... types) throws OldParserException {
		if (pos < tokens.size()) {
			if (Arrays.asList(types).contains(tokens.get(pos).getType())) {
				return tokens.get(pos++);
			} else {
				throw new OldParserException(this, types);
			}
		} else {
			throw new OldParserException("Attempt to read beyond end");
		}
	}

	public OldToken consume() throws OldParserException {
		if (pos < tokens.size()) {
			return tokens.get(pos++);
		} else if (pos == tokens.size()) {
			return null;
		} else {
			throw new OldParserException("Attempt to read beyond end");
		}
	}

	public OldToken peek(int off) {
		int n = pos + off;
		if (n >= 0 && n < tokens.size()) {
			return tokens.get(pos + off);
		} else {
			return null;
		}
	}

}
