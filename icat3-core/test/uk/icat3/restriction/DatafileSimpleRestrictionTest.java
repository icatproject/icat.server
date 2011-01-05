/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction;

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
import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import static org.junit.Assert.*;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class DatafileSimpleRestrictionTest extends BaseParameterSearchTest {

    @Test
    public void differentsAttr () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.STARTS_WITH, "datafile");

        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, ComparisonOperator.ENDS_WITH, ""))
                ;

        // Datafile search
        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restrLog, em);

        assertEquals("Results of Datafiles incorrect.", 4, li.size());
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
        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatafileInclude.NONE, -1, -1, em);

       assertEquals("Results of Datafiles incorrect.", 1, li.size());
       assertTrue("Datafile name incorrect.", li.get(0).getName().contains("datafile_2"));
    }

    @Test
    public void betweenTest () throws ParameterSearchException, RestrictionException {
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Datafile search
        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatafileInclude.NONE, -1, -1, em);

       assertEquals("Results of Datafiles incorrect.", 3, li.size());
       assertTrue("Datafile name incorrect.", li.get(0).getName().contains("datafile_1"));
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
        restricLog.setOrderByDesc(RestrictionAttributes.DATAFILE_NAME);
        // Datafile search
        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION
                        , restricLog, DatafileInclude.NONE, -1, -1, em);

        for (Datafile d : li) {
            System.out.println(" ----> " + d.getName());
        }
       
       assertEquals("Results of Datafiles incorrect.", 4, li.size());
       assertEquals("Datafile name incorrect.", "datafile_2", li.get(0).getName());
    }

    @Test
    public void restrictionComparisonTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
        
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue");

        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restriction1
                , DatafileInclude.NONE, -1, -1, em);

        assertEquals("Results of datafiles incorrect.", 3, li.size());
//        assertEquals("Number of Results of Datasets of 'datafile_1' are incorrect.",
//               2, li.get(0).getDatasetCollection().size());
    }

    @Test
    public void restrictionLogicalConditionTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        // Restriction condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.CONTAINS, "Investigation");
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "red"))
                ;
        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatafileInclude.NONE, -1, -1, em);

       assertEquals("Results of Datafiles incorrect.", 1, li.size());
       assertEquals("Datafile name incorrect.", "datafile_2", li.get(0).getName());
    }

    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {

         RestrictionCondition cond = new RestrictionCondition();
        cond.setReturnLongId(true);

        List li = (List) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, cond
                , DatafileInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of datafiles should be at least 1, not " + li.size(), (li.size() > 1));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatafileSimpleRestrictionTest.class);
    }
}
