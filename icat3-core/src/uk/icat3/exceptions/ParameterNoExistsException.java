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
public class ParameterNoExistsException extends ParameterSearchException {

    private final static String msg = "No exists parameter";

    public ParameterNoExistsException() {
        super (ParameterNoExistsException.msg);
    }


    public ParameterNoExistsException(Parameter param) {
        super (ParameterNoExistsException.msg + " with name '" + param.getParameterPK().getName() +
                "' and units '" + param.getParameterPK().getUnits() + "'");
    }
}
