package uk.icat3.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Rule;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.AccessType;

public class GateKeeper {

	static Logger logger = Logger.getLogger(GateKeeper.class);
	static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();

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

	/**
	 * Perform authorization check for any object
	 */
	public static void performAuthorisation(String user, EntityBaseBean object, AccessType access, EntityManager manager)
			throws InsufficientPrivilegesException {
		try {
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
			Class<? extends EntityBaseBean> objectClass = object.getClass();
		

			TypedQuery<String> query = manager.createNamedQuery(qName, String.class).setParameter("member", user)
					.setParameter("what", objectClass.getSimpleName());
			
			List<String> restrictions = query.getResultList();
				logger.debug("Got " + restrictions.size() + " authz queries for " + access + " by " + user + " to a " + objectClass.getSimpleName());

			for (String restriction : restrictions) {
				logger.debug("Query: " + restriction);
				if (restriction == null) {
					logger.info("Null restriction => Operation permitted");
					return;
				}
			}

			/*
			 * Sort a copy of the results by string length. It is probably faster to evaluate a
			 * shorter query.
			 */
			List<String> sortedQueries = new ArrayList<String>();
			sortedQueries.addAll(query.getResultList());
			Collections.sort(sortedQueries, stringsBySize);

			for (String qString : sortedQueries) {
				TypedQuery<Long> q = manager.createQuery(qString, Long.class);
				if (qString.contains(":user")) {
					q.setParameter("user", user);
				}
				List<String> keys = pkHandler.getKeysFor(objectClass);
				if (keys.size() == 1) {
					Method m = null;
					try {
						m = objectClass.getDeclaredMethod(keys.get(0));
					} catch (NoSuchMethodException e) {
						throw new IcatInternalException(e.getMessage());
					}
					Object keyVal = null;
					try {
						keyVal = m.invoke(object);
					} catch (Exception e) {
						throw new IcatInternalException(e.getMessage());
					}
					q.setParameter("pkid", keyVal);
				} else {
					// Is > 1
					Object startObj = object;
					int n = 0;
					boolean first = true;
					for (String key : keys) {
						Method m = null;
						try {
							m = objectClass.getDeclaredMethod(key);
						} catch (NoSuchMethodException e) {
							throw new IcatInternalException(e.getMessage());
						}
						Object keyVal = null;
						try {
							keyVal = m.invoke(startObj);
						} catch (Exception e) {
							throw new IcatInternalException(e.getMessage());
						}
						if (first) {
							first = false;
							startObj = keyVal;
						} else {
							q.setParameter("pkid" + n++, keyVal);
						}
					}

				}
				Long r = q.getSingleResult();
				if (r == 1) {
					logger.info("Operation permitted by " + qString);
					return;
				} else {
					logger.info("Operation denied by " + qString);
				}
			}
			throw new InsufficientPrivilegesException(access + " access to this " + objectClass.getSimpleName() + " is not allowed.");
		} catch (IcatInternalException e) {
			// TODO remove this try ... catch block
			throw new InsufficientPrivilegesException(e.getMessage());
		}
	}

}
