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
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.AccessType;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestGateKeeperDownloaderDataset extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperDownloaderDataset.class);
    private Random random = new Random();
    
    /**
     * Tests downloader on valid Dataset for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testDownloaderReadOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for reading Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid Dataset for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting Dataset is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDeleteOnDatasetNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for deleting Dataset not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Dataset Dataset = getDatasetNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testDownloaderDownloadOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for download Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests downloader on valid Dataset for remove (cos Dataset this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests downloader on valid Dataset for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update Dataset is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateOnDatasetNotFA() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update Dataset not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Dataset Dataset = getDatasetNotFA_Acquired();
        
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for insert (cos Dataset this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for insert (cos Dataset this test insert root)
     *
     * ACTION_ROOT_INSERT - Y (set null in inv_id for ICAT_DOWNLOADER_USER)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderDatasetInsertOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Dataset dataset = getDataset(false);
        dataset.setInvestigation(investigation);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dataset, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderSetFAOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for set FA on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, Dataset, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    //now try for insert and remove of none Dataset objects to test insert and remove
    /**
     * Tests downloader on valid Dataset for insert keyword
     *
     * ACTION_INSERT - Y
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderInsertDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for insert DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for update keyword
     *
     * ACTION_UPDATE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderUpdateDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for update DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
                try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.UPDATE, em);
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
        
        Dataset dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dataset, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests downloader on valid Dataset for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleter on valid Dataset for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterManageUsersOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for ManageUsers on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
               
        try {
       GateKeeper.performAuthorisation(DOWNLOADER_USER, dataset, AccessType.MANAGE_USERS, em);
         } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    /**
     * Tests downloader on valid Dataset for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDownloaderRemoveDatasetParameterOnDataset2() throws ICATAPIException {
        log.info("Testing  user: "+DOWNLOADER_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setCreateId(ADMIN_USER);
        dsp.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(DOWNLOADER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
     /**
     * Tests deleter on valid Dataset for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleterRemoveDatasetParameterOnDataset2() throws ICATAPIException {
        log.info("Testing  user: "+DELETER_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setCreateId(ADMIN_USER);
        dsp.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(DELETER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperDownloaderDataset.class);
    }
}
