/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.parametersearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import uk.icat3.exceptions.ParameterSearchException;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterComparisonCondition;
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
    public void paramsearchWithLogicaCondition () throws RestrictionException {
        // Instruments logical condition
        ParameterLogicalCondition restInstrumentCond = new ParameterLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        ParameterLogicalCondition restCycleCond = new ParameterLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        
        // Get the parameters manually or from a service
        Parameter voltage = new Parameter("V", "voltage");

         ParameterComparisonCondition compVoltageSample =
                new ParameterComparisonCondition(
                        new ParameterSearch(ParameterType.SAMPLE, voltage),
                        ComparisonOperator.IN,
                        Arrays.asList(new Double(55.55), new Double(34)));

        ParameterLogicalCondition r2 = new ParameterLogicalCondition();
        r2.setOperator(LogicalOperator.AND);
        r2.add(compVoltageSample);

        restInstrumentCond.getListComparable().add(new ParameterSearch(ParameterType.SAMPLE, voltage));
        restInstrumentCond.getListComparable().add(restCycleCond);
        restCycleCond.getListComparable().add(r2);

        Collection ldat = DatasetSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , restInstrumentCond, Queries.NO_RESTRICTION
                , DatasetInclude.NONE, em);
        Collection li = InvestigationSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , restInstrumentCond, Queries.NO_RESTRICTION
                , InvestigationInclude.NONE, em);
        Collection ldaf = DatafileSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , restInstrumentCond, Queries.NO_RESTRICTION
                , DatafileInclude.NONE, em);
        Collection ls = SampleSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , restInstrumentCond, Queries.NO_RESTRICTION
                , SampleInclude.NONE, em);
        
        assertEquals("Number of Investigation in Parameter Search 'Voltage' 'V'", 1, li.size());
        assertEquals("Number of Datasets in Parameter Search 'Voltage' 'V'", 2, ldat.size());
        assertEquals("Number of Samples in Parameter Search 'Voltage' 'V'", 1, ls.size());
        assertEquals("Number of Datafiles in Parameter Search 'Voltage' 'V'", 3, ldaf.size());
    }

    @Test
    public void andOrNested () throws RestrictionException {
        // Instruments logical condition
        ParameterLogicalCondition restInstrumentCond = new ParameterLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        ParameterLogicalCondition restCycleCond = new ParameterLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        
        // Get the parameters manually or from a service
        Parameter voltage = new Parameter("V", "voltage");
        // Create new comparison
        ParameterComparisonCondition compVoltageSample =
                new ParameterComparisonCondition(
                        new ParameterSearch(ParameterType.SAMPLE, voltage),
                        ComparisonOperator.IN,
                        Arrays.asList(new Double(55.55), new Double(34)));

        ParameterLogicalCondition r2 = new ParameterLogicalCondition();
        r2.setOperator(LogicalOperator.AND);

        restInstrumentCond.getListComparable().add(compVoltageSample);
        restInstrumentCond.getListComparable().add(restCycleCond);
        restCycleCond.getListComparable().add(r2);

        Collection li = DatasetSearch.searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                , restInstrumentCond, Queries.NO_RESTRICTION
                , DatasetInclude.NONE, em);
        assertEquals("Number of investigation per instrument", 2, li.size());
    }

    /**
     * Test between example
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void comparisonExample () throws ParameterSearchException, RestrictionException {

        // Get the parameter, manually or get from a service
        Parameter dateTime = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));
        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter.
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, dateTime);
        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.BETWEEN);
        // Add the value to compare
        comp1.setValue(new Date(0));
        // Add a second value if needed (only for BETWEEN)
        comp1.setValueRight("2010-10-10 00:00:00");

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        List<Dataset> ldat = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatasetInclude.NONE, em);

        assertEquals("Results of datafiles incorrect.", 1, ld.size());
        assertEquals("Results of dataset incorrect.", 1, ldat.size());
    }

    @Test
    public void parameterExample () throws ParameterSearchException, RestrictionException {
        // List of parameter searchs
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        // Get the parameters manually or from a service
        Parameter voltage = new Parameter("V", "voltage");
        Parameter dateTime = new Parameter("yyyy-MM-dd HH:mm:ss", "time1");
        // Create the parameter searchs.
        ParameterSearch psSample = new ParameterSearch(ParameterType.SAMPLE, voltage);
        ParameterSearch psDatafile = new ParameterSearch(ParameterType.DATAFILE, dateTime);
        // Add to a list
        lp.add(psSample);
        lp.add(psDatafile);

        List<Datafile> li = (List<Datafile>) DatafileSearch
            .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatafileInclude.NONE, 1, -1, em);

        assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
    }

    @Test
    public void comparisonConditionExample () throws ParameterSearchException, RestrictionException {

        // Get the parameters manually or from a service
        Parameter voltage = new Parameter("V", "voltage");
        Parameter string = new Parameter("str", "string1");
        Parameter dateTime = new Parameter("yyyy-MM-dd HH:mm:ss", "time1");
        // Create comparisons
        // voltage = 55.55
        ParameterComparisonCondition compVoltageSample =
                new ParameterComparisonCondition(
                        new ParameterSearch(ParameterType.SAMPLE, voltage),
                        ComparisonOperator.IN,
                        Arrays.asList(new Double(55.55), new Double(34)));
        // string like '%21%'
        ParameterComparisonCondition compStringDatafile =
                 new ParameterComparisonCondition(
                        new ParameterSearch(ParameterType.DATAFILE, string),
                        ComparisonOperator.CONTAINS, "number");
        // dateTime > Date(0)
        ParameterComparisonCondition compDateTimeDatafile =
                new ParameterComparisonCondition(
                        new ParameterSearch(ParameterType.DATAFILE, dateTime),
                        ComparisonOperator.GREATER_THAN,
                        new Date(0));
        // Create logical condition
        ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
        ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
        // Create the structure to compare.
        // compDataTimeDatafile OR (compVoltageSample AND compStringDatafile)
        op2.add(compVoltageSample);
        op2.add(compStringDatafile);
        op1.add(compDateTimeDatafile);
        op1.add(op2);

        List<Datafile> li = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, DatafileInclude.NONE, 1, -1, em);

        assertTrue("Results of investigations should be 1 not " + li.size(), (li.size() == 1));
    }

    /**
     * Test datetime parameter search
     *
     * @throws ParameterSearchException
     */
    @Test
    public void dateTime () throws ParameterSearchException, RestrictionException {
        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.LESS_EQUAL);
        // Add the value to compare
        comp1.setValue(new Date());
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test stringdatetime parameter test
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void stringDateTime () throws ParameterSearchException, RestrictionException {
        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.LESS_EQUAL);
        // Add the value to compare
        comp1.setValue("2010-02-22 00:00:00");
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test string value parameter search
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void stringValue () throws ParameterSearchException, RestrictionException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAINS);
        // Add the value to compare
        comp1.setValue("number");
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertEquals("Results of Datafiles incorrect", 1, ld.size());
    }

    /**
     * Test string value parameter search
     *
     * @throws ParameterSearchException
     */
    @Test
    public void sensitiveValue () throws ParameterSearchException, RestrictionException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAINS);
        // Add the value to compare
        comp1.setValue("NuMbEr");
        comp1.setSensitive(true);
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertEquals("Results of Datafiles incorrect", 0, ld.size());
    }

    /**
     * Test string value parameter search
     *
     * @throws ParameterSearchException
     */
    @Test
    public void inSensitiveValue () throws ParameterSearchException, RestrictionException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAINS);
        // Add the value to compare
        comp1.setValue("NuMbEr");

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertEquals("Results of Datafiles incorrect", 1, ld.size());
    }

    /**
     * Test numericValue parameter search
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void numericValue () throws ParameterSearchException, RestrictionException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAINS);
        // Add the value to compare
        comp1.setValue(21);
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, DatafileInclude.NONE, em);

        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(UsesExamples.class);
    }
}
