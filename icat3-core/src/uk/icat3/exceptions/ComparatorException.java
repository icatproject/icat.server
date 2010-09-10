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
public class ComparatorException extends ParameterSearchException {

    private static String msg = "Comparator exception";

    public ComparatorException(String msg) {
        super (ComparatorException.msg + ":" + msg);
    }

    
}
