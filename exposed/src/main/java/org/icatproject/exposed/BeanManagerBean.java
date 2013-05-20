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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.NotificationMessage;

@Stateless()
@TransactionManagement(TransactionManagementType.BEAN)
public class BeanManagerBean {

	private static final Logger logger = Logger.getLogger(BeanManagerBean.class);

	private Map<String, Authenticator> authPlugins = new HashMap<String, Authenticator>();

	private int lifetimeMinutes;

	@PersistenceContext(unitName = "icat")
	protected EntityManager manager;

	@EJB
	Transmitter transmitter;

	@Resource
	private UserTransaction userTransaction;

	public BeanManagerBean() {
	}

	public long create(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			CreateResponse createResponse = BeanManager.create(userId, bean, manager,
					userTransaction);
			transmitter.processMessage(createResponse.getNotificationMessage());
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
				transmitter.processMessage(createResponse.getNotificationMessage());
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
			transmitter.processMessage(BeanManager.delete(userId, bean, manager, userTransaction));
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
			List<NotificationMessage> nms = BeanManager.deleteMany(userId, beans, manager,
					userTransaction);
			for (NotificationMessage nm : nms) {
				transmitter.processMessage(nm);
			}
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}

	}

	public void dummy(Facility facility) {
		// Do nothing
	}

	public EntityBaseBean get(String sessionId, String query, long primaryKey) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return BeanManager.get(userId, query, primaryKey, manager);
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
			return BeanManager.search(userId, query, manager);
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
			transmitter.processMessage(BeanManager.update(userId, bean, manager, userTransaction));
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	public void refresh(String sessionId) throws IcatException {
		BeanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction);
	}

	public void testCreate(String sessionId, EntityBaseBean bean) throws IcatException {
		String userId = getUserName(sessionId);
		BeanManager.testCreate(userId, bean, manager, userTransaction);
	}

	public void testUpdate(String sessionId, EntityBaseBean bean) throws IcatException {
		String userId = getUserName(sessionId);
		BeanManager.testUpdate(userId, bean, manager, userTransaction);
	}

	public void testDelete(String sessionId, EntityBaseBean bean) throws IcatException {
		String userId = getUserName(sessionId);
		BeanManager.testDelete(userId, bean, manager, userTransaction);
	}

}
