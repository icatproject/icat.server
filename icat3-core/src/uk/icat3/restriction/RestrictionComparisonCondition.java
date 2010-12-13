/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restriction;

import java.util.Collection;
import java.util.Date;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionValueClassException;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.parameter.ComparisonOperator;

/**
 * This class contains information about the restriction and the value which is
 * going to be compare with.
 *
 * @author cruzcruz
 */
public class RestrictionComparisonCondition extends RestrictionCondition {
    /** Attribute to compare with */
    private RestrictionAttributes restrictionAttribute;
    /** Restriction operator */
    private ComparisonOperator comparisonOperator;
    /** Value to compare with attribute */
    private Object value;
    /** Second value for BETWEEN operator */
    private Object valueRight = null;

    /**
     * Construction
     */
    public RestrictionComparisonCondition() {
        this.restrictionAttribute = null;
        this.comparisonOperator = null;
        this.value = null;
    }
    /**
     * Constructor Between for dates
     * 
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, Date lValue, Object rValue){
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = lValue;
        this.valueRight = rValue;
    }

    /**
     * Constructor Between for numbers.
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, Number lValue, Number rValue){
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = lValue;
        this.valueRight = rValue;
    }

    /**
     * Constructor
     * 
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, String value) {
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = value;
    }
    /**
     * Constructor for IN
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, Collection value) {
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = value;
    }
    /**
     * Constructor
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, Date value) {
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = value;
    }
    /**
     * Constructor
     *
     * @param restAttr Restriction attribute to compare with value
     * @param restOp Restriction operator
     * @param value Restriction value to compare with attribute
     */
    public RestrictionComparisonCondition(RestrictionAttributes restAttr, ComparisonOperator restOp, Number value) {
        this.restrictionAttribute = restAttr;
        this.comparisonOperator = restOp;
        this.value = value;
    }
    /**
     * Check the restriction is well contructed.
     * 
     * @throws RestrictionNullException
     */
    public void validate () throws RestrictionNullException, RestrictionException {
        // Check if any private field is null.
        if (this.restrictionAttribute == null ||
                this.comparisonOperator == null ||
                this.value == null)
            throw new RestrictionNullException ();
        // If Between operator is selected, check if value2 is not null
        if (this.comparisonOperator == ComparisonOperator.BETWEEN && this.valueRight == null)
            throw new RestrictionNullException ();
        // Check if value and attribute belongs to same type
        if (this.restrictionAttribute.isObject()) {
            // Datafile attribute, value has to be Datafile class
            if (this.restrictionAttribute == RestrictionAttributes.DATAFILE &&
                    this.getValue().getClass() != Datafile.class)
                throw new RestrictionValueClassException("Attempt to set a value of type "
                        + this.getValue().getClass().getSimpleName()
                        + " where expected type of class Datafile");
            // Dataset attribute, value has to be Dataset class
            if (this.restrictionAttribute == RestrictionAttributes.DATASET &&
                    this.getValue().getClass() != Dataset.class)
                throw new RestrictionValueClassException("Attempt to set a value of type "
                        + this.getValue().getClass().getSimpleName()
                        + " where expected type of class Dataset");
            // Sample attribute, value has to be Sample class
            if (this.restrictionAttribute == RestrictionAttributes.SAMPLE &&
                    this.getValue().getClass() != Sample.class)
                throw new RestrictionValueClassException("Attempt to set a value of type "
                        + this.getValue().getClass().getSimpleName()
                        + " where expected type of class Sample");
            // Investigation attribute, value has to be Investigation class
            if (this.restrictionAttribute == RestrictionAttributes.INVESTIGATION &&
                    this.getValue().getClass() != Investigation.class)
                throw new RestrictionValueClassException("Attempt to set a value of type "
                        + this.getValue().getClass().getSimpleName()
                        + " where expected type of class Investigation");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //                     GETTERS and SETTERS                              //
    //////////////////////////////////////////////////////////////////////////

    public RestrictionAttributes getRestrictionAttribute() {
        return restrictionAttribute;
    }

    public void setRestrictionAttribute(RestrictionAttributes attribute) {
        this.restrictionAttribute = attribute;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValueRight() {
        return valueRight;
    }

    public void setValueRight(Object valueRight) {
        this.valueRight = valueRight;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }
}
