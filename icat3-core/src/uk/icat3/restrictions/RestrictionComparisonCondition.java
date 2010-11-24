/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restrictions;

import java.util.Collection;
import java.util.Date;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.restrictions.attributes.RestrictionAttributes;

/**
 *
 * @author cruzcruz
 */
public class RestrictionComparisonCondition extends RestrictionCondition {

    RestrictionAttributes restAttr;
    RestrictionOperator restOp;
    Object value;

    /**
     * Constructor
     * 
     * @param restAttr
     * @param restOp
     * @param value
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, String value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr
     * @param restOp
     * @param value
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Collection<String> value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr
     * @param restOp
     * @param value
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Date value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param restAttr
     * @param restOp
     * @param value
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, RestrictionOperator restOp, Number value) {
        this.restAttr = restAttr;
        this.restOp = restOp;
        this.value = value;
    }

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
