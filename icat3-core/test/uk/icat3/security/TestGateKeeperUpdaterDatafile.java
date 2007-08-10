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
public class TestGateKeeperUpdaterDatafile extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperUpdaterDatafile.class);
    private Random random = new Random();
    
    /**
     * Tests updator on valid Datafile for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testUpdatorReadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for reading Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updator on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorDeleteOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for deleting Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorDeleteOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for deleting Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testUpdatorDownloadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for download Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        
        GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.DOWNLOAD, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updator on valid Datafile for remove (cos Datafile this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorRemoveOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for remove Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests updator on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorUpdateOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for update Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test
    public void testUpdatorUpdateOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for update Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updator on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - v (set null in inv_id for ICAT_UPDATER_USER)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorDatafileInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
                
        Dataset dataset = em.find(Dataset.class, VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        Datafile datafile = getDatafile(false);
        datafile.setDataset(dataset);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorSetFAOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for set FA on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, Datafile, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    //now try for insert and remove of none Datafile objects to test insert and remove
    /**
     * Tests updator on valid Datafile for insert keyword
     *
     * ACTION_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorInsertDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for insert DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, dsp, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for update keyword
     *
     * ACTION_UPDATE - N
     */
    @Test
    public void testUpdatorUpdateDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for update DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        GateKeeper.performAuthorisation(UPDATER_USER, dsp, AccessType.UPDATE, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests updator on valid Datafile for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorManageUsersOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for ManageUsers on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, dataset, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorRemoveDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests updator on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N is same create id as user
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testUpdatorRemoveDatafileParameterOnDatafile2() throws ICATAPIException {
        log.info("Testing  user: "+UPDATER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setCreateId(UPDATER_USER);
        dsp.setDatafile(dataset);
        
        
        
        try {
            GateKeeper.performAuthorisation(UPDATER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperUpdaterDatafile.class);
    }
}
