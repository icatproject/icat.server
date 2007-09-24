/*
 * NoSuchObjectFoundException.java
 *
 * Created on 21 March 2007, 09:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exceptions;

import javax.ejb.ApplicationException;

/**
 * This is thrown when an investigation, dataset or datafile is not found in the DB from a primary key ID
 *
 * @author gjd37
 */
@ApplicationException(rollback=true)
public class ObjectDeletedException extends NoSuchObjectFoundException {
    
    /**
     * Creates a new instance of <code>NoSuchObjectFoundException</code> without detail message.
     */
    public ObjectDeletedException() {
    }
    
    
    /**
     * Constructs an instance of <code>NoSuchObjectFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ObjectDeletedException(String msg) {
        super(msg);
    }
}
