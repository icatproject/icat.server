/*
 * InsufficientPrivilegesException.java
 *
 * Created on 13 February 2007, 09:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exceptions;

/**
 *
 * @author df01
 */
public class InsufficientPrivilegesException extends java.lang.Exception {
    
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
