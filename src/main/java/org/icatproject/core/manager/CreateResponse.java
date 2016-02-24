package org.icatproject.core.manager;

public class CreateResponse {

	private Long pk;
	private NotificationMessage notificationMessage;

	public CreateResponse(Long pk, NotificationMessage notificationMessage) {
		this.pk = pk;
		this.notificationMessage = notificationMessage;
	}

	public Long getPk() {
		return pk;
	}

	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

}
