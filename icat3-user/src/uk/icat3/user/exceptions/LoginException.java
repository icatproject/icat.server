/*
 * LoginException.java
 *
 * Created on 21 February 2007, 10:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user.exceptions;

/**
 *
 * @author df01
 */
public class LoginException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>LoginException</code> without detail message.
     */
    public LoginException() {
    }
    
    
    /**
     * Constructs an instance of <code>LoginException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LoginException(String msg) {
        super(msg);
    }
}
