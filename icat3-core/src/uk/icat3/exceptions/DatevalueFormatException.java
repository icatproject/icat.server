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
public class DatevalueFormatException extends ComparasionValueException {

    private final static String msg = "Date value.";

    public DatevalueFormatException() {
        super (DatevalueFormatException.msg);
    }


    public DatevalueFormatException(String format) {
        super (DatevalueFormatException.msg + "Date should be in next format " + format);
    }
}
