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
     */
    @Test
    public void testDownloaderReadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for reading investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid investigation for delete
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     * Tests downloader on valid investigation for download
     */
    @Test
    public void testDownloaderDownloadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for download investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
        Investigation investigation = getInvestigation(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, investigation, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid investigation for remove (cos investigation this test remove root)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     * Tests downloader on valid investigation for insert (cos investigation this test insert root)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     */
    @Test
    public void testDownloaderInvestigationInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
        Investigation investigation = getInvestigation(false);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER+"_investigation", investigation, AccessType.CREATE, em);
        
        //no exception
        assertTrue("This should be true", true);
        
    }
    
    /**
     * Tests downloader on valid investigation for update
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderSetFAOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for set FA on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
     * Tests downloader on valid investigation for update keyword
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove keyword on investigation Id: "+VALID_INVESTIGATION_ID_FOR_DOWNLOADER);
        
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
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperDownloaderInvestigation.class);
    }
}
