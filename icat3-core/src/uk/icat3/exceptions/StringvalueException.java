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
public class StringvalueException extends ComparasionValueException {

    private final static String msg = "String value error.";

    public StringvalueException() {
        super (StringvalueException.msg);
    }


    public StringvalueException(String number) {
        super (StringvalueException.msg + "'" + number +"' it's not a string ");
    }
}
