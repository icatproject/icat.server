/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.investigation;

import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import static org.junit.Assert.*;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class InvestigationSimpleRestrictionTest extends BaseParameterSearchTest {

//    @Test
//    public void datafilesIncludeDatafileTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
//        // Restriction condition
//        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
//                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 2");
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
//        List<Investigation> li = (List<Investigation>) InvestigationSearch
//                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
//                        , restricLog, InvestigationInclude.DATASET_DATAFILES_AND_PARAMETERS, 1, -1, em);
//
//       assertEquals("Results of Investigations incorrect.", 1, li.size());
//       assertTrue("Investigation name should be 'investigation_1', not " + li.get(0).getTitle(),
//               (li.get(0).getTitle().contains("investigation_1")));
//       assertEquals("Number of Results of Datafiles of 'investigation_1' are incorrect.",
//               2, li.get(0).getDatafileCollection().size());
//    }

    @Test
    public void differentsAttr () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "Investigation");
        restriction1.setSensitive(true);

        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, ComparisonOperator.ENDS_WITH, ""))
                ;

        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restrLog, em);

        assertEquals("Results of Investigations incorrect.", 2, li.size());
//        assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(1).getDatafileCollection().size());
    }

    @Test
    public void restrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 2");
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertTrue("Investigation name incorrect.", li.get(0).getTitle().contains("Investigation 2"));
    }

    @Test
    public void betweenTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_START_DATE, ComparisonOperator.BETWEEN, new Date(0), new Date(1324));
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(RestrictionCondition.Not(restriction1))
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.INVESTIGATION_TITLE);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertTrue("Investigation name incorrect.", li.get(0).getTitle().contains("Investigation 1"));
    }

    @Test
    public void restrictionCondition2Test () throws ParameterSearchException, RestrictionException {
        RestrictionLogicalCondition investDat = new RestrictionLogicalCondition(LogicalOperator.AND)
                 .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_START_DATE, ComparisonOperator.EQUALS, new Date(0)))
                 .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_END_DATE, ComparisonOperator.EQUALS, new Date(2342342)));
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(RestrictionCondition.Not(investDat))
                .add(new RestrictionLogicalCondition(LogicalOperator.OR)
                    .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                    .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))

                    )
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.INVESTIGATION_TITLE);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, InvestigationInclude.NONE, 1, -1, em);
       
       assertEquals("Results of Investigations incorrect.", 2, li.size());
       assertTrue("Investigation name incorrect.", li.get(0).getTitle().contains("Investigation 1"));
    }

    @Test
    public void restrictionComparisonTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
        
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue");

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restriction1
                , InvestigationInclude.DATASETS_ONLY, 1, -1, em);

        assertEquals("Results of investigations incorrect.", 1, li.size());
//        assertEquals("Number of Results of Datasets of 'investigation_1' are incorrect.",
//               2, li.get(0).getDatasetCollection().size());
    }

    @Test
    public void restrictionLogicalConditionTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        // Restriction condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 1");
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertTrue("Investigation name incorrect.", li.get(0).getTitle().contains("Investigation 1"));
    }

    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {

        RestrictionCondition cond = new RestrictionCondition();
        cond.setReturnLongId(true);
        List li = (List) InvestigationSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, cond
                , InvestigationInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of investigations should be at least 1, not " + li.size(), (li.size() > 1));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(InvestigationSimpleRestrictionTest.class);
    }
}
