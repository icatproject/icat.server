/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.restriction;

import uk.icat3.parametersearch.*;
import java.util.ArrayList;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import uk.icat3.exceptions.ParameterSearchException;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * This class show some examples of search
 * 
 * @author cruzcruz
 */
public class UsesExamples extends BaseParameterSearchTest  {

    /**
     * Restriction logical condition example
     * 
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionLogicalCondition () throws ParameterSearchException, RestrictionException {
        // Restriction comparison
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.START_WITH, "Investigation 1");
        // Create a list for operator IN
        List<String> inList = new ArrayList<String>();
        inList.add("ASC'\\'\"II");
        inList.add("cosa");
        // Restriction condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.INVESTIGATION_START_DATE, RestrictionOperator.GREATER_THAN, new Date(0)))
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_FORMAT_TYPE, RestrictionOperator.IN, inList))
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_TYPE, RestrictionOperator.EQUAL, "experiment_raw"))
                .add (new RestrictionLogicalCondition(LogicalOperator.OR)
                        .add(restriction1)
                        .add(new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                     )
                ;
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ld = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatafileInclude.NONE, 1, -1, em);

        assertEquals("Results of datasets incorrect", 2, li.size());
        assertEquals("Results of datafiles incorrect", 3, ld.size());
    }

    /**
     * Restriction order by and NOT exapmle
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionNotOrder () throws ParameterSearchException, RestrictionException {
        // Restriction condition. Example
            // NOT (INVESTIGATION_START_DATE = Date(0)) AND
            // DATAFILE_FORMAT_TYPE IN ('ASCII', 'cosa) AND
            // DATASET_TYPE = 'test' AND
            // (   INVESTIGATION_TILE like 'Investigation 1%' OR
            //     DATASET_NAME like '%blue')
        // Creation of a simple comparison.
        // INVESTIGATION_TILE like 'Investigation 1%'
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.START_WITH, "Investigation 1");
        // List for operator IN
        List<String> inList = new ArrayList<String>();
        inList.add("ASCII");
        inList.add("no type");
        // Creation of a logical restriction
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                // Set NOT restriction
                // NOT (INVESTIGATION_START_DATE = Date(0))
                .add (RestrictionCondition.Not(new RestrictionComparisonCondition(
                            RestrictionAttributes.INVESTIGATION_START_DATE, RestrictionOperator.EQUAL, new Date(0))))
                // DATAFILE_FORMAT_TYPE IN ('ASCII', 'cosa)
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_FORMAT_TYPE, RestrictionOperator.IN, inList))
                // DATASET_TYPE = 'test'
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_TYPE, RestrictionOperator.EQUAL, "experiment_raw"))
                // OR
                .add (new RestrictionLogicalCondition(LogicalOperator.OR)
                        // INVESTIGATION_TILE like 'Investigation 1%'
                        .add(restriction1)
                        // DATASET_NAME like '%blue'
                        .add(new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                     )
                ;
        // Set order ASC
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        
        // Dataset search
        List<Dataset> ldats = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldatf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, 1, -1, em);

        assertEquals("Number of Datasets incorrect", 2, ldats.size());
        assertEquals("Number of Datafiles incorrect", 3, ldatf.size());
        assertEquals("Number of Samples incorrect", 2, ls.size());
        assertEquals("Number of Investigations incorrect", 2, li.size());
        assertEquals("First dataset name incorrect.", "dataset_1 blue", ldats.get(0).getName());
    }

    /**
     * Restriction comparison example
     * 
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionComparisonCondition () throws ParameterSearchException, RestrictionException {
        // Restriction comparison
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.START_WITH, "Investigation 1");
        // Parameter search
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        // Call to dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restriction1, DatasetInclude.NONE, 1, -1, em);

        assertEquals("Number of datasets incorrect.", 2, li.size());
    }

     @Test
    public void returnIdsTest () throws ParameterSearchException, RestrictionException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
        lp.add(pv2);
        lp.add(pv3);

        List li = (List) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, null
                , DatasetInclude.ALL_DATASET_ID
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

         List ld = (List) DatafileSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, null
                , DatafileInclude.ALL_DATAFILE_ID
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertTrue("Results of datasets should be 1, not " + li.size(), (li.size() == 1));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(UsesExamples.class);
    }
}
