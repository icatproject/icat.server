/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 23 juil. 2010
 */

package uk.icat3.search.parameter.util;

import uk.icat3.search.parameter.ParameterType;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.search.parameter.ParameterCondition;

/**
 * This class contains the information about a parameter which is going to
 * be used into a parameter search
 * 
 * @author cruzcruz
 */
public class ParameterSearch extends ParameterCondition {
    /** JQPL field name for NUMERIC_VALUE */
    private static String NUMERIC_VALUE = "numericValue";
    /** JQPL field name for STRING_VALUE */
    private static String STRING_VALUE = "stringValue";
    /** JQPL field name for DATE_TIME_VALUE */
    private static String DATE_TIME_VALUE = "dateTimeValue";
    /** Type of parameter (dataset, datafile, sample, ALL) */
    private ParameterType type;
    /** Parameter to compare */
    private Parameter param;


    ///////////////////////////////////////////////////////////////////////////
    //                CONSTRUCTORS                                           //
    ///////////////////////////////////////////////////////////////////////////
    public ParameterSearch() {
    }

    public ParameterSearch (Parameter param) {
        this.param = param;
    }

    public ParameterSearch(ParameterType type) {
        this.type = type;
    }

    public ParameterSearch(ParameterType type, Parameter param) {
        this.type = type;
        this.param = param;
    }

    /**
     * Return the JPQL field name of the parameter value.
     *
     * @return Field name
     */
    public String getValueType() {
        if (getParam().isNumeric())
            return NUMERIC_VALUE;
        if(getParam().isDateTime())
            return DATE_TIME_VALUE;
        return STRING_VALUE;
    }

    /**
     * Check the parameter valued is correctly defined
     * 
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws NoParameterTypeException
     */
    public void validate() throws NoSearchableParameterException, NullParameterException, NoParameterTypeException {
        if (param == null)
            throw new NullParameterException("parameter ");

        if (this.type == null)
            throw new NoParameterTypeException("type is null");
    }


    ///////////////////////////////////////////////////////////////////////////
    //                               GETTERS and SETTERS                     //
    ///////////////////////////////////////////////////////////////////////////


    public Parameter getParam() {
        return param;
    }

    public void setParam(Parameter param) {
        this.param = param;
    }
    
    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }
}
