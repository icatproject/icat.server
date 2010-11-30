/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restriction;

import uk.icat3.restriction.attribute.RestrictionAttributes;

/**
 * This class represents restriction conditions with which we
 * construct the restrictions structures.
 *
 * @author cruzcruz
 */
public class RestrictionCondition {
    /** Indicates if restriction condition is negated (NOT) */
    private boolean isNegate = false;
    /** Contain order by field, if exists */
    private RestrictionAttributes orderByAttr = null;
    /** Indicates the order direction */
    private boolean orderByAsc;

    /**
     * Negates the restriction condition 'cond'
     *
     * @param cond Restriction condition to negate
     * @return
     */
    public static RestrictionCondition Not (RestrictionCondition cond) {
        cond.isNegate = true;
        return cond;
    }

    public static RestrictionLogicalCondition orderByDesc (RestrictionAttributes attr) {
        RestrictionLogicalCondition a = new RestrictionLogicalCondition(null);
        a.setOrderByDesc(attr);
        return a;
    }

    public static RestrictionLogicalCondition orderByAsc (RestrictionAttributes attr) {
        RestrictionLogicalCondition a = new RestrictionLogicalCondition(null);
        a.setOrderByAsc(attr);
        return a;
    }
    /**
     * Set ascending order and attribute
     *
     * @param attr Attribute to order by
     */
    public void setOrderByAsc (RestrictionAttributes attr) {
        this.orderByAttr = attr;
        this.orderByAsc = true;
    }
    /**
     * Set descending order and attribute
     *
     * @param attr Attribute to order by
     */
    public RestrictionCondition setOrderByDesc (RestrictionAttributes attr) {
        this.orderByAttr = attr;
        this.orderByAsc = false;
        return this;
    }
    /**
     * Check if this condition is negated
     *
     * @return True if this condition is negated. Otherwise false
     */
    public boolean isIsNegate() {
        return isNegate;
    }
    /**
     * Get attribute to order by.
     *
     * @return Attribute to order by.
     */
    public RestrictionAttributes getOderByAttr() {
        return orderByAttr;
    }

    public boolean hasOrder () {
        if (this.orderByAttr == null)
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
}
