/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.parametersearch.sample;

import uk.icat3.parametersearch.*;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import uk.icat3.exceptions.ParameterSearchException;
import org.junit.Test;
import uk.icat3.entity.Sample;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 *
 * @author cruzcruz
 */
public class ValueTypeSampleTest extends BaseParameterSearchTest {

    /**
     * Test Between comparator search
     *
     * @throws ParameterSearchException
     */
    @Test
    public void between () throws ParameterSearchException, RestrictionException {

        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("deg", "datafile1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.BETWEEN);
        // Add the value to compare
        comp1.setValue(3);
        // Add a second value if needed (only for BETWEEN)
        comp1.setValueRight(new Double (4));

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, SampleInclude.NONE, em);
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
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
        comp1.setComparator(ComparisonOperator.BETWEEN);
        // Add the value to compare
        comp1.setValue(new Date(0));
        // Add a second value if needed (only for BETWEEN)
        comp1.setValueRight("2010-10-10 00:00:00");

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, SampleInclude.NONE, em);
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test stringdatetime value for datetime parameter search
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

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, SampleInclude.NONE, em);

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

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, SampleInclude.NONE, em);

        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test numeric value parameter search.
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

        List<Sample> ld = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, Queries.NO_RESTRICTION, SampleInclude.NONE, em);

        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(ValueTypeSampleTest.class);
    }
}
