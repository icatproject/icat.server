/*
 * NoSuchUserException.java
 *
 * Created on 21 February 2007, 14:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

/**
 *
 * @author df01
 */
@ApplicationException(rollback=true)
public class NoSuchUserException extends SessionException {
    
    /**
     * Creates a new instance of <code>NoSuchUserException</code> without detail message.
     */
    public NoSuchUserException() {
    }
    
    
    /**
     * Constructs an instance of <code>NoSuchUserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchUserException(String msg) {
        super(msg);
    }
}
