/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restrictions;

import java.util.ArrayList;
import java.util.List;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author cruzcruz
 */
public class RestrictionLogicalCondition extends RestrictionCondition {

    private List<RestrictionCondition> restConditions;
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
     * @param restCondition
     * @return
     * @throws CyclicException
     */
    public RestrictionLogicalCondition add (RestrictionCondition restCondition) throws CyclicException {
        if (restCondition == this)
            throw new CyclicException("It's the same object");

        if (restConditions.contains(restCondition))
            throw new CyclicException("This ParameterOperator has already been inserted");

        if (restCondition.getClass() == RestrictionLogicalCondition.class) {
            RestrictionLogicalCondition op = (RestrictionLogicalCondition)restCondition;
            if (op.restConditions.contains(this))
                throw new CyclicException("Cyclic structure. " + this.toString());
            for (RestrictionCondition p : restConditions)
                if (p.getClass() == RestrictionLogicalCondition.class && op.restConditions.contains(p))
                    throw new CyclicException("Cyclic structure. " + this.toString());
        }
        restConditions.add(restCondition);
        
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
