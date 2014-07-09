package org.icatproject.core.parser;

import java.util.Arrays;
import java.util.List;

import org.icatproject.core.parser.Token.Type;

public class Input {

	private List<Token> tokens;
	private int pos;

	public Input(List<Token> tokens) {
		this.tokens = tokens;
		this.pos = 0;
	}
	
	public void reset() {
		this.pos = 0;
	}

	public Token consume(Type... types) throws ParserException {
		if (pos < tokens.size()) {
			if (Arrays.asList(types).contains(tokens.get(pos).getType())) {
				return tokens.get(pos++);
			} else {
				throw new ParserException(this, types);
			}
		} else {
			throw new ParserException("Attempt to read beyond end");
		}
	}

	public Token consume() throws ParserException {
		if (pos < tokens.size()) {
			return tokens.get(pos++);
		} else if (pos == tokens.size()) {
			return null;
		} else {
			throw new ParserException("Attempt to read beyond end");
		}
	}

	public Token peek(int off) {
		int n = pos + off;
		if (n >= 0 && n < tokens.size()) {
			return tokens.get(pos + off);
		} else {
			return null;
		}
	}

}
