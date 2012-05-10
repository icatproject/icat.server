package uk.icat3.parser;

import java.util.Set;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.manager.DagHandler;
import uk.icat3.manager.EntityInfoHandler;

public class GetQuery {

	// GetQuery ::= name Include

	static Logger logger = Logger.getLogger(GetQuery.class);

	private Class<? extends EntityBaseBean> bean;

	private Include include;

	public GetQuery(Input input) throws ParserException, IcatInternalException, BadParameterException {
		this.bean = EntityInfoHandler.getClass(input.consume(Token.Type.NAME).getValue());
		this.include = new Include(input);
		Token t;
		if ((t = input.peek(0)) != null) {
			throw new BadParameterException("Trailing tokens at end of query " + t + "...");
		}

		/* Make sure that all is well - and the entities are connected */
		Set<Class<? extends EntityBaseBean>> es = include.getBeans(bean);
		if (es != null) {
			DagHandler.fixes(bean, es);
		}
	}

	public Class<? extends EntityBaseBean> getFirstEntity() throws BadParameterException {
		return this.bean;
	}
	
	public Set<Class<? extends EntityBaseBean>> getIncludes() throws BadParameterException, IcatInternalException {
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
