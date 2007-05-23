/*
 * ValidationException.java
 *
 * Created on 08 March 2007, 12:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

/**
 *  Exception for entity validation
 *
 * @author gjd37
 */
@ApplicationException(rollback=true)
public class ValidationException extends ICATAPIException {
    
    /**
     * Creates a new instance of <code>ValidationException</code> without detail message.
     */
    public ValidationException() {
    }
    
    
    /**
     * Constructs an instance of <code>ValidationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ValidationException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ValidationException</code> with the specified detail message
     * and original exception.
     * 
     * @param msg   the detail message.
     * @param e     the underlying exception
     */
    public ValidationException(String msg, Exception e) {
        super(msg, e);
    }
}
