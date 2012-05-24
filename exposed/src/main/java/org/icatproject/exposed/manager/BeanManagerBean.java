package org.icatproject.exposed.manager;

import java.util.ArrayList;
import java.util.List;

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
import javax.jws.WebMethod;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.GetResponse;
import org.icatproject.core.manager.NotificationMessages;
import org.icatproject.core.manager.SearchResponse;
import org.icatproject.exposed.EJBObject;

@Stateless()
@TransactionManagement(TransactionManagementType.BEAN)
public class BeanManagerBean extends EJBObject implements BeanManagerLocal {

	static Logger logger = Logger.getLogger(BeanManagerBean.class);

	public BeanManagerBean() {
	}

	@Resource(name = "jms/ICATQueueConnectionFactory")
	private QueueConnectionFactory queueConnectionFactory;

	@Resource(name = "jms/ICATTopicConnectionFactory")
	private TopicConnectionFactory topicConnectionFactory;

	// TODO - this use of mappedName rather than name lags elegance - but it
	// works
	@Resource(mappedName = "jms/ICATQueue")
	private Queue queue;

	@Resource(mappedName = "jms/ICATTopic")
	private Topic topic;

	@Resource
	private UserTransaction userTransaction;

	private QueueConnection queueConnection;
	private TopicConnection topicConnection;

	@SuppressWarnings("unused")
	@PostConstruct()
	private void initBMB() {
		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
			topicConnection = topicConnectionFactory.createTopicConnection();
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
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

	@WebMethod
	public long create(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);
			CreateResponse createResponse = BeanManager.create(userId, bean, manager,
					userTransaction);
			Transmitter.processMessages(createResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return createResponse.getPk();
		} catch (IcatException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public List<Long> createMany(String sessionId, List<EntityBaseBean> beans) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);
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
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod()
	public void delete(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);
			NotificationMessages nms = BeanManager.delete(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (IcatException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod()
	public void update(String sessionId, EntityBaseBean bean) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);
			NotificationMessages nms = BeanManager.update(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (IcatException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public void dummy(Facility facility) {
		// Do nothing
	}

	@Override
	public EntityBaseBean get(String sessionId, String query, long primaryKey) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);

			GetResponse getResponse = BeanManager.get(userId, query, primaryKey, manager);
			Transmitter.processMessages(getResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return getResponse.getBean();
		} catch (IcatException e) {
			logger.debug(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public List<?> search(String sessionId, String query) throws IcatException {
		try {
			String userId = user.getUserName(sessionId);
			SearchResponse searchResponse = BeanManager.search(userId, query, manager);
			Transmitter.processMessages(searchResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return searchResponse.getList();
		} catch (IcatException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@Override
	public EntityInfo getEntityInfo(String beanName) throws IcatException {
		return BeanManager.getEntityInfo(beanName);
	}

}
