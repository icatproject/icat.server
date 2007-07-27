/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.security;

import uk.icat3.investigationmanager.*;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.AccessType;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestGateKeeperUpdaterInvestigation extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperCreatorInvestigation.class);
    private Random random = new Random();
    
    /**
     * Tests updater on valid investigation for read
     */
    @Test
    public void testUpdaterReadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for reading investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updater on valid investigation for delete
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterDeleteOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for deleting investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.DELETE, em);
            
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updater on valid investigation for download
     */
    @Test
    public void testUpdaterDownloadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for download investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updater on valid investigation for remove (cos investigation this test remove root)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterRemoveOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for remove investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updater on valid investigation for update
     */
    @Test
    public void testUpdaterUpdateOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for update investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updater on valid investigation for insert (cos investigation this test insert root)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updater on valid investigation for insert (cos investigation this test insert root)
     */
    @Test
    public void testUpdaterInvestigationInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(false);
        
        GateKeeper.performAuthorisation(UPDATER_USER+"_investigation", investigation, AccessType.CREATE, em);
        
        //no exception
        assertTrue("This should be true", true);
        
    }
    
    /**
     * Tests updater on valid investigation for update
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterSetFAOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for set FA on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, investigation, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    //now try for insert and remove of none investigation objects to test insert and remove
    /**
     * Tests updater on valid investigation for insert keyword
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterInsertKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, keyword, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests updater on valid investigation for update keyword
     */
    @Test
    public void testUpdaterUpdateKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for update keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        
        GateKeeper.performAuthorisation(UPDATER_USER, keyword, AccessType.UPDATE, em);
        
        assertTrue("This should be true", true);
    }
    
       /**
     * Tests updater on valid investigation for update keyword
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdaterRemoveKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for remove keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_UPDATER);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
             GateKeeper.performAuthorisation(UPDATER_USER, keyword, AccessType.REMOVE, em);      
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperUpdaterInvestigation.class);
    }
}
