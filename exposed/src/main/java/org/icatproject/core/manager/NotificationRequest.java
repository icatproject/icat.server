package org.icatproject.core.manager;

import org.icatproject.core.manager.PropertyHandler.Operation;

public class NotificationRequest {

	private Operation operation;
	private String entity;

	public NotificationRequest(Operation operation, String entity) {
		this.operation = operation;
		this.entity = entity;
	}

	public String getEntity() {
		return entity;
	}

	public Operation getOperation() {
		return operation;
	}

}
