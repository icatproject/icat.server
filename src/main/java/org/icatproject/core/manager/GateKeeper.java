package org.icatproject.core.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Singleton
@Startup
public class GateKeeper {

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

	private final Logger logger = LoggerFactory.getLogger(GateKeeper.class);
	Marker fatal = MarkerFactory.getMarker("FATAL");

	private int maxIdsInQuery;

	@EJB
	GateKeeperHelper gateKeeperHelper;

	@EJB
	PropertyHandler propertyHandler;

	private Map<String, Set<String>> publicSteps;

	private Set<String> publicTables;

	private Set<String> rootUserNames;

	private boolean publicStepsStale;

	private boolean publicTablesStale;

	private boolean publicSearchFieldsStale;


	/**
	 * Return true if allowed because destination table is public or because
	 * step is public
	 */
	public boolean allowed(Relationship r) {
		String beanName = r.getDestinationBean().getSimpleName();
		if (getPublicTables().contains(beanName)) {
			return true;
		}

		String originBeanName = r.getOriginBean().getSimpleName();
		Set<String> fieldNames = getPublicSteps().get(originBeanName);
		if (fieldNames != null && fieldNames.contains(r.getField().getName())) {
			return true;
		}

		return false;
	}

	private Map<String, Set<String>> getPublicSteps() {
		if (publicStepsStale) {
			updatePublicSteps();
		}
		return publicSteps;
	}

	public Set<String> getPublicTables() {
		if (publicTablesStale) {
			updatePublicTables();
		}
		return publicTables;
	}

	public Boolean getPublicSearchFieldsStale() {
		return publicSearchFieldsStale;
	}

	/**
	 * Gets READ restrictions that apply to entities of type simpleName, that are
	 * relevant for the given userId. If userId belongs to a root user, or one of
	 * the restrictions is itself null, then null is returned. This corresponds to a
	 * case where the user can READ any entity of type simpleName.
	 * 
	 * @param userId     The user making the READ request.
	 * @param simpleName The name of the requested entity type.
	 * @param manager    The EntityManager to use.
	 * @return Returns a list of restrictions that apply to the requested entity
	 *         type. If there are no restrictions, then returns null.
	 */
	private List<String> getRestrictions(String userId, String simpleName, EntityManager manager) {
		if (rootUserNames.contains(userId)) {
			logger.info("\"Root\" user " + userId + " is allowed READ to " + simpleName);
			return null;
		}

		List<String> restrictions = gateKeeperHelper.getRules(Rule.INCLUDE_QUERY, userId, simpleName);
		logger.debug("Got " + restrictions.size() + " authz queries for READ by " + userId + " to a "
				+ simpleName);

		for (String restriction : restrictions) {
			logger.debug("Query: " + restriction);
			if (restriction == null) {
				logger.info("Null restriction => READ permitted to " + simpleName);
				return null;
			}
		}

		return restrictions;
	}

	/**
	 * Returns a sub list of the passed entities that the user has READ access to.
	 * Note that this method accepts and returns instances of EntityBaseBean, unlike
	 * getReadableIds.
	 * 
	 * @param userId  The user making the READ request.
	 * @param beans   The entities the user wants to READ.
	 * @param manager The EntityManager to use.
	 * @return A list of entities the user has read access to
	 */
	public List<EntityBaseBean> getReadable(String userId, List<EntityBaseBean> beans, EntityManager manager) {

		if (beans.size() == 0) {
			return beans;
		}
		EntityBaseBean object = beans.get(0);
		Class<? extends EntityBaseBean> objectClass = object.getClass();
		String simpleName = objectClass.getSimpleName();

		List<String> restrictions = getRestrictions(userId, simpleName, manager);
		if (restrictions == null) {
			return beans;
		}

		Set<Long> readableIds = getReadableIds(userId, beans, restrictions, manager);

		List<EntityBaseBean> results = new ArrayList<>();
		for (EntityBaseBean bean : beans) {
			if (readableIds.contains(bean.getId())) {
				results.add(bean);
			}
		}
		return results;
	}

