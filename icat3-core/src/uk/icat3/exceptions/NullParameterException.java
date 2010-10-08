/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 juin 2010
 */

package uk.icat3.exceptions;

import uk.icat3.exceptions.ParameterSearchException;

/**
 *
 * @author cruzcruz
 */
public class NullParameterException extends ParameterSearchException {

    private static String msg = NullParameterException.class.getName();

    public NullParameterException(String fieldName) {
        super (msg + ": field " + fieldName + " cannot be null");
    }

    public NullParameterException() {
        super (msg + ": parameter is null");
    }


}
