package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.PropertyHandler.Entity;
import org.icatproject.core.PropertyHandler.Operation;
import org.icatproject.core.entity.EntityBaseBean;

public class NotificationMessages {

	public class Message {
		private Operation operation;
		private Entity entityName;
		private Long entityId;

		public Message(Operation operation, Entity entityName, Long entityId) {
			this.operation = operation;
			this.entityName = entityName;
			this.entityId = entityId;
		}

		public String getEntityName() {
			return entityName.name();
		}

		public long getEntityId() {
			return entityId;
		}

		public String getOperation() {
			return operation.name();
		}
	}

	private final static EntityInfoHandler entityInfoHandler = EntityInfoHandler.getInstance();
	private final static Logger logger = Logger.getLogger(NotificationMessages.class);

	private final static List<NotificationRequest> notificationRequests = PropertyHandler
			.getInstance().getNotificationRequests();

	public static EntityInfoHandler getEntityinfohandler() {
		return entityInfoHandler;
	}

	public static Logger getLogger() {
		return logger;
	}

	private final List<Message> messages = new ArrayList<Message>();

	public NotificationMessages(Operation operation, EntityBaseBean bean, EntityManager manager)
			throws IcatException {

		/* Only go in here for datafile and dataset operations - to save time */
		String beanClassName = bean.getClass().getSimpleName();
		if (beanClassName.equals("Datafile") || beanClassName.equals("Dataset")) {
			for (NotificationRequest nr : notificationRequests) {
				Entity nrentity = nr.getEntity();
				if (nrentity.name().equals(beanClassName.toUpperCase())) {
					Operation nroperation = nr.getOperation();
					if (nroperation == operation) {
						this.messages.add(new Message(nroperation, nrentity, bean.getId()));
					}
				}
			}
		}
	}

	public List<Message> getMessages() {
		return this.messages;
	}

}
