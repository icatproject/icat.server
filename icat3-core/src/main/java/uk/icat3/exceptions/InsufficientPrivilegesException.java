package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class InsufficientPrivilegesException extends ICATAPIException {

	public InsufficientPrivilegesException(String msg) {
		super(msg);
	}
}
