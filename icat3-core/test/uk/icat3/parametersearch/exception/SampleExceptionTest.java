/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.exception;

import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoNumericComparatorException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Sample;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import static org.junit.Assert.*;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.RestrictionINException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
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
 *
 * @author cruzcruz
 */
public class SampleExceptionTest extends BaseParameterSearchTest {

    /**
     * The parameter contains a numberic value but the comparator is for a string.
     */
    @Test
    public void comparatorExceptionTest () {
        boolean exception = false;
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
             // ------------- ComparisonOperator 1 ----------------------
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            comp1.setParameterValued(new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1")));
            comp1.setComparator(ComparisonOperator.START_WITH);
            comp1.setNumericValue(new Double (3.14));
            lc.add(comp1);
            SampleSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);

        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            exception = true;
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a NoNumericComparatorException", exception);
        }
    }

    /**
     * Add the operator itself produces an cyclic execption.
     */
    @Test
    public void cyclicExceptionTest () {
        boolean exception = false;
        try {
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
            op2.add(pcDatafile.get(0));
            op2.add(pcDatafile.get(1));
            op1.add(op2);
            op1.add(op1);
            List<Sample> li = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CyclicException ex) {
            exception = true;
        }
        finally {
            assertTrue("Should be a CyclicException", exception);
        }
    }

    @Test
    public void nullParameterExceptionTest () {
        boolean exception = false;
        try {
            List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
             // ------------- ComparisonOperator 1 ----------------------
            ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
            comp1.setParameterValued(null);
            comp1.setComparator(ComparisonOperator.EQUAL);
            comp1.setNumericValue(new Double (3.14));
            lc.add(comp1);
            SampleSearch.searchByParameterComparisonList(VALID_USER_FOR_INVESTIGATION, lc, Queries.NO_RESTRICTION, SampleInclude.NONE, -1, -1, em);

        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            exception = true;
        }
        finally {
            assertTrue("Should be a NullParameterException", exception);
        }
    }

    /**
     * Operator is empty
     */
    @Test
    public void emptyListExceptionTest () {
        boolean exception = false;
        try {
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            List<Sample> li = (List<Sample>) SampleSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyOperatorException ex) {
            exception = true;
        } catch (NullParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a EmptyOperatorException", exception);
        }
    }


     /**
     * No searchable exception occurs when try to search by a datafile parameter
     * but this parameter is not relevant to datafile.
     */
    @Test
    public void noSearchableParameterException () {
        boolean exception = false;
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
            lp.add(pv3);
            lp.add(pv4);
            pv3.getParam().setSearchable("N");
            SampleSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            exception = true;
        } catch (NullParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a NoSearchableException", exception);
            pv3.getParam().setSearchable("Y");
        }
    }

    @Test
    public void ParameterNoExistsException () {
        boolean exception = false;
        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            Parameter param = new Parameter(new ParameterPK("noName", "noUnits"));
            param.setDatafileParameter(true);
            ParameterSearch pv4 = new ParameterSearch(ParameterType.SAMPLE, param);
            lp.add(pv3);
            lp.add(pv4);
            SampleSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, SampleInclude.NONE, 1, -1, em);
        } catch (ParameterNoExistsException ex) {
            exception = true;
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(SampleExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a ParameterNoExistsException", exception);
        }
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(SampleExceptionTest.class);
    }
}
