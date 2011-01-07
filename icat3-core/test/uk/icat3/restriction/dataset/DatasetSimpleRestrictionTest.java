/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.dataset;

import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import static org.junit.Assert.*;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class DatasetSimpleRestrictionTest extends BaseParameterSearchTest {


    @Test
    public void differentsAttr () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.STARTS_WITH, "dataset");
        restriction1.setSensitive(true);

        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, ComparisonOperator.ENDS_WITH, ""))
                ;

        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restrLog, em);

        assertEquals("Results of Datasets incorrect.", 3, li.size());
//        assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(1).getDatafileCollection().size());
    }

    @Test
    public void restrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.CONTAINS, "dataset");
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 1, li.size());
       assertTrue("Dataset name incorrect.", li.get(0).getName().contains("dataset_2"));
    }

    @Test
    public void betweenTest () throws ParameterSearchException, RestrictionException {
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, li.size());
       assertTrue("Dataset name incorrect.", li.get(0).getName().contains("dataset_1"));
    }

    @Test
    public void restrictionCondition2Test () throws ParameterSearchException, RestrictionException {
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR)
                    .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                    .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))

                    )
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 3, li.size());
       assertTrue("Dataset name incorrect.", li.get(0).getName().contains("dataset_1"));
    }

    @Test
    public void restrictionComparisonTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue");

        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restriction1
                , DatasetInclude.NONE, 1, -1, em);

        assertEquals("Results of datasets incorrect.", 2, li.size());
//        assertEquals("Number of Results of Datasets of 'dataset_1' are incorrect.",
//               2, li.get(0).getDatasetCollection().size());
    }

    @Test
    public void restrictionLogicalConditionTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        // Restriction condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.CONTAINS, "dataset");
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, li.size());
       assertTrue("Dataset name incorrect.", li.get(0).getName().contains("dataset_1"));
    }

    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {

        RestrictionCondition cond = new RestrictionCondition();
        cond.setReturnLongId(true);

        List li = (List) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, cond
                , DatasetInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of datasets should be at least 1, not " + li.size(), (li.size() > 2));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatasetSimpleRestrictionTest.class);
    }
}
