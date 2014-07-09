package org.icatproject.core.oldparser;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class OldGetQuery {

	// GetQuery ::= name Include?

	static Logger logger = Logger.getLogger(OldGetQuery.class);

	private Class<? extends EntityBaseBean> bean;

	private OldInclude include;

	public OldGetQuery(OldInput input) throws OldParserException, IcatException {
		this.bean = EntityInfoHandler.getClass(input.consume(OldToken.Type.NAME).getValue());
		OldToken t = input.peek(0);
		if (t != null && t.getType() == OldToken.Type.INCLUDE) {
			this.include = new OldInclude(bean, input);
			t = input.peek(0);
		}
		if (t != null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Trailing tokens at end of query " + t + "...");
		}
	}

	public String getNewQuery() throws IcatException {
		StringBuilder sb = new StringBuilder(bean.getSimpleName() + " AS " + bean.getSimpleName()
				+ "$");
		if (include != null) {
			sb.append(" " + include.getNewInclude(bean));
		}
		return sb.toString();
	}

}
