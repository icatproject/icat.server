package org.icatproject.core.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class GetQuery {

	// GetQuery ::= name (AS? name Include);

	static Logger logger = Logger.getLogger(GetQuery.class);

	private Class<? extends EntityBaseBean> bean;

	private IncludeClause include;

	public GetQuery(Input input) throws ParserException, IcatException {
		this.bean = EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue());
		Token t = input.peek(0);
		if (t != null && t.getType() == Token.Type.AS) {
			input.consume();
			t = input.peek(0);
		}
		Map<String, Integer> idVarMap = new HashMap<>();
		if (t != null && t.getType() == Token.Type.NAME) {

			idVarMap.put(t.getValue().toUpperCase(), 0);
			input.consume();
			t = input.peek(0);
		}
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			include = new IncludeClause(bean, input, idVarMap);
			t = input.peek(0);
		}
		if (t != null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Trailing tokens at end of query " + t + "...");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.bean.getSimpleName());
		if (include != null) {
			sb.append(" AS $0$ " + include);
		}
		return sb.toString();
	}

	public Class<? extends EntityBaseBean> getBean() {
		return bean;
	}

	public IncludeClause getInclude() {
		return include;
	}

}
