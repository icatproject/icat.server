package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jws.WebMethod;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.icatproject.core.IcatException;
import org.icatproject.core.authentication.Authentication;
import org.icatproject.core.authentication.Authenticator;
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
@ExcludeDefaultInterceptors
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

	@WebMethod
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

	@WebMethod()
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

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
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
		logger.info("Loaded log4j properties from : " + log4jFile + " and will watch it.");

		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
			topicConnection = topicConnectionFactory.createTopicConnection();
		} catch (JMSException e) {
			logger.fatal("Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

		File f = new File("icat.properties");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(f));
		} catch (Exception e) {
			logger.fatal("Problem with " + f.getAbsolutePath() + "  " + e.getMessage());
			throw new IllegalStateException(e.getMessage());
		}
		Context ctx = null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			throw new IllegalStateException(e.getClass() + " " + e.getMessage());
		}

		String name = "java:global/authn_db.ear-1.0.0-SNAPSHOT/authn_db.ejb-1.0.0-SNAPSHOT/DB_Authenticator";
		try {
			Authenticator authenticator = (Authenticator) ctx.lookup(name);
			logger.debug("Found Authenticator: " + authenticator.getClass() + " " + authenticator);
		} catch (NamingException e) {
			throw new IllegalStateException(e.getClass() + " " + e.getMessage());
		}
		for (Entry<Object, Object> entry : props.entrySet()) {
			if (((String) entry.getKey()).startsWith("authn")) {
				String[] bits = ((String) entry.getValue()).split("\\s+");
				if (bits.length != 2) {
					String msg = "authn entries must have two values (the mnemonic and the class) : "
							+ entry.getValue() + " was split into:";
					for (String bit : bits) {
						msg = msg + " (" + bit + ")";
					}
					logger.fatal(msg);
					throw new IllegalStateException(msg);
				}
				try {
					Authenticator authenticator = (Authenticator) ctx.lookup(bits[1]);
					logger.debug("Found Authenticator: " + authenticator.getClass() + " "
							+ authenticator);
					authPlugins.put(bits[0], authenticator);
				} catch (NamingException e) {
					throw new IllegalStateException(e.getClass() + " " + e.getMessage());
				}
			}
		}
		String ltm = props.getProperty("lifetimeMinutes");
		if (ltm == null) {
			String msg = "lifetimeMinutes is not set";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

		try {
			lifetimeMinutes = Integer.parseInt(ltm);
		} catch (NumberFormatException e) {
			String msg = "lifetimeMinutes does not represent an integer";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

	}

	@SuppressWarnings("unused")
	@AroundInvoke
	private Object logMethods(InvocationContext ctx) throws IcatException {

		String className = ctx.getTarget().getClass().getName();
		String methodName = ctx.getMethod().getName();
		String target = className + "." + methodName + "()";

		long start = System.currentTimeMillis();

		logger.debug("Invoking " + target);
		try {
			return ctx.proceed();
		} catch (IcatException e) {
			throw e;
		} catch (Exception e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		} finally {
			long time = System.currentTimeMillis() - start;
			logger.debug("Method " + target + " took " + time / 1000f + "s to execute");
		}
	}

	public String login(String plugin, Map<String, String> credentials, HttpServletRequest req)
			throws IcatException {

		Authenticator authenticator = authPlugins.get(plugin);
		if (authenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}

		Authentication authentication = authenticator
				.authenticate(credentials, req.getRemoteAddr());
		logger.debug("About to call the BeanManager");
		return BeanManager.login(authentication, lifetimeMinutes, manager, userTransaction);
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
