/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restrictions;

import uk.icat3.restrictions.attributes.RestrictionAttributes;

/**
 *
 * @author cruzcruz
 */
public class RestrictionCondition {

    private boolean isNegate = false;
    private RestrictionAttributes oderByAttr = null;
    private boolean orderByAsc;

    public static RestrictionCondition Not (RestrictionCondition cond) {
        cond.isNegate = true;
        return cond;
    }


    public void setOrderByAsc (RestrictionAttributes attr) {
        this.oderByAttr = attr;
        this.orderByAsc = true;
    }

     public RestrictionCondition setOrderByDesc (RestrictionAttributes attr) {
        this.oderByAttr = attr;
        this.orderByAsc = false;
        return this;
    }

    public boolean isIsNegate() {
        return isNegate;
    }

    public RestrictionAttributes getOderByAttr() {
        return oderByAttr;
    }

    public boolean isOrderByAsc() {
        return orderByAsc;
    }
}
