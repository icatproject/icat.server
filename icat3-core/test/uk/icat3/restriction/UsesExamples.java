/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.restriction;

import uk.icat3.parametersearch.*;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import uk.icat3.exceptions.ParameterSearchException;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.manager.FacilityManager;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 * This class show some examples of search
 * 
 * @author cruzcruz
 */
public class UsesExamples extends BaseParameterSearchTest  {

    @Test
    public void keywords () throws RestrictionException {
        // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.KEYWORD);
        comparisonInstr.setComparisonOperator(ComparisonOperator.CONTAINS);
        comparisonInstr.setValue("keyword number");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        RestrictionLogicalCondition r3 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);
        r2.getRestConditions().add(r3);

        Collection li = InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        Collection ldat = DatasetSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        Collection ldaf = DatafileSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        Collection ls = SampleSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        
        assertEquals("Number of Investigation for Keyword 'keyword number'", 1, li.size());
        assertEquals("Number of Datasets for Keyword 'keyword number'", 2, ldat.size());
        assertEquals("Number of Datafiles for Keyword 'keyword number'", 3, ldaf.size());
        assertEquals("Number of Samples for Keyword 'keyword number'", 1, ls.size());
    }

    @Test
    public void keywords2 () throws RestrictionException {
        // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.KEYWORD);
        comparisonInstr.setComparisonOperator(ComparisonOperator.STARTS_WITH);
        comparisonInstr.setValue("my keyword");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);

        Collection li = InvestigationSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , op1, restInstrumentCond
                , InvestigationInclude.NONE, Queries.NO_PAGINATION, Queries.NO_PAGINATION, em);
        Collection ldat = DatasetSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , op1, restInstrumentCond
                , DatasetInclude.NONE, Queries.NO_PAGINATION, Queries.NO_PAGINATION, em);
        Collection ldaf = DatafileSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , op1, restInstrumentCond
                , DatafileInclude.NONE, Queries.NO_PAGINATION, Queries.NO_PAGINATION, em);
        Collection ls = SampleSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , op1, restInstrumentCond
                , SampleInclude.NONE, Queries.NO_PAGINATION, Queries.NO_PAGINATION, em);

        assertEquals("Number of Investigation for Keyword 'keyword number'", 1, li.size());
        assertEquals("Number of Datasets for Keyword 'keyword number'", 1, ldat.size());
        assertEquals("Number of Datafiles for Keyword 'keyword number'", 2, ldaf.size());
        assertEquals("Number of Samples for Keyword 'keyword number'", 1, ls.size());
    }

    @Test
    public void andOrNested () throws RestrictionException {
        // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.INVESTIGATION_INSTRUMENT);
        comparisonInstr.setComparisonOperator(ComparisonOperator.EQUALS);
        comparisonInstr.setValue("instrument");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);

        Collection li = InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        assertEquals("Number of investigation per instrument", 2, li.size());
    }

    /**
     * Restriction logical condition example
     *
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrinINCondition () throws ParameterSearchException, RestrictionException {
        Collection<Long> lid = new ArrayList<Long>();
        lid.add((long)7928950);
        lid.add((long)7928951);
        lid.add((long)7928952);
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        // Restriction comparison
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_ID, ComparisonOperator.IN, lid);
        // Dataset search
        Collection ldf = DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restriction1, DatasetInclude.NONE, 1, -1, em);
//       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
    }
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
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "Investigation 1");
        restriction1.setSensitive(true);
        // Create a list for operator IN
        List<String> inList = new ArrayList<String>();
        inList.add("nexus");
        inList.add("cosa");
        // Restriction condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.INVESTIGATION_START_DATE, ComparisonOperator.GREATER_THAN, new Date(0)))
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_FORMAT_TYPE, ComparisonOperator.IN, inList))
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_TYPE, ComparisonOperator.EQUALS, "experiment_raw"))
                .add (new RestrictionLogicalCondition(LogicalOperator.OR)
                        .add(restriction1)
                        .add(new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "bLue"))
                     )
                ;
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);
       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    @Test
    public void inSensitiveCondition () throws ParameterSearchException, RestrictionException {
        // Restriction comparison
       RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
               .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "INVestiGation 1"))
               .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "BlUE"))

       ;
        
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, InvestigationInclude.NONE, 1, -1, em);
       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    @Test
    public void sensitiveCondition () throws ParameterSearchException, RestrictionException {
        // Restriction comparison
       RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
               .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "InvestiGation 1"))
               .add (new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))

       ;

        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, InvestigationInclude.NONE, 1, -1, em);
       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    

    /**
     * Restriction order by and Not exapmle
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionNotOrder () throws ParameterSearchException, RestrictionException {
        // Restriction condition. Example
            // Not (INVESTIGATION_START_DATE = Date(0)) AND
            // DATAFILE_FORMAT_TYPE IN ('ASCII', 'no type') AND
            // DATASET_TYPE = 'test' AND
            // (   INVESTIGATION_TILE like 'Investigation 1%' OR
            //     DATASET_NAME like '%blue')
        // Creation of a simple comparison.
        // INVESTIGATION_TILE like 'Investigation 1%'
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "Investigation 1");
        // List for operator IN
        List<String> inList = new ArrayList<String>();
        inList.add("NeXus");
        inList.add("no type");
        // Creation of a logical restriction
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                // Set Not restriction
                // Not (INVESTIGATION_START_DATE = Date(0))
                .add (RestrictionCondition.Not(new RestrictionComparisonCondition(
                            RestrictionAttributes.INVESTIGATION_START_DATE, ComparisonOperator.EQUALS, new Date(0))))
                // DATAFILE_FORMAT_TYPE IN ('ASCII', 'no type')
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_FORMAT_TYPE, ComparisonOperator.IN, inList))
                // DATASET_TYPE = 'test'
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_TYPE, ComparisonOperator.EQUALS, "experiment_raw"))
                // OR
                .add (new RestrictionLogicalCondition(LogicalOperator.OR)
                        // INVESTIGATION_TILE like 'Investigation 1%'
                        .add(restriction1)
                        // DATASET_NAME like '%blue'
                        .add(new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                     )
                ;
        // Set order ASC
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        restricLog.setOrderByDesc(RestrictionAttributes.DATASET_NAME);
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        restricLog.setOrderByDesc(null);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
       assertEquals("First dataset name incorrect.", "dataset_3 blue", lds.get(0).getName());
    }

    /**
     * Restriction order by and Not exapmle
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionOrderByDesc () throws ParameterSearchException, RestrictionException {
        // Set order ASC
        RestrictionCondition restricLog = RestrictionCondition.orderByDesc(RestrictionAttributes.DATASET_NAME);
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        restricLog.setOrderByAttribute(null);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 3, lds.size());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Results of Investigations incorrect.", 2, li.size());
       assertEquals("Results of Samples incorrect.", 2, ls.size());
//       assertEquals("First dataset name incorrect.", "dataset_3 blue", lds.get(0).getName());
    }

    @Test
    public void orderByAsc () throws ParameterSearchException, RestrictionException {
        // Set order ASC, without restriction
        RestrictionCondition restricLog = RestrictionCondition.orderByAsc(RestrictionAttributes.DATASET_NAME);
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        restricLog.setOrderByAttribute(null);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);

       assertEquals("Results of Datasets incorrect.", 3, lds.size());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Results of Investigations incorrect.", 2, li.size());
       assertEquals("Results of Samples incorrect.", 2, ls.size());
       assertEquals("Second dataset name incorrect.", "dataset_2 red", lds.get(1).getName());
    }

    @Test
    public void maxResults () throws ParameterSearchException, RestrictionException {
        // Set order ASC, without restriction
        RestrictionCondition restricLog = RestrictionCondition.orderByAsc(RestrictionAttributes.DATASET_NAME);
        // Set max of results
        restricLog.setMaxResults(2);
        // List of parameter
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Parameter search
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, -1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, -1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, -1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, -1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
       assertEquals("Results of Investigations incorrect.", 2, li.size());
       assertEquals("Results of Samples incorrect.", 2, ls.size());
       assertEquals("Second dataset name incorrect.", "dataset_2 red", lds.get(1).getName());
    }

    @Test
    public void firstResults () throws ParameterSearchException, RestrictionException {
        // Create comparison
        RestrictionComparisonCondition restricLog = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.STARTS_WITH, "Investigation");
        // Set first result
        restricLog.setFirstResult(1);

        restricLog.setOrderByAsc(true);
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);

        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatasetInclude.NONE, -1, -1, em);
        restricLog.setOrderByAsc(RestrictionAttributes.DATAFILE_NAME);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatafileInclude.NONE, -1, -1, em);
        restricLog.setOrderByAsc(RestrictionAttributes.SAMPLE_NAME);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, SampleInclude.NONE, -1, -1, em);
        restricLog.setOrderByAsc(RestrictionAttributes.INVESTIGATION_TITLE);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, InvestigationInclude.NONE, -1, -1, em);

       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Dataset names incorrect.", "dataset_2 red", lds.get(0).getName());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Datafile names incorrect.", "datafile_1Dat3", ldf.get(0).getName());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertTrue("Investigation names incorrect.", li.get(0).getTitle().startsWith("Investigation 2"));
       assertEquals("Results of Samples incorrect.", 1, ls.size());
       assertEquals("Sample names incorrect.", "Sample_2", ls.get(0).getName());
    }

    /**
     * Restriction comparison example
     * 
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void restrictionComparisonCondition () throws ParameterSearchException, RestrictionException {
        // Creation of a logical restriction
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                // Set Not restriction
                // Not (INVESTIGATION_START_DATE = Date(0))
                .add (RestrictionCondition.Not(new RestrictionComparisonCondition(
                            RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 1")));
        // Parameter search
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);

        assertEquals("Results of Datafiles incorrect.", 1, ldf.size());
        assertEquals("Results of Datasets incorrect.", 1, lds.size());
       
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    @Test
    public void returnIdsTest () throws ParameterSearchException, RestrictionException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
//        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
//        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
//        lp.add(pv2);
//        lp.add(pv3);
        RestrictionCondition cond = new RestrictionCondition();
//        cond.setDatafileInclude(DatafileInclude.ALL_DATAFILE_ID);
//        cond.setInvestigationInclude(InvestigationInclude.ALL_INVESTIGATION_ID);
//        cond.setDatasetInclude(DatasetInclude.ALL_DATASET_ID);
        cond.setReturnLongId(true);

        List li = (List) InvestigationSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, cond
                , InvestigationInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        List lds = (List) DatasetSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, cond
                , DatasetInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

         List ldf = (List) DatafileSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, cond
                , DatafileInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);
         
//         cond.setSampleInclude(SampleInclude.ALL_SAMPLE_ID);
         List ls = (List) SampleSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, cond
                , SampleInclude.NONE
                , Queries.NO_LIMITED_RESULTS
                , Queries.NO_LIMITED_RESULTS, em);

        assertEquals("Results of Datasets incorrect.", 3, lds.size());
        assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
        assertEquals("Results of Investigations incorrect.", 2, li.size());
        assertEquals("Results of Samples incorrect.", 2, ls.size());
        assertEquals("Dataset return type is not Long", Long.class, lds.get(0).getClass());
        assertEquals("Datafile return type is not Long", Long.class, ldf.get(0).getClass());
        assertEquals("Investigation return type is not Long", Long.class, li.get(0).getClass());
        assertEquals("Sample return type is not Long", Long.class, ls.get(0).getClass());
    }

    @Test
    public void restrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 2");
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(RestrictionCondition.Not(restriction1))
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        op1.add(pcDataset.get(0));
        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(0));
        op1.add(pcDatafile.get(1));
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION,op1
                        , restricLog, DatasetInclude.NONE, 1, -1, em);
         // Dataset search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, DatafileInclude.NONE, 1, -1, em);
         // Dataset search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, InvestigationInclude.NONE, 1, -1, em);
         // Dataset search
        List<Sample> ls = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, SampleInclude.NONE, 1, -1, em);

        assertEquals("Results of Datasets incorrect.", 1, lds.size());
        assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
        assertEquals("Results of Investigations incorrect.", 1, li.size());
        assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    @Test
    public void notRestrictionConditionTest () throws ParameterSearchException, RestrictionException {
        // Restriction comparison condition
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 2");
        // Restricction logical condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR)
//                    .add(new RestrictionComparisonCondition(
//                        RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 1"))
                    .add(restriction1)
                    )
                .add(new RestrictionComparisonCondition(
                    RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                ;
        restricLog.setOrderByAsc(RestrictionAttributes.DATASET_NAME);
        // Parameter conditions
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
//        op1.add(pcDataset.get(0));
//        op1.add(pcDataset.get(1));
        op1.add(pcSample.get(1));
//        op1.add(pcDatafile.get(1));
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, ParameterCondition.Not(op1)
                        , restricLog, DatasetInclude.NONE, 1, -1, em);
        restricLog.setOrderByAttribute(null);
         // Dataset search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, DatafileInclude.NONE, 1, -1, em);
         // Dataset search

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, InvestigationInclude.NONE, 1, -1, em);
        for (Investigation i : li) {
            System.out.println(i.getId() + "-> " + i.getTitle());
        }
         // Dataset search
        List<Sample> ls = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1
                        , restricLog, SampleInclude.NONE, 1, -1, em);

        assertEquals("Results of Datasets incorrect.", 2, lds.size());
        assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
        assertEquals("Results of Investigations incorrect.", 1, li.size());
        assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

//    /**
//     * Restriction logical condition example
//     *
//     * @throws ParameterSearchException
//     * @throws RestrictionException
//     */
//    @Test
//    public void investigatorLogicalCondition () throws ParameterSearchException, RestrictionException {
//        // Restriction comparison
//        RestrictionComparisonCondition restricLog = new RestrictionComparisonCondition(
//                RestrictionAttributes.INVESTIGATOR_USER_ID, ComparisonOperator.STARTS_WITH, "T");
//       // List of parameter
//        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
//        // Parameter search
//        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
//        lp.add(pv1);
//        // Dataset search
//        List<Dataset> lds = (List<Dataset>) DatasetSearch
//                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
//        // Datafile search
//        List<Datafile> ldf = (List<Datafile>) DatafileSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
//        // Sample search
//        List<Sample> ls = (List<Sample>) SampleSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
//        // Investigation search
//        List<Investigation> li = (List<Investigation>) InvestigationSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);
//       assertEquals("Results of Datasets incorrect.", 2, lds.size());
//       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
//       assertEquals("Results of Investigations incorrect.", 1, li.size());
//       assertEquals("Results of Samples incorrect.", 1, ls.size());
//    }

    /**
     * Restriction logical condition example
     *
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void betweenDates () throws ParameterSearchException, RestrictionException {
        // Restriction comparison
        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_START_DATE, ComparisonOperator.BETWEEN
                , new Date(0)
                , "2050-01-01 ");
        // Restriction condition
        RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add (new RestrictionLogicalCondition(LogicalOperator.AND)
                        .add(restriction1)
                        .add(new RestrictionComparisonCondition(
                            RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue"))
                     )
                ;
        // Dataset search
        List<Dataset> lds = (List<Dataset>) DatasetSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatasetInclude.NONE, 1, -1, em);
        // Datafile search
        List<Datafile> ldf = (List<Datafile>) DatafileSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, DatafileInclude.NONE, 1, -1, em);
        // Sample search
        List<Sample> ls = (List<Sample>) SampleSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, SampleInclude.NONE, 1, -1, em);
        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
            .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog, InvestigationInclude.NONE, 1, -1, em);
       assertEquals("Results of Datasets incorrect.", 2, lds.size());
       assertEquals("Results of Datafiles incorrect.", 3, ldf.size());
       assertEquals("Results of Investigations incorrect.", 1, li.size());
       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    /**
     * Restriction logical condition example
     *
     * @throws ParameterSearchException
     * @throws RestrictionException
     */
    @Test
    public void facilityUser () throws ParameterSearchException, RestrictionException {
        String contain = "n";
        // Create restriction comparison for proposal start with
        RestrictionComparisonCondition compFirstName = new RestrictionComparisonCondition();
        compFirstName.setRestrictionAttribute(RestrictionAttributes.INVESTIGATOR_USER_FIRST_NAME);
        compFirstName.setComparisonOperator(ComparisonOperator.STARTS_WITH);
        compFirstName.setValue(contain);
        // Create restriction comparison for proposal start with
        RestrictionComparisonCondition compMiddleName = new RestrictionComparisonCondition();
        compMiddleName.setRestrictionAttribute(RestrictionAttributes.INVESTIGATOR_USER_MIDDLE_NAME);
        compMiddleName.setComparisonOperator(ComparisonOperator.STARTS_WITH);
        compMiddleName.setValue(contain);
        // Create restriction comparison for proposal start with
        RestrictionComparisonCondition compLastName = new RestrictionComparisonCondition();
        compLastName.setRestrictionAttribute(RestrictionAttributes.INVESTIGATOR_USER_LAST_NAME);
        compLastName.setComparisonOperator(ComparisonOperator.STARTS_WITH);
        compLastName.setValue(contain);
        // Create logical condition
        RestrictionLogicalCondition logCond = new RestrictionLogicalCondition();
        logCond.setOperator(LogicalOperator.OR);
        // Maximum results returned
        logCond.setMaxResults(15);
        List<FacilityUser> lf = (List<FacilityUser>) FacilityManager.searchByRestriction(logCond, em);
        // Add all title investigation to list res
        for (FacilityUser user : lf)
                System.out.println(user.getFacilityUserId());
//        // Dataset search
//        List<Dataset> lds = (List<Dataset>) DatasetSearch
//                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatasetInclude.NONE, 1, -1, em);
//        // Datafile search
//        List<Datafile> ldf = (List<Datafile>) DatafileSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, DatafileInclude.NONE, 1, -1, em);
//        // Sample search
//        List<Sample> ls = (List<Sample>) SampleSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, SampleInclude.NONE, 1, -1, em);
//        // Investigation search
//        List<Investigation> li = (List<Investigation>) InvestigationSearch
//            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restricLog, InvestigationInclude.NONE, 1, -1, em);
//       assertEquals("Results of Datasets incorrect.", 2, lds.size());
//       assertEquals("Results of Datafiles incorrect.", 2, ldf.size());
//       assertEquals("Results of Investigations incorrect.", 1, li.size());
//       assertEquals("Results of Samples incorrect.", 1, ls.size());
    }

    @Test
    public void equalString () throws RestrictionException {
         // Restriction comparison
        RestrictionComparisonCondition r = new RestrictionComparisonCondition();
        r.setRestrictionAttribute(RestrictionAttributes.INVESTIGATION_INSTRUMENT);
        r.setComparisonOperator(ComparisonOperator.EQUALS);
        r.setValue("instrument");
        Collection li = InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, r, em);
        assertEquals("Number of investigation per instrument", 2, li.size());
    }

    

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(UsesExamples.class);
    }
}
