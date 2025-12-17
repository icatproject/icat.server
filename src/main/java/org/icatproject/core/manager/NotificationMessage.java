package org.icatproject.core.manager;

import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.PropertyHandler.Operation;

public class NotificationMessage {

	public class Message {
		private Operation operation;
		private String entityName;
		private Long entityId;

		public Message(Operation operation, String entityName, Long entityId) {
			this.operation = operation;
			this.entityName = entityName;
			this.entityId = entityId;
		}

		public String getEntityName() {
			return entityName;
		}

		public long getEntityId() {
			return entityId;
		}

		public String getOperation() {
			return operation.name();
		}
	}

	private Message message;

	public NotificationMessage(Operation operation, EntityBaseBean bean, Map<String, NotificationRequest> notificationRequests) throws IcatException {
		String entity = bean.getClass().getSimpleName();
		String key = entity + ":" + operation.name();
		NotificationRequest nr = notificationRequests.get(key);
		if (nr != null) {
			message = new Message(operation, entity, bean.getId());
		}
	}

	public Message getMessage() {
		return this.message;
	}

}
