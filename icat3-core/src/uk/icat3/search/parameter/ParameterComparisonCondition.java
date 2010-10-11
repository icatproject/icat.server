/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.DatevalueFormatException;

/**
 * This class contains information about the parameter and the value which is
 * going to be compare with.
 *
 * @author cruzcruz
 * @see ParameterCondition
 */
public final class ParameterComparisonCondition extends ParameterCondition{

    private static final String sqlFormat = "yyyy-MM-dd HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(sqlFormat);
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
    public void validate () throws NullParameterException {
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

    public ParameterSearch getParameterValued() {
        return param;
    }

    public void setParameterValued(ParameterSearch param) {
        this.param = param;
    }

//    public void setValue(Object value) {
//        this.value = value;
//    }

    public Object getValue() {
        return value;
    }

    /**
     * Set a java.lang.Date date to compare with a datetime parameter
     * 
     * @param value Date value to compare with
     */
    public void setDatetimeValue(Date value) {
        this.value = value;
    }

    /**
     * Set a java.lang.String date to compare with a datetime parameter.
     * The String has to match the database date format 'yyyy-MM-dd HH:mm:ss'.
     * 
     * @param value String value to compare with
     */
    public void setDatetimeValue(String value) throws DatevalueFormatException {
        try {
            this.value = dateFormat.parse(value);
        } catch (ParseException ex) {
            throw new DatevalueFormatException(sqlFormat);
        }
    }

    /**
     * Set a java.lang.String value to compare with
     * 
     * @param value String value to compare with
     */
    public void setStringValue(String value) {
        this.value = value;
    }

    /**
     * Set a Number value to compare with.
     * 
     * @param value Number value to compare with
     */
    public void setNumericValue(Number value) {
        this.value = value;
    }

    public Object getValueRight() {
        return valueRight;
    }

    /**
     * Set the second java.lang.Date date to compare with a datetime parameter.
     * Only valid for BETWEEN comparator.
     * 
     * @param value Date value (Date java class) to compare with
     */
    public void setDatetimeValueRight (Date valueRight) {
        this.valueRight = valueRight;
    }

    /**
     * Set the second java.lang.String date to compare with a datetime parameter.
     * The String has to match the SQL format 'yyyy-MM-dd HH:mm:ss'.
     * Only valid for BETWEEN comparator.
     *
     * @param value Date value (String class) to compare with
     */
    public void setDatetimeValueRight (String valueRight) throws DatevalueFormatException {
        try {
            this.valueRight = dateFormat.parse(valueRight);
        } catch (ParseException ex) {
            throw new DatevalueFormatException(sqlFormat);
        }
    }

    /**
     * Set the second java.lang.String value to compare with a string parameter.
     * Only valid for BETWEEN comparator.
     *
     * @param value String value to compare with
     */
    public void setStringValueRight (String valueRight) {
        this.valueRight = valueRight;
    }

    /**
     * Set the second java.lang.Number numeric to compare with a numeric parameter.
     * Only valid for BETWEEN comparator.
     *
     * @param value Numeric value to compare with
     */
    public void setNumericValueRight (Number valueRight) {
        this.valueRight = valueRight;
    }
}
