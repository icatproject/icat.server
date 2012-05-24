package org.icatproject.core.parser;

import java.util.Set;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;


public class GetQuery {

	// GetQuery ::= name Include

	static Logger logger = Logger.getLogger(GetQuery.class);

	private Class<? extends EntityBaseBean> bean;

	private Include include;

	public GetQuery(Input input) throws ParserException, IcatException {
		this.bean = EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue());
		this.include = new Include(input);
		Token t;
		if ((t = input.peek(0)) != null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Trailing tokens at end of query " + t + "...");
		}

		/* Make sure that all is well - and the entities are connected */
		Set<Class<? extends EntityBaseBean>> es = include.getBeans(bean);
		if (es != null) {
			DagHandler.findSteps(bean, es);
		}
	}

	public Class<? extends EntityBaseBean> getFirstEntity() {
		return this.bean;
	}

	public Set<Class<? extends EntityBaseBean>> getIncludes() throws IcatException {
		return this.include.getBeans(bean);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.bean.getSimpleName());
		sb.append(this.include);
		return sb.toString();
	}

}
