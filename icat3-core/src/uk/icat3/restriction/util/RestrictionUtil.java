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
 *
 * @author cruzcruz
 */
public class RestrictionUtil {

    private String sentenceJPQL;
    private RestrictionType restType;
    private boolean containSampleAttributes;
    private boolean containDatafileAttributes;
    private boolean containDatasetAttributes;
    private boolean containInvestigationAttributes;
    private final String PARAM_NAME = "restric";
    private int contParameter;
    /** List of JPQL parameters */
    private Map<String, Object> jpqlParameter;

    public RestrictionUtil(RestrictionCondition restCond, RestrictionType restType) throws RestrictionEmptyListException, DatevalueException, RestrictionOperatorException, RestrictionINException, RestrictionNullException  {
        sentenceJPQL = "";
        this.restType = restType;
        contParameter = 0;
        jpqlParameter = new HashMap<String, Object>();
        containDatasetAttributes = containDatafileAttributes
                = containInvestigationAttributes = containSampleAttributes = false;

        if (restCond != null) {
            extractJPQL(restCond);
            // If it's ordered
            if (restCond.getOderByAttr() != null) {
                String order = " DESC";
                if (restCond.isOrderByAsc())
                   order = " ASC";

                this.sentenceJPQL += " order by "
                            + getParamName(restCond.getOderByAttr())
                            + restCond.getOderByAttr().getValue()
                            + order;
            }
        }
    }


    public boolean isEmpty () {
        return this.sentenceJPQL.isEmpty();
    }

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

    private void openParenthesis() {
        sentenceJPQL += "(";
    }

    private void addCondition(LogicalOperator logicalOperator) {
       sentenceJPQL += " " + logicalOperator.name() + " ";
    }

    private void addNotCondition() {
        sentenceJPQL += " NOT ";
    }

    private void closeParenthesis() {
        sentenceJPQL += ")";
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
     * Return correcto JPQL parameter name
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
            else if (attr.isInvestigation()) {
                paramName += ".investigation";
            }
        }
        // Restriction is over a Datafile search
        else if (restType == RestrictionType.DATAFILE) {
            // Dataset attributes
            if (attr.isDataset()) {
                paramName = ".dataset";
            }
            // Sample attributes
            else if (attr.isSample()) {
                paramName = "sample";
                containSampleAttributes = true;
            }
            // Investigation attributes
            else if (attr.isInvestigation()) {
                paramName += ".dataset.investigation";

            }
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
                paramName += "sample";
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
            if (comp.getValue() instanceof Collection) {
                Collection col = (Collection) comp.getValue();
                String value = "";
                for (Object o : col) {
                    value += ", '" + removeBadChar(o.toString()) + "'";
                }

                return value.substring(2);
            }
            else if (comp.getValue() instanceof String) {
                String value = removeBadChar(comp.getValue().toString());
                value = value.replaceAll("\\s*,\\s*", "','")
                             .replaceAll("^\\s*", "'")
                             .replaceAll("\\s*$", "'");
                return value;
            }
            throw new RestrictionINException();
        }
        // Numeric, String or Date operator. Value is an Object
        else {
            paramValue = getNextParamName();

            // If attribute is a DateTime value
            if (comp.getRestAttr().isDateTime()) {
                if (comp.getValue().getClass() == String.class) {
                    try {
                        jpqlParameter.put(paramValue, Queries.dateFormat.parse(comp.getValue().toString()));
                    } catch (ParseException ex) {
                        Logger.getLogger(RestrictionUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (comp.getValue() instanceof Date)
                    jpqlParameter.put(paramValue, comp.getValue());
                else
                    throw new DatevalueException(comp.getValue().toString());
            }
            // If attribute is a Numeric value
            else if (comp.getRestAttr().isNumeric()) {
                try {
                    if (comp.getValue().getClass() == String.class)
                        jpqlParameter.put(paramValue, Long.parseLong(comp.getValue().toString()));
                } catch (Throwable t) {
                    Logger.getLogger(RestrictionUtil.class.getName()).log(Level.SEVERE, null, t);
                }
            }
            else
                jpqlParameter.put(paramValue, comp.getValue());

            return ":" + paramValue;
        }
    }

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
