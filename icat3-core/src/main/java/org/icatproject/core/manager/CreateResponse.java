package org.icatproject.core.manager;

public class CreateResponse {

	private Object pk;
	private NotificationMessages notificationMessage;

	public CreateResponse(Object pk, NotificationMessages notificationMessage) {
		this.pk = pk;
		this.notificationMessage = notificationMessage;
	}

	public Object getPk() {
		return pk;
	}

	public NotificationMessages getNotificationMessages() {
		return notificationMessage;
	}

}
