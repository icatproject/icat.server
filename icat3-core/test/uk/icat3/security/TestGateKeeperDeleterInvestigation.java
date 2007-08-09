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
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestGateKeeperDeleterInvestigation extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperCreatorInvestigation.class);
    private Random random = new Random();
    
    /**
     * Tests deleter on valid investigation for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testDeleterReadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for reading investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests deleter on valid investigation for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterDeleteOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for deleting investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for delete
     *
     * ACTION_DELETE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testDeleterDeleteOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for deleting investigation not FA Id: "+VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);
        
        Investigation investigation = getInvestigationNotFA_Acquired();
        
        GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.DELETE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests deleter on valid investigation for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testDeleterDownloadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for download investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests deleter on valid investigation for remove (cos investigation this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterRemoveOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for remove investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterUpdateOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for update investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for update
     *
     * ACTION_UPDATE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testAdminUpdateOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for update investigation not FA Id: "+VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);
        
        Investigation investigation = getInvestigationNotFA_Acquired();
        
        GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests deleter on valid investigation for insert (cos investigation this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for insert (cos investigation this test insert root)
     *
     * ACTION_ROOT_INSERT - N (set null in inv_id for ICAT_ADMIN_USER, but still does not have root insert action)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterInvestigationInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(false);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterSetFAOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for set FA on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    //now try for insert and remove of none investigation objects to test insert and remove
    /**
     * Tests deleter on valid investigation for insert keyword
     *
     * ACTION_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterInsertKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for insert keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, keyword, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests deleter on valid investigation for update keyword
     *
     * ACTION_UPDATE - Y
     */
    @Test
    public void testDeleterUpdateKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for update keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        GateKeeper.performAuthorisation(DELETER_USER, keyword, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests creator on valid Dataset for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterManageUsersOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for ManageUsers on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, investigation, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid investigation for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testtDeleterRemoveKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for remove keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, keyword, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
     /**
     * Tests admin on valid Dataset for update keyword
     *
     * ACTION_REMOVE - N is same create id as user
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testAdminRemoveDatasetParameterOnDataset2() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
                
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setCreateId(DELETER_USER);
        keyword.setInvestigation(investigation);
                
        try {
        GateKeeper.performAuthorisation(DELETER_USER, keyword, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperDeleterInvestigation.class);
    }
}
