// $Id: MissingMethodException.java 933 2011-08-09 12:23:31Z abm65@FED.CCLRC.AC.UK $
package uk.icat.cmd.exception;

public class MissingMethodException extends Exception {

	private static final long serialVersionUID = -2304208582767312682L;

	public MissingMethodException(String message) {
		super(message);
	}

}
