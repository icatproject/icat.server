// $Id: MethodCallException.java 933 2011-08-09 12:23:31Z abm65@FED.CCLRC.AC.UK $
package uk.icat.cmd.exception;

public class MethodCallException extends Exception {

	private static final long serialVersionUID = -6855378536495599060L;

	public MethodCallException() {
	}

	public MethodCallException(String message) {
		super(message);
	}

	public MethodCallException(Throwable cause) {
		super(cause);
	}

	public MethodCallException(String message, Throwable cause) {
		super(message, cause);
	}

}
