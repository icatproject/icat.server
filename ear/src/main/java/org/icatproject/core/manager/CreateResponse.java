package org.icatproject.core.manager;

public class CreateResponse {

	private long pk;
	private NotificationMessage notificationMessage;

	public CreateResponse(long pk, NotificationMessage notificationMessage) {
		this.pk = pk;
		this.notificationMessage = notificationMessage;
	}

	public long getPk() {
		return pk;
	}

	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

}
