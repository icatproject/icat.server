/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.exception;

import uk.icat3.parametersearch.exception.*;
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
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import static org.junit.Assert.*;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
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
import uk.icat3.search.DatafileSearch;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class DatafileExceptionTest extends BaseParameterSearchTest {

    /**
     * List parameter error test. Test the exceptions work fine.
     */
    @Test
    public void noSearchableExceptionTest () throws ParameterSearchException {
        boolean exception = false;
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2"));
            pv3.getParam().setSearchable("N");
            lp.add(pv3);
            lp.add(pv4);
            DatafileSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatafileInclude.NONE, 1, -1, em);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(DatafileExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(DatafileExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionINException ex) {
            Logger.getLogger(DatafileExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(DatafileExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(DatafileExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {
            assertTrue("Should be a NoSearchableException", exception);
            pv3.getParam().setSearchable("Y");
        }
    }

    

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatafileExceptionTest.class);
    }
}
