package uk.icat3.security;

import java.util.HashSet;
import java.util.Set;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.security.EntityInfoHandler.Relationship;

public class DagHandler {

	private static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();

	public  static class Step {

		private Class<? extends EntityBaseBean> bean;
		private Relationship relationship;
		private Set<Step> to;

		public Step(Class<? extends EntityBaseBean> bean, Relationship relationship, Set<Step> to) {
			this.bean = bean;
			this.relationship = relationship;
			this.to = to;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (relationship == null) {
				sb.append(bean.getSimpleName());
			} else {
				sb.append(relationship);
			}
			if (!to.isEmpty()) {
				sb.append(" to ");
			}
			boolean first = true;
			for (Step s : to) {
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
			if (to.isEmpty()) {
				return "";
			} else {
				String alias = null;
				if (relationship == null) {
					alias = bean.getSimpleName() + "$";
				} else {
					alias = relationship.getBean().getSimpleName() + "$";
				}
				StringBuilder sb = new StringBuilder();
				for (Step s : to) {
					sb.append("LEFT JOIN " + alias + "." + s.relationship.getField().getName() + " AS "
							+ s.relationship.getBean().getSimpleName() + "$");
					sb.append(" " + s.join());
				}
				return sb.toString();
			}
		}
	}

	public static Step fixes(Class<? extends EntityBaseBean> start, Set<Class<? extends EntityBaseBean>> es)
			throws IcatInternalException, BadParameterException {
		Set<Class<? extends EntityBaseBean>> allBeans = new HashSet<Class<? extends EntityBaseBean>>(es);
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
			throw new BadParameterException("Unable to reach " + sb);

		}
		return step;

	}

	private static Set<Step> follow(Class<? extends EntityBaseBean> predecessor, Class<? extends EntityBaseBean> from,
			Set<Class<? extends EntityBaseBean>> allBeans, Set<Class<? extends EntityBaseBean>> used)
			throws IcatInternalException, BadParameterException {
		Set<Step> steps = new HashSet<Step>();
		Set<Relationship> navto = pkHandler.getRelatedEntities(from);
		for (Relationship relationship : navto) {
			Class<? extends EntityBaseBean> bean = relationship.getBean();
			if (used.contains(bean) && !bean.equals(predecessor)) {
				throw new BadParameterException("Can't have loop in graph of entities " + bean
						+ " was encountered twice");
			}
			if (allBeans.contains(bean) && !used.contains(bean)) {
				used.add(bean);
				Step s = new Step(bean, relationship, follow(from, bean, allBeans, used));
				steps.add(s);
			}
		}
		return steps;
	}

}
