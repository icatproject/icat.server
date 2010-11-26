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
    private LogicalOperator logicalOperator;

    /**
     * Constructor.
     * 
     * @param logicalOp
     */
    public RestrictionLogicalCondition(LogicalOperator logicalOperator) {
        restConditions = new ArrayList<RestrictionCondition> ();
        this.logicalOperator = logicalOperator;
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
        // The restriction object itself has been added.
        if (restCondition == this)
            throw new CyclicException("It's the same object");
        // The restriction object already exists
        if (restConditions.contains(restCondition))
            throw new CyclicException("This ParameterOperator has already been inserted");
        // If it's a logical condition
        if (restCondition.getClass() == RestrictionLogicalCondition.class) {
            RestrictionLogicalCondition op = (RestrictionLogicalCondition)restCondition;
            // Check if this object is contained inside the restCondition
            if (op.restConditions.contains(this))
                throw new CyclicException("Cyclic structure. " + this.toString());
            // Check for each condition in the list, restCondition doesn't exists
            for (RestrictionCondition p : restConditions)
                if (p.getClass() == RestrictionLogicalCondition.class && op.restConditions.contains(p))
                    throw new CyclicException("Cyclic structure. " + this.toString());
        }
        // Add new condition to the list
        restConditions.add(restCondition);
        // Return this object
        return this;
    }


    //////////////////////////////////////////////////////////////
    //                  GETTERS and SETTERS                     //
    //////////////////////////////////////////////////////////////
    
    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public List<RestrictionCondition> getRestConditions() {
        return restConditions;
    }


}
