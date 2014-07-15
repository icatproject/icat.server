package org.icatproject.integration.client;

public class IcatException extends Exception {

	public enum IcatExceptionType {
		BAD_PARAMETER, INTERNAL, INSUFFICIENT_PRIVILEGES, NO_SUCH_OBJECT_FOUND, OBJECT_ALREADY_EXISTS, SESSION, VALIDATION
	}

	private IcatExceptionType type;
	private int offset = -1;

	@Override
	public String toString() {
		return type + " " + super.toString();
	}

	public IcatException(IcatExceptionType type, String msg) {
		super(msg);
		this.type = type;
	}

	public IcatException(IcatExceptionType type, String msg, int offset) {
		this(type, msg);
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

//	public void setOffset(int offset) {
//		this.offset = offset;
//	}

	// public IcatExceptionType getType() {
	// return type;
	// }

	public void setType(IcatExceptionType type) {
		this.type = type;
	}
}

