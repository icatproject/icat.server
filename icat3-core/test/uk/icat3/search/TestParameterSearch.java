/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.util.LogicalOperator;
import static org.junit.Assert.*;

/**
 *
 * @author gjd37
 */
public class TestParameterSearch extends BaseParameterSearchTest {
    
    private static Logger log = Logger.getLogger(TestParameterSearch.class);
    
    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByName(){
        Collection<Parameter> li = ParameterSearch.getParameterByName(VALID_USER_FOR_INVESTIGATION, "Datafile", em);
        assertEquals("Number of parameter incorrect", 5, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByUnits(VALID_USER_FOR_INVESTIGATION, "str", em);
        assertEquals("Number of parameter with units 'str' incorrect", 2, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByNameUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByNameUnits(VALID_USER_FOR_INVESTIGATION, "scan", "str", em);
        assertEquals("Number of parameter incorrect", 1, li.size());
    }
    @Test
    public void testgetParmaeterByRestriction() throws RestrictionException {
     // Instruments logical condition
        RestrictionLogicalCondition restInstrumentCond = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);
        // Cycles logical condition
        RestrictionLogicalCondition restCycleCond = new RestrictionLogicalCondition();
        restCycleCond.setOperator(LogicalOperator.AND);
        // Create new comparison
        RestrictionComparisonCondition comparisonInstr;
        comparisonInstr = new RestrictionComparisonCondition();
        comparisonInstr.setRestrictionAttribute(RestrictionAttributes.PARAMETER_UNITS);
        comparisonInstr.setComparisonOperator(ComparisonOperator.STARTS_WITH);
        comparisonInstr.setValue("str");

        RestrictionLogicalCondition r2 = new RestrictionLogicalCondition();
        restInstrumentCond.setOperator(LogicalOperator.AND);

        restInstrumentCond.getRestConditions().add(comparisonInstr);
        restInstrumentCond.getRestConditions().add(restCycleCond);
        restCycleCond.getRestConditions().add(r2);
        restInstrumentCond.setOrderByAttribute(RestrictionAttributes.PARAMETER_NAME);
        restInstrumentCond.setOrderByAsc(true);

        List<Parameter> li = (List<Parameter>) ParameterSearch.getParameterByRestriction(VALID_USER_FOR_INVESTIGATION, restInstrumentCond, em);
        assertEquals("First parameter name incorrect", "scanType", li.get(0).getParameterPK().getName());
        assertEquals("Number of parameter with units 'str' incorrect", 2, li.size());
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestParameterSearch.class);
    }
}
