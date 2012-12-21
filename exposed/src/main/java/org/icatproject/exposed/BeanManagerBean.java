package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.GetResponse;
import org.icatproject.core.manager.NotificationMessages;
import org.icatproject.core.manager.SearchResponse;

@Stateless()
@TransactionManagement(TransactionManagementType.BEAN)
public class BeanManagerBean {

	static Logger logger = Logger.getLogger(BeanManagerBean.class);

	private Map<String, Authenticator> authPlugins = new HashMap<String, Authenticator>();

	private int lifetimeMinutes;

	@PersistenceContext(unitName = "icat")
	protected EntityManager manager;

	// TODO - this use of mappedName rather than name lags elegance - but it
	// works
	@Resource(mappedName = "jms/ICATQueue")
	private Queue queue;

	private QueueConnection queueConnection;

	@Resource(name = "jms/ICATQueueConnectionFactory")
	private QueueConnectionFactory queueConnectionFactory;

	@Resource(mappedName = "jms/ICATTopic")
	private Topic topic;

	private TopicConnection topicConnection;

	@Resource(name = "jms/ICATTopicConnectionFactory")
	private TopicConnectionFactory topicConnectionFactory;

	@Resource
	private UserTransaction userTransaction;

	public BeanManagerBean() {
	}

	public long create(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			CreateResponse createResponse = BeanManager.create(userId, bean, manager,
					userTransaction);
			Transmitter.processMessages(createResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return createResponse.getPk();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public List<Long> createMany(String sessionId, List<EntityBaseBean> beans) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			List<CreateResponse> createResponses = BeanManager.createMany(userId, beans, manager,
					userTransaction);
			List<Long> lo = new ArrayList<Long>();
			for (CreateResponse createResponse : createResponses) {
				Transmitter.processMessages(createResponse.getNotificationMessages(),
						queueConnection, queue, topicConnection, topic);
				lo.add(createResponse.getPk());
			}
			return lo;
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void delete(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			NotificationMessages nms = BeanManager.delete(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void deleteMany(String sessionId, List<EntityBaseBean> beans) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			List<NotificationMessages> nms = BeanManager.deleteMany(userId, beans, manager,
					userTransaction);
			for (NotificationMessages nm : nms) {
				Transmitter.processMessages(nm, queueConnection, queue, topicConnection, topic);
			}
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}

	}

	@PreDestroy()
	private void destroyBMB() {
		try {
			queueConnection.close();
			queueConnection = null;
			topicConnection.close();
			topicConnection = null;
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void dummy(Facility facility) {
		// Do nothing
	}

	public EntityBaseBean get(String sessionId, String query, long primaryKey) throws IcatException {
		try {
			String userId = getUserName(sessionId);

			GetResponse getResponse = BeanManager.get(userId, query, primaryKey, manager);
			Transmitter.processMessages(getResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return getResponse.getBean();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public EntityInfo getEntityInfo(String beanName) throws IcatException {
		return BeanManager.getEntityInfo(beanName);
	}

	public double getRemainingMinutes(String sessionId) throws IcatException {
		return BeanManager.getRemainingMinutes(sessionId, manager);
	}

	public String getUserName(String sessionId) throws IcatException {
		return BeanManager.getUserName(sessionId, manager);
	}

	@PostConstruct()
	private void init() {
		/*
		 * Set up log4j. Note that even if the requested log4j.properties file is not found or is
		 * corrupt log4j will do its best to produce some output. The file will be checked for
		 * changes every minute. Existing properties will NOT be removed - so to reduce logging you
		 * may need to specify a logging level of INHERIT to take values from further up the tree.
		 */
		String log4jFile = "log4j.properties";
		LogManager.resetConfiguration();
		PropertyConfigurator.configureAndWatch(log4jFile);
		logger = Logger.getLogger(BeanManagerBean.class);
		logger.info("BeanManagerBean post construct method called");
		logger.info("Loaded log4j properties from : " + log4jFile + " and will watch it.");

		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
			topicConnection = topicConnectionFactory.createTopicConnection();
		} catch (JMSException e) {
			logger.fatal("Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

		PropertyHandler p = PropertyHandler.getInstance();
		authPlugins = p.getAuthPlugins();
		lifetimeMinutes = p.getLifetimeMinutes();
	}

	public String login(String plugin, Map<String, String> credentials, HttpServletRequest req)
			throws IcatException {

		Authenticator authenticator = authPlugins.get(plugin);
		if (authenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}

		logger.debug("Using " + plugin + " to authenticate");
		String userName = authenticator.authenticate(credentials, req.getRemoteAddr())
				.getUserName();
		return BeanManager.login(userName, lifetimeMinutes, manager, userTransaction);
	}

	public void logout(String sessionId) throws IcatException {
		BeanManager.logout(sessionId, manager, userTransaction);
	}

	private void reportIcatException(IcatException e) throws IcatException {
		logger.debug("IcatException " + e.getType() + " " + e.getMessage()
				+ (e.getOffset() >= 0 ? " at offset " + e.getOffset() : ""));
	}

	private void reportThrowable(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(baos);
		e.printStackTrace(s);
		s.close();
		logger.error("Unexpected failure in Java "
				+ System.getProperties().getProperty("java.version") + " " + baos);
	}

	public List<?> search(String sessionId, String query) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			SearchResponse searchResponse = BeanManager.search(userId, query, manager);
			Transmitter.processMessages(searchResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return searchResponse.getList();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void update(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			NotificationMessages nms = BeanManager.update(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

}
