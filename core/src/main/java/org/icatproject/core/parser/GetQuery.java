package org.icatproject.core.parser;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class GetQuery {

	// GetQuery ::= name Include?

	static Logger logger = Logger.getLogger(GetQuery.class);

	private Class<? extends EntityBaseBean> bean;

	private Include include;

	public GetQuery(Input input) throws ParserException, IcatException {
		this.bean = EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue());
		Token t = input.peek(0);
		if (t != null && t.getType() == Token.Type.INCLUDE) {
			this.include = new Include(bean, input);
			t = input.peek(0);
		}
		if (t != null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Trailing tokens at end of query " + t + "...");
		}
	}

	public Class<? extends EntityBaseBean> getFirstEntity() {
		return this.bean;
	}

	public Include getInclude() throws IcatException {
		return this.include;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.bean.getSimpleName());
		if (include != null) {
			sb.append(this.include);
		}
		return sb.toString();
	}

}
