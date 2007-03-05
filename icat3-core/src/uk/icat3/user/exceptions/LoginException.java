
package uk.icat3.user.exceptions;

/*
 * LoginException.java
 *
 * Created on 21 February 2007, 10:17
 *
 * Exception class that must be thrown by facility user
 * database in circumstances as decribed in User.java
 * 
 * @author df01
 * version 1.0
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