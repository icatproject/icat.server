package uk.icat3.exceptions;

/*
 * AddElementException.java
 * 
 * Created on 29-May-2007, 16:11:05
 * 
 * Exception to be thrown by business logic when an error is encountered adding an
 * element e.g. Keyword, Publication, Investigation etc. to the database.
 *
 * @author df01
 */
public class AddElementException extends ICATAPIException {

    /**
     * Creates a new instance of <code>AddElementException</code> without detail message.
     */
    public AddElementException() {
    }


    /**
     * Constructs an instance of <code>AddElementException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AddElementException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>AddElementException</code> with the specified detail message
     * and original exception that was thrown.
     * @param msg 
     * @param e 
     */ 
    public AddElementException(String msg, Exception e) {
        super(msg, e);
    }
}
