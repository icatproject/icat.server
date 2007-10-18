/*
 * TotalSizeExceededException.java
 * 
 * Created on 17-Oct-2007, 14:41:20
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.data.exceptions;

/**
 *
 * @author gjd37
 */
public class TotalSizeExceededException extends Exception {

    /**
     * Creates a new instance of <code>TotalSizeExceededException</code> without detail message.
     */
    public TotalSizeExceededException() {
    }


    /**
     * Constructs an instance of <code>TotalSizeExceededException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TotalSizeExceededException(String msg) {
        super(msg);
    }
}
