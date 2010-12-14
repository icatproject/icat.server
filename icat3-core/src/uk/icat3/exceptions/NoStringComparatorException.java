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
 * This exception is executed when a string value is compared using a numeric comparator
 *
 * @author cruzcruz
 */
public class NoStringComparatorException extends ComparatorException {

    private static String msg = "Extracting JPQL condition exception:";
   

    public NoStringComparatorException (Parameter param, ComparisonOperator comp) {
        super (NoStringComparatorException.msg +
                "Parameter with name='" +
                param.getParameterPK().getName() +
                "' and units='" + param.getParameterPK().getUnits() +
                "', is a STRING parameter, " +
                "comparator '" + comp.name() + "' is only for Numeric or Date values.");
    }

    
}
