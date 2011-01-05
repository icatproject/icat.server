/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.sample;

import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.search.parameter.util.ParameterSearch;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.SampleSearch;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 *
 * @author cruzcruz
 */
public class SampleTest extends BaseParameterSearchTest {

    @Test
    public void listParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, RestrictionException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
        lp.add(pv2);
        lp.add(pv3);
        
        List<Sample> ls = (List<Sample>) SampleSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);

        assertFalse("Results of investigations should not be ZERO", (ls.size() == 0));
    }

    /**
     * List comparators Test
     *
     * @throws NoParameterTypeException
     * @throws ParameterSearchException
     */
    @Test
    public void listComparatorTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDataset.get(1));
        lc.add(pcSample.get(1));
        lc.add(pcDatafile.get(2));

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);

       assertTrue("Results of investigations should not be ZERO", (ld.size() == 1));
    }

    /**
     * Operable test
     *
     * @throws NoParameterTypeException
     * @throws ParameterSearchException
     */
    @Test
    public void operableTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);

        op2.add(pcSample.get(0));
        op2.add(pcDatafile.get(1));
        op2.add(pcDataset.get(0));
        op1.add(op2);
        op1.add(pcSample.get(1));

        List<Sample> li = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);

       assertTrue("Results of investigations should be 2 not " + li.size(), (li.size() == 2));
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(SampleTest.class);
    }
}
