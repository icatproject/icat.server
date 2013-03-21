package org.icatproject.core.manager;

import org.icatproject.core.PropertyHandler.Entity;
import org.icatproject.core.PropertyHandler.Operation;

public class NotificationRequest {

	private Operation operation;
	private Entity entity;

	public NotificationRequest(Operation operation, Entity entity) {
		this.operation = operation;
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	public Operation getOperation() {
		return operation;
	}

}
