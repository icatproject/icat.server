/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 29 juin 2010
 */

package uk.icat3.search.parameter.util;

import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.List;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.util.LogicalOperator;

/**
 * This class contains util functions that extract from parameter, comparators
 * and operator JPQL information.
 * 
 * @author cruzcruz
 */
public class ParameterSearchUtil {

    /** This counter contains the number of JPQL parameters created (param_0, param_1)*/
    private int contParameter;
    /** Root name of the parameters used in the JPQL statment ('PARAM_NAME'_'contParameter')*/
    public final static String PARAM_NAME = "p";

    /**
     * Constructor
     */
    public ParameterSearchUtil() {
        contParameter = 0;
    }

    /**
     * Reset globar variables.
     */
    public void reset() {
        contParameter = 0;
    }

    /**
     * Return next the parameter name not used yet.
     *
     * @return Next parameter name
     */
    private String getNextParamName () {
        return PARAM_NAME + contParameter++;
    }


    /**
     * Extract condition from one parameter comparator. 
     * 
     * Return example: param_0.numeric_value > 1.0
     *
     * @param paramName Parameter name in the JPQL statement
     * @param paramComp ParameterComparisonCondition
     * @return JPQL condition
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     */
    private String getCondition (String paramName, ParameterComparisonCondition paramComp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException {

        Parameter param = paramComp.getParameterValued().getParam();
        ComparisonOperator comparator = paramComp.getComparator();
        Object value = paramComp.getValue();

        // Numeric comparators
        if (param.isNumeric() || param.isDateTime()) {
            String name = getNextParamName();
            ejpql.addParameterJPQL(name, value);
            value = ":" + name;
            if (comparator == ComparisonOperator.GREATER_THAN)
                return paramName + " > " + value.toString();
            if (comparator == ComparisonOperator.GREATER_EQUAL)
                return paramName + " >= " + value.toString();
            if (comparator == ComparisonOperator.LESS_THAN)
                return paramName + " < " + value.toString();
            if (comparator == ComparisonOperator.LESS_EQUAL)
                return paramName + " <= " + value.toString();
            if (comparator == ComparisonOperator.EQUAL)
                return paramName + " = " + value.toString();

            throw new NoNumericComparatorException(param, comparator);
        }
        // String comparators
        else {
            if (comparator == ComparisonOperator.CONTAIN)
                return paramName + " like '%" + value.toString() + "%'";
            if (comparator == ComparisonOperator.START_WITH)
                return paramName + " like '" + value.toString() + "%'";
            if (comparator == ComparisonOperator.END_WITH)
                return paramName + " like '%" + value.toString() + "'";
            if (comparator == ComparisonOperator.EQUAL)
                return paramName + " = '" + value.toString() + "'";
            
            throw new NoStringComparatorException(param, comparator);
        }
    }

    /**
     * Extract JPQL statement from a parameter search structure defined inside
     * the ParameterCondition object called 'paramOperable'
     * 
     * @param paramOperable Contains the parameter search structure
     * @return JPQL statement
     * @throws ParameterSearchException
     */
    public ExtractedJPQL extractJPQLOperable (ParameterCondition paramOperable) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, NoParametersException {
        ExtractedJPQLPriva ejpql = new ExtractedJPQLPriva();

        extractJPQL(paramOperable, ejpql);

        return ejpql;
    }

    /**
     * Extract JPQL statement condition from a ComparisonOperator and store in the ejpql
     * object.
     * 
     * @param comp ComparisonOperator
     * @param ejpql Object where the information is stored
     * @throws NoParameterTypeException
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws ParameterSearchException
     */
    private void extractJPQLComparator (ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoParameterTypeException, NoStringComparatorException, NoSearchableParameterException, NullParameterException, NoNumericComparatorException {
        // Check that the comparator is well contructed.
        comp.validate();

        // Check if the parameter to compare with it's a parameter
        if (comp.getValue().getClass() == ParameterValued.class) {
            String paramNameVal = this.getNextParamName();
            ParameterValued p = (ParameterValued)comp.getValue();
            ejpql.addParameter(paramNameVal, p);
            comp.setValue(paramNameVal + "." + ((ParameterValued) comp.getValue()).getValueType());
        }
//        addParameterCondition(this.getNextParamName(), comp, ejpql);
        
        String paramName = getNextParamName();
        ejpql.addParameter(paramName, comp.getParameterValued());
        // Add condition for parameter
        ejpql.addStartCondition(this.getParameterCondition (paramName, paramName), LogicalOperator.AND);
        // Add condition for comparator
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterValued().getValueType(), comp, ejpql));
    }

