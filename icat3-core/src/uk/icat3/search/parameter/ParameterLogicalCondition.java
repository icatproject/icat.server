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
    public void add (ParameterCondition param) throws CyclicException  {
        if (param == this)
            throw new CyclicException("It's the same object");

        if (listComparable.contains(param))
            throw new CyclicException("This ParameterOperator has already been inserted");
        
        if (param.getClass() == ParameterLogicalCondition.class) {
            ParameterLogicalCondition op = (ParameterLogicalCondition)param;
            if (op.listComparable.contains(this))
                throw new CyclicException("Cyclic structure. " + this.toString());
            for (ParameterCondition p : listComparable)
                if (p.getClass() == ParameterLogicalCondition.class && op.listComparable.contains(p))
                    throw new CyclicException("Cyclic structure. " + this.toString());
        }
        listComparable.add(param);
    }


    ////////////////////////////////////////////////////////////////////////////
    //                               GETTERS and SETTERS
    //////////////////////////////////////////////////////////////////////
    public List<ParameterCondition> getListComparable() {
        return listComparable;
    }

    public void setListComparable(List<ParameterCondition> lc){
        listComparable = lc;
    }
    
    public LogicalOperator getLogicalOperator() {
        return operator;
    }

    public LogicalOperator getOperator(){
        return operator;
    }
    
    public void setOperator(LogicalOperator operator) {
        this.operator = operator;
    }
}
