package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;

@DependsOn("LoggingConfigurator")
@Singleton
@Startup
public class GateKeeper {

	public Set<String> getRootSpecials() {
		return rootSpecials;
	}

	public Set<String> getRootUserNames() {
		return rootUserNames;
	}

	@PersistenceContext(unitName = "icat")
	private EntityManager manager;

	@EJB
	PropertyHandler propertyHandler;

	private final Logger logger = Logger.getLogger(GateKeeper.class);

	public static Comparator<String> stringsBySize = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			int l1 = o1.length();
			int l2 = o2.length();
			if (l1 < l2) {
				return -1;
			} else if (l1 == l2) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	private Set<String> publicTables = new ConcurrentSkipListSet<>();
	private Map<String, Set<String>> publicSteps = new ConcurrentSkipListMap<>();

	private Set<String> rootSpecials = new HashSet<String>(Arrays.asList("User", "Grouping",
			"UserGroup", "Rule", "PublicStep"));

	private Set<String> rootUserNames;

	@PostConstruct
	void init() {
		logger.info("Creating GateKeeper singleton");
		rootUserNames = propertyHandler.getRootUserNames();
		updateCache();
		SingletonFinder.setGateKeeper(this);
		logger.info("Created GateKeeper singleton" + rootSpecials);
	}

	/**
	 * Perform authorization check for any object
	 * 
	 * @throws IcatException
	 */
	public void performAuthorisation(String user, EntityBaseBean object, AccessType access,
			EntityManager manager) throws IcatException {

		Class<? extends EntityBaseBean> objectClass = object.getClass();
		String simpleName = objectClass.getSimpleName();
		if (rootUserNames.contains(user)) {
			if (rootSpecials.contains(simpleName)) {
				logger.info("\"Root\" user " + user + " is allowed " + access + " to " + simpleName);
				return;
			}
		}

		String qName = null;
		if (access == AccessType.CREATE) {
			qName = Rule.CREATE_QUERY;
		} else if (access == AccessType.READ) {
			if (publicTables.contains(simpleName)) {
				logger.info("All are allowed " + access + " to " + simpleName);
				return;
			}
			qName = Rule.READ_QUERY;
		} else if (access == AccessType.UPDATE) {
			qName = Rule.UPDATE_QUERY;
		} else if (access == AccessType.DELETE) {
			qName = Rule.DELETE_QUERY;
		} else {
			throw new RuntimeException(access + " is not handled yet");
		}

		TypedQuery<String> query = manager.createNamedQuery(qName, String.class)
				.setParameter("member", user).setParameter("bean", simpleName);

		List<String> restrictions = query.getResultList();
		logger.debug("Got " + restrictions.size() + " authz queries for " + access + " by " + user
				+ " to a " + objectClass.getSimpleName());

		for (String restriction : restrictions) {
			logger.debug("Query: " + restriction);
			if (restriction == null) {
				logger.info("Null restriction => " + access + " permitted to " + simpleName);
				return;
			}
		}

		/*
		 * Sort a copy of the results by string length. It is probably faster to evaluate a shorter
		 * query.
		 */
		List<String> sortedQueries = new ArrayList<String>();
		sortedQueries.addAll(restrictions);
		Collections.sort(sortedQueries, stringsBySize);

		Long keyVal = object.getId();

		for (String qString : sortedQueries) {
			TypedQuery<Long> q = manager.createQuery(qString, Long.class);
			if (qString.contains(":user")) {
				q.setParameter("user", user);
			}
			q.setParameter("pkid", keyVal);
			Long r = q.getSingleResult();
			if (r == 1) {
				logger.info(access + " to " + simpleName + " permitted by " + qString);
				return;
			}
		}
		throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES, access
				+ " access to this " + objectClass.getSimpleName() + " is not allowed.");

	}

	public void updatePublicSteps() {
		List<PublicStep> steps = manager.createNamedQuery(PublicStep.GET_ALL_QUERY,
				PublicStep.class).getResultList();
		publicSteps.clear();
		for (PublicStep step : steps) {
			Set<String> fieldNames = publicSteps.get(step.getOrigin());
			if (fieldNames == null) {
				fieldNames = new ConcurrentSkipListSet<>();
				publicSteps.put(step.getOrigin(), fieldNames);
			}
			fieldNames.add(step.getField());
		}
		logger.debug("There are " + steps.size() + " publicSteps");
	}

	public void updatePublicTables() {
		List<String> tableNames = manager.createNamedQuery(Rule.PUBLIC_QUERY, String.class)
				.getResultList();
		publicTables.clear();
		publicTables.addAll(tableNames);
		logger.debug("There are " + publicTables.size() + " publicTables");
	}

	/** Return true if allowed because destination table is public or because step is public */
	public boolean allowed(Relationship r) {
		String beanName = r.getDestinationBean().getSimpleName();
		if (publicTables.contains(beanName)) {
			return true;
		}
		String originBeanName = r.getOriginBean().getSimpleName();
		Set<String> fieldNames = publicSteps.get(originBeanName);
		if (fieldNames != null && fieldNames.contains(r.getField().getName())) {
			return true;
		}
		return false;
	}

	public void updateCache() {
		updatePublicTables();
		updatePublicSteps();
	}

	public Set<String> getPublicTables() {
		return publicTables;
	}

	public List<EntityBaseBean> getReadable(String userId, List<EntityBaseBean> beans,
			EntityManager manager) {

		if (beans.size() == 0) {
			return beans;
		}

		EntityBaseBean object = beans.get(0);

		Class<? extends EntityBaseBean> objectClass = object.getClass();
		String simpleName = objectClass.getSimpleName();
		if (rootUserNames.contains(userId)) {
			if (rootSpecials.contains(simpleName)) {
				logger.info("\"Root\" user " + userId + " is allowed READ to " + simpleName);
				return beans;
			}
		}

		TypedQuery<String> query = manager.createNamedQuery(Rule.INCLUDE_QUERY, String.class)
				.setParameter("member", userId).setParameter("bean", simpleName);

		List<String> restrictions = query.getResultList();
		logger.debug("Got " + restrictions.size() + " authz queries for READ by " + userId
				+ " to a " + objectClass.getSimpleName());

		for (String restriction : restrictions) {
			logger.debug("Query: " + restriction);
			if (restriction == null) {
				logger.info("Null restriction => READ permitted to " + simpleName);
				return beans;
			}
		}

		/*
		 * Sort a copy of the results by string length. It is probably faster to evaluate a shorter
		 * query.
		 */
		List<String> sortedQueries = new ArrayList<String>();
		sortedQueries.addAll(restrictions);
		Collections.sort(sortedQueries, stringsBySize);

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (EntityBaseBean bean : beans) {
			if (first) {
				sb.append(bean.getId());
				first = false;
			} else {
				sb.append("," + bean.getId());
			}
		}

		Set<Long> ids = new HashSet<>();
		for (String qString : sortedQueries) {
			TypedQuery<Long> q = manager.createQuery(qString.replace(":pkids", sb.toString()),
					Long.class);
			if (qString.contains(":user")) {
				q.setParameter("user", userId);
			}
			ids.addAll(q.getResultList());
		}

		List<EntityBaseBean> results = new ArrayList<>();
		for (EntityBaseBean bean : beans) {
			if (ids.contains(bean.getId())) {
				results.add(bean);
			}
		}
		return results;
	}

	public void checkRule(String query) throws IcatException {
		try {
			manager.createQuery(query);
		} catch (Exception e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}
		
	}

}
