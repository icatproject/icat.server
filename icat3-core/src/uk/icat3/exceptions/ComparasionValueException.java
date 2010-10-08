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
public class ComparasionValueException extends ParameterSearchException {

    private final static String msg = "Comparasion value. ";

    public ComparasionValueException() {
        super (ComparasionValueException.msg);
    }

   public ComparasionValueException(String msg) {
        super (ComparasionValueException.msg + msg);
    }
}
