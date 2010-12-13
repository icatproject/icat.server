/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

import uk.icat3.search.Condition;

/**
 * This is the parent class for ParameterOperator and ParameterComparator, and
 * it's used to create the parameter search structure.
 *
 * This is an example of how it works:
 *
 * ParameterComparator comp1 = new ParameterComparator (PRESS, EQUAL,
 *       new Float(21.00), SAMPLE);
 * ParameterComparator comp2 = new ParameterComparator (TEMP, LESS_THAN,
 *       new Float(2.10), DATAFILE);
 * ParameterComparator comp3 = new ParameterComparator (TEMP, LESS_THAN,
 *       new Float(2.10), DATAFILE);

 * ParameterOperator paramOp = new ParameterOperator (AND);

 * paramOp.add(new ParameterComparator (PRESS, EQUAL,new Float(21.00),
 *             SAMPLE));
 * paramOp.add(new ParameterOperator
 *             (OR,new ParameterCondition[]{comp1, comp2, comp3})
 *            )
 * paramOp.add(new ParameterComparator (VOLT, EQUAL, new Float(15.10),
 *             DATASET))
 *
 * 
 * @author cruzcruz
 * @see ParameterComparator
 * @see ParameterOperator
 */
 public class  ParameterCondition extends Condition {

    /**
     * Negates this condition
     *
     * @param cond Parameter condition
     *
     * @return Object itself
     */
    public static ParameterCondition Not (ParameterCondition cond) {
        cond.setNegate(true);
        return cond;
    }
}
