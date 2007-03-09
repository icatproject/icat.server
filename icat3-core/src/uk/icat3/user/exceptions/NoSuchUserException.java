/*
 * NoSuchUserException.java
 *
 * Created on 21 February 2007, 14:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user.exceptions;

/**
 *
 * @author df01
 */
public class NoSuchUserException extends java.lang.Exception {
    
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
