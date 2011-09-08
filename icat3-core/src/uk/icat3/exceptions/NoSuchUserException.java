package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

@SuppressWarnings("serial")
@ApplicationException(rollback=true)
public class NoSuchUserException extends SessionException {
  
    public NoSuchUserException(String msg) {
        super(msg);
    }
}
