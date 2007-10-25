/*
 * DownloadException.java
 * 
 * Created on 17-Oct-2007, 13:14:45
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.data.exceptions;

/**
 *
 * @author gjd37
 */
public class DownloadException extends Exception {

    /**
     * Creates a new instance of <code>DownloadException</code> without detail message.
     */
    public DownloadException() {
    }


    /**
     * Constructs an instance of <code>DownloadException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DownloadException(String msg) {
        super(msg);
    }
}
