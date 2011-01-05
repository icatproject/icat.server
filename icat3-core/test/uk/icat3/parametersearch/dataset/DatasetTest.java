/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.dataset;

import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.search.parameter.util.ParameterSearch;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.RestrictionException;
import static org.junit.Assert.*;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.DatasetSearch;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class DatasetTest extends BaseParameterSearchTest {


    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
        lp.add(pv2);
        lp.add(pv3);

        RestrictionCondition cond = new RestrictionCondition();
        cond.setReturnLongId(true);

        List li = (List) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, cond
                , DatasetInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of datasets should be 1, not " + li.size(), (li.size() == 1));
        assertEquals("Object should be Long, not ", Long.class, li.get(0).getClass());
    }

//    @Test
//    public void returnCountTest () throws NoParameterTypeException, ParameterSearchException, RestrictionException {
//        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
//        ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
//
//        op2.add(pcDataset.get(0));
//        op2.add(pcSample.get(0));
//        op2.add(pcDatafile.get(1));
//        op1.add(op2);
//        op1.add(pcDataset.get(1));
//
//        List li = (List) DatasetSearch
//                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, null
//                , DatasetInclude.DATASET_NUMBER_OF_RESULTS
//                , Queries.NO_PAGINATION, Queries.NO_PAGINATION, em);
//
//        assertTrue("Result object type should be Long, Not " + li.get(0).getClass().getName()
//                , (li.get(0).getClass() == Long.class));
//        assertTrue("Results of investigations should be 2 Not " + (Long)li.get(0)
//                , (((Long)li.get(0)) == 2));
//    }

    @Test
    public void listParameterTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
        lp.add(pv2);
        lp.add(pv3);

        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, -1, -1, em);

        assertTrue("Results of datasets should be 1, not " + li.size(), (li.size() == 1));
        
    }

    /**
     * List comparators Test
     *
     * @throws NoParameterTypeException
     * @throws ParameterSearchException
     */
    @Test
    public void listComparatorTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDataset.get(1));
        lc.add(pcSample.get(1));
        lc.add(pcDatafile.get(2));

        List<Dataset> ld = (List<Dataset>) DatasetSearch
                .searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, DatasetInclude.NONE, -1, -1, em);

       assertEquals("Results of datasets incorrect.", 1, ld.size());
    }


    /**
     * Operable test
     *
     * @throws NoParameterTypeException
     * @throws ParameterSearchException
     */
    @Test
    public void operableTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);

        op2.add(pcDataset.get(0));
        op2.add(pcSample.get(0));
        op2.add(pcDatafile.get(1));
        op1.add(op2);
        op1.add(pcDataset.get(1));

        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, DatasetInclude.NONE, -1, -1, em);

       assertEquals("Results of datasets incorrect.", 2, li.size());
    }
    

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatasetTest.class);
    }
}
