package org.icatproject.core.manager;

import java.util.List;

public class SearchResponse {

	private final List<?> list;
	private final NotificationMessages notificationMessages;

	public SearchResponse(List<?> list, NotificationMessages notificationMessages) {
		this.list = list;
		this.notificationMessages = notificationMessages;
	}

	public List<?> getList() {
		return this.list;
	}

	public NotificationMessages getNotificationMessages() {
		return this.notificationMessages;
	}

}
