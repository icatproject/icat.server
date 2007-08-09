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
public class TestGateKeeperIcatAdminDataset extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperIcatAdminDataset.class);
    private Random random = new Random();
    
    /**
     * Tests icat admin on valid Dataset for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testIcatAdminReadOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for reading Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testIcatAdminDeleteOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for deleting Dataset is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests icat admin on valid Dataset for delete
     *
     * ACTION_DELETE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testIcatAdminDeleteOnDatasetNotFA() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for deleting Dataset not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Dataset Dataset = getDatasetNotFA_Acquired();
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.DELETE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testIcatAdminDownloadOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for download Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for remove (cos Dataset this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testIcatAdminRemoveOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for remove Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests icat admin on valid Dataset for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testIcatAdminUpdateOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for update Dataset is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests icat admin on valid Dataset for update
     *
     * ACTION_UPDATE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testIcatAdminUpdateOnDatasetNotFA() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for update Dataset not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Dataset Dataset = getDatasetNotFA_Acquired();
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for insert (cos Dataset this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testIcatAdminInsertOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for insert on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests icat admin on valid Dataset for insert (cos Dataset this test insert root)
     *
     * ACTION_ROOT_INSERT - Y (set null in inv_id for ICAT_ICAT_ADMIN_USER)
     */
    @Test
    public void testIcatAdminDatasetInsertOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for insert on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Investigation investigation = getInvestigation(true);
        Dataset dataset = getDataset(false);
        dataset.setInvestigation(investigation);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dataset, AccessType.CREATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for update
     *
     * ACTION_SET_FA - Y
     */
    @Test
    public void testIcatAdminSetFAOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for set FA on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset Dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Dataset, AccessType.SET_FA, em);
        
        //no exception
        assertTrue("This should be true", true);
        
    }
    
    //now try for insert and remove of none Dataset objects to test insert and remove
    /**
     * Tests icat admin on valid Dataset for insert keyword
     *
     * ACTION_INSERT - Y
     */
    @Test
    public void testIcatAdminInsertDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for insert DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.CREATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for update keyword
     *
     * ACTION_UPDATE - Y
     */
    @Test
    public void testIcatAdminUpdateDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for update DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for update keyword
     *
     * MANAGE_USERS - Y
     */
    @Test
    public void testIcatAdminManageUsersOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for ManageUsers on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Dataset dataset = getDataset(true);
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dataset, AccessType.MANAGE_USERS, em);
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests icat admin on valid Dataset for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testIcatAdminRemoveDatasetParameterOnDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests icat admin on valid Dataset for update keyword
     *
     * ACTION_REMOVE - Y is same create id as user
     */
    @Test
    public void testIcatAdminRemoveDatasetParameterOnDataset2() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for remove DatasetParameter on Dataset Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Dataset dataset = getDataset(true);
        DatasetParameter dsp = new DatasetParameter();
        dsp.setCreateId(ICAT_ADMIN_USER);
        dsp.setDataset(dataset);
        
        
        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.REMOVE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperIcatAdminDataset.class);
    }
}
