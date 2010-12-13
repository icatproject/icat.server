/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 23 nov. 2010
 */

package uk.icat3.exceptions;

/**
 * Excpetion in class value type
 * 
 * @author cruzcruz
 */
public class RestrictionValueClassException extends RestrictionException {

    private static final String msg = "Values class type not expected. ";

    public RestrictionValueClassException() {
        super (msg);
    }

    public RestrictionValueClassException (String msg) {
        super (RestrictionValueClassException.msg + msg);
    }
}
