/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.sample;

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
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import static org.junit.Assert.*;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.SampleInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class SampleSimpleRestrictionTest extends BaseParameterSearchTest {

//    @Test
//    public void datafilesIncludeDatafileTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
//        // Restriction condition
//        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
//                RestrictionAttributes.SAMPLE_TITLE, ComparisonOperator.CONTAINS, "gation 2");
//        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
//                .add(RestrictionCondition.Not(restriction1))
//                .add(new RestrictionComparisonCondition(
//                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
//                ;
//        // Parameter condition
//        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
//
//        op1.add(pcDataset.get(0));
//        op1.add(pcDataset.get(1));
//        op1.add(pcSample.get(0));
//        op1.add(pcDatafile.get(1));
//
//        List<Sample> li = (List<Sample>) SampleSearch
//                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
//                        , restricLog, SampleInclude.DATASET_DATAFILES_AND_PARAMETERS, 1, -1, em);
//
//       assertEquals("Results of Samples incorrect.", 1, li.size());
//       assertTrue("Sample name should be 'sample_1', not " + li.get(0).getName(),
//               (li.get(0).getName().contains("sample_1")));
//       assertEquals("Number of Results of Datafiles of 'sample_1' are incorrect.",
//               2, li.get(0).getDatafileCollection().size());
//    }

    @Test
    public void differentsAttr () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {


        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, ComparisonOperator.STARTS_WITH, "Sample_"))
                ;

        // Sample search
        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restrLog, em);

        assertEquals("Results of Samples incorrect.", 2, li.size());
//        assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(1).getDatafileCollection().size());
    }

    @Test
    public void restrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, SampleInclude.NONE, 1, -1, em);

       assertEquals("Results of Samples incorrect.", 1, li.size());
       assertTrue("Sample name incorrect.", li.get(0).getName().contains("Sample_2"));
    }

    @Test
    public void betweenTest () throws ParameterSearchException, RestrictionException {
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.SAMPLE_NAME);
        // Sample search
        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, SampleInclude.NONE, 1, -1, em);

       assertEquals("Results of Samples incorrect.", 1, li.size());
       assertTrue("Sample name incorrect.", li.get(0).getName().contains("Sample_1"));
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
        restricLog.setOrderByAsc(RestrictionAttributes.SAMPLE_NAME);
        // Sample search
        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, SampleInclude.NONE, 1, -1, em);
       
       assertEquals("Results of Samples incorrect.", 2, li.size());
       assertTrue("Sample name incorrect.", li.get(0).getName().contains("Sample_1"));
    }

    @Test
    public void restrictionComparisonTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
        
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue");

        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restriction1
                , SampleInclude.NONE, 1, -1, em);

        assertEquals("Results of samples incorrect.", 1, li.size());
//        assertEquals("Number of Results of Datasets of 'sample_1' are incorrect.",
//               2, li.get(0).getDatasetCollection().size());
    }

    @Test
    public void restrictionLogicalConditionTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        // Restriction condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        List<Sample> li = (List<Sample>) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, SampleInclude.NONE, 1, -1, em);

       assertEquals("Results of Samples incorrect.", 1, li.size());
       assertTrue("Sample name incorrect.", li.get(0).getName().contains("Sample_1"));
    }

    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {

        RestrictionCondition cond = new RestrictionCondition();
        cond.setReturnLongId(true);
        List li = (List) SampleSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, cond
                , SampleInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of samples should be at least 1, not " + li.size(), (li.size() > 1));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(SampleSimpleRestrictionTest.class);
    }
}
