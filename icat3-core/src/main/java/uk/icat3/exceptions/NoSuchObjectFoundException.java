package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class NoSuchObjectFoundException extends ICATAPIException {

	public NoSuchObjectFoundException(String msg) {
		super(msg);
	}
}