	/**
	 * Returns a set of ids that indicate entities of type simpleName that the user
	 * has READ access to. If all of the entities can be READ (restrictions are
	 * null) then null is returned. Note that while this accepts anything that
	 * HasEntityId, the ids are returned as a Set<Long> unlike getReadable.
	 * 
	 * @param userId     The user making the READ request.
	 * @param entities   The entities to check.
	 * @param simpleName The name of the requested entity type.
	 * @param manager    The EntityManager to use.
	 * @return Set of the ids that the user has read access to. If there are no
	 *         restrictions, then returns null.
	 */
	public Set<Long> getReadableIds(String userId, List<? extends HasEntityId> entities, String simpleName,
			EntityManager manager) {

		if (entities.size() == 0) {
			return null;
		}

		List<String> restrictions = getRestrictions(userId, simpleName, manager);
		if (restrictions == null) {
			return null;
		}

		return getReadableIds(userId, entities, restrictions, manager);
	}

	/**
	 * Returns a set of ids that indicate entities that the user has READ access to.
	 * 
	 * @param userId       The user making the READ request.
	 * @param entities     The entities to check.
	 * @param restrictions The restrictions applying to the entities.
	 * @param manager      The EntityManager to use.
	 * @return Set of the ids that the user has read access to.
	 */
	private Set<Long> getReadableIds(String userId, List<? extends HasEntityId> entities, List<String> restrictions,
			EntityManager manager) {

		/*
		 * IDs are processed in batches to avoid Oracle error: ORA-01795:
		 * maximum number of expressions in a list is 1000
		 */

		List<String> idLists = new ArrayList<>();
		StringBuilder sb = null;

		int i = 0;
		for (HasEntityId entity : entities) {
			if (i == 0) {
				sb = new StringBuilder();
				sb.append(entity.getId());
				i = 1;
			} else {
				sb.append("," + entity.getId());
				i++;
			}
			if (i == maxIdsInQuery) {
				i = 0;
				idLists.add(sb.toString());
				sb = null;
			}
		}
		if (sb != null) {
			idLists.add(sb.toString());
		}

		logger.debug("Check readability of " + entities.size() + " beans has been divided into " + idLists.size()
				+ " queries.");

		Set<Long> readableIds = new HashSet<>();
		for (String idList : idLists) {
			for (String qString : restrictions) {
				TypedQuery<Long> q = manager.createQuery(qString.replace(":pkids", idList), Long.class);
				if (qString.contains(":user")) {
					q.setParameter("user", userId);
				}
				readableIds.addAll(q.getResultList());
			}
		}

		return readableIds;
	}

	public Set<String> getRootUserNames() {
		return rootUserNames;
	}

	@PostConstruct
	private void init() {
		logger.info("Creating GateKeeper singleton");
		maxIdsInQuery = propertyHandler.getMaxIdsInQuery();
		rootUserNames = propertyHandler.getRootUserNames();

		updatePublicTables();
		updatePublicSteps();

		logger.info("Created GateKeeper singleton");
	}

	/**
	 * Is the operation allowed
	 */
	public boolean isAccessAllowed(String user, EntityBaseBean object, AccessType access, EntityManager manager) {

		Class<? extends EntityBaseBean> objectClass = object.getClass();
		String simpleName = objectClass.getSimpleName();

		if (rootUserNames.contains(user)) {
			logger.info("\"Root\" user " + user + " is allowed " + access + " to " + simpleName);
			return true;
		}

		String qName = null;
		if (access == AccessType.CREATE) {
			qName = Rule.CREATE_QUERY;
		} else if (access == AccessType.READ) {
			if (getPublicTables().contains(simpleName)) { // TODO see other comment on publicTables vs getPublicTables
				logger.info("All are allowed " + access + " to " + simpleName);
				return true;
			}
			qName = Rule.READ_QUERY;
		} else if (access == AccessType.UPDATE) {
			qName = Rule.UPDATE_QUERY;
		} else if (access == AccessType.DELETE) {
			qName = Rule.DELETE_QUERY;
		} else {
			throw new RuntimeException(access + " is not handled yet");
		}

		logger.debug("Checking " + qName + " " + user + " " + simpleName);
		List<String> restrictions = gateKeeperHelper.getRules(qName, user, simpleName);
		logger.debug(
				"Got " + restrictions.size() + " authz queries for " + access + " by " + user + " to a " + simpleName);

		for (String restriction : restrictions) {
			logger.debug("Query: " + restriction);
			if (restriction == null) {
				logger.info("Null restriction => " + access + " permitted to " + simpleName);
				return true;
			}
		}

		/*
		 * Sort a copy of the results by string length. It is probably faster to
		 * evaluate a shorter query.
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
			if (q.getSingleResult() > 0) {
				logger.info(access + " to " + simpleName + " permitted by " + qString);
				return true;
			}
		}
		return false;
	}

	public void markPublicStepsStale() {
		publicStepsStale = true;
		publicSearchFieldsStale = true;
	}

	public void markPublicTablesStale() {
		publicTablesStale = true;
		publicSearchFieldsStale = true;
	}

	public void markPublicSearchFieldsFresh() {
		publicSearchFieldsStale = false;
	}

	/**
	 * Perform authorization check for any object
	 * 
	 * @throws IcatException
	 */
	public void performAuthorisation(String user, EntityBaseBean object, AccessType access, EntityManager manager)
			throws IcatException {

		if (!isAccessAllowed(user, object, access, manager)) {
			throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES,
					access + " access to this " + object.getClass().getSimpleName() + " is not allowed.");
		}
	}

