/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restriction;

import java.util.ArrayList;
import java.util.List;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.util.LogicalOperator;

/**
 * This class represents a restriction logical condition in which it is
 * defined logical operator combinations. That means this
 * object aims to construct AND, OR operations.
 * 
 * @author cruzcruz
 */
public class RestrictionLogicalCondition extends RestrictionCondition {
    /** List of restrictions */
    private List<RestrictionCondition> restConditions;
    /** Restriction locial operator */
    private LogicalOperator operator;

    /**
     * Constructor
     */
    public RestrictionLogicalCondition() {
        this.operator = LogicalOperator.AND;
        restConditions = new ArrayList<RestrictionCondition> ();
    }
    /**
     * Constructor.
     * 
     * @param logicalOp
     */
    public RestrictionLogicalCondition(LogicalOperator logicalOperator) {
        restConditions = new ArrayList<RestrictionCondition> ();
        this.operator = logicalOperator;
    }

    /**
     * Add a new restriction condition
     * 
     * @param restCondition Restriction condition
     * @return this object
     * 
     * @throws CyclicException
     */
    public RestrictionLogicalCondition add (RestrictionCondition restCondition) throws CyclicException {
        // Add new condition to the list
        restConditions.add(restCondition);
        // Return this object
        return this;
    }

    /**
    * Check if there is a cyclic structure. A list of checked conditions are passed.
    * If this object belongs already to that object, there is a cyclic structure.
    *
    * @param checkCondList
    * @throws CyclicException
    */
    private boolean validate (List<RestrictionCondition> checkCondList) throws CyclicException, EmptyListParameterException {
        boolean hasComparison = false;
        for (RestrictionCondition restCondition : restConditions) {
            if (restCondition == null)
                throw new EmptyListParameterException("A condition in List of RestrictionCondition is NULL.");
            // The restriction object itself has been added.
            if (restCondition == this)
                throw new CyclicException("It's the same object");
            // If it's a logical condition
            if (restCondition.getClass() == RestrictionLogicalCondition.class) {
                RestrictionLogicalCondition op = (RestrictionLogicalCondition)restCondition;
                // Check if the condition 'op' already exists
                if (checkCondList.contains(op))
                    throw new CyclicException("Cyclic structure. " + this.toString());
                // Add checked condition to check condition list
                checkCondList.add(this);
                // Validate other restriction logical condition
                if (op.validate(checkCondList))
                    hasComparison = true;
            }
            // If it's a comparison condition
            else if (restCondition.getClass() == RestrictionComparisonCondition.class) {
                hasComparison = true;
            }
        }
        return hasComparison;
    }

    /**
     * Check if this object is well construct
     *
     * @throws CyclicException
     * @throws EmptyOperatorException
     */
    public boolean validate () throws CyclicException, EmptyOperatorException, EmptyListParameterException {
        if (this.operator == null)
            throw new EmptyOperatorException();
        return validate (new ArrayList<RestrictionCondition>());
    }


    //////////////////////////////////////////////////////////////
    //                  GETTERS and SETTERS                     //
    //////////////////////////////////////////////////////////////
    
    public LogicalOperator getOperator() {
        return operator;
    }

    public List<RestrictionCondition> getRestConditions() {
        return restConditions;
    }

    public void setRestConditions(List<RestrictionCondition> restConditions) {
        this.restConditions = restConditions;
    }

    public void setOperator(LogicalOperator logicalOperator) {
        this.operator = logicalOperator;
    }
}
