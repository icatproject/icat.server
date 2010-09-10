/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

import uk.icat3.exceptions.NullParameterException;
import uk.icat3.search.parameter.util.ParameterValued;
import uk.icat3.entity.Parameter;

/**
 * This class contains information about the parameter and the value which is
 * going to be compare with.
 *
 * @author cruzcruz
 * @see ParameterCondition
 */
public final class ParameterComparisonCondition extends ParameterCondition{

    /** Parameter valued which contains parameter and its type */
    private ParameterValued param;
    /** Type of comparator (greater_than, less_than, equal, ..)*/
    private ComparisonOperator comparator = null;
    
    /** Value to compare with */
    private Object value = null;
    /** Second value to compare with (only for some comparators like between)*/
    private Object value2 = null;

    /**
     * Empty constructor. Needed for Web Service implementation
     */
    public ParameterComparisonCondition() {
        this.param = new ParameterValued ();
    }


    /**
     * Contructor
     *
     * @param param Parameter
     * @param comparator ComparisonOperator which compare with
     * @param value Value to compare with parameter 'param'
     */
    public ParameterComparisonCondition(Parameter param, ComparisonOperator comparator, String value) {
        this.param = new ParameterValued(param);
        this.comparator = comparator;
        this.value = value;
    }

    /**
     * Check that the compartor is well contructed and the fields are filled.
     *
     * @return False in case of missing a field.
     */
    public void validate () throws NullParameterException {
        if (this.param == null)
            throw new NullParameterException(this.getClass().getName() + ".param");
        if (this.comparator == null)
            throw new NullParameterException(this.getClass().getName() + ".comparator");
        if (this.value == null)
            throw new NullParameterException(this.getClass().getName() + ".value");
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

    public ParameterValued getParameterValued() {
        return param;
    }

    public void setParameterValued(ParameterValued param) {
        this.param = param;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if(value.getClass()==com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.class){
            this.value = ((com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl) value).toGregorianCalendar().getTime();
        } else {
            this.value = value;
        }
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }
}
