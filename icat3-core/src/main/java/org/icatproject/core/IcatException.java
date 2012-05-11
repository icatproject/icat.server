package org.icatproject.core;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class IcatException extends Exception {

	public enum Type {
		BAD_PARAMETER, INTERNAL, INSUFFICIENT_PRIVILEGES, NO_SUCH_OBJECT_FOUND, OBJECT_ALREDAY_EXISTS, SESSION, VALIDATION
	}

	private Type type;

	public IcatException(Type type, String msg) {
		super(msg);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
