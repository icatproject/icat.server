/*
 * SessionException.java
 *
 * Created on 20 March 2007, 09:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.exception;

/**
 *
 * @author gjd37
 */
public class SessionException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>SessionException</code> without detail message.
     */
    public SessionException() {
    }
    
    
    /**
     * Constructs an instance of <code>SessionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SessionException(String msg) {
        super(msg);
    }
}
