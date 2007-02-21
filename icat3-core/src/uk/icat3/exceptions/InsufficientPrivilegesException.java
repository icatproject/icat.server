package uk.icat3.exceptions;

/*
 * InsufficientPrivilegesException.java
 *
 * Created on 13 February 2007, 09:55
 *
 * Exception class to denote that user does not have the appropriate privileges 
 * to perform a requested operation.  Used by the GateKeeper service.
 *
 * @author df01
 * @version 1.0
 */
public class InsufficientPrivilegesException extends ICATAPIException {
    
    /**
     * Creates a new instance of <code>InsufficientPrivilegesException</code> without detail message.
     */
    public InsufficientPrivilegesException() {
    }
    
    
    /**
     * Constructs an instance of <code>InsufficientPrivilegesException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InsufficientPrivilegesException(String msg) {
        super(msg);
    }
}
