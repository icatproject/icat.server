/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 juin 2010
 */

package uk.icat3.exceptions;

import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.entity.Parameter;

/**
 * This exception is executed when a numeric value is compared using a string comparator
 * 
 * @author cruzcruz
 */
public class NoDatetimeComparatorException extends ComparatorException {

    private static String msg = "Extracting JPQL condition exception:";

    public NoDatetimeComparatorException (Parameter param, ComparisonOperator comp) {
        super (NoDatetimeComparatorException.msg +
                "Parameter '" +
                param.getParameterPK().getName() +
                "(" + param.getParameterPK().getUnits() +
                ")' contains Datetime value " +
                "but comparator '" + comp.name() + "' is for string values");
    }
}