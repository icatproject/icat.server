/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restriction;

import java.util.Collection;
import java.util.Date;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.restriction.attribute.RestrictionAttributes;

/**
 * This class contains information about the restriction and the value which is
 * going to be compare with.
 *
 * @author cruzcruz
 */
public class RestrictionComparisonCondition extends RestrictionCondition {
    /** Attribute to compare with */
    private RestrictionAttributes restAttr;
    /** Restriction operator */
    private RestrictionOperator restOp;
    /** Value to compare with attribute */
    private Object value;

    /**
     * Constructor
     * 
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, String value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Collection<String> value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Date value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Number value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }
    /**
     * Check the restriction is well contructed.
     * 
     * @throws RestrictionNullException
     */
    public void validate () throws RestrictionNullException {
        if (this.restAttr == null ||
                this.restOp == null ||
                this.value == null)
            throw new RestrictionNullException ();
    }

    //////////////////////////////////////////////////////////////////////////
    //                     GETTERS and SETTERS                              //
    //////////////////////////////////////////////////////////////////////////

    public RestrictionAttributes getRestAttr() {
        return restAttr;
    }

    public void setRestAttr(RestrictionAttributes restAttr) {
        this.restAttr = restAttr;
    }

    public RestrictionOperator getRestOp() {
        return restOp;
    }

    public void setRestOp(RestrictionOperator restOp) {
        this.restOp = restOp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
