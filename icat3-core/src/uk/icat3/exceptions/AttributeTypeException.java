/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.exceptions;

/**
 * Exception in the restriction attribute types
 * 
 * @author cruzcruz
 */
public class AttributeTypeException extends RestrictionException {

    private static String msg = "Attribute incompatibility. The attibute could not" +
            " be relationed to the search type.";

    public AttributeTypeException(String msg) {
        super (AttributeTypeException.msg + msg);
    }
}
