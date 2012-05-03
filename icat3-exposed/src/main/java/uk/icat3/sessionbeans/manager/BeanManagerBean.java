package uk.icat3.sessionbeans.manager;

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

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Facility;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.BeanManager;
import uk.icat3.manager.CreateResponse;
import uk.icat3.manager.EntityInfo;
import uk.icat3.manager.GetResponse;
import uk.icat3.manager.NotificationMessages;
import uk.icat3.manager.SearchResponse;
import uk.icat3.sessionbeans.EJBObject;

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
	public Object create(String sessionId, EntityBaseBean bean) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException,
			ObjectAlreadyExistsException, IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			CreateResponse createResponse = BeanManager.create(userId, bean, manager,
					userTransaction);
			Transmitter.processMessages(createResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return createResponse.getPk();
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ValidationException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ObjectAlreadyExistsException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@Override
	public List<Object> createMany(String sessionId, List<EntityBaseBean> beans)
			throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException,
			ValidationException, ObjectAlreadyExistsException, IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			List<CreateResponse> createResponses = BeanManager.createMany(userId, beans, manager,
					userTransaction);
			List<Object> lo = new ArrayList<Object>();
			for (CreateResponse createResponse : createResponses) {
				Transmitter.processMessages(createResponse.getNotificationMessages(),
						queueConnection, queue, topicConnection, topic);
				lo.add(createResponse.getPk());
			}
			return lo;
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ValidationException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ObjectAlreadyExistsException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@WebMethod()
	public void delete(String sessionId, EntityBaseBean bean) throws SessionException,
			NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException,
			IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			NotificationMessages nms = BeanManager.delete(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ValidationException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@WebMethod()
	public void update(String sessionId, EntityBaseBean bean) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException,
			IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			NotificationMessages nms = BeanManager.update(userId, bean, manager, userTransaction);
			Transmitter.processMessages(nms, queueConnection, queue, topicConnection, topic);
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ValidationException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@Override
	public void dummy(Facility facility) {
		// Do nothing
	}

	@Override
	public EntityBaseBean get(String sessionId, String query, Object primaryKey)
			throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException,
			BadParameterException, IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);

			GetResponse getResponse = BeanManager.get(userId, query, primaryKey, manager);
			Transmitter.processMessages(getResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return getResponse.getBean();
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (BadParameterException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}

	}

	@Override
	public List<?> search(String sessionId, String query) throws SessionException,
			IcatInternalException, BadParameterException, InsufficientPrivilegesException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			SearchResponse searchResponse = BeanManager.search(userId, query, manager);
			Transmitter.processMessages(searchResponse.getNotificationMessages(), queueConnection,
					queue, topicConnection, topic);
			return searchResponse.getList();
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (BadParameterException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@Override
	public EntityInfo getEntityInfo(String beanName) throws BadParameterException,
			IcatInternalException {
		return BeanManager.getEntityInfo(beanName);
	}

}
