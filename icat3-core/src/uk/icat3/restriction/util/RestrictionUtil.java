/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restriction.util;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.AttributeTypeException;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.OperatorINException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.ElementType;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * This class provides methods to transform from a restriction structure to
 * a JPQL String sentence. It also contains information about the characteristics
 * of the search (number of results, order).
 * 
 * @author cruzcruz
 */
public class RestrictionUtil {
    /** JPQL setence where condition are defined */
    private String sentenceJPQL;
    /** Restriction type */
    private RestrictionType restType;
    /** Check if restriction contains Sample attributes */
    private boolean containSampleAttributes;
    /** Check if restriction contains Investigator attributes */
    private boolean containInvestigatorAttributes;
    /** Check if restriction contains Keyword attributes */
    private boolean containKeywordAttributes;
    /** Check if restriction contains Datafile attributes */
    private boolean containDatafileAttributes;
    /** Check if restriction contains Dataset attributes */
    private boolean containDatasetAttributes;
    /** Check if restriction contains Investigation attributes */
    private boolean containInvestigationAttributes;
    /** Parameter name for JPQL query parameter */
    private final String PARAM_NAME = "restric";
    /** Counter for parameter name */
    private int contParameter;
    /** List of JPQL parameters */
    private Map<String, Object> jpqlParameter;
    /** Contain order by JPQL sentece */
    private String orderByJPQL;
    /** Maximun number of results to return */
    private int maxResults;
    /** Defines order direction */
    private boolean orderByAsc;
    /** Defines attribute to order by */
    private RestrictionAttributes orderByAttr;
    /** Indicates start of result */
    private int firstResult;
    /** Include options */
    private Enum enumInclude;
    /** Indicate return is a list of long */
    private boolean returnLongId;

    /**
     * Constructor
     *
     * @param restCond Restriction Condition
     * @param restType Restriction Type
     * 
     * @throws RestrictionEmptyListException
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     */
    public RestrictionUtil(RestrictionCondition restCond, RestrictionType restType) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException, RestrictionEmptyListException, CyclicException, EmptyOperatorException, RestrictionException  {
        // Initialites variables
        this.returnLongId = false;
        this.enumInclude = null;
        this.sentenceJPQL = "";
        this.orderByJPQL = "";
        this.maxResults = -1;
        this.firstResult = 0;
        this.orderByAsc = false;
        this.orderByAttr = null;
        this.restType = restType;
        contParameter = 0;
        jpqlParameter = new HashMap<String, Object>();
        containDatasetAttributes = containDatafileAttributes = containInvestigatorAttributes
                = containInvestigationAttributes = containKeywordAttributes
                = containSampleAttributes = false;
        // Check restriction is not null
        if (restCond != null) {
            extractJPQL(restCond);
            checkConditionOptions(restCond);
            // If it's ordered. The attribute type should be the same that
            // the restriction type. (If order by Investigation.name and
            // the results are Datasets, it could repeat entries).
            // FIXME: it returns same object several times if we allow that.
            // && this.restType == restCond.getOrderByAttr().getAttributeType()
            if (this.orderByAttr != null )  {
                String order = " DESC";
                if (this.orderByAsc)
                   order = " ASC";

                // Add order to sentence JPQL
                this.orderByJPQL = " order by "
                            + getParamName(this.orderByAttr)
                            + this.orderByAttr.getValue()
                            + order;
            }
        }
    }
    /**
     * Check if max results was set
     *
     * @return true it max results was set. Otherwise false.
     */
    public boolean hasMaxResults () {
        if (maxResults <= 0)
            return false;
        return true;
    }
    /**
     * Check if first result was set
     *
     * @return true it max results was set. Otherwise false.
     */
    public boolean hasFirstResults () {
        if (firstResult <= 0)
            return false;
        return true;
    }
    /**
     * Number maximun of results to return.
     *
     * @return maximun results to return
     */
    public int getMaxResults () {
        return this.maxResults;
    }
    /**
     * Get order by condition JPQL
     *
     * @return Order by condition
     */
    public String getOrderBy () {
        return this.orderByJPQL;
    }

    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Check if JPQL sentence is empty
     *
     * @return true if JPQL final sentence is empty
     */
    public boolean isEmpty () {
        return this.sentenceJPQL.isEmpty();
    }
    /**
     * Extract JPQL sentence from restriction condition
     * 
     * @param restCond Restriction Condition
     * @throws RestrictionEmptyListException
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     */
    private String extractJPQL(RestrictionCondition restCond) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException, CyclicException, EmptyOperatorException, RestrictionException {
        // Check if this condition is negated
        if (restCond.isNegate())
            addNotCondition();

        // If it's a parameterComparator
        if (restCond.getClass() == RestrictionComparisonCondition.class)
            addRestrictionCondition((RestrictionComparisonCondition) restCond);

        // If it's a ParameterLogicalCondition
        else if (restCond.getClass() == RestrictionLogicalCondition.class) {
            RestrictionLogicalCondition op = (RestrictionLogicalCondition) restCond;
            if (op.getRestConditions().isEmpty() || !op.validate())
                return Queries.EMPTY_CONDITION;
            // Open parenthesis for the list of comparators
            openParenthesis();
            // Numbers of conditions
            int size = op.getRestConditions().size();
            String ret;
            // Read all conditions
            for (int i = 0; i < size; i++) {
                // If search is sensitive, propagate to their children
                if (op.isSensitive())
                    op.getRestConditions().get(i).setSensitive(true);
                // If operator is an OR
                if (op.getOperator() == LogicalOperator.OR)
                    ret = extractJPQL(op.getRestConditions().get(i));
                // If operator is an AND
                else
                    ret = extractJPQL(op.getRestConditions().get(i));
                // Not add Logical Operator if it is the last one or
                // the codintion before was empty
                if (i < (size - 1) && !Queries.EMPTY_CONDITION.equals(ret)) {
                    addCondition(op.getOperator());
                }
            }
            // If ends with an LogicalOperator, remove
            if (sentenceJPQL.endsWith(op.getOperator().name() + " "))
                sentenceJPQL = sentenceJPQL.replaceAll(op.getOperator().name() + " $", " ");
            // Close the parenthesis for the comparators
            closeParenthesis();
        }
        checkConditionOptions (restCond);
        
        return null;
    }

