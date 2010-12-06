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
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.EmptyOperatorException;
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
    /** Indicates if there was conditions comparisons defined */
    private boolean conditionsDefined;
    /** Indicates start of index */
    private int startIndex;

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
    public RestrictionUtil(RestrictionCondition restCond, RestrictionType restType) throws DatevalueException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, RestrictionEmptyListException, CyclicException, EmptyOperatorException  {
        // Initialites variables
        this.sentenceJPQL = "";
        this.conditionsDefined = false;
        this.orderByJPQL = "";
        this.maxResults = -1;
        this.startIndex = 0;
        this.orderByAsc = false;
        this.orderByAttr = null;
        this.restType = restType;
        contParameter = 0;
        jpqlParameter = new HashMap<String, Object>();
        containDatasetAttributes = containDatafileAttributes = containInvestigatorAttributes
                = containInvestigationAttributes = containSampleAttributes = false;
        // Check restriction is not null
        if (restCond != null) {
            extractJPQL(restCond);
            if (!conditionsDefined)
                sentenceJPQL = "";
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

    public int getStartIndex() {
        return startIndex;
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
    private void extractJPQL(RestrictionCondition restCond) throws DatevalueException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, CyclicException, EmptyOperatorException {
        // Check if this condition is negated
        if (restCond.isNegate())
            addNotCondition();

        // If it's a parameterComparator
        if (restCond.getClass() == RestrictionComparisonCondition.class) {
            ((RestrictionComparisonCondition) restCond).validate();
            this.conditionsDefined = true;
            addRestrictionCondition((RestrictionComparisonCondition) restCond);
        }

        // If it's a ParameterLogicalCondition
        else if (restCond.getClass() == RestrictionLogicalCondition.class) {
            RestrictionLogicalCondition op = (RestrictionLogicalCondition) restCond;
            op.validate();

            // Open parenthesis for the list of comparators
            openParenthesis();
            // Numbers of conditions
            int size = op.getRestConditions().size();
            // Read all conditions
            for (int i = 0; i < size; i++) {
                if (op.getOperator() == LogicalOperator.OR)
                    extractJPQL(op.getRestConditions().get(i));
                else
                    extractJPQL(op.getRestConditions().get(i));

                // Not add last Logical Operator
                if (i < (size - 1))
                    addCondition(op.getOperator());
            }
            // Close the parenthesis for the comparators
            closeParenthesis();
        }
        // Check if maximun of results was set in any condition
        if (restCond.hasMaxResults()) {
            this.maxResults = restCond.getMaxResults();
            this.startIndex = 0;
        }
        // Check if order was set in any condition
        if (restCond.hasOrder()) {
            this.orderByAsc = restCond.isOrderByAsc();
            this.orderByAttr = restCond.getOrderByAttr();
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
        // Add restricion.
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
        // Restriction is for Dataset search
        if (restType == RestrictionType.DATASET) {
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
            // Investigator attributes
            else if (attr.isInvestigator()) {
                paramName = Queries.INVESTIGATOR_NAME;
                containInvestigatorAttributes = true;
            }
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
            // Investigator attributes
            else if (attr.isInvestigator()) {
                paramName = Queries.INVESTIGATOR_NAME;
                containInvestigatorAttributes = true;
            }
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
            // Investigator attributes
            else if (attr.isInvestigator()) {
                paramName = Queries.INVESTIGATOR_NAME;
                containInvestigatorAttributes = true;
            }
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
            // Investigator attributes
            else if (attr.isInvestigator()) {
                paramName = Queries.INVESTIGATOR_NAME;
                containInvestigatorAttributes = true;
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
            // Attribute is a Number, but should be String
            if (comp.getRestAttr().isNumeric())
                throw new RestrictionOperatorException("Attribute is Numeric");
            // Attribute is a Date, but should be String
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
        // BETWEEN operator
        else if (comp.getRestOp() == RestrictionOperator.BETWEEN) {
            paramValue = getNextParamName();
            String paramValue2 = getNextParamName();
            jpqlParameter.put(paramValue, comp.getValue());
            jpqlParameter.put(paramValue2, comp.getValueRigth());
            return comp.getRestOp().getRestrictionBetween(":" + paramValue, ":" + paramValue2);
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
        }
        // Sample
        else if (searchType == ElementType.SAMPLE) {
            // Datafile
            if (this.isContainDatafileAttributes() && parameterType == ElementType.DATAFILE) {
                restrictionParam += ", IN(" + Queries.DATASET_NAME
                        + ".datafileCollection) " + Queries.DATAFILE_NAME;
            }
            // Dataset
            else if (this.isContainDatasetAttributes() && parameterType == ElementType.DATASET)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL
                        + ".investigationId.datasetCollection) " + Queries.DATASET_NAME;
            
            // Investigator
            if (this.isContainInvestigatorAttributes() && parameterType == ElementType.INVESTIGATOR)
                restrictionParam += ", IN(" + Queries.PARAM_NAME_JPQL + ".investigationId.investigatorCollection) "
                        + Queries.INVESTIGATOR_NAME;
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

    public boolean isContainInvestigatorAttributes() {
        return containInvestigatorAttributes;
    }

    public Map<String, Object> getJpqlParameter() {
        return jpqlParameter;
    }
}
