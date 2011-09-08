package uk.icat3.exceptions;

import javax.ejb.ApplicationException;


@SuppressWarnings("serial")
@ApplicationException(rollback=true)
public class IcatInternalException extends ICATAPIException {
    
    public IcatInternalException(String msg) {
        super(msg);
    }

}
