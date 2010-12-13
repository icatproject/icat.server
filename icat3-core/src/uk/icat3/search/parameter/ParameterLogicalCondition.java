/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

import uk.icat3.exceptions.CyclicException;
import java.util.ArrayList;
import java.util.List;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.util.LogicalOperator;

/**
 * This class contains a list of ParameterCondition that defines a parameter
 * search structure.
 * 
 * @author cruzcruz
 * @see ParameterCondition
 */
public final class ParameterLogicalCondition extends ParameterCondition {

    /** List of ParameterCondition objects */
    private List<ParameterCondition> listComparable;
    /** Operator which joins the comparators */
    private LogicalOperator operator;


    /**
     *  Constructor
     */
    public ParameterLogicalCondition() {
        operator = null;
        listComparable = new ArrayList<ParameterCondition> ();
    }

    /**
     * Contructor
     * 
     * @param op Logical Operator to compare with
     */
    public ParameterLogicalCondition (LogicalOperator op) {
        listComparable = new ArrayList<ParameterCondition> ();
        this.operator = op;
    }
   

    /**
     * Add a new Parameter Operable into the list
     *
     * @param param Parmareter Operable to add
     * @throws CyclicException In case a cyclic structure had been build.
     */
    public ParameterLogicalCondition add (ParameterCondition param) throws CyclicException  {
        listComparable.add(param);
        return this;
    }

   /**
    * Check if there is a cyclic structure. A list of checked conditions are passed.
    * If this object belongs already to that object, there is a cyclic structure.
    * 
    * @param checkCondList
    * @throws CyclicException
    */
    private boolean validate (List<ParameterLogicalCondition> checkCondList) throws CyclicException, EmptyListParameterException  {
        boolean hasComparison = false;
        // Lists parameter Collection
        for (ParameterCondition param : listComparable) {
            // If parameter is null
            if (param == null)
                throw new EmptyListParameterException("A condition in List of ParameterCondition is NULL.");
            // If this object is inserted in its list
            if (this == param)
                throw new CyclicException("Cyclic structure. " + this.toString());
            // If the object is a parameter logical condition
            if (param.getClass() == ParameterLogicalCondition.class) {
                ParameterLogicalCondition op = ((ParameterLogicalCondition)param);
                // Check the object doesn't exists in the list of checked conditions
                if (checkCondList.contains(op))
                    throw new CyclicException("Cyclic structure. " + this.toString());
                // Add check condition to the list
                checkCondList.add(this);
                // Validate the condition
                if (op.validate(checkCondList))
                    hasComparison = true;
            }
            // If the object is a parameter logical condition
            else if (param instanceof ParameterCondition)
                hasComparison = true;
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
        return validate (new ArrayList<ParameterLogicalCondition>());
    }


    ////////////////////////////////////////////////////////////////////////
    //                               GETTERS and SETTERS                  //
    ////////////////////////////////////////////////////////////////////////
    public List<ParameterCondition> getListComparable() {
        return listComparable;
    }

    public void setListComparable(List<ParameterCondition> lc){
        listComparable = lc;
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    public void setOperator(LogicalOperator operator) {
        this.operator = operator;
    }
}
