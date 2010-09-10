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
public class NoSearchableParameterException extends ParameterSearchException {

    private static String msg = NoSearchableParameterException.class.getName();

    public NoSearchableParameterException(Parameter param) {
        super (msg + ": parameter '" + param.getParameterPK().getName() + "(" +
                param.getParameterPK().getUnits() + ")' is not searchable");
    }

     public NoSearchableParameterException(Parameter param, String message) {
        super (msg + ": parameter '" + param.getParameterPK().getName() + "(" +
                param.getParameterPK().getUnits() + ")' is not searchable: " + message);
    }

}