	public void performUpdateAuthorisation(String user, EntityBaseBean bean, JsonObject contents, EntityManager manager)
			throws IcatException {
		if (isAccessAllowed(user, bean, AccessType.UPDATE, manager)) {
			return;
		}

		// Now see if all the updated attributes are allowed individually
		logger.info("Consider {}", contents);
		Class<? extends EntityBaseBean> klass = bean.getClass();
		String simpleName = klass.getSimpleName();
		Set<Field> updaters = EntityInfoHandler.getSettersForUpdate(klass).keySet();
		Map<String, Field> fieldsByName = EntityInfoHandler.getFieldsByName(klass);

		for (Entry<String, JsonValue> fentry : contents.entrySet()) {
			String fName = fentry.getKey();
			if (!fName.equals("id")) {
				Field field = fieldsByName.get(fName);
				if (updaters.contains(field)) {
					String qName = Rule.UPDATE_ATTRIBUTE_QUERY;
					logger.debug("Checking " + qName + " " + user + " " + simpleName + "." + fName);
					List<String> restrictions = gateKeeperHelper.getRules(qName, user, simpleName, fName);
					logger.debug("Got " + restrictions.size() + " authz queries for UPDATE by " + user + " to a "
							+ simpleName + "." + fName);
					boolean ok = false;
					for (String restriction : restrictions) {
						if (restriction == null) {
							logger.info("Null restriction => UPDATE permitted to " + simpleName + "." + fName);
							ok = true;
						}
					}
					if (!ok) {
						/*
						 * Sort a copy of the results by string length. It is
						 * probably faster to evaluate a shorter query.
						 */
						List<String> sortedQueries = new ArrayList<String>();
						sortedQueries.addAll(restrictions);
						Collections.sort(sortedQueries, stringsBySize);

						Long keyVal = bean.getId();

						for (String qString : sortedQueries) {
							TypedQuery<Long> q = manager.createQuery(qString, Long.class);
							if (qString.contains(":user")) {
								q.setParameter("user", user);
							}
							q.setParameter("pkid", keyVal);
							if (q.getSingleResult() > 0) {
								logger.info("UPDATE to " + simpleName + "." + fName + " permitted by " + qString);
								ok = true;
								break;
							}
						}
					}
					if (!ok) {
						throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES,
								"UPDATE access to this " + bean.getClass().getSimpleName() + " is not allowed.");
					}
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES,
							"UPDATE access to this " + bean.getClass().getSimpleName() + " is not allowed.");
				}
			}

		}

	}

	/** Do it locally and send requests to other icats */
	public void requestUpdatePublicSteps() {
		markPublicStepsStale();
	}

	/** Do it locally and send requests to other icats */
	public void requestUpdatePublicTables() {
		markPublicTablesStale();
	}

	public void updatePublicSteps() {
		publicSteps = gateKeeperHelper.getPublicSteps();
		publicStepsStale = false;
		logger.debug("There are " + publicSteps.size() + " publicSteps: " + publicSteps.toString());
	}

	public void updatePublicTables() {
		publicTables = gateKeeperHelper.getPublicTables();
		publicTablesStale = false;
		logger.debug("There are " + publicTables.size() + " publicTables: " + publicTables.toString());
	}
}
