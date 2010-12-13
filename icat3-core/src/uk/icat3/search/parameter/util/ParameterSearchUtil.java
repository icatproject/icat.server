/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 29 juin 2010
 */

package uk.icat3.search.parameter.util;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import uk.icat3.exceptions.CyclicException;
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
import java.util.List;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.OperatorINException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.manager.ParameterManager;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * This class contains util functions that extract from parameter, comparators
 * and operator JPQL information.
 * 
 * @author cruzcruz
 */
public class ParameterSearchUtil {

    /** Actual EntityManager */
    private EntityManager manager;
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
     * @param parameterName Parameter name in the JPQL statement
     * @param paramComp ParameterComparisonCondition
     * @param ejpql Parameter information container
     * 
     * @return JPQL condition
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     */
    private String getCondition (final String parameterName, ParameterComparisonCondition paramComp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException {

        // Get parameter
        Parameter param = paramComp.getParameterSearch().getParam();
        ComparisonOperator comparator = paramComp.getComparator();
        Object value = paramComp.getValue();
        String paramName = parameterName;
        // If search is insensitive and parameter is a String, make lower JPQL parameter name
        if (param.isString() && !paramComp.isSensitive())
            paramName = " LOWER(" + paramName +")";
        // Check if comparator is IN
        if (comparator == ComparisonOperator.IN)
           return paramName + " IN (" + getValueIN (ejpql, param, value) + ")";
        // Numeric comparators
        if (param.isNumeric()) {
            String valueStr = parseNumericValue(value, ejpql);
            if (comparator == ComparisonOperator.GREATER_THAN)
                return paramName + " > " + valueStr;
            if (comparator == ComparisonOperator.GREATER_EQUAL)
                return paramName + " >= " + valueStr;
            if (comparator == ComparisonOperator.LESS_THAN)
                return paramName + " < " + valueStr;
            if (comparator == ComparisonOperator.LESS_EQUAL)
                return paramName + " <= " + valueStr;
            if (comparator == ComparisonOperator.EQUALS)
                return paramName + " = " + valueStr;
            if (comparator == ComparisonOperator.BETWEEN)
                return paramName + " BETWEEN " + valueStr + " AND " + 
                        parseNumericValue(paramComp.getValueRight(), ejpql);
            

            throw new NoNumericComparatorException(param, comparator);
        }
        // Datetime comparators
        else if (param.isDateTime()) {
            
            String valueStr = parseDateTimeValue(value, ejpql);
            if (comparator == ComparisonOperator.GREATER_THAN)
                return paramName + " > " + valueStr;
            if (comparator == ComparisonOperator.GREATER_EQUAL)
                return paramName + " >= " + valueStr;
            if (comparator == ComparisonOperator.LESS_THAN)
                return paramName + " < " + valueStr;
            if (comparator == ComparisonOperator.LESS_EQUAL)
                return paramName + " <= " + valueStr;
            if (comparator == ComparisonOperator.EQUALS)
                return paramName + " = " + valueStr;
            if (comparator == ComparisonOperator.BETWEEN) {
                if (paramComp.getValueRight() != null)
                return paramName + " BETWEEN " + valueStr + " AND " + 
                        parseDateTimeValue(paramComp.getValueRight(), ejpql);
            }
            throw new NoDatetimeComparatorException(param, comparator);
        }
        // String comparators
        else {
            String valueStr = getStringValue(paramComp, paramComp.getValue());
            if (comparator == ComparisonOperator.CONTAINS)
                return paramName + " like '%" + valueStr + "%'";
            if (comparator == ComparisonOperator.STARTS_WITH)
                return paramName + " like '" + valueStr + "%'";
            if (comparator == ComparisonOperator.ENDS_WITH)
                return paramName + " like '%" + valueStr + "'";
            if (comparator == ComparisonOperator.EQUALS)
                return paramName + " = '" + valueStr + "'";
            
            throw new NoStringComparatorException(param, comparator);
        }
    }
    /**
     * Return String value, depending on sensitive or insensitive case
     *
     * @param comp Restriction Comparison Condition
     * @param value Value translatable to String
     * @return
     */
    private String getStringValue (ParameterComparisonCondition comp, Object value) {
        // If search is sensitive, no make changes inside String value
        if (comp.isSensitive())
            return removeBadChar(value.toString());
        // If search is insensitive, transform String to lower case
        else
            return removeBadChar(value.toString().toLowerCase());
    }

    /**
     * Return JPQL parameter name which represents the numeric value to compare
     * with a numeric parameter.
     * 
     * @param value Value to compare with
     * @param ejpql JPQL object utils
     * @return JPQL parameter name
     * 
     * @throws NumericvalueException
     */
    private String parseNumericValue (Object value, ExtractedJPQLPriva ejpql) throws NumericvalueException {
        Number number;
        try {
            number = Number.class.cast(value);
        } catch (Throwable t) {
            throw new NumericvalueException(value.toString());
        }
        
        String name = getNextParamName();
        ejpql.addParameterJPQL(name, number.doubleValue());
        return ":" + name;
    }

    /**
     * Return JPQL parameter name which represents the datetime value to compare
     * with a datatime parameter.
     *
     * @param value Value to compare with
     * @param ejpql JPQL object utils
     * @return JPQL parameter name
     * 
     * @throws DatevalueException
     * @throws DatevalueFormatException
     */
    private String parseDateTimeValue (Object value, ExtractedJPQLPriva ejpql) throws DatevalueException, DatevalueFormatException {
        Date date = null;
        try {
            if (value.getClass() == String.class)
                date = Queries.dateFormat.parse(value.toString());
            else if (value.getClass() == Date.class)
                date = (Date)value;
            else
                date = XMLGregorianCalendarImpl.parse(value.toString()).toGregorianCalendar().getTime();

        } catch (Throwable t) {
            throw new DatevalueException(value.toString(), t);
        }
        // Add JPQL parameter to compare object witth object, not Strings
        String name = getNextParamName();
        ejpql.addParameterJPQL(name, date);
        return ":" + name;
    }

    /**
     * Extract JPQL statement from a parameter search structure defined inside
     * the ParameterCondition object called 'paramOperable'
     * 
     * @param paramOperable Contains the parameter search structure
     * @param manager Entity manager to database
     * @return JPQL statement
     * @throws ParameterSearchException
     */
    public ExtractedJPQL extractJPQLOperable (ParameterCondition paramOperable, EntityManager manager) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, NoParametersException, NoDatetimeComparatorException, ParameterNoExistsException, DatevalueException, NumericvalueException, DatevalueFormatException, CyclicException, OperatorINException, EmptyListParameterException {
        ExtractedJPQLPriva ejpql = new ExtractedJPQLPriva();

        this.manager = manager;
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
    private void extractJPQLComparator (ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoParameterTypeException, NoStringComparatorException, NoSearchableParameterException, NullParameterException, NoNumericComparatorException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException {
        // Check that the comparator is well contructed.
        comp.validate();

        if (comp.isNegate())
            ejpql.addNotCondition();

        // Check if the parameter to compare with it's a parameter
        if (comp.getValue().getClass() == ParameterSearch.class) {
            String paramNameVal = this.getNextParamName();
            ParameterSearch p = (ParameterSearch)comp.getValue();
            ejpql.addParameter(paramNameVal, p);
            comp.setValue(paramNameVal + "." + ((ParameterSearch) comp.getValue()).getValueType());
        }
//        addParameterCondition(this.getNextParamName(), comp, ejpql);
        
        String paramName = getNextParamName();
        ejpql.addParameter(paramName, comp.getParameterSearch());
        // Add condition for parameter
        ejpql.addStartCondition(this.getParameterCondition (paramName, paramName), LogicalOperator.AND);
        // Add condition for comparator
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterSearch().getValueType(), comp, ejpql));
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
    private void addParameterCondition (String paramName, ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, NoParameterTypeException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException {
        ejpql.addParameter(paramName, comp.getParameterSearch());

        ejpql.openParenthesis();
        ejpql.addCondition(this.getParameterCondition(paramName, paramName));
        ejpql.addCondition(LogicalOperator.AND);
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterSearch().getValueType(), comp, ejpql));
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
    private void addParameterOrCondition (String paramName, String param, ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql) throws NoStringComparatorException, NoNumericComparatorException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException {
        ejpql.addParameterJPQL(param, comp.getParameterSearch().getParam());
        
        ejpql.openParenthesis();
        ejpql.addCondition(this.getParameterCondition(paramName, param));
        ejpql.addCondition(LogicalOperator.AND);
        ejpql.addCondition(this.getCondition(paramName + "." + comp.getParameterSearch().getValueType(), comp, ejpql));
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
     * @return Parameter name
     *
     * @throws NullParameterException
     * @throws NoSearchableParameterException
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws NoParameterTypeException
     * @throws NoParametersException
     * @throws NoDatetimeComparatorException
     * @throws ParameterNoExistsException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
    private String extractJPQL (ParameterComparisonCondition comp, ExtractedJPQLPriva ejpql, String paramName) throws NullParameterException, NoSearchableParameterException, NoStringComparatorException,  NoNumericComparatorException, NoParameterTypeException, NoParametersException, NoDatetimeComparatorException, ParameterNoExistsException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException {
        // Check the parameter comparison is well construct
        comp.validate();
        // Check the parameter is searchable
        checkParameter(comp.getParameterSearch(), manager);
        // Check if the parameter to compare with, it's a parameter
        if (comp.getValue().getClass() == ParameterSearch.class) {
            String paramNameVal = this.getNextParamName();
            ParameterSearch p = (ParameterSearch)comp.getValue();
            checkParameter(p, manager);
            ejpql.addParameter(paramNameVal, p);
            comp.setValue(paramNameVal + "." + p.getValueType());
        }
        // No parameter name has been defined in OR condition
        if ((paramName == null) ||
            (comp.getParameterSearch().getType() != ejpql.getParameterType(paramName))) {
            paramName = this.getNextParamName();
            addParameterCondition(paramName, comp, ejpql);
        }
        // Use a parameter which has been declared before
        else 
            addParameterOrCondition(paramName, this.getNextParamName(), comp, ejpql);
        // Return parameter name
        return paramName;
    }

    /**
     * Extract JPQL statement condition from a Operable parameter and store it in the ejpql
     * object.
     * 
     * @param paramOperable Operable parameter
     * @param ejpql Object where the information is stored
     * @throws EmptyOperatorException
     * @throws NullParameterException
     * @throws NoSearchableParameterException
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws NoParametersException
     * @throws NoParameterTypeException
     * @throws NoDatetimeComparatorException
     * @throws ParameterNoExistsException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
    private void extractJPQL(ParameterCondition paramOperable, ExtractedJPQLPriva ejpql) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException,  NoNumericComparatorException, NoParametersException, NoParameterTypeException, NoDatetimeComparatorException, ParameterNoExistsException, DatevalueException, NumericvalueException, DatevalueFormatException, CyclicException, OperatorINException, EmptyListParameterException {
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
    private String extractJPQL(ParameterCondition parameterOperable, ExtractedJPQLPriva ejpql, String paramName) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, NoDatetimeComparatorException, ParameterNoExistsException, DatevalueException, NumericvalueException, DatevalueFormatException, CyclicException, OperatorINException, EmptyListParameterException  {
        if (parameterOperable.isNegate())
            ejpql.addNotCondition();
        
        // If it's a parameterComparatory
        if (parameterOperable.getClass() == ParameterComparisonCondition.class)
            return extractJPQL((ParameterComparisonCondition) parameterOperable, ejpql, paramName);
        
        // If it's a ParameterLogicalCondition
        else if (parameterOperable.getClass() == ParameterLogicalCondition.class) {
            ParameterLogicalCondition op = (ParameterLogicalCondition) parameterOperable;
            // Check logical condition is well construct
            if (op.getListComparable().isEmpty() || !op.validate())
                return Queries.EMPTY_CONDITION;
            // Open parenthesis for the list of comparators
            ejpql.openParenthesis();
            int size = op.getListComparable().size();
            for (int i = 0; i < size; i++) {
                 // If search is sensitive, propagate to their children
                if (op.isSensitive())
                    op.getListComparable().get(i).setSensitive(true);
                // Extract JPQL
                if (op.getOperator() == LogicalOperator.OR)
                    paramName = extractJPQL(op.getListComparable().get(i), ejpql, paramName);
                else
                    paramName = extractJPQL(op.getListComparable().get(i), ejpql, null);

                // If it is last iteration or condition before was empty, not add LogicalOperator
                if (i < (size - 1) && !Queries.EMPTY_CONDITION.equals(paramName))
                    ejpql.addCondition(op.getOperator());
            }
            // If condition end with a LogicalOperator, delete it
            if (ejpql.getCondition().endsWith(op.getOperator().name() + " "))
                ejpql.removeCharacter(op.getOperator().name().length() + 2);
            // Close the parenthesis for the comparators
            ejpql.closeParenthesis();
        }
        // If is a parameter search
        else if (parameterOperable.getClass() == ParameterSearch.class) {
            ParameterSearch ps = (ParameterSearch)parameterOperable;
            String name = getNextParamName();
            // Check if parameter exists
            checkParameter(ps, manager);
            // Add condition
            ejpql.addCondition (getParameterCondition(name, name));
            // Add JPQL parameter
            ejpql.addParameter(name, (ParameterSearch)parameterOperable);
        }
        return paramName;
    }

    /**
     * Create JPQL statement for a list of parameters. The JQPL statement created
     * will return all the investigations which have some datafile that includes all the parameters
     * defined in 'listParameter'.
     *
     * @param listParameters List of parameter valued
     * @param manager  Entity manager to database
     * @return
     * @throws NoParameterTypeException
     * @throws EmptyListParameterException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     */
    public ExtractedJPQL extractJPQLParameters(List<ParameterSearch> listParameters, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException {
        checkList(listParameters);
        for (ParameterSearch p : listParameters)
            checkParameter(p, manager);

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
     * @param listComparators List of comparators
     * @param manager Entity managet to database
     *
     * @return ExtractedJPQL object
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws NoDatetimeComparatorException
     * @throws ParameterNoExistsException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
    public ExtractedJPQL extractJPQLComparators(List<ParameterComparisonCondition> listComparators, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, NoDatetimeComparatorException, ParameterNoExistsException, DatevalueException, NumericvalueException, DatevalueFormatException, OperatorINException  {
        checkList(listComparators);
        for (ParameterComparisonCondition cond : listComparators) {
            checkParameter(cond.getParameterSearch(), manager);
        }
        
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
     * Check the parameter is correct, the parameter exists and is searchable.
     *
     * @param paramV ParameterSearch contained parameter
     * @param manager Entity Manager to database
     * 
     * @throws ParameterNoExistsException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws NoParameterTypeException
     */
    private void checkParameter (ParameterSearch paramV,EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NullParameterException, NoParameterTypeException {
        if (paramV == null)
            throw new NullParameterException ();
        
        paramV.validate();
        ParameterPK pk = paramV.getParam().getParameterPK();
        Parameter param = ParameterManager.getParameter(pk.getName(), pk.getUnits(), manager);
        ParameterType type = paramV.getType();
        
        if (param == null)
             throw new ParameterNoExistsException( paramV.getParam());
         if (!param.getSearchable().equalsIgnoreCase("Y"))
            throw new NoSearchableParameterException(param);
        else if (type == ParameterType.DATAFILE && !param.isDatafileParameter())
            throw new NoSearchableParameterException(param, "Parameter not relevant for Datafile");
        else if (type == ParameterType.DATASET && !param.isDatasetParameter())
            throw new NoSearchableParameterException(param, "Parameter not relevant for Dataset");
        else if (type == ParameterType.SAMPLE && !param.isSampleParameter())
            throw new NoSearchableParameterException(param, "Parameter not relevant for Sample");
        
        paramV.setParam(param);
    }
    /**
     * Retrun the value for IN comparator
     * 
     * @param ejpql Parameter information container
     * @param param Parameter to compare with value
     * @param value Value to compare with parameter
     * 
     * @return jpql sentence
     * 
     * @throws OperatorINException
     */
    private String getValueIN(ExtractedJPQLPriva ejpql, Parameter param, Object value) throws OperatorINException {
        // If value is an instance of Collection
        if (value instanceof Collection) {
            Collection col = (Collection)value;
            String res = "";
            // Check if parameter is Numeric or Date
            if (param.isNumeric() || param.isDateTime()) {
                for (Object o : col) {
                    if (!(o instanceof Number) && !(o instanceof Date))
                        throw new OperatorINException("Class " + o.getClass()
                                + " no recognized. Only Date, Number or String classes are accepted.");
                    String name = getNextParamName();
                    ejpql.addParameterJPQL(name, o);
                    res += ", :" + name;
                }
            }
            // Parameter is String
            else {
                for (Object o : col)
                    res += ", '" + removeBadChar(o.toString()) + "'";
            }

            return res.substring(2);
        }
        // If value is a Date or a Number
        else if (value instanceof Date || value instanceof Number) {
            String name = getNextParamName();
            ejpql.addParameterJPQL(name, value);
            return ":" + name;
        }
        // If value is a String separated by ','
        else {
            String res = removeBadChar(value.toString());
            res = res.replaceAll("\\s*,\\s*", "','")
                         .replaceAll("^\\s*", "'")
                         .replaceAll("\\s*$", "'");
            return res;
        }
    }

    /**
     * Remove chars not allowed from a String
     *
     * @param value String
     * @return
     */
    private String removeBadChar (String value) {
        return value.replaceAll("['\"\\\\]", "");
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
         * @param paramName JQPL parameter name for the ParameterSearch
         * @param p ParameterSearch to be added
         */
        private void addParameter (String paramName, ParameterSearch p) throws NoSearchableParameterException, NullParameterException, NoParameterTypeException {
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

        private void addNotCondition() {
            this.condition.append(" NOT ");
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
        /**
         * Remove last 'numberChar' characteres
         *
         * @param numberChar
         */
        private void removeCharacter(int numberChar) {
            condition.delete(condition.length() - numberChar, condition.length());
        }
    }
}
