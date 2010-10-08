/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.parametersearch;

import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import uk.icat3.exceptions.ParameterSearchException;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;

/**
 * This class show some examples of search
 * 
 * @author cruzcruz
 */
public class UsesExamples extends BaseParameterSearchTest {

    /**
     * Test between example
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void betweenExample () throws ParameterSearchException {

        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("deg", "datafile1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterValued(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.BETWEEN);
        // Add the value to compare
        comp1.setDatetimeValue(new Date(0));
        // Add a second value if needed (only for BETWEEN)
        comp1.setDatetimeValueRight("2010-10-10 00:00:00");

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, em);

        for (Datafile d : ld)
            System.out.println("-- " + d.getName());
    }

    /**
     * Test datetime parameter search
     *
     * @throws ParameterSearchException
     */
    @Test
    public void dateTime () throws ParameterSearchException {
        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterValued(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.LESS_EQUAL);
        // Add the value to compare
        comp1.setDatetimeValue(new Date());
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, em);

        System.out.println("********** NUMBER OF RESULTS: " + ld.size());
        for (Datafile d : ld) {
            System.out.println("-- " + d.getName());
        }
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test stringdatetime parameter test
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void stringDateTime () throws ParameterSearchException {
        // Get the parameter, manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterValued(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.LESS_EQUAL);
        // Add the value to compare
        comp1.setDatetimeValue("2010-02-22 00:00:00");
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, em);

        System.out.println("********** NUMBER OF RESULTS: " + ld.size());
        for (Datafile d : ld) {
            System.out.println("-- " + d.getName());
        }
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test string value parameter search
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void stringValue () throws ParameterSearchException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterValued(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAIN);
        // Add the value to compare
        comp1.setStringValue("number");
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, em);

        System.out.println("********** NUMBER OF RESULTS: " + ld.size());
        for (Datafile d : ld) {
            System.out.println("-- " + d.getName());
        }
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }

    /**
     * Test numericValue parameter search
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void numericValue () throws ParameterSearchException {
        // Get the parameter manually or get from a service
        Parameter datfile = new Parameter(new ParameterPK("str", "string1"));

        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter. (Try to find a better name to parameterValued)
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, datfile);

        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();

        // Add the parameterValued
        comp1.setParameterValued(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.CONTAIN);
        // Add the value to compare
        comp1.setNumericValue(21);
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight(new Double (4));

        List<Datafile> ld = (List<Datafile>) DatafileSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, comp1, em);

        System.out.println("********** NUMBER OF RESULTS: " + ld.size());
        for (Datafile d : ld) {
            System.out.println("-- " + d.getName());
        }
        assertTrue("Results of investigations should be 1 not " + ld.size(), (ld.size() == 1));
    }
}
