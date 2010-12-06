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
import uk.icat3.search.parameter.util.ParameterSearch;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.RestrictionException;
import static org.junit.Assert.*;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionOperator;
import uk.icat3.restriction.RestrictionOperator;
import uk.icat3.restriction.attribute.RestrictionAttributes;
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

//    @Test
//    public void datafilesIncludeDatafileTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
//        // Restriction condition
//        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
//                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.CONTAIN, "gation 2");
//        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
//                .add(RestrictionCondition.Not(restriction1))
//                .add(new RestrictionComparisonCondition(
//                    RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
//                ;
//        // Parameter condition
//        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
//
//        op1.add(pcDataset.get(0));
//        op1.add(pcDataset.get(1));
//        op1.add(pcSample.get(0));
//        op1.add(pcDatafile.get(1));
//
//        List<Dataset> li = (List<Dataset>) DatasetSearch
//                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
//                        , restricLog, DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS, 1, -1, em);
//
//       assertEquals("Results of Datasets incorrect.", 1, li.size());
//       assertTrue("Dataset name should be 'dataset_1', not " + li.get(0).getName(),
//               (li.get(0).getName().contains("dataset_1")));
//       assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(0).getDatafileCollection().size());
//    }

    @Test
    public void differentsAttr () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue");

        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, RestrictionOperator.END_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.END_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, RestrictionOperator.END_WITH, ""))
                ;

        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        // Investigation search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restrLog, DatasetInclude.NONE, 1, -1, em);

        assertEquals("Results of Datasets incorrect.", 3, li.size());
//        assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(1).getDatafileCollection().size());
    }
    @Test
    public void restrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.CONTAIN, "gation 2");
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(RestrictionCondition.Not(restriction1))
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(0));
        op1.add(pcDatafile.get(1));
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 1, li.size());
       assertEquals("Dataset name incorrect.", li.get(0).getName(),
               "dataset_1 blue");
    }

    @Test
    public void betweenTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_START_DATE, RestrictionOperator.BETWEEN, new Date(0), new Date(1324));
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(RestrictionCondition.Not(restriction1))
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(0));
        op1.add(pcDatafile.get(1));
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, li.size());
        assertEquals("Dataset name incorrect.", li.get(0).getName(),
               "dataset_1 blue");
    }

    @Test
    public void restrictionCondition2Test () throws ParameterSearchException, RestrictionException {
        RestrictionLogicalCondition investDat = new RestrictionLogicalCondition(LogicalOperator.AND)
                 .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_START_DATE, RestrictionOperator.EQUAL, new Date(0)))
                 .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_END_DATE, RestrictionOperator.EQUAL, new Date(2342342)));
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(RestrictionCondition.Not(investDat))
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(0));
        op1.add(pcDatafile.get(1));
        // Dataset search
        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, DatasetInclude.NONE, 1, -1, em);
       
       assertEquals("Results of Datasets incorrect.", 2, li.size());
       assertEquals("Dataset name incorrect", li.get(0).getName(),
               "dataset_1 blue");
    }

    @Test
    public void restrictionComparisonTest () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {
        
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue");

        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restriction1, DatasetInclude.DATASET_AND_DATAFILES_ONLY, 1, -1, em);

        assertEquals("Results of datasets incorrect.", 2, li.size());
//        assertEquals("Number of Results of Datafiles of 'dataset_1' are incorrect.",
//               2, li.get(1).getDatafileCollection().size());
    }

    @Test
    public void restrictionLogicalConditionTest () throws NoParameterTypeException, RestrictionException, ParameterSearchException {
        // Restriction condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, RestrictionOperator.CONTAIN, "gation 1");
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, RestrictionOperator.END_WITH, "blue"))
                ;
        // Parameter condition
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);

        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(0));
        op1.add(pcDatafile.get(1));

        List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 1, li.size());
       assertTrue("Dataset name should be 'dataset_1', not " + li.get(0).getName(),
               (li.get(0).getName().contains("dataset_1")));
    }

    @Test
    public void returnIdsTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, RestrictionException {
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

        assertTrue("Results of datasets should be 1, not " + li.size(), (li.size() == 1));
        assertTrue("Object should be Long, not " + li.get(0).getClass().getName()
                , Long.class == li.get(0).getClass());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatasetTest.class);
    }
}
