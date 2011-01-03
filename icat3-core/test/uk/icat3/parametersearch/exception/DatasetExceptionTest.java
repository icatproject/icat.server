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
import uk.icat3.search.DatasetSearch;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * Exception Test ofr Dataset searchs
 * 
 * @author cruzcruz
 */
public class DatasetExceptionTest extends BaseParameterSearchTest {


    /**
     * The parameter contains a numberic value but the comparator is for a string.
     */
    @Test
    public void comparatorExceptionTest () {
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            // Numeric parameter
            comp1.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1")));
            // String operator
            comp1.setComparator(ComparisonOperator.STARTS_WITH);
            comp1.setValue(new Double (3.14));
            lc.add(comp1);
            DatasetSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, DatasetInclude.NONE, -1, -1, em);
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
    public void cyclicExceptionTest ()  {
        try {
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
            op2.add(pcDatafile.get(0));
            op2.add(pcDatafile.get(1));
            op1.add(op2);
            // Cyclic operation
            op1.add(op1);
            DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", CyclicException.class, ex.getClass());
            return;
        }
        assertTrue("CyclicException expected.", false);
    }

    /**
     * Parameter search is null
     * @
     */
    @Test
    public void nullParameterExceptionTest ()  {
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            // Parameter search is NULL
            comp1.setParameterSearch(null);
            comp1.setComparator(ComparisonOperator.EQUALS);
            comp1.setValue(new Double (3.14));
            lc.add(comp1);
            DatasetSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, DatasetInclude.NONE, -1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NullParameterException.class, ex.getClass());
            return;
        }
        assertTrue("NullParameterException expected.", false);
    }

    /**
     * Parameter is no searchable
     */
    @Test
    public void noSearchableParameter ()  {
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
            // Set parmaeter NOT searchable
            parameter.get("dataset1").setSearchable("N");
            lp.add(pv3);
            lp.add(pv4);
            DatasetSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NoSearchableParameterException.class, ex.getClass());
            return;
        } finally {
            // Restore parameter value.
            parameter.get("dataset1").setSearchable("Y");
        }
        assertTrue("NoSearchableParameterException expected.", false);
    }

    /**
     * Parameter no exists
     * 
     * @
     */
    @Test
    public void ParameterNoExistsException ()  {
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            // Parameter not exists
            Parameter param = new Parameter(new ParameterPK("noName", "noUnits"));
            param.setDatafileParameter(true);
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATASET, param);
            lp.add(pv3);
            lp.add(pv4);
            DatasetSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", ParameterNoExistsException.class, ex.getClass());
            return;
        }
        assertTrue("ParameterNoExistsException expected.", false);
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatasetExceptionTest.class);
    }
}
