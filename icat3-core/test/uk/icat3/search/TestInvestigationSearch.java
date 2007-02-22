/*
 * TestInvestigationSearch.java
 *
 * Created on 22 February 2007, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.util.BaseTestClass;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.util.TestConstants;
import uk.icat3.entity.Investigation;
/**
 *
 * @author gjd37
 */
public class TestInvestigationSearch extends BaseTestClass{
    
    private static Logger log = Logger.getLogger(TestInvestigationSearch.class);
    
    @Test public void firstTest(){
        log.debug("Test worked");
        assertTrue("Test worked", true);
    }
    
    
    @Test public void testInvalidUser(){
        log.info("Testing invalid user, should be no investigations or keywords");
       
        
        log.debug("Testing user investigations: "+INVALID_USER);        
        Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
        log.trace("Investigations for user "+INVALID_USER+" is "+userInvestigations.size());        
        
        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0 , userInvestigations.size());
        
        log.debug("Testing user investigations: "+INVALID_USER);        
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUser(INVALID_USER, VALID_INVESTIGATION_SURNAME, em);
        log.trace("Investigations for user "+VALID_INVESTIGATION_SURNAME+" is "+investigationsSurname.size());        
        
        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero size", 0 , investigationsSurname.size());
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigationSearch.class);
    }
    
}
