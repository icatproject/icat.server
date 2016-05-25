package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@DependsOn("LoggingConfigurator")
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

	private final static Pattern tsRegExp = Pattern
			.compile("\\{\\s*ts\\s+\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\s*\\}");

	private TopicConnection topicConnection;

	private Topic topic;

	@PersistenceContext(unitName = "icat")
	private EntityManager gateKeeperManager;
	private final Logger logger = LoggerFactory.getLogger(GateKeeper.class);
	Marker fatal = MarkerFactory.getMarker("FATAL");

	private int maxIdsInQuery;

	@EJB
	PropertyHandler propertyHandler;

	private Map<String, Set<String>> publicSteps = new ConcurrentSkipListMap<>();

	private Set<String> publicTables = new ConcurrentSkipListSet<>();

	private Set<String> rootUserNames;

	private boolean stalePublicSteps;

	private boolean stalePublicTables;

	/**
	 * Return true if allowed because destination table is public or because
	 * step is public
	 */
	public boolean allowed(Relationship r) {
		String beanName = r.getDestinationBean().getSimpleName();
		if (publicTables.contains(beanName)) {
			return true;
		}
		String originBeanName = r.getOriginBean().getSimpleName();
		if (stalePublicSteps) {
			updatePublicSteps();
		}
		Set<String> fieldNames = publicSteps.get(originBeanName);
		if (fieldNames != null && fieldNames.contains(r.getField().getName())) {
			return true;
		}
		return false;
	}

	public void checkJPQL(String query) throws IcatException {

		Matcher m = tsRegExp.matcher(query);

		query = m.replaceAll(" CURRENT_TIMESTAMP ");
		try {
			gateKeeperManager.createQuery(query);
		} catch (Exception e) {
			m.reset();
			if (m.find()) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Timestamp literals have been replaced... " + e.getMessage());
			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}

		}

	}

	@PreDestroy()
	private void exit() {
		try {
			if (topicConnection != null) {
				topicConnection.close();
			}
			logger.info("GateKeeper closing down");
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public Set<String> getPublicTables() {
		if (stalePublicTables) {
			updatePublicTables();
		}
		return publicTables;
	}

	public List<EntityBaseBean> getReadable(String userId, List<EntityBaseBean> beans, EntityManager manager) {

		if (beans.size() == 0) {
			return beans;
		}

		EntityBaseBean object = beans.get(0);

		Class<? extends EntityBaseBean> objectClass = object.getClass();
		String simpleName = objectClass.getSimpleName();
		if (rootUserNames.contains(userId)) {
			logger.info("\"Root\" user " + userId + " is allowed READ to " + simpleName);
			return beans;
		}

		TypedQuery<String> query = manager.createNamedQuery(Rule.INCLUDE_QUERY, String.class)
				.setParameter("member", userId).setParameter("bean", simpleName);

		List<String> restrictions = query.getResultList();
		logger.debug("Got " + restrictions.size() + " authz queries for READ by " + userId + " to a "
				+ objectClass.getSimpleName());

		for (String restriction : restrictions) {
			logger.debug("Query: " + restriction);
			if (restriction == null) {
				logger.info("Null restriction => READ permitted to " + simpleName);
				return beans;
			}
		}

		/*
		 * IDs are processed in batches to avoid Oracle error: ORA-01795:
		 * maximum number of expressions in a list is 1000
		 */

		List<String> idLists = new ArrayList<>();
		StringBuilder sb = null;

		int i = 0;
		for (EntityBaseBean bean : beans) {
			if (i == 0) {
				sb = new StringBuilder();
				sb.append(bean.getId());
				i = 1;
			} else {
				sb.append("," + bean.getId());
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

		logger.debug("Check readability of " + beans.size() + " beans has been divided into " + idLists.size()
				+ " queries.");

		Set<Long> ids = new HashSet<>();
		for (String idList : idLists) {
			for (String qString : restrictions) {
				TypedQuery<Long> q = manager.createQuery(qString.replace(":pkids", idList), Long.class);
				if (qString.contains(":user")) {
					q.setParameter("user", userId);
				}
				ids.addAll(q.getResultList());
			}
		}

		List<EntityBaseBean> results = new ArrayList<>();
		for (EntityBaseBean bean : beans) {
			if (ids.contains(bean.getId())) {
				results.add(bean);
			}
		}
		return results;
	}

	public Set<String> getRootUserNames() {
		return rootUserNames;
	}

	@PostConstruct
	private void init() {
		logger.info("Creating GateKeeper singleton");
		maxIdsInQuery = propertyHandler.getMaxIdsInQuery();
		rootUserNames = propertyHandler.getRootUserNames();

		SingletonFinder.setGateKeeper(this);

		try {
			InitialContext ic = new InitialContext();
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ic
					.lookup(propertyHandler.getJmsTopicConnectionFactory());
			topicConnection = topicConnectionFactory.createTopicConnection();
			topic = (Topic) ic.lookup("jms/ICAT/Synch");
		} catch (JMSException | NamingException e) {
			logger.error(fatal, "Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

		updatePublicTables();
		updatePublicSteps();

		logger.info("Created GateKeeper singleton");
	}

	public void markStalePublicSteps() {
		stalePublicSteps = true;

	}

	public void markStalePublicTables() {
		stalePublicTables = true;

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
			if (publicTables.contains(simpleName)) {
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
		TypedQuery<String> query = manager.createNamedQuery(qName, String.class).setParameter("member", user)
				.setParameter("bean", simpleName);
		List<String> restrictions = query.getResultList();
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

	public void requestUpdatePublicSteps() throws JMSException {
		Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer jmsProducer = session.createProducer(topic);
		TextMessage jmsg = session.createTextMessage("updatePublicSteps");
		logger.debug("Sending jms message: updatePublicSteps");
		jmsProducer.send(jmsg);
		session.close();
	}

	public void requestUpdatePublicTables() throws JMSException {
		Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer jmsProducer = session.createProducer(topic);
		TextMessage jmsg = session.createTextMessage("updatePublicTables");
		logger.debug("Sending jms message: updatePublicTables");
		jmsProducer.send(jmsg);
		session.close();
	}

	public void updateCache() throws JMSException {
		requestUpdatePublicTables();
		requestUpdatePublicSteps();
	}

	public void updatePublicSteps() {
		List<PublicStep> steps = gateKeeperManager.createNamedQuery(PublicStep.GET_ALL_QUERY, PublicStep.class)
				.getResultList();
		publicSteps.clear();
		for (PublicStep step : steps) {
			Set<String> fieldNames = publicSteps.get(step.getOrigin());
			if (fieldNames == null) {
				fieldNames = new ConcurrentSkipListSet<>();
				publicSteps.put(step.getOrigin(), fieldNames);
			}
			fieldNames.add(step.getField());
		}
		stalePublicSteps = false;
		logger.debug("There are " + steps.size() + " publicSteps");
	}

	public void updatePublicTables() {
		try {
			List<String> tableNames = gateKeeperManager.createNamedQuery(Rule.PUBLIC_QUERY, String.class)
					.getResultList();
			publicTables.clear();
			publicTables.addAll(tableNames);
			stalePublicTables = false;
			logger.debug("There are " + publicTables.size() + " publicTables");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unexpected exception", e);
		}
	}

}
