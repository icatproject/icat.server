/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.restriction.exception;

import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.NoParametersException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.AttributeTypeException;
import static org.junit.Assert.*;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.exceptions.RestrictionValueClassException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 * Restriction Exception tests
 * 
 * @author cruzcruz
 */
public class RestrictionExceptionTest extends BaseParameterSearchTest {

    /**
     * Exception where searching by Dataset, Datafile, Sample or Investigation
     * object.
     */
   @Test
   public void objectRestrictions () throws NoParameterTypeException, RestrictionException, NoParametersException, ParameterSearchException {

        RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(
                RestrictionAttributes.DATASET_NAME, ComparisonOperator.ENDS_WITH, "blue");

        RestrictionLogicalCondition restrLog = new RestrictionLogicalCondition(LogicalOperator.OR)
                .add(restriction1)
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.ENDS_WITH, ""))
                .add(new RestrictionComparisonCondition(
                RestrictionAttributes.SAMPLE_NAME, ComparisonOperator.ENDS_WITH, ""))
                ;

        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, parameter.get("datafile2_1"));
        lp.add(pv1);

        // Investigation search
        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, restrLog, InvestigationInclude.NONE, 1, -1, em);

        // Compare a Dataset with a Investigation = Exception
        RestrictionComparisonCondition objComp = new RestrictionComparisonCondition();
        // Attribute is a Dataset
        objComp.setRestrictionAttribute(RestrictionAttributes.DATASET);
        objComp.setComparisonOperator(ComparisonOperator.EQUALS);
        // Value is a investigation
        objComp.setValue(li.get(0));
        //// Datafile search ////
        boolean exception = false;
        try {
            DatafileSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, objComp, em);
        } catch (RestrictionValueClassException e) {
            exception = true;
        }
        assertTrue("Exception RestrictionValueClassException expected ", exception);
        //// Dataset search ////
        exception = false;
         try {
            DatasetSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, objComp, em);
        } catch (RestrictionValueClassException e) {
            exception = true;
        }
        assertTrue("Exception RestrictionValueClassException expected ", exception);
        //// Sample search ////
        exception = false;
         try {
            SampleSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, objComp, em);
        } catch (RestrictionValueClassException e) {
            exception = true;
        }
        assertTrue("Exception RestrictionValueClassException expected ", exception);
    }

   /**
    * Adding a empty condition is not cause to throw an Exception
    */
    @Test
    public void EmptyListParameterException () {
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

        // Empty condition
        RestrictionLogicalCondition emptyCondition = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(restCycleCond);

        restCycleCond.getRestConditions().add(emptyCondition);
        restCycleCond.getRestConditions().add(comparisonInstr);

        try {
            InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        } catch (Throwable e) {
            assertTrue("Exception not expected " + e.getMessage(), true);
        }
    }

    /**
     * Attribute cannot be relationed to the search.
     */
    @Test
    public void AttributeTypeException () {
        // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        // PARAMETER_NAME belongs to parameter
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.PARAMETER_NAME);
        comparisonInstr.setComparisonOperator(ComparisonOperator.CONTAINS);
        comparisonInstr.setValue("keyword number");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);

        try {
            // Attribute PARAMETER_NAME could not be relationed to Investigation search
            InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        } catch (Throwable ex) {
           assertEquals("Exception unexpected. ", AttributeTypeException.class, ex.getClass());
           return;
        }
        assertTrue("AttributeTypeException should be thrown.", false);
    }

    /**
     * Attribute cannot be relationed to the search.
     */
    @Test
    public void AttributeTypeException1 () {
        // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        // FACILITY_USER_FIRST_NAME belongs to parameter
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.FACILITY_USER_FIRST_NAME);
        comparisonInstr.setComparisonOperator(ComparisonOperator.CONTAINS);
        comparisonInstr.setValue("keyword number");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);

        try {
            // Attribute FACILITY_USER_FIRST_NAME could not be relationed to Investigation search
            InvestigationSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", AttributeTypeException.class, ex.getClass());
            return;
        }
        assertTrue("AttributeTypeException should be thrown.", false);
    }

    

    /**
     * Add the operator itself produces an cyclic execption.
     */
    @Test
    public void RestrictionOperatorExceptionTest () {
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add (new RestrictionComparisonCondition(
                            RestrictionAttributes.DATAFILE_ID, ComparisonOperator.STARTS_WITH, 123));

            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            ParameterLogicalCondition op2 = new ParameterLogicalCondition(LogicalOperator.AND);
            op2.add(pcDatafile.get(0));
            op2.add(pcDatafile.get(1));
            op1.add(op2);
            DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", RestrictionOperatorException.class, ex.getClass());
            return;
        }
        assertTrue("RestrictionOperatorException expected.", false);
    }

    /**
     * Cyclic exception
     */
    @Test
    public void CyclicExceptionTest () {
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR))
                .add(new RestrictionLogicalCondition(LogicalOperator.AND))
                .add(new RestrictionComparisonCondition(null, null, new Date()))
                ;

            // Cyclic operation; restricLog adds restricLog
            restricLog.add(restricLog);
            
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            op1.add(pcDatafile.get(1));
            List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", CyclicException.class, ex.getClass());
            return;
        }
        assertTrue("CyclicException expected.", false);
    }

    /**
     * Cyclic exception
     */
    @Test
    public void cyclicExceptionTest () {
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND);
            RestrictionLogicalCondition restr2 = new RestrictionLogicalCondition(LogicalOperator.AND);
            RestrictionLogicalCondition restr3 = new RestrictionLogicalCondition();
            restricLog.add(restr2);
            restricLog.add(restr3);
            // Cyclic operation
            restr2.add(restr3);
            restr3.add(restr2);
            restr2.setMaxResults(2);
            restr2.setMaxResults(5);
            
            DatafileSearch.searchByRestriction(VALID_USER_FOR_INVESTIGATION, restricLog
                    , DatafileInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", CyclicException.class, ex.getClass());
            return;
        }
        assertTrue("CyclicException expected.", false);
    }

    /**
     * A restriction condition is null
     */
    @Test
    public void restrictionNullTest () {
        try {
            // Restriction condition
            RestrictionLogicalCondition restricLog = new RestrictionLogicalCondition(LogicalOperator.AND)
                .add(new RestrictionLogicalCondition(LogicalOperator.OR))
                .add(new RestrictionLogicalCondition(LogicalOperator.AND))
                // NULL restriction condition
                .add(new RestrictionComparisonCondition(null, null, ""))
                ;

            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            op1.add(pcDatafile.get(1));
            List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restricLog, DatasetInclude.NONE, 1, -1, em);
            assertTrue("Results of investigations should be 2 not " + li.size(), li.size() == 2);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", RestrictionNullException.class, ex.getClass());
            return;
        }
        assertTrue("RestrictionNullException expected.", false);
    }


    /**
     * Parameter is not searchable
     */
    @Test
    public void DatevalueExceptionParameter () {
        ParameterSearch pv3 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
        try {
            List<ParameterSearch> lp = new ArrayList<ParameterSearch>();
            
            ParameterSearch pv4 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
            // Parameter sets to NOT searchable
            parameter.get("dataset1").setSearchable("N");
            lp.add(pv3);
            lp.add(pv4);
            DatasetSearch.searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, Queries.NO_RESTRICTION, DatasetInclude.NONE, 1, -1, em);
        } catch (Throwable ex) {
            assertEquals("Exception unexpected. ", NoSearchableParameterException.class, ex.getClass());
            return;
        } finally {
            // Restore parameter searchable value
            parameter.get("dataset1").setSearchable("Y");
        }
        assertTrue("NoSearchableParameterException expected.", false);
    }

    /**
     * Parmaeter not exists
     */
    @Test
    public void ParameterNoExistsException () {
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
        return new JUnit4TestAdapter(RestrictionExceptionTest.class);
    }
}
