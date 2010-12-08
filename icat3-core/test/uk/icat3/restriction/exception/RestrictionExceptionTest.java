/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.exception;

import uk.icat3.exceptions.RestrictionNullException;
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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import static org.junit.Assert.*;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.OperatorINException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionOperator;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.search.DatasetSearch;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author cruzcruz
 */
public class RestrictionExceptionTest extends BaseParameterSearchTest {

    /**
     * Add the operator itself produces an cyclic execption.
     */
    @Test
    public void RestrictionOperatorExceptionTest () throws RestrictionException {
        boolean exception = false;
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_ID, RestrictionOperator.START_WITH, 123));

            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
            op2.add(pcDatafile.get(0));
            op2.add(pcDatafile.get(1));
            op1.add(op2);
            DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperatorINException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            exception = true;
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CyclicException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a CyclicException", exception);
        }
    }

    /**
     * Operator is empty
     */
    @Test
    public void CyclicExceptionTest () throws RestrictionException {
        boolean exception = false;
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR))
                .add(new RestrictionLogicalCondition(LogicalOperator.AND))
                .add(new RestrictionComparisonCondition(null, null, new Date()))
                ;
            restricLog.add(restricLog);
            
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            op1.add(pcDatafile.get(1));
            List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CyclicException ex) {
            exception = true;
        } catch (OperatorINException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a EmptyOperatorException", exception);
        }
    }

    /**
     * Operator is empty
     */
    @Test
    public void cyclicExceptionTest () throws RestrictionException {
        boolean exception = false;
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                
                ;
            RestrictionLogicalCondition restr2 = new RestrictionLogicalCondition(LogicalOperator.AND);
            RestrictionLogicalCondition restr3 = new RestrictionLogicalCondition();
            restricLog.add(restr2);
//            restricLog.add(restr3);
//            restr2.add(restr3);
//            restr2.setMaxResults(2);

            restr2.setMaxResults(5);
//            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
//            op1.add(pcDatafile.get(10));
            
            List<Datafile> li =  (List<Datafile>) DatafileSearch
                .searchByRestriction(VALID_USER_FOR_INVESTIGATION, restr3, DatafileInclude.NONE, 1, -1, em);
            for (Datafile d : li) {
                System.out.println("--------------------> " + d.getName());
            }
            } catch (Throwable t) {
                t.printStackTrace();
            }
//            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
//        } catch (RestrictionNullException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (CyclicException ex) {
//            exception = true;
//        } catch (OperatorINException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (RestrictionOperatorException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (RestrictionEmptyListException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (DatevalueException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (EmptyOperatorException ex) {
//            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        finally {
//            assertTrue("Should be a EmptyOperatorException", exception);
//        }
    }

    /**
     * Operator is empty
     */
    @Test
    public void restrictionNullTest () throws RestrictionException {
        boolean exception = false;
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR))
                .add(new RestrictionLogicalCondition(LogicalOperator.AND))
                .add(new RestrictionComparisonCondition(null, null, ""))
                ;

            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            op1.add(pcDatafile.get(1));
            List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
        } catch (RestrictionNullException ex) {
            exception = true;
        } catch (CyclicException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperatorINException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoDatetimeComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumericvalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoStringComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoNumericComparatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
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
    public void DatevalueExceptionParameter () throws RestrictionException {
        boolean exception = false;
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
            parameter.get("dataset1").setSearchable("N");
            lp.add(pv3);
            lp.add(pv4);
            DatasetSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        }catch (EmptyOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CyclicException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperatorINException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            exception = true;
        } catch (NullParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a NoSearchableException", exception);
            parameter.get("dataset1").setSearchable("Y");
        }
    }

    @Test
    public void ParameterNoExistsException () throws RestrictionException {
        boolean exception = false;
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            Parameter param = new Parameter(new ParameterPK("noName", "noUnits"));
            param.setDatafileParameter(true);
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATASET, param);
            lp.add(pv3);
            lp.add(pv4);
            DatasetSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        }catch (EmptyOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CyclicException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionNullException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperatorINException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatevalueException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionOperatorException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RestrictionEmptyListException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoParametersException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParameterNoExistsException ex) {
            exception = true;
        } catch (NoParameterTypeException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmptyListParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSearchableParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(RestrictionExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            assertTrue("Should be a ParameterNoExistsException", exception);
        }
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(RestrictionExceptionTest.class);
    }
}
