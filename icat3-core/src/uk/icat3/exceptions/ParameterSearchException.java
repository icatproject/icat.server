/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 juin 2010
 */

package uk.icat3.exceptions;

/**
 *
 * @author cruzcruz
 */
public class ParameterSearchException extends RestrictionException {

    public ParameterSearchException() {
    }

    public ParameterSearchException(String msg) {
        super(msg);
    }

}
