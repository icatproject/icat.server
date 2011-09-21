// $Id: ParameterCreationException.java 933 2011-08-09 12:23:31Z abm65@FED.CCLRC.AC.UK $
package uk.icat.cmd.exception;

public class ParameterCreationException extends Exception {

	private static final long serialVersionUID = -8974010065119117319L;

	public ParameterCreationException() {
		super();
	}

	public ParameterCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParameterCreationException(String message) {
		super(message);
	}

	public ParameterCreationException(Throwable cause) {
		super(cause);
	}

}