    /**
     * Return include
     * @return
     */
    public Enum getInclude() {
        return enumInclude;
    }
    /**
     * Check if there exists include.
     * 
     * @return
     */
    public boolean hasInclude () {
        if (this.enumInclude != null)
            return true;
        return false;
    }

    /**
     * Open a parenthesis in JPQL final sentence
     */
    private void openParenthesis() {
        sentenceJPQL += "(";
    }

    /**
     * Close a parenthesis in JPQL final sentence
     */
    private void closeParenthesis() {
        sentenceJPQL += ")";
    }
    /**
     * Add logical operator condition into JPQL final sentence
     * @param logicalOperator
     */
    private void addCondition(LogicalOperator logicalOperator) {
       sentenceJPQL += " " + logicalOperator.name() + " ";
    }
    /**
     * Add NOT condition
     */
    private void addNotCondition() {
        sentenceJPQL += " NOT ";
    }

    /**
     * Add restriction comparison condition to the JPQL final sentence.
     *
     * @param comp
     */
    private void addRestrictionCondition(RestrictionComparisonCondition comp) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionException {
        // Check if comparison is well construct
        comp.validate();
        // Get parameter name
        String parameter = getParamName(comp.getRestrictionAttribute()) + comp.getRestrictionAttribute().getValue();
        // If search is insensitive and attribute type is String, lower JPQL name attribute
        if (!comp.isSensitive() && comp.getRestrictionAttribute().isString())
            parameter = "LOWER (" + parameter + ")";
        // Add restricion.
        sentenceJPQL += parameter + " "
                        + comp.getComparisonOperator().getCondition(getParamValue(comp));
    }

