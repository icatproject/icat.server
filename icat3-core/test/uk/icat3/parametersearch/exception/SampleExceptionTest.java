/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.exception;

import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.NoNumericComparatorException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import static org.junit.Assert.*;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.search.SampleSearch;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 * Exception test for Sample searchs
 * 
 * @author cruzcruz
 */
public class SampleExceptionTest extends BaseParameterSearchTest {

    /**
     * The parameter contains a numberic value but the comparator is for a string.
     */
    @Test
    public void comparatorExceptionTest () {
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
             // ------------- ComparisonOperator 1 ----------------------
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            comp1.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1")));
            comp1.setComparator(ComparisonOperator.STARTS_WITH);
            comp1.setValue(new Double (3.14));
            lc.add(comp1);
            SampleSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NoNumericComparatorException.class, ex.getClass());
            return;
        }
        assertTrue("NoNumericComparatorException expected.", false);
    }

    /**
     * Add the operator itself produces an cyclic execption.
     */
    @Test
    public void cyclicExceptionTest () {
        try {
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
            op2.add(pcDatafile.get(0));
            op2.add(pcDatafile.get(1));
            op1.add(op2);
            op1.add(op1);
            SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", CyclicException.class, ex.getClass());
            return;
        }
        assertTrue("CyclicException expected.", false);
    }

    /**
     * Insert a null parameter search.
     */
    @Test
    public void nullParameterExceptionTest () {
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            // null parameter search
            comp1.setParameterSearch(null);
            comp1.setComparator(ComparisonOperator.EQUALS);
            comp1.setValue(new Double (3.14));
            lc.add(comp1);
            SampleSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NullParameterException.class, ex.getClass());
            return;
        }
        assertTrue("NullParameterException expected.", false);
    }


    /**
     * Parameter in not searchable
     */
    @Test
    public void noSearchableParameterException () {
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
            lp.add(pv3);
            lp.add(pv4);
            // Set parameter is not searchable
            pv3.getParam().setSearchable("N");
            SampleSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NoSearchableParameterException.class, ex.getClass());
            return;
        } finally {
            // Restore to its original value
            pv3.getParam().setSearchable("Y");
        }
        assertTrue("NoSearchableParameterException expected.", false);
    }

    /**
     * Parameter not exists in database
     */
    @Test
    public void ParameterNoExistsException () {
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            // create a parameter that doesn't exists.
            Parameter param = new Parameter(new ParameterPK("noName", "noUnits"));
            param.setDatafileParameter(true);
            ParameterSearch pv4 = new ParameterSearch(ParameterType.SAMPLE, param);
            lp.add(pv3);
            lp.add(pv4);
            SampleSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", ParameterNoExistsException.class, ex.getClass());
            return;
        }
        assertTrue("ParameterNoExistsException expected.", false);
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(SampleExceptionTest.class);
    }
}
