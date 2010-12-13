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
    EQUALS,
    /** <b>param.value >= value</b><br/>Parameter value greater or equal that a value */
    GREATER_EQUAL,
    /** <b>param.value <= value</b><br/>Parameter value less or equal that a value */
    LESS_EQUAL,
    /** <b>value1 < param.value < value2</b><br/>Parameter value between two value */
    BETWEEN,
    /** <b>param.value like (%value%)</b><br/>Parameter string value contains string value */
    CONTAINS,
    /** <b>param.value like (value%)</b><br/>Parameter value starts with string value */
    STARTS_WITH,
    /** <b>param.value like (%value)</b><br/>Parameter value ends with string value */
    ENDS_WITH,
    /** <b>param.value in (23, 23,23)</b><br/>Parameter value in selection of values */
    IN;

    /**
     * Extract operation together with the string value for the
     * operator and the value.
     *
     * @param value Operation
     * @return
     */
    public String getCondition(String value) {
        if (this == ComparisonOperator.GREATER_THAN)
            return "> " + value;

        if (this == ComparisonOperator.LESS_THAN)
            return "< " + value;

        if (this == ComparisonOperator.GREATER_EQUAL)
            return ">= " + value;

        if (this == ComparisonOperator.LESS_EQUAL)
            return "<= " + value;

        if (this == ComparisonOperator.EQUALS)
            return "= " + value;

        if (this == ComparisonOperator.CONTAINS)
            return "LIKE '%" + value + "%'";

        if (this == ComparisonOperator.STARTS_WITH)
            return "LIKE '" + value + "%'";

        if (this == ComparisonOperator.ENDS_WITH)
            return "LIKE '%" + value + "'";

        if (this == ComparisonOperator.IN)
            return "IN (" + value + ")";

        return value;
    }

    public String getConditionBetween (String value, String value2) {
        if (this == ComparisonOperator.BETWEEN)
            return "BETWEEN " + value + " AND " + value2;

        return "";
    }
}