    /**
     * Return correct JPQL parameter name
     *
     * @param comp
     * @return
     */
    private String getParamName (RestrictionAttributes attr) throws RestrictionException {
        String paramName = Queries.PARAM_NAME_JPQL;
        // Restriction is for a Facility User
        if ((restType == RestrictionType.FACILITY_USER && !attr.isFacilityUser()) ||
                (restType != RestrictionType.FACILITY_USER && attr.isFacilityUser())) {
            // If attribute is not from FacilityUser, it is an error
            throw new AttributeTypeException(restType.name() + " cannot be relatitoned with" +
                    " attribute '" + attr.name() + "'. Uses RestrictionAttributtes.INVESTIGATOR instead.");
        }
        // Restriction is for a Parameter
        else if ((restType == RestrictionType.PARAMETER && !attr.isParameter()) ||
                (restType != RestrictionType.PARAMETER && attr.isParameter())) {
            // If attribute is not from FacilityUser, it is an error
            throw new AttributeTypeException(restType.name() + " cannot be relatitoned with" +
                    " attribute '" + attr.name() + "'. Uses 'searchbyParameterRestriction' method instead.");
        }
        // Investigator attributes
        else if (attr.isInvestigator()) {
            paramName = Queries.INVESTIGATOR_NAME;
            containInvestigatorAttributes = true;
        }
        // Keyword attribute
        else if (attr.isKeyword()) {
            paramName = Queries.KEYWORD_NAME;
            this.containKeywordAttributes = true;
        }
        // Restriction is for Dataset search
        else if (restType == RestrictionType.DATASET) {
            // Datafile attributes
            if (attr.isDatafile()) {
                paramName = Queries.DATAFILE_NAME;
                containDatafileAttributes = true;
            }
            // Sample attributes
            else if (attr.isSample()) {
                paramName = Queries.SAMPLE_NAME;
                containSampleAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".investigation";
        }
        // Restriction is for a Datafile search
        else if (restType == RestrictionType.DATAFILE) {
            // Dataset attributes
            if (attr.isDataset())
                paramName += ".dataset";
            // Sample attributes
            else if (attr.isSample()) {
                paramName = Queries.SAMPLE_NAME;
                containSampleAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".dataset.investigation";
        }
        // Restriction is for a Sample search
        else if (restType == RestrictionType.SAMPLE) {
            // Dataset attributes
            if (attr.isDataset()) {
                paramName = Queries.DATASET_NAME;
                containDatasetAttributes = true;
            }
            // Datafile attributes
            else if (attr.isDatafile()) {
                paramName = Queries.DATAFILE_NAME;
                containDatafileAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".investigationId";
        }
        // Restriction is for a Investigation search
        else if (restType == RestrictionType.INVESTIGATION) {
            // Dataset attributes
            if (attr.isDataset()) {
                paramName = Queries.DATASET_NAME;
                containDatasetAttributes = true;
            }
            // Datafile attributes
            else if (attr.isDatafile()) {
                paramName = Queries.DATAFILE_NAME;
                containDatafileAttributes = true;
            }
            // Sample attributes
            else if (attr.isSample()) {
                paramName = Queries.SAMPLE_NAME;
                containSampleAttributes = true;
            }
        }
        // If attribute is an Object
        if (attr.isObject())
            return paramName;
        return paramName + ".";
    }
        
    /**
     * Return the real value of the parameter.
     *
     * @param comp
     * @return
     */
    private String getParamValue (RestrictionComparisonCondition comp) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionException {
        String paramValue = "";
        // String operator. Value must be a String
        if (comp.getComparisonOperator() == ComparisonOperator.CONTAINS ||
                comp.getComparisonOperator() == ComparisonOperator.STARTS_WITH ||
                comp.getComparisonOperator() == ComparisonOperator.ENDS_WITH) {
            // Attribute is a Number, but should be String
            if (comp.getRestrictionAttribute().isNumeric())
                throw new RestrictionOperatorException("Attribute is Numeric");
            // Attribute is a Date, but should be String
            else if (comp.getRestrictionAttribute().isDateTime())
                throw new RestrictionOperatorException("Attribute is Datetime");
            // Attribute is a Date, but should be String
            else if (comp.getRestrictionAttribute().isObject())
                throw new RestrictionOperatorException("Attribute is an Object");
            // Return String value
            return getStringValue(comp, comp.getValue());
        }
        // IN operator
        else if (comp.getComparisonOperator() == ComparisonOperator.IN) {
            // If value is an instance of Collection
            if (comp.getValue() instanceof Collection) {
                Collection col = (Collection) comp.getValue();
                if (col.isEmpty())
                    throw new RestrictionException ("List of IN parameters is empty.");
                String value = "";
                // If attribute is Numeric or a Date
                if (comp.getRestrictionAttribute().isNumeric() ||
                        comp.getRestrictionAttribute().isDateTime()) {
                    // Lists the collection
                    for (Object o : col) {
                        if (!(o instanceof Number) && !(o instanceof Date))
                            o = transformNumberDate (comp.getRestrictionAttribute(), o.toString());
                        // Add jpql parameter
                        String name = getNextParamName();
                        jpqlParameter.put(name, o);
                        value += ", :" + name;
                    }
                }
                // If attribute is an object
                else if (comp.getRestrictionAttribute().isObject()) {
                    for (Object o : col) {
                        String name = getNextParamName();
                        jpqlParameter.put(name, o);
                        value += ", :" + name;
                    }
                }
                // If attribute is a String
                else
                    for (Object o : col)
                        value += ", '" + getStringValue(comp, o) + "'";
                return value.substring(2);
            }
            // If value is a Date or a Number
            else if (comp.getValue() instanceof Date || comp.getValue() instanceof Number) {
                String name = getNextParamName();
                jpqlParameter.put(name, comp.getValue());
                return ":" + name;
            }
            // If value is a String separated by ','
            else if (comp.getValue() instanceof String) {
                String value = getStringValue(comp, comp.getValue());
                // If attribute is a String, construct IN ('val', 'val,...)
                if (comp.getRestrictionAttribute().isString())
                    return value.replaceAll("\\s*,\\s*", "','")
                                 .replaceAll("^\\s*", "'")
                                 .replaceAll("\\s*$", "'");
                // Else if attribute is Numberic or Date
                // Tokenizer
                StringTokenizer token = new StringTokenizer(value, ",");
                Object obj;
                String nextToken;
                // Check all tokens
                while (token.hasMoreTokens()) {
                    nextToken = token.nextToken().trim();
                    obj = transformNumberDate (comp.getRestrictionAttribute(), nextToken);
                    String name = getNextParamName();
                    jpqlParameter.put(name, obj);
                    value += ", :" + name;
                }

                return value;
            }
            // If attribute is an Object
            else if (comp.getRestrictionAttribute().isObject() &&
                     comp.getComparisonOperator() == ComparisonOperator.EQUALS) {
                String name = getNextParamName();
                jpqlParameter.put(name, comp.getValue());
                return ":" + name;
            }
            // Restriciton exception. Operator IN only List<String> or String
            throw new OperatorINException();
        }
        // BETWEEN operator
        else if (comp.getComparisonOperator() == ComparisonOperator.BETWEEN) {
            Object value = comp.getValue();
            Object valueRight = comp.getValueRight();
            // If attribute is a Numeric
            if (comp.getRestrictionAttribute().isNumeric()) {
                if (!(value instanceof Number))
                    value = Double.parseDouble(value.toString());
                if (!(valueRight instanceof Number))
                    valueRight = Double.parseDouble(valueRight.toString());
            }
            // If attribute is a Date
            else if (comp.getRestrictionAttribute().isDateTime()) {
                if (!(value instanceof Date))
                    value = parseDatetimeValue (value);
                if (!(valueRight instanceof Date))
                    valueRight = parseDatetimeValue (valueRight);
            }
            // Bettween operator only aplicable for Numeric or Date values
            else
                throw new RestrictionException("Values for operator BETWEEN must be Date or Number");
            // Add JPQL parameter
            paramValue = getNextParamName();
            String paramValue2 = getNextParamName();
            jpqlParameter.put(paramValue, value);
            jpqlParameter.put(paramValue2, valueRight);
            return comp.getComparisonOperator().getConditionBetween(":" + paramValue, ":" + paramValue2);
        }
        // Numeric, String or Date operator. Value is an Object
        else {
            paramValue = getNextParamName();
            // If attribute is a DateTime value
            if (comp.getRestrictionAttribute().isDateTime()) {
                // Parser value to Date
                jpqlParameter.put(paramValue, parseDatetimeValue(comp.getValue()));
            }
            // If attribute is a Numeric value
            else if (comp.getRestrictionAttribute().isNumeric()) {
                try {
                    // Parse string value to Number
                    if (comp.getValue().getClass() == String.class)
                        jpqlParameter.put(paramValue, Double.parseDouble(comp.getValue().toString()));
                    // If value class instance of Number, add the value
                    else if (comp.getValue() instanceof Number)
                        jpqlParameter.put(paramValue, comp.getValue());
                } catch (Throwable t) {
                    Logger.getLogger(RestrictionUtil.class.getName()).error(t);
                }
            }
            // If attribute is an Object and Comparison is EQUALS
            else if (comp.getRestrictionAttribute().isObject() &&
                    comp.getComparisonOperator() == ComparisonOperator.EQUALS)
                jpqlParameter.put(paramValue, comp.getValue());
            // Attribute is a String
            else {
                // Return String value
                return "'" + getStringValue(comp, comp.getValue()) + "'";
            }
            // Return value
            return ":" + paramValue;
        }
    }
    /**
     * Return String value, depending on sensitive or insensitive case
     *
     * @param comp Restriction Comparison Condition
     * @param value Value translatable to String
     * @return
     */
    private String getStringValue (RestrictionComparisonCondition comp, Object value) {
        // If search is sensitive, no make changes inside String value
        if (comp.isSensitive())
            return removeBadChar(value.toString());
        // If search is insensitive, transform String to lower case
        else
            return removeBadChar(value.toString().toLowerCase());
    }
    /**
     * Tranformation from a String to a Number or Date, depending of
     * RestrcitionAttribute type. If the transformation cannot be
     * applied, the String itself will be returned.
     * 
     * @param attr Restriction Attribute
     * @param strValue String to transform to
     * @return Number or Date transformed, or 'nextToken'.
     * @throws OperatorINException
     */
    private Object transformNumberDate (RestrictionAttributes attr, String strValue) throws OperatorINException {
        try {
            // If attribute is Numeric, transform string into a number
            if (attr.isNumeric())
                return Double.parseDouble(strValue);
            // If attribute is Date, transform string into a Date
            else if (attr.isDateTime()) {
                return parseDatetimeValue(strValue);
            }
        } catch (Throwable t) {
            throw new OperatorINException("String " + strValue
                    + " no translable. Only Date, Number or String classes are accepted.");
        }
        return strValue;
    }

    /**
     * Return JPQL parameters, according to the type of search, and the type of parameter
     * you want to get.
     *
     * @param searchType
     * @param parameterType
     * @return
     */
    public String getParameterJPQL (ElementType searchType, ElementType parameterType) {
        String restrictionParam = "";
        // Datafile
        if (searchType == ElementType.DATAFILE) {
            // Sample
            if (this.isContainSampleAttributes() && parameterType == ElementType.SAMPLE)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".dataset.investigation.sampleCollection) " + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes() && parameterType == ElementType.INVESTIGATOR)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".dataset.investigation.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes() && parameterType == ElementType.KEYWORD)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".dataset.investigation.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Dataset
        else if (searchType == ElementType.DATASET) {
            // Datafile
            if (this.isContainDatafileAttributes() && parameterType == ElementType.DATAFILE)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".datafileCollection) " + Queries.DATAFILE_NAME;
            // Sample
            else if (this.isContainSampleAttributes() && parameterType == ElementType.SAMPLE)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.sampleCollection) " + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes() && parameterType == ElementType.INVESTIGATOR)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes() && parameterType == ElementType.KEYWORD)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Sample
        else if (searchType == ElementType.SAMPLE) {
            // Datafile
            if (this.isContainDatafileAttributes() && parameterType == ElementType.DATAFILE)
                restrictionParam += ", IN(" + Queries.DATASET_NAME
                        + ".datafileCollection) " + Queries.DATAFILE_NAME;
            // Dataset
            else if (this.isContainDatasetAttributes() && parameterType == ElementType.DATASET)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".investigationId.datasetCollection) " + Queries.DATASET_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes() && parameterType == ElementType.INVESTIGATOR)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes() && parameterType == ElementType.KEYWORD)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Investigation
        else if (searchType == ElementType.INVESTIGATION) {
            // Datafile
            if (this.isContainDatafileAttributes() && parameterType == ElementType.DATAFILE) {
                restrictionParam += ", IN(" + Queries.DATASET_NAME + ".datafileCollection) " + Queries.DATAFILE_NAME;
            }
            // Dataset
            else if (this.isContainDatasetAttributes() && parameterType == ElementType.DATASET)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".datasetCollection) " 
                        + Queries.DATASET_NAME;
            // Sample
            else if (this.isContainSampleAttributes() && parameterType == ElementType.SAMPLE)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".sampleCollection) "
                        + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes() && parameterType == ElementType.INVESTIGATOR)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes() && parameterType == ElementType.KEYWORD)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        return restrictionParam;
    }

    /**
     * Return JPQL parameters, according to the type of search
     * you want to get.
     *
     * @param searchType Search type
     * @return
     */
    public String getParameterJPQL (ElementType searchType) {
        String restrictionParam = "";
        // Datafile
        if (searchType == ElementType.DATAFILE) {
            // Sample
            if (this.isContainSampleAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".dataset.investigation.sampleCollection) " + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".dataset.investigation.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".dataset.investigation.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Dataset
        else if (searchType == ElementType.DATASET) {
            // Datafile
            if (this.isContainDatafileAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".datafileCollection) " + Queries.DATAFILE_NAME;
            // Sample
            if (this.isContainSampleAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.sampleCollection) " + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigation.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Sample
        else if (searchType == ElementType.SAMPLE) {
             // Datafile
            if (this.isContainDatafileAttributes()) {
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.datasetCollection) "
                        + Queries.DATASET_NAME;
                restrictionParam += ", IN(" + Queries.DATASET_NAME + ".datafileCollection) "
                        + Queries.DATAFILE_NAME;
            }
            // Dataset
            else if (this.isContainDatasetAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.datasetCollection) "
                        + Queries.DATASET_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.keywordCollection) "
                        + Queries.KEYWORD_NAME;
        }
        // Investigation
        else if (searchType == ElementType.INVESTIGATION) {
            // Datafile
            if (this.isContainDatafileAttributes()) {
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".datasetCollection) "
                        + Queries.DATASET_NAME;
                restrictionParam += ", IN(" + Queries.DATASET_NAME + ".datafileCollection) "
                        + Queries.DATAFILE_NAME;
            }
            // Dataset
            else if (this.isContainDatasetAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".datasetCollection) "
                        + Queries.DATASET_NAME;
            // Sample
            if (this.isContainSampleAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".sampleCollection) "
                        + Queries.SAMPLE_NAME;
            // Investigator
            if (this.isContainInvestigatorAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
            // Keywords
            if (this.isContainKeywordAttributes())
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".keywordCollection) "
                        + Queries.KEYWORD_NAME;

        }
        return restrictionParam;
    }

    /**
     * Remove chars not allowed from a String
     *
     * @param value String
     * @return
     */
    private String removeBadChar (String value) {
        return value.replaceAll("[^\\w\\s-:,.]", "");
    }


    /**
     * Return next the parameter name to use.
     *
     * @return Next parameter name
     */
    private String getNextParamName () {
        return PARAM_NAME + contParameter++;
    }

    //////////////////////////////////////////////////////////////////
    //                 GETTERS and SETTERS                          //
    //////////////////////////////////////////////////////////////////

    public String getSentenceJPQL () {
        return sentenceJPQL;
    }

    public boolean isContainDatafileAttributes() {
        return containDatafileAttributes;
    }

    public boolean isContainDatasetAttributes() {
        return containDatasetAttributes;
    }

    public boolean isContainInvestigationAttributes() {
        return containInvestigationAttributes;
    }

    public boolean isContainSampleAttributes() {
        return containSampleAttributes;
    }

    public boolean isContainInvestigatorAttributes() {
        return containInvestigatorAttributes;
    }

    public boolean isContainKeywordAttributes() {
        return containKeywordAttributes;
    }

    public Map<String, Object> getJpqlParameter() {
        return jpqlParameter;
    }

    public boolean isReturnLongId() {
        return returnLongId;
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
    private Date parseDatetimeValue (Object value) throws DatevalueException {
        Date date = null;
        try {
            if (value.getClass() == String.class) {
                String strValue = value.toString();
                strValue = strValue.trim();
                // Accept dates like yyyy-MM-dd, without time
                if (strValue.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    strValue += " 00:00:00";
                }
                value = strValue;
                date = Queries.dateFormat.parse(strValue);
            }
            else if (value.getClass() == Date.class)
                date = (Date)value;
            else
                date = XMLGregorianCalendarImpl.parse(value.toString()).toGregorianCalendar().getTime();
        } catch (Throwable t) {
            throw new DatevalueException(value.toString(), t);
        }
        return date;
    }
    /**
     * Check for the conditons options inside a RestrictionCondition
     * 
     * @param restCond Restriction condition
     */
    private void checkConditionOptions(RestrictionCondition restCond) {
        // Check if maximun of results was set in any condition
        if (restCond.hasMaxResults()) {
            this.maxResults = restCond.getMaxResults();
            this.firstResult = 0;
        }
        // Check if first results was set in any condition
        if (restCond.hasFirstResults())
            this.firstResult = restCond.getFirstResult();

        // Check if order was set in any condition
        if (restCond.hasOrder()) {
            this.orderByAsc = restCond.isOrderByAsc();
            this.orderByAttr = restCond.getOrderByAttribute();
        }
        // Check if there exists include options
        if (restCond.hasInclude(restType)) {
            this.enumInclude = restCond.getInclude(restType);
        }
        // Chekf return option
        if (restCond.isReturnLongId())
            this.returnLongId = true;
    }
}
