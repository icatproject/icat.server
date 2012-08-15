package org.icatproject.core.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Rule;


public class GateKeeper {

	private final static Logger logger = Logger.getLogger(GateKeeper.class);
	private final static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();
	private final static Set<String> rootUserNames = PropertyHandler.getInstance().getRootUserNames();

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

	public static Set<String> rootSpecials = new HashSet<String>(Arrays.asList("User", "Group",
			"UserGroup", "Rule"));

	/**
	 * Perform authorization check for any object
	 * 
	 * @throws IcatException
	 */
	public static void performAuthorisation(String user, EntityBaseBean object, AccessType access,
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
		} else if (access == AccessType.READ || access == AccessType.DOWNLOAD) {
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
				logger.info("Null restriction => Operation permitted");
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

		Field key = pkHandler.getKeyFor(objectClass);
		Method m = pkHandler.getGetters(objectClass).get(key);
		Object keyVal = null;
		try {
			keyVal = m.invoke(object);
		} catch (Exception e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
		for (String qString : sortedQueries) {
			TypedQuery<Long> q = manager.createQuery(qString, Long.class);
			if (qString.contains(":user")) {
				q.setParameter("user", user);
			}
			q.setParameter("pkid", keyVal);
			Long r = q.getSingleResult();
			if (r == 1) {
				logger.info("Operation permitted by " + qString);
				return;
			} else {
				logger.info("Operation denied by " + qString);
			}
		}
		throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES, access
				+ " access to this " + objectClass.getSimpleName() + " is not allowed.");

	}

}
