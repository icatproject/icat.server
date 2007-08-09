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
public class TestGateKeeperDownloaderInvestigation extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperCreatorInvestigation.class);
    private Random random = new Random();
    
    /**
     * Tests downloader on valid investigation for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testDownloaderReadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for reading investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid investigation for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests admin on valid investigation for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting investigation not FA Id: "+VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);
        
        Investigation investigation = getInvestigationNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testDownloaderDownloadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for download investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid investigation for remove (cos investigation this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for update
     *
     * ACTION_UPDATE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update investigation not FA Id: "+VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);
        
        Investigation investigation = getInvestigationNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for insert (cos investigation this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for insert (cos investigation this test insert root)
     *
     * ACTION_ROOT_INSERT - N (set null in inv_id for ICAT_ADMIN_USER, no root insert )
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInvestigationInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(false);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests downloader on valid investigation for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderSetFAOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for set FA on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    //now try for insert and remove of none investigation objects to test insert and remove
    /**
     * Tests downloader on valid investigation for insert keyword
     *
     * ACTION_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, keyword, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for update keyword
     *
     * ACTION_UPDATE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, keyword, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderManageUsersOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for ManageUsers on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid investigation for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, keyword, AccessType.REMOVE, em);
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
    public void testAdmiDownloaderDatasetParameterOnDataset2() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove keyword on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setCreateId(DOWNLOADER_USER);
        keyword.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, keyword, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperDownloaderInvestigation.class);
    }
}
