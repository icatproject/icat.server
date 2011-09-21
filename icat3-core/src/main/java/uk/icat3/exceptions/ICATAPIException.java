package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback = true)
public class ICATAPIException extends java.lang.Exception {
	public ICATAPIException(String msg) {
		super(msg);
	}
}
