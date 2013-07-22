package org.icatproject.core.oldparser;

import java.util.HashSet;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;

/**
 * 
 * Ensures that the entities can only be linked in one way.
 * 
 */
public class DagHandler {

	private static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();

	/**
	 * A nested structure with a bean a relationship and the set of steps. It is a set because there
	 * may be a need to follow more than one chain of entities.
	 */
	public static class Step {

		private Class<? extends EntityBaseBean> bean;
		private Relationship relationship;
		private Set<Step> steps;

		public Step(Class<? extends EntityBaseBean> bean, Relationship relationship, Set<Step> steps) {
			this.bean = bean;
			this.relationship = relationship;
			this.steps = steps;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (relationship == null) {
				sb.append(bean.getSimpleName());
			} else {
				sb.append(relationship);
			}
			if (!steps.isEmpty()) {
				sb.append(" to ");
			}
			boolean first = true;
			for (Step s : steps) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append("[" + s + "]");
			}
			return sb.toString();
		}

		public String join() {
			if (steps.isEmpty()) {
				return "";
			} else {
				String alias = null;
				if (relationship == null) {
					alias = bean.getSimpleName() + "$";
				} else {
					alias = relationship.getBean().getSimpleName() + "$";
				}
				StringBuilder sb = new StringBuilder();
				for (Step s : steps) {
					sb.append("JOIN " + alias + "." + s.relationship.getField().getName() + " AS "
							+ s.relationship.getBean().getSimpleName() + "$");
					sb.append(" " + s.join());
				}
				return sb.toString();
			}
		}
	}

	/**
	 * Takes the start entity and the set of other entities and produces a Step, which is a nested
	 * structure. It will fail if the entities are not properly linked.
	 */
	public static Step findSteps(Class<? extends EntityBaseBean> start,
			Set<Class<? extends EntityBaseBean>> es) throws IcatException {
		Set<Class<? extends EntityBaseBean>> allBeans = new HashSet<Class<? extends EntityBaseBean>>(
				es);
		allBeans.add(start);

		Set<Class<? extends EntityBaseBean>> used = new HashSet<Class<? extends EntityBaseBean>>();
		used.add(start);

		Step step = new Step(start, null, follow(null, start, allBeans, used));
		allBeans.removeAll(used);
		if (!allBeans.isEmpty()) {
			boolean first = true;
			StringBuilder sb = new StringBuilder();
			for (Class<? extends EntityBaseBean> bean : allBeans) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(bean.getSimpleName());
			}
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Unable to reach " + sb);

		}
		return step;

	}

	private static Set<Step> follow(Class<? extends EntityBaseBean> predecessor,
			Class<? extends EntityBaseBean> from, Set<Class<? extends EntityBaseBean>> allBeans,
			Set<Class<? extends EntityBaseBean>> used) throws IcatException {
		Set<Step> steps = new HashSet<Step>();
		Set<Relationship> navto = pkHandler.getRelatedEntities(from);
		for (Relationship relationship : navto) {
			Class<? extends EntityBaseBean> bean = relationship.getBean();
			if (allBeans.contains(bean) && !bean.equals(predecessor)) {
				if (used.contains(bean)) {
					throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
							"Can't have loop in graph of entities. '" + bean.getSimpleName()
									+ "' was encountered twice following "
									+ predecessor.getSimpleName() + " -> " + from.getSimpleName()
									+ " -> " + bean.getSimpleName());
				} else {
					used.add(bean);
					Step s = new Step(bean, relationship, follow(from, bean, allBeans, used));
					steps.add(s);
				}
			}
		}
		return steps;
	}

	public static void checkIncludes(Class<? extends EntityBaseBean> start,
			Set<Class<? extends EntityBaseBean>> es) throws IcatException {
		Set<Class<? extends EntityBaseBean>> allBeans = new HashSet<Class<? extends EntityBaseBean>>(
				es);
		allBeans.add(start);

		Set<Class<? extends EntityBaseBean>> used = new HashSet<Class<? extends EntityBaseBean>>();
		used.add(start);

		new Step(start, null, followIncludes(null, start, allBeans, used, true));
		allBeans.removeAll(used);
		if (!allBeans.isEmpty()) {
			boolean first = true;
			StringBuilder sb = new StringBuilder();
			for (Class<? extends EntityBaseBean> bean : allBeans) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(bean.getSimpleName());
			}
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
					"Unable to reach " + sb + " in list of INCLUDES.");

		}

	}

	private static Set<Step> followIncludes(Class<? extends EntityBaseBean> predecessor,
			Class<? extends EntityBaseBean> from, Set<Class<? extends EntityBaseBean>> allBeans,
			Set<Class<? extends EntityBaseBean>> used, boolean followCascades) throws IcatException {
		Set<Step> steps = new HashSet<Step>();
		Set<Relationship> navto = pkHandler.getIncludesToFollow(from);
		for (Relationship relationship : navto) {
			if (!relationship.isCascaded() || followCascades) {
				Class<? extends EntityBaseBean> bean = relationship.getBean();
				if (allBeans.contains(bean) && !bean.equals(predecessor)) {
					if (used.contains(bean)) {
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								"Can't have loop in graph of entities. '" + bean.getSimpleName()
										+ "' was encountered twice following "
										+ (predecessor == null ? "" : predecessor.getSimpleName())
										+ " -> " + from.getSimpleName() + " -> "
										+ bean.getSimpleName() + " in list of INCLUDES.");
					} else {
						used.add(bean);
						Step s = new Step(bean, relationship, followIncludes(from, bean, allBeans,
								used, relationship.isCollection()));
						steps.add(s);
					}
				}
			}
		}
		return steps;
	}

}
