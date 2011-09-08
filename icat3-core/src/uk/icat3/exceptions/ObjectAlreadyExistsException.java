package uk.icat3.exceptions;

import javax.ejb.ApplicationException;


@SuppressWarnings("serial")
@ApplicationException(rollback=true)
public class ObjectAlreadyExistsException extends ICATAPIException {
    
    public ObjectAlreadyExistsException(String msg) {
        super(msg);
    }

}
