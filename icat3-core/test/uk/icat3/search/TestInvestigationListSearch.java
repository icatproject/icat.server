/*
 * TestInvestigationSearch.java
 *
 * Created on 22 February 2007, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import org.junit.Test;

import uk.icat3.entity.Investigation;
import uk.icat3.search.parameter.util.ElementType;
import uk.icat3.util.BaseClassTransaction;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
public class TestInvestigationListSearch extends BaseClassTransaction{
    
    private static Logger log = Logger.getLogger(TestInvestigationListSearch.class);
       
    @Test
    public void testGetAllNoUserInvestigations(){
        log.info("Testing ANY, getAllInvestigations: ANY");
        
        log.debug("Testing user investigations: ANY");
        
        Collection<Investigation> invs = em.createQuery(Queries.INVESTIGATIONS_BY_USER_JPQL).
                setParameter("objectType",ElementType.INVESTIGATION).
                setParameter("userId","ANY").getResultList();
        
        log.trace("Investigations for user ANY is "+invs.size());
        
        assertNotNull("Must not be an empty collection", invs);
        assertEquals("Collection 'all Investigations' should be 1", 1 , invs.size());
    }
    
    @Test
    public void testGetAllNoneScientistInvestigations(){
        log.info("Testing none_facility_scientist, getAllInvestigations: none_facility_scientist");
        
        log.debug("Testing user investigations: none_facility_scientist");
        
        Collection<Investigation> invs = em.createQuery(Queries.INVESTIGATIONS_BY_USER_JPQL).
                setParameter("objectType",ElementType.INVESTIGATION).
                setParameter("userId","none_facility_scientist").getResultList();
        
        log.trace("Investigations for user none_facility_scientist is "+invs.size());
        
        assertNotNull("Must not be an empty collection", invs);
        assertEquals("Collection 'all Investigations' should be 1", 1 , invs.size());
    }
    
    //@Test
    public void testGetAllScientistInvestigations(){
        log.info("Testing facility_scientist, getAllInvestigations: facility_scientist");
        
        log.debug("Testing user investigations: facility_scientist");
        
        Collection<Investigation> invs = em.createQuery(Queries.INVESTIGATIONS_BY_USER_JPQL).
                setParameter("objectType",ElementType.INVESTIGATION).
                setParameter("userId","facility_scientist").getResultList();
        
        log.trace("Investigations for user facility_scientist is "+invs.size());
        
        assertNotNull("Must not be an empty collection", invs);
        assertEquals("Collection 'all Investigations' should be 5", 5 , invs.size());
    }    
    
    @Test
    public void testGetAllDeletedScientistInvestigations(){
        log.info("Testing deleted_facility_scientist, getAllInvestigations: deleted_facility_scientist");
        
        log.debug("Testing user investigations: deleted_facility_scientist");
        
        Collection<Investigation> invs = em.createQuery(Queries.INVESTIGATIONS_BY_USER_JPQL).
                setParameter("objectType",ElementType.INVESTIGATION).
                setParameter("userId","deleted_facility_scientist").getResultList();
        
        log.trace("Investigations for user deleted_facility_scientist is "+invs.size());
        
        assertNotNull("Must not be an empty collection", invs);
        assertEquals("Collection 'all Investigations' should be 1", 1 , invs.size());
    }    
             
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigationListSearch.class);
    }
}
