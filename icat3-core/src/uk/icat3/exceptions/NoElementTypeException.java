/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 juin 2010
 */

package uk.icat3.exceptions;

import uk.icat3.util.ElementType;

/**
 *
 * @author cruzcruz
 */
public class NoElementTypeException extends ParameterSearchException {

    private static String msg = NoElementTypeException.class.getName();

    public NoElementTypeException(ElementType type) {
        super (msg + ": element type '" + type.name() + "' no supported");
    }


}
