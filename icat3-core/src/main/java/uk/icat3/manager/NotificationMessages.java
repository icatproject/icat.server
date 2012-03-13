package uk.icat3.manager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.NotificationRequest;
import uk.icat3.entity.NotificationRequest.DestType;
import uk.icat3.exceptions.IcatInternalException;

public class NotificationMessages {

	public class Message {
		private DestType destType;

		public String userId;

		public String notificationName;

		public String entityName;

		public Map<String, Serializable> pk;;

		public String args;

		public String getArgs() {
			return this.args;
		}

		public DestType getDestType() {
			return this.destType;
		}

		public String getEntityName() {
			return this.entityName;
		}

		public String getNotificationName() {
			return this.notificationName;
		}

		public Map<String, Serializable> getPk() {
			return this.pk;
		}

		public String getUserId() {
			return this.userId;
		}

	}

	private final static Logger logger = Logger.getLogger(NotificationMessages.class);
	private final static EntityInfoHandler entityInfoHandler = EntityInfoHandler.getInstance();

	public static EntityInfoHandler getEntityinfohandler() {
		return entityInfoHandler;
	}

	public static Logger getLogger() {
		return logger;
	}

	private final List<Message> messages = new ArrayList<Message>();

	public NotificationMessages(String userId, EntityBaseBean bean, uk.icat3.manager.AccessType accessType,
			EntityManager manager) throws IcatInternalException {
		String qName = null;
		if (accessType == AccessType.CREATE) {
			qName = NotificationRequest.CREATE_QUERY;
		} else if (accessType == AccessType.READ || accessType == AccessType.DOWNLOAD) {
			qName = NotificationRequest.READ_QUERY;
		} else if (accessType == AccessType.UPDATE) {
			qName = NotificationRequest.UPDATE_QUERY;
		} else if (accessType == AccessType.DELETE) {
			qName = NotificationRequest.DELETE_QUERY;
		} else {
			throw new RuntimeException(accessType + " is not handled yet");
		}
		final Class<? extends EntityBaseBean> objectClass = bean.getClass();

		final TypedQuery<NotificationRequest> query = manager.createNamedQuery(qName, NotificationRequest.class)
				.setParameter("bean", objectClass.getSimpleName());

		for (final NotificationRequest nr : query.getResultList()) {
			final String jpql = nr.getCrudJPQL();
			if (jpql != null) {
				final TypedQuery<Long> q = manager.createQuery(jpql, Long.class);
				if (jpql.contains(":user")) {
					q.setParameter("user", userId);
				}
				final Field key = entityInfoHandler.getKeyFor(objectClass);
				final Method m = entityInfoHandler.getGetters(objectClass).get(key);

				Object keyVal = null;
				try {
					keyVal = m.invoke(bean);
				} catch (final Exception e) {
					throw new IcatInternalException(e.getMessage());
				}
				q.setParameter("pkid", keyVal);

				if (q.getSingleResult() != 1) {
					continue;
				}
			}
			this.generateMessage(nr, userId, bean, null);

		}
	}

	public NotificationMessages(String userId, int size, Class<? extends EntityBaseBean> beanClass, String queryString,
			EntityManager manager) throws IcatInternalException {

		String beanClassName = beanClass.getSimpleName();
		final TypedQuery<NotificationRequest> query = manager.createNamedQuery(NotificationRequest.SEARCH_QUERY,
				NotificationRequest.class).setParameter("bean", beanClassName);

		for (final NotificationRequest nr : query.getResultList()) {
			this.generateMessage(nr, userId, null, queryString);
		}

	}

	private void generateMessage(NotificationRequest nr, String userId, EntityBaseBean bean, String queryString)
			throws IcatInternalException {
		logger.info("Notification required by " + nr.getWhat());

		final Message message = new Message();
		this.messages.add(message);
		message.destType = nr.getDestType();

		if (nr.isNotificationNameWanted()) {
			message.notificationName = nr.getName();
		}
		if (nr.isUseridWanted()) {
			message.userId = userId;
		}
		if (nr.isEntityNameWanted()) {
			message.entityName = nr.getBean();
		}

		if (nr.isKeyWanted() && bean != null) {
			message.pk = new HashMap<String, Serializable>();
			Class<? extends EntityBaseBean> beanClass = bean.getClass();
			final Field key = entityInfoHandler.getKeyFor(beanClass);
			final Method m = entityInfoHandler.getGetters(beanClass).get(key);
			Object keyVal = null;
			try {
				keyVal = m.invoke(bean);
			} catch (final Exception e) {
				throw new IcatInternalException(e.getMessage());
			}
			message.pk.put(key.getName(), (Serializable) keyVal);
		}
		if (nr.isArgsWanted() && queryString != null) {
			message.args = queryString;
		}

	}

	public List<Message> getMessages() {
		return this.messages;
	}

}