    /**
     * Add a parameter and its condition
     * 
     * @param paramName
     * @param comp
     * @param ejpql
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     */
    private void addParameterCondition (String paramName, ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, NoParameterTypeException {
        ejpql.addParameter(paramName, comp.getParameterValued());

        ejpql.openParenthesis();
        ejpql.addCondition(this.getParameterCondition(paramName, paramName));
        ejpql.addCondition(LogicalOperator.AND);
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterValued().getValueType(), comp, ejpql));
        ejpql.closeParenthesis();
    }

    /**
     * Add a parameter for an OR statement
     * 
     * @param paramName
     * @param param
     * @param comp
     * @param ejpql
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     */
    private void addParameterOrCondition (String paramName, String param, ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException {
        ejpql.addParameterJPQL(param, comp.getParameterValued().getParam());
        
        ejpql.openParenthesis();
        ejpql.addCondition(this.getParameterCondition(paramName, param));
        ejpql.addCondition(LogicalOperator.AND);
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterValued().getValueType(), comp, ejpql));
        ejpql.closeParenthesis();
    }

    /**
     * Extract JPQL statement condition from a ComparisonOperator and store in the ejpql
     * object. To optimize the ejecution of SQL queries where the OR condition
     * is present, the code try to reuse declared paremeters to use them
     * inside OR condition. The last parameter name using in a OR condition is
     * 'paramName'
     *
     * DatafileParameter p0 where
     *              p0.parameter = scanType OR p0.parameter = wavelength
     * 
     * DatafileParameter p0, DatafileParameter p1 where
     *              p0.datafile = p1.datafile AND
     *              p0.parameter = scanType AND p1.parameter = wavelength
     *
     * The difference between the sentences above, is that the first use only
     * one parameter to make the OR condition, and the other has to use 2
     * parameters.
     * 
     * @param comp ComparisonOperator
     * @param ejpql Object where the information is stored
     * @param paramName Last parameter name used in OR condition
     * @return
     * @throws ParameterSearchException
     */

    private String extractJQPL (ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql, String paramName) throws NullParameterException, NoSearchableParameterException, NoStringComparatorException,  NoNumericComparatorException, NoParameterTypeException, NoParametersException {
        comp.validate();

        // Check if the parameter to compare with, it's a parameter
        // TODO: make some test with this.
        if (comp.getValue().getClass() == ParameterValued.class) {
            String paramNameVal = this.getNextParamName();
            ParameterValued p = (ParameterValued)comp.getValue();
            ejpql.addParameter(paramNameVal, p);
            comp.setValue(paramNameVal + "." + ((ParameterValued) comp.getValue()).getValueType());
        }
        // No parameter name has been defined in OR condition
        if ((paramName == null) ||
            (comp.getParameterValued().getType() != ejpql.getParameterType(paramName))) {
            paramName = this.getNextParamName();
            addParameterCondition(paramName, comp, ejpql);
        }
        // User a parameter which has been declared before
        else {
            addParameterOrCondition(paramName, this.getNextParamName(), comp, ejpql);
//               ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterValued().getValueType(), comp));
            }
        return paramName;
    }

    /**
     * Extract JPQL statement condition from a Operable parameter and store it in the ejpql
     * object.
     * 
     * @param paramOperable Operable parameter
     * @param ejpql Object where the information is stored
     * @throws ParameterSearchException
     */
    private void extractJPQL(ParameterCondition paramOperable, ExtractedJPQLPriva ejpql) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException,  NoNumericComparatorException, NoParametersException, NoParameterTypeException {
        extractJPQL(paramOperable, ejpql, null);
    }

    /**
     * Extract JPQL statement condition from a Operable parameter and store in the ejpql
     * object. To optimize the ejecution of SQL queries, where the OR condition
     * is present the code try to reuse declared paremeters and use them
     * inside OR condition. The last parameter name using in a OR condition is
     * 'paramName'.
     * 
     * Ex:
     * DatafileParameter p0 where
     *              p0.parameter = scanType  OR p0.parameter = wavelength
     *
     * DatafileParameter p0, DatafileParameter p1 where
     *              p0.parameter = scanType AND p1.parameter = wavelength
     *
     * The difference between the sentences above, is that the first use only
     * one parameter to make the OR condition, and the other has to use 2
     * parameters.
     * 
     * @param parameterOperable Operable parameter
     * @param ejpql Object where the information is stored
     * @param paramName Last parameter name used in OR condition
     * @return
     * @throws ParameterSearchException
     */
    private String extractJPQL(ParameterCondition parameterOperable, ExtractedJPQLPriva ejpql, String paramName) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException  {
        // If it's a parameterComparator
        if (parameterOperable.getClass() == ParameterComparisonCondition.class)
            return extractJQPL((ParameterComparisonCondition) parameterOperable, ejpql, paramName);
        
        // If it's a ParameterLogicalCondition
        else if (parameterOperable.getClass() == ParameterLogicalCondition.class) {
            ParameterLogicalCondition op = (ParameterLogicalCondition) parameterOperable;

            if (op.getListComparable().isEmpty())
                throw new EmptyOperatorException();
            // Open parenthesis for the list of comparators
            ejpql.openParenthesis();

            int size = op.getListComparable().size();
            // Iterates over the list except the first parameter
            for (int i = 0; i < size; i++) {
                try {
                    if (op.getLogicalOperator() == LogicalOperator.OR)
                        paramName = extractJPQL(op.getListComparable().get(i), ejpql, paramName);
                    else
                        extractJPQL(op.getListComparable().get(i), ejpql);

                    // Not add last Logical Operator
                    if (i < (size - 1))
                        ejpql.addCondition(op.getLogicalOperator());
                    
                } catch (EmptyOperatorException ex) {
                    // In case there is empty listOperable
                }
            }
            // Close the parenthesis for the comparators
            ejpql.closeParenthesis();
        }
        return paramName;
    }

    /**
     * Create JPQL statement for a list of parameters. The JQPL statement created
     * will return all the investigations which have some datafile that includes all the parameters
     * defined in 'listParameter'.
     *
     * @param listParameters
     * @return
     * @throws NoParameterTypeException
     */
    public ExtractedJPQL extractJPQLParameters(List<ParameterValued> listParameters) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NullParameterException {
        checkList(listParameters);

        ExtractedJPQLPriva ejpql = new ExtractedJPQLPriva();

        String paramName;

        int size = listParameters.size();
        for (int i = 1; i < size; i++) {
            paramName = getNextParamName();
            ejpql.addParameter(paramName, listParameters.get(i));
            ejpql.addCondition(getParameterCondition(paramName, paramName));
            ejpql.addCondition(LogicalOperator.AND);
        }

        // Add last parameter
        paramName = getNextParamName();
        ejpql.addCondition (getParameterCondition(paramName, paramName));
        ejpql.addParameter(paramName, listParameters.get(0));

        return ejpql;
    }

    /**
     * Extract JPQL statement from a list of comparators and store it in the ejpql
     * object.
     * 
     * @param listComparators
     * @return
     * @throws ParameterSearchException
     */
    public ExtractedJPQL extractJPQLComparators(List<ParameterComparisonCondition> listComparators) throws EmptyListParameterException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException  {
        checkList(listComparators);
        
        ExtractedJPQLPriva ejpql = new ExtractedJPQLPriva();

        int size = listComparators.size();
        // Extract all the comparators except the first one.
        for (int i = 1; i < size; i++) {
            extractJPQLComparator(listComparators.get(i), ejpql);
            ejpql.addCondition(LogicalOperator.AND);
        }

        // Extract first comparator
        extractJPQLComparator(listComparators.get(0), ejpql);

        return ejpql;
    }

    /**
     * Check the list is not null or empty. Throw an exception if
     * any of them is present.
     * 
     * @param list list to check
     * @throws EmptyListParameterException
     */
    private void checkList (List list) throws EmptyListParameterException {
        if (list == null || list.isEmpty())
            throw new EmptyListParameterException();
    }

    /**
     * Return the JPQL condition for a parameter
     * 
     * @param paramName Name or the parameter
     * @return JPQL condition
     */
    private String getParameterCondition(String paramName, String param) {
        return paramName + ".parameter = " + ":" + param;
    }

    /**
     * Private class that extends ExtractedJPQL for adding information, only
     * used inside this class
     *
     * @see ExtractedJPQL
     */
    private class ExtractedJPQLPriva extends ExtractedJPQL {
        /**
         * Add a condition between parenthesis
         * 
         * @param condition Condition to add
         */
        private void addCondition (String condition) {
            this.condition.append("(" + condition + ")");
        }

        /**
         * Open a parenthesis in the condition string.
         */
        private void openParenthesis () {
            this.condition.append("(");
        }

        /**
         * Close a parenthesis in the condition string.
         */
        private void closeParenthesis () {
            this.condition.append(")");
        }

        /**
         * Add a parameter
         *
         * @param paramName JQPL parameter name for the ParameterValued
         * @param p ParameterValued to be added
         */
        private void addParameter (String paramName, ParameterValued p) throws NoSearchableParameterException, NullParameterException, NoParameterTypeException {
            p.validate();
            
            if (p.getType() == ParameterType.DATAFILE) 
                datafileParameter.put(paramName, p.getParam());
            else if (p.getType() == ParameterType.DATASET)
                datasetParameter.put(paramName, p.getParam());
            else if (p.getType() == ParameterType.SAMPLE)
                sampleParameter.put(paramName, p.getParam());
        }

        /**
         * Add a parameter for JPQL execution.
         *
         * (ex: 'select inv from Investigation inv where inv.title = :title')
         *
         * The expression ':title' is JPQL parameter, that it has to be added
         * before the JPQL statement is executed.
         *
         * @param param Parameter name
         * @param obj Object to be replace in JPQL statement
         */
        private void addParameterJPQL(String param, Object obj) {
            jpqlParameter.put(param, obj);
        }

        /**
         * Add a condition in the begining of the string
         * 
         * @param condition Condition to be added
         * @param op Operator to added
         */
        private void addStartCondition (String condition, LogicalOperator op) {
            this.condition.insert(0, "(" + condition + ") " + op.name() + " ");
        }

        /**
         * Add an operator to the condition
         *
         * @param logicalOperator Operator to be added.
         */
        private void addCondition(LogicalOperator logicalOperator) {
            this.condition.append(" " + logicalOperator + " ");
        }

        /**
         * Return the parameter type of a JPQL parameter name
         * 
         * @param paramName JPQL parameter name
         * @return Parameter type
         * @throws NoParametersException
         */
        private ParameterType getParameterType(String paramName) throws NoParametersException {
            if (datafileParameter.containsKey(paramName))
                return ParameterType.DATAFILE;
            if (datasetParameter.containsKey(paramName))
                return ParameterType.DATASET;
            if (sampleParameter.containsKey(paramName))
                return ParameterType.SAMPLE;

            throw new NoParametersException("No parameter name for '" + paramName + "' defined");
        }
    }
}
