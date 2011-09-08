package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class ValidationException extends ICATAPIException {

	public ValidationException(String msg) {
		super(msg);
	}

}
