package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class SessionException extends ICATAPIException {

	public SessionException(String msg) {
		super(msg);
	}

}
