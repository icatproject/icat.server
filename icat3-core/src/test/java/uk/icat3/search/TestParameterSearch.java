/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Test;

import uk.icat3.entity.Parameter;
import uk.icat3.util.BaseClassTransaction;
import uk.icat3.util.TestConstants;

/**
 *
 * @author gjd37
 */
public class TestParameterSearch extends BaseClassTransaction {
    
    private static Logger log = Logger.getLogger(TestParameterSearch.class);
    public static final String VALID_USER_FOR_INVESTIGATION = TestConstants.VALID_USER_FOR_INVESTIGATION;
    
    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByName(){
        Collection<Parameter> li = ParameterSearch.getParameterByName(VALID_USER_FOR_INVESTIGATION, "Datafile", em);
        assertEquals("Number of parameter incorrect", 0, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByUnits(VALID_USER_FOR_INVESTIGATION, "str", em);
        assertEquals("Number of parameter with units 'str' incorrect", 0, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByNameUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByNameUnits(VALID_USER_FOR_INVESTIGATION, "scan", "str", em);
        assertEquals("Number of parameter incorrect", 0, li.size());
    }
 
    
 
}
