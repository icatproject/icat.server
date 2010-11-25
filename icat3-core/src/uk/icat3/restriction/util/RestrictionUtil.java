/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restriction.util;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.RestrictionINException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionOperator;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * This class provides methods to transform from a restriction structure to
 * a JPQL String sentence.
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

    /**
     * Constructor
     *
     * @param restCond Restriction Condition
     * @param restType Restriction Type
     * 
     * @throws RestrictionEmptyListException
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws RestrictionINException
     * @throws RestrictionNullException
     */
    public RestrictionUtil(RestrictionCondition restCond, RestrictionType restType) throws RestrictionEmptyListException, DatevalueException, RestrictionOperatorException, RestrictionINException, RestrictionNullException  {
        // Initialites variables
        sentenceJPQL = "";
        this.restType = restType;
        contParameter = 0;
        jpqlParameter = new HashMap<String, Object>();
        containDatasetAttributes = containDatafileAttributes
                = containInvestigationAttributes = containSampleAttributes = false;
        // Check restriction is not null
        if (restCond != null) {
            extractJPQL(restCond);
            // If it's ordered
            if (restCond.getOderByAttr() != null) {
                String order = " DESC";
                if (restCond.isOrderByAsc())
                   order = " ASC";
                // Add order
                this.sentenceJPQL += " order by "
                            + getParamName(restCond.getOderByAttr())
                            + restCond.getOderByAttr().getValue()
                            + order;
            }
        }
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
     * @throws RestrictionINException
     * @throws RestrictionNullException
     */
    private void extractJPQL(RestrictionCondition restCond) throws RestrictionEmptyListException, DatevalueException, RestrictionOperatorException, RestrictionINException, RestrictionNullException {

        if (restCond.isIsNegate())
            addNotCondition();

        // If it's a parameterComparator
        if (restCond.getClass() == RestrictionComparisonCondition.class) {
            ((RestrictionComparisonCondition) restCond).validate();
            addRestrictionCondition((RestrictionComparisonCondition) restCond);
        }

        // If it's a ParameterLogicalCondition
        else if (restCond.getClass() == RestrictionLogicalCondition.class) {
            RestrictionLogicalCondition op = (RestrictionLogicalCondition) restCond;

            if (op.getRestConditions().isEmpty())
                throw new RestrictionEmptyListException();

            // Open parenthesis for the list of comparators
            openParenthesis();

            int size = op.getRestConditions().size();
            for (int i = 0; i < size; i++) {
                try {
                    if (op.getLogicalOperator() == LogicalOperator.OR)
                        extractJPQL(op.getRestConditions().get(i));
                    else
                        extractJPQL(op.getRestConditions().get(i));

                    // Not add last Logical Operator
                    if (i < (size - 1))
                        addCondition(op.getLogicalOperator());

                } catch (RestrictionEmptyListException ex) {
                    // In case there is empty listOperable
                }
            }
            // Close the parenthesis for the comparators
            closeParenthesis();
        }
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
    private void addRestrictionCondition(RestrictionComparisonCondition comp) throws DatevalueException, RestrictionOperatorException, RestrictionINException {
        sentenceJPQL += getParamName(comp.getRestAttr()) + comp.getRestAttr().getValue() + " "
                        + comp.getRestOp().getRestriction(getParamValue(comp));
    }

    /**
     * Return correct JPQL parameter name
     *
     * @param comp
     * @return
     */
    private String getParamName (RestrictionAttributes attr) {
        String paramName = Queries.PARAM_NAME_JPQL;
        // Restriction is over a Dataset search
        if (restType == RestrictionType.DATASET) {
            // Datafile attributes
            if (attr.isDatafile()) {
                paramName = "df";
                containDatafileAttributes = true;
            }
            // Sample attributes
            else if (attr.isSample()) {
                paramName = "sample";
                containSampleAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".investigation";
        }
        // Restriction is over a Datafile search
        else if (restType == RestrictionType.DATAFILE) {
            // Dataset attributes
            if (attr.isDataset())
                paramName += ".dataset";
            // Sample attributes
            else if (attr.isSample()) {
                paramName = "sample";
                containSampleAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".dataset.investigation";
        }
        // Restriction is over a Sample search
        else if (restType == RestrictionType.SAMPLE) {
            // Dataset attributes
            if (attr.isDataset()) {
                paramName = "ds";
                containDatasetAttributes = true;
            }
            // Datafile attributes
            else if (attr.isDatafile()) {
                paramName = "df";
                containDatafileAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation())
                paramName += ".investigation";
        }
        // Restriction is over a Investigation search
        else if (restType == RestrictionType.INVESTIGATION) {
            // Dataset attributes
            if (attr.isDataset()) {
                paramName = "ds";
                containDatasetAttributes = true;
            }
            // Datafile attributes
            else if (attr.isDatafile()) {
                paramName = "df";
                containDatafileAttributes = true;
            }
            // Sample attributes
            else if (attr.isSample()) {
                paramName = "sample";
                containSampleAttributes = true;
            }
        }
        return paramName + ".";
    }
        
    /**
     * Return the real value of the parameter.
     *
     * @param comp
     * @return
     */
    private String getParamValue (RestrictionComparisonCondition comp) throws DatevalueException, RestrictionOperatorException, RestrictionINException {
        String paramValue = "";
        // String operator. Value must be a String
        if (comp.getRestOp() == RestrictionOperator.CONTAIN ||
                comp.getRestOp() == RestrictionOperator.START_WITH ||
                comp.getRestOp() == RestrictionOperator.END_WITH) {
            if (comp.getRestAttr().isNumeric())
                throw new RestrictionOperatorException("Attribute is Numeric");
            else if (comp.getRestAttr().isDateTime())
                throw new RestrictionOperatorException("Attribute is Datetime");
            
            return comp.getValue().toString();
        }
        // IN operator
        else if (comp.getRestOp() == RestrictionOperator.IN) {
            // If value is an instance of Collection
            if (comp.getValue() instanceof Collection) {
                Collection col = (Collection) comp.getValue();
                String value = "";
                for (Object o : col)
                    value += ", '" + removeBadChar(o.toString()) + "'";

                return value.substring(2);
            }
            // If value is a String separated by ','
            else if (comp.getValue() instanceof String) {
                String value = removeBadChar(comp.getValue().toString());
                value = value.replaceAll("\\s*,\\s*", "','")
                             .replaceAll("^\\s*", "'")
                             .replaceAll("\\s*$", "'");
                return value;
            }
            // Restriciton exception. Operator IN only List<String> or String
            throw new RestrictionINException();
        }
        // Numeric, String or Date operator. Value is an Object
        else {
            paramValue = getNextParamName();
            // If attribute is a DateTime value
            if (comp.getRestAttr().isDateTime()) {
                // If value is String, transform into a Date
                if (comp.getValue().getClass() == String.class) {
                    try {
                        jpqlParameter.put(paramValue, Queries.dateFormat.parse(comp.getValue().toString()));
                    } catch (ParseException ex) {
                        Logger.getLogger(RestrictionUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // If value is a Date
                else if (comp.getValue() instanceof Date)
                    jpqlParameter.put(paramValue, comp.getValue());
                // Date value exception
                else
                    throw new DatevalueException(comp.getValue().toString());
            }
            // If attribute is a Numeric value
            else if (comp.getRestAttr().isNumeric()) {
                try {
                    if (comp.getValue().getClass() == String.class)
                        jpqlParameter.put(paramValue, Long.parseLong(comp.getValue().toString()));
                    else if (comp.getValue() instanceof Number)
                        jpqlParameter.put(paramValue, comp.getValue());
                } catch (Throwable t) {
                    Logger.getLogger(RestrictionUtil.class.getName()).log(Level.SEVERE, null, t);
                }
            }
            else
                jpqlParameter.put(paramValue, comp.getValue());

            return ":" + paramValue;
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

    public Map<String, Object> getJpqlParameter() {
        return jpqlParameter;
    }
}
