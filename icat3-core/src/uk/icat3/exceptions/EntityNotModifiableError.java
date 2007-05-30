/*
 * EntityNotModifiableError.java
 *
 * Created on 29 March 2007, 08:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.exceptions;


import javax.ejb.ApplicationException;

/**
 * Runtime error when attemping to modify facility acquired data.
 * This should not be thrown unless a check has been missed by application
 *
 * @author gjd37
 */
@ApplicationException(rollback=true)
public class EntityNotModifiableError extends java.lang.Error {
    
    /**
     * Creates a new instance of <code>EntityNotModifiableError</code> without detail message.
     */
    public EntityNotModifiableError() {
    }
    
    
    /**
     * Constructs an instance of <code>EntityNotModifiableError</code> with the specified detail message.
     * 
     * @param msg the detail message.
     */
    public EntityNotModifiableError(String msg) {
        super(msg);
    }
}
