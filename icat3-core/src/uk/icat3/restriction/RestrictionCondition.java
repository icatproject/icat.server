/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restriction;

import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.Condition;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.SampleInclude;

/**
 * This class represents restriction conditions with which we
 * construct the restrictions structures.
 *
 * @author cruzcruz
 */
public class RestrictionCondition extends Condition {
    /** Contain order by field, if exists */
    private RestrictionAttributes orderByAttribute = null;
    /** Indicates the order direction */
    private boolean orderByAsc = true;
    /** Maximun number of results to return */
    private Integer maxResults;
    /** Number of firs result */
    private Integer firstResult;
    /** Investigation include options */
    private InvestigationInclude investigationInclude = null;
    /** Dataset include options */
    private DatasetInclude datasetInclude = null;
    /** Datafile include options */
    private DatafileInclude datafileInclude = null;
    /** Sample include options */
    private SampleInclude sampleInclude = null;
    /** Return only identification long without limit of number results */
    private boolean returnLongId = false;

    /**
     * Negates this condition
     *
     * @param cond Parameter condition
     *
     * @return Object itself
     */
    public static RestrictionCondition Not (RestrictionCondition cond) {
        cond.setNegate(true);
        return cond;
    }
    /**
     * Creates an order by descend object
     *
     * @param attr Attriube to order by
     * @return
     */
    public static RestrictionCondition orderByDesc (RestrictionAttributes attr) {
        RestrictionCondition a = new RestrictionCondition();
        a.setOrderByDesc(attr);
        return a;
    }
    /**
     * Creates an order by ascend object
     *
     * @param attr Attriube to order by
     * @return
     */
    public static RestrictionCondition orderByAsc (RestrictionAttributes attr) {
        RestrictionCondition a = new RestrictionCondition();
        a.setOrderByAsc(attr);
        return a;
    }

    /**
     * Set descending order and attribute
     *
     * @param attr Attribute to order by
     */
    public RestrictionCondition setOrderByDesc (RestrictionAttributes attr) {
        this.orderByAttribute = attr;
        this.orderByAsc = false;
        return this;
    }

    /**
     * Check if max results was set
     *
     * @return true it max results was set. Otherwise false.
     */
    public boolean hasMaxResults () {
        if (this.maxResults == null || this.maxResults <= 0)
            return false;
        return true;
    }
    /**
     * Check if max results was set
     *
     * @return true it max results was set. Otherwise false.
     */
    public boolean hasFirstResults () {
        if (this.firstResult == null || this.firstResult <= 0)
            return false;
        return true;
    }
    /**
     * Check if has include options
     * 
     * @return true if any include options were defined. Otherwise false
     */
    public boolean hasInclude(RestrictionType restType) {
        if (this.sampleInclude != null && restType == RestrictionType.SAMPLE)
            return true;
        if (this.investigationInclude != null && restType == RestrictionType.INVESTIGATION)
            return true;
        if (this.datafileInclude != null && restType == RestrictionType.DATAFILE)
            return true;
        if (this.datasetInclude != null && restType == RestrictionType.DATASET)
            return true;
        return false;
    }

    public Enum getInclude (RestrictionType restType) {
         if (this.sampleInclude != null && restType == RestrictionType.SAMPLE)
            return this.sampleInclude;
        if (this.investigationInclude != null && restType == RestrictionType.INVESTIGATION)
            return this.investigationInclude;
        if (this.datafileInclude != null && restType == RestrictionType.DATAFILE)
            return this.datafileInclude;
        if (this.datasetInclude != null && restType == RestrictionType.DATASET)
            return this.datasetInclude;
        return null;
    }

    ////////////////////////////////////////////////////////////////////
    //                   GETTERS and SETTERS                          //
    ////////////////////////////////////////////////////////////////////

    /**
     * Set ascending order and attribute
     *
     * @param attr Attribute to order by
     */
    public void setOrderByAsc (RestrictionAttributes attr) {
        this.orderByAttribute = attr;
        this.orderByAsc = true;
    }
    
    /**
     * Check if the restriction has order by
     * 
     * @return true if has order by. Otherwise false
     */
    public boolean hasOrder () {
        if (this.orderByAttribute == null)
            return false;
        return true;
    }
    /**
     * Check if order is Ascending
     *
     * @return True if this condition is negated. Otherwise false
     */
    public boolean isOrderByAsc() {
        return orderByAsc;
    }
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setOrderByAsc(boolean orderByAsc) {
        this.orderByAsc = orderByAsc;
    }

    public RestrictionAttributes getOrderByAttribute() {
        return orderByAttribute;
    }

    public void setOrderByAttribute(RestrictionAttributes orderByAttribute) {
        this.orderByAttribute = orderByAttribute;
    }

    /**
     * Number maximun of results to return.
     *
     * @return maximun results to return
     */
    public int getMaxResults() {
        if (maxResults == null)
            return -1;
        return maxResults;
    }

    public Integer getFirstResult() {
        if (firstResult == null)
            return -1;
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public DatafileInclude getDatafileInclude() {
        return datafileInclude;
    }

    public void setDatafileInclude(DatafileInclude datafileInclude) {
        this.datafileInclude = datafileInclude;
    }

    public DatasetInclude getDatasetInclude() {
        return datasetInclude;
    }

    public void setDatasetInclude(DatasetInclude datasetInclude) {
        this.datasetInclude = datasetInclude;
    }

    public InvestigationInclude getInvestigationInclude() {
        return investigationInclude;
    }

    public void setInvestigationInclude(InvestigationInclude investigationInclude) {
        this.investigationInclude = investigationInclude;
    }

    public SampleInclude getSampleInclude() {
        return sampleInclude;
    }

    public void setSampleInclude(SampleInclude sampleInclude) {
        this.sampleInclude = sampleInclude;
    }

    public boolean isReturnLongId() {
        return returnLongId;
    }

    public void setReturnLongId(boolean returnLongId) {
        this.returnLongId = returnLongId;
    }
}
