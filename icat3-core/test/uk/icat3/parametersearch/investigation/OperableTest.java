/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.investigation;

import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class OperableTest extends BaseParameterSearchTest {

    @Test
    public void datafileParameterTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {


        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);

        op2.add(pcDatafile.get(0));
        op2.add(pcDatafile.get(1));
        op1.add(op2);
        op1.add(pcDatafile.get(2));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, em);
        

       assertTrue("Results of investigations should be 2 not " + li.size(), (li.size() == 2));
    }

    @Test
    public void datasetParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, RestrictionException {

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDataset.get(0));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, em);

       assertTrue("Results of investigations should not be ZERO", (li.size() == 1));

    }

    @Test
    public void sampleParameterTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {

        ParameterLogicalCondition op1 = new ParameterLogicalCondition();
        ParameterLogicalCondition op2 = new ParameterLogicalCondition();
        ParameterLogicalCondition op3 = new ParameterLogicalCondition();
        ParameterLogicalCondition op4 = new ParameterLogicalCondition();

        op1.setOperator(LogicalOperator.AND);
        op2.setOperator(LogicalOperator.AND);
        op3.setOperator(LogicalOperator.OR);
        op4.setOperator(LogicalOperator.AND);

        op1.add(pcSample.get(0));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, em);


        assertTrue("Results of investigations should be 1 not " + li.size(), (li.size() == 1));
    }

    @Test
    public void allParameterTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {

        ParameterLogicalCondition op1 = new ParameterLogicalCondition();
        ParameterLogicalCondition op2 = new ParameterLogicalCondition();

        op1.setOperator(LogicalOperator.OR);
        op2.setOperator(LogicalOperator.AND);

        // (comp4 OR (comp1 AND comp2))
        op2.add(pcDatafile.get(0));
        op2.add(pcDataset.get(0));

        op1.add(pcDatafile.get(2));
        op1.add(op2);
        

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, em);


       assertTrue("Results of investigations should be 2 not " + li.size(), (li.size() == 2));
    }

    @Test
    public void allParameterTest2 () throws NoParameterTypeException, ParameterSearchException, RestrictionException {

        ParameterLogicalCondition op1 = new ParameterLogicalCondition();
        ParameterLogicalCondition op2 = new ParameterLogicalCondition();

        op1.setOperator(LogicalOperator.AND);
        op2.setOperator(LogicalOperator.AND);

        // (comp4 OR (comp1 AND comp2))
        op2.add(pcDatafile.get(0));
        op2.add(pcDataset.get(0));

        op1.add(pcDatafile.get(2));
        op1.add(op2);

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, em);


       assertTrue("Results of investigations should be ZERO not " + li.size(), (li.size() == 0));
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(OperableTest.class);
    }
}
