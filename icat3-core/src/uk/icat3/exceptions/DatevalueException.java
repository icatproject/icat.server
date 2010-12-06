/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 juin 2010
 */

package uk.icat3.exceptions;

import uk.icat3.entity.Parameter;


/**
 *
 * @author cruzcruz
 */
public class DatevalueException extends ComparasionValueException {

    private final static String msg = "Date value. ";

    public DatevalueException() {
        super (DatevalueException.msg);
    }


    public DatevalueException(String value) {
        super (DatevalueException.msg + "'" + value +"' is not a Date");
    }

    public DatevalueException(String value, Throwable t) {
        super (DatevalueException.msg + "'" + value +"' is not a Date. " + t.getClass().getName());
    }
}
