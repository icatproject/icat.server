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
public class TestGateKeeperReaderDatafile extends TestGateKeeperUtil {
    
    private static Logger log = Logger.getLogger(TestGateKeeperReaderDatafile.class);
    private Random random = new Random();
    
    /**
     * Tests reader on valid Datafile for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testReaderReadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for reading Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.READ, em);
        
        //no exception
        assertTrue("This should be true", true);
    }
    
    /**
     * Tests reader on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderDeleteOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for deleting Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderDeleteOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for deleting Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderDownloadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for download Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.DOWNLOAD, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for remove (cos Datafile this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderRemoveOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for remove Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    /**
     * Tests reader on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderUpdateOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for update Datafile is FA Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderUpdateOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for update Datafile not FA Id: "+VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        
        Datafile Datafile = getDatafileNotFA_Acquired();
        
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - v (set null in inv_id for ICAT_READER_USER)
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderDatafileInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for insert on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
          Dataset dataset = em.find(Dataset.class, VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        Datafile datafile = getDatafile(false);
        datafile.setDataset(dataset);
        
                
        try {
            GateKeeper.performAuthorisation(READER_USER, datafile, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update
     *
     * ACTION_SET_FA - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderSetFAOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for set FA on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile Datafile = getDatafile(true);
        
        
        try {
            GateKeeper.performAuthorisation(READER_USER, Datafile, AccessType.SET_FA, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
    }
    
    //now try for insert and remove of none Datafile objects to test insert and remove
    /**
     * Tests reader on valid Datafile for insert keyword
     *
     * ACTION_INSERT - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderInsertDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for insert DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        
        try {
            GateKeeper.performAuthorisation(READER_USER, dsp, AccessType.CREATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update keyword
     *
     * ACTION_UPDATE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderUpdateDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for update DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        
        try {
            GateKeeper.performAuthorisation(READER_USER, dsp, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update keyword
     *
     * MANAGE_USERS - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderManageUsersOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for ManageUsers on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        Datafile dataset = getDatafile(true);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, dataset, AccessType.MANAGE_USERS, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderRemoveDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);
        
        try {
            GateKeeper.performAuthorisation(READER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests reader on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N is same create id as user
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testReaderRemoveDatafileParameterOnDatafile2() throws ICATAPIException {
        log.info("Testing  user: "+READER_USER+ " for remove DatafileParameter on Datafile Id: "+VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);
        
        
        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setCreateId(READER_USER);
        dsp.setDatafile(dataset);
        
        
        
        try {
            GateKeeper.performAuthorisation(READER_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestGateKeeperReaderDatafile.class);
    }
}
