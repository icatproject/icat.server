package org.icatproject.core;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class IcatException extends Exception {

	public enum IcatExceptionType {
		BAD_PARAMETER, INTERNAL, INSUFFICIENT_PRIVILEGES, NO_SUCH_OBJECT_FOUND, OBJECT_ALREADY_EXISTS, SESSION, VALIDATION
	}

	private IcatExceptionType type;

	@Override
	public String toString() {
		return type + " " + super.toString();
	}

	public IcatException(IcatExceptionType type, String msg) {
		super(msg);
		this.type = type;
	}

	public IcatExceptionType getType() {
		return type;
	}

	public void setType(IcatExceptionType type) {
		this.type = type;
	}
}
