package org.icatproject.core.manager;

public class CreateResponse {

	private long pk;
	private NotificationMessages notificationMessage;

	public CreateResponse(long pk, NotificationMessages notificationMessage) {
		this.pk = pk;
		this.notificationMessage = notificationMessage;
	}

	public long getPk() {
		return pk;
	}

	public NotificationMessages getNotificationMessages() {
		return notificationMessage;
	}

}
