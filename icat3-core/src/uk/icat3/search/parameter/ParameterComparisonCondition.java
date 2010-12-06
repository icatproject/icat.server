/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

import uk.icat3.exceptions.NullParameterException;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.exceptions.DatevalueFormatException;

/**
 * This class contains information about the parameter and the value which is
 * going to be compare with.
 *
 * @author cruzcruz
 * @see ParameterCondition
 */
public final class ParameterComparisonCondition extends ParameterCondition{
    
    /** Parameter valued which contains parameter and its type */
    private ParameterSearch param;
    /** Type of comparator (greater_than, less_than, equal, ..)*/
    private ComparisonOperator comparator = null;
    /** Value to compare with */
    private Object value = null;
    /** Second value to compare with (only for some comparators like between)*/
    private Object valueRight = null;

    /**
     * Empty constructor. Needed for Web Service implementation
     */
    public ParameterComparisonCondition() {
        this.param = new ParameterSearch ();
    }

    /**
     * Contructor
     *
     * @param param Parameter
     * @param comparator ComparisonOperator which compare with
     * @param value Value to compare with parameter 'param'
     */
    public ParameterComparisonCondition(ParameterSearch param, ComparisonOperator comparator, Object value) {
        this.param = param;
        this.comparator = comparator;
        this.value = value;
    }

    /**
     * Check that the compartor is well contructed and the fields are filled.
     *
     * @return False in case of missing a field.
     */
    public void validate () throws NullParameterException, DatevalueFormatException {
        if (this.param == null)
            throw new NullParameterException(this.getClass().getName() + ".param");
        if (this.comparator == null)
            throw new NullParameterException(this.getClass().getName() + ".comparator");
        if (this.value == null)
            throw new NullParameterException(this.getClass().getName() + ".value");
        if (this.comparator == ComparisonOperator.BETWEEN && this.valueRight == null)
            throw new NullParameterException(this.getClass().getName() + ".valueRight");
    }

    /////////////////////////////////////////////////////////////////////////////
    //                   GETTERS and SETTERS
    ///////////////////////////////////////////////////////////////////////////
    
    public ComparisonOperator getComparator() {
        return comparator;
    }

    public void setComparator(ComparisonOperator comparator) {
        this.comparator = comparator;
    }

    public ParameterSearch getParameterSearch() {
        return param;
    }

    public void setParameterSearch(ParameterSearch param) {
        this.param = param;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValueRight() {
        return valueRight;
    }

    public void setValueRight(Object valueRight) {
        this.valueRight = valueRight;
    }
}