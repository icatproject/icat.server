package uk.icat3.manager;

import uk.icat3.entity.EntityBaseBean;

public class GetResponse {

	private final EntityBaseBean bean;
	private final NotificationMessages notificationMessages;

	public GetResponse(EntityBaseBean bean, NotificationMessages notificationMessages) {
		this.bean = bean;
		this.notificationMessages = notificationMessages;
	}

	public EntityBaseBean getBean() {
		return this.bean;
	}

	public NotificationMessages getNotificationMessages() {
		return this.notificationMessages;
	}

}
