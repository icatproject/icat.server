package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class BadParameterException extends ICATAPIException {

	public BadParameterException(String msg) {
		super(msg);
	}

}
