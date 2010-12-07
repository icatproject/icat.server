/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.search.parameter;

/**
 * Define the different comparators which can be used for a parameter search
 * 
 * @author cruzcruz
 */
public enum ComparisonOperator {
    /** <b>param.value > value</b><br/> Parameter value greater that a value.*/
    GREATER_THAN,
    /** <b>param.value < value</b><br/>Parameter value lower that a value */
    LESS_THAN,
    /** <b>param.value = value</b><br/>Parameter value equal to a value */
    EQUAL,
    /** <b>param.value >= value</b><br/>Parameter value greater or equal that a value */
    GREATER_EQUAL,
    /** <b>param.value <= value</b><br/>Parameter value less or equal that a value */
    LESS_EQUAL,
    /** <b>value1 < param.value < value2</b><br/>Parameter value between two value */
    BETWEEN,
    /** <b>param.value like (%value%)</b><br/>Parameter string value contains string value */
    CONTAIN,
    /** <b>param.value like (value%)</b><br/>Parameter value starts with string value */
    START_WITH,
    /** <b>param.value like (%value)</b><br/>Parameter value ends with string value */
    END_WITH,
    /** <b>param.value in (23, 23,23)</b><br/>Parameter value in selection of values */
    IN;
}
