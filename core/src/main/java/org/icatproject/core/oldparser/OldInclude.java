package org.icatproject.core.oldparser;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;

public class OldInclude {

	// OldInclude ::= "INCLUDE" "1" | (Name ("," Name )*)

	private enum FollowCascades {
		TRUE, FALSE
	};

	private enum Position {
		FIRST, LOWER
	};

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private Set<Class<? extends EntityBaseBean>> includes = new HashSet<Class<? extends EntityBaseBean>>();

	static Logger logger = Logger.getLogger(OldInclude.class);

	private boolean one;

	public OldInclude(Class<? extends EntityBaseBean> bean, OldInput input) throws OldParserException,
			IcatException {

		input.consume(OldToken.Type.INCLUDE);
		OldToken name = input.consume(OldToken.Type.NAME, OldToken.Type.INTEGER);
		String value = name.getValue();
		if (name.getType() == OldToken.Type.NAME) {
			this.includes.add(EntityInfoHandler.getClass(value));
			OldToken t;
			while ((t = input.peek(0)) != null && t.getType() == OldToken.Type.COMMA) {
				input.consume();
				name = input.consume(OldToken.Type.NAME);
				value = name.getValue();
				this.includes.add(EntityInfoHandler.getClass(value));
			}
			DagHandler.checkIncludes(bean, includes);
		} else if (value.equals("1")) {
			one = true;
		} else {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Only integer value allowed in the INCLUDE list is 1");
		}

	}

	public String getNewInclude(Class<? extends EntityBaseBean> firstBean) throws IcatException {
		StringBuilder sb = new StringBuilder();
		if (one) {
			sb.append("INCLUDE 1");
		} else {
			StringBuilder sbinc = new StringBuilder();
			addIncludes(sbinc, firstBean, includes, FollowCascades.TRUE, Position.FIRST);
			logger.debug(sbinc.toString());
			sb.append(sbinc);
		}
		return sb.toString();
	}

	private static String addIncludes(StringBuilder sb,
			Class<? extends EntityBaseBean> entityClass,
			Set<Class<? extends EntityBaseBean>> includes, FollowCascades followCascades,
			Position position) throws IcatException {
		boolean first = position == Position.FIRST;
		String suffix = first ? "$" : "_$";
		Set<Relationship> relationships = eiHandler.getIncludesToFollow(entityClass);
		for (Relationship r : relationships) {
			if (!r.isCascaded() || followCascades == FollowCascades.TRUE) {
				Class<? extends EntityBaseBean> bean = r.getBean();
				if (includes.contains(bean)) {

					if (sb.length() == 0) {
						sb.append("INCLUDE ");
					} else {
						sb.append(", ");
					}

					sb.append(entityClass.getSimpleName() + suffix + "." + r.getField().getName()
							+ " AS " + bean.getSimpleName() + "_$");

					// Avoid looping forever
					Set<Class<? extends EntityBaseBean>> includeReduced = new HashSet<Class<? extends EntityBaseBean>>(
							includes);
					includeReduced.remove(bean);

					if (r.isCollection()) {
						addIncludes(sb, bean, includeReduced, FollowCascades.TRUE, Position.LOWER);
					} else {
						addIncludes(sb, bean, includeReduced, FollowCascades.FALSE, Position.LOWER);
					}
				}
			}
		}
		return sb.toString();
	}

}
