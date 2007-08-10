/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.security;


import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.AccessType;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestGateKeeperDownloaderDatafile extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperDownloaderDatafile.class);
    private Random random = new Random();
    
    /**
     * Tests downloader on valid Datafile for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testDownloaderReadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for reading Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testDownloaderDownloadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for download Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid Datafile for remove (cos Datafile this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests downloader on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - Y (set null in inv_id for ICAT_DOWNLOADER_USER)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDatafileInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
         Dataset dataset = em.find(Dataset.class, VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        Datafile datafile = getDatafile(false);
        datafile.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderSetFAOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for set FA on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Datafile, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    //now try for insert and remove of none Datafile objects to test insert and remove
    /**
     * Tests downloader on valid Datafile for insert keyword
     *
     * ACTION_INSERT - Y
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for update keyword
     *
     * ACTION_UPDATE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
                try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderManageUsersOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for ManageUsers on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dataset, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid Datafile for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterManageUsersOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for ManageUsers on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
               
        try {
       GateKeeper.performAuthorisation(DOWNLOADER_USER, dataset, AccessType.MANAGE_USERS, em);
         } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    /**
     * Tests downloader on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveDatafileParameterOnDatafile2() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setCreateId(ADMIN_USER);
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
     /**
     * Tests deleter on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterRemoveDatafileParameterOnDatafile2() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setCreateId(ADMIN_USER);
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperDownloaderDatafile.class);
    }
}
