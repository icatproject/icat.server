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
public class TestGateKeeperIcatAdminDatafile extends TestGateKeeperUtil {

    private static Logger log = Logger.getLogger(TestGateKeeperIcatAdminDatafile.class);
    private Random random = new Random();

    /**
     * Tests icat admin on valid Datafile for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testIcatAdminReadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for reading Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.READ, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for delete
     *
     * ACTION_DELETE - N (But no on SET_FA = 'Y')
     */
    @Test(expected = InsufficientPrivilegesException.class)
    public void testIcatAdminDeleteOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for deleting Datafile is FA Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.DELETE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }

    /**
     * Tests icat admin on valid Datafile for delete
     *
     * ACTION_DELETE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testIcatAdminDeleteOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for deleting Datafile not FA Id: " + VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);

        Datafile Datafile = getDatafileNotFA_Acquired();

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.DELETE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testIcatAdminDownloadOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for download Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.DOWNLOAD, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for remove (cos Datafile this test remove root)
     *
     * ACTION_REMOVE_ROOT - N
     */
    @Test(expected = InsufficientPrivilegesException.class)
    public void testIcatAdminRemoveOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for remove Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }

    }

    /**
     * Tests icat admin on valid Datafile for update
     *
     * ACTION_UPDATE - N (But no on SET_FA = 'Y')
     */
    @Test(expected = InsufficientPrivilegesException.class)
    public void testIcatAdminUpdateOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for update Datafile is FA Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.UPDATE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }

    /**
     * Tests icat admin on valid Datafile for update
     *
     * ACTION_UPDATE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testIcatAdminUpdateOnDatafileNotFA() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for update Datafile not FA Id: " + VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);

        Datafile Datafile = getDatafileNotFA_Acquired();

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.UPDATE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - Y on datafile
     */
    @Test
    public void testIcatAdminInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for insert on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for insert (cos Datafile this test insert root)
     *
     * ACTION_ROOT_INSERT - Y (set null in inv_id for ICAT_ICAT_ADMIN_USER)
     */
    @Test
    public void testIcatAdminDatafileInsertOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for insert on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Dataset dataset = em.find(Dataset.class, VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);
        Datafile datafile = getDatafile(false);
        datafile.setDataset(dataset);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, datafile, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for update
     *
     * ACTION_SET_FA - Y
     */
    @Test
    public void testIcatAdminSetFAOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for set FA on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile Datafile = getDatafile(true);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, Datafile, AccessType.SET_FA, em);

        //no exception
        assertTrue("This should be true", true);

    }

    //now try for insert and remove of none Datafile objects to test insert and remove
    /**
     * Tests icat admin on valid Datafile for insert keyword
     *
     * ACTION_INSERT - Y
     */
    @Test
    public void testIcatAdminInsertDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for insert DatafileParameter on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for update keyword
     *
     * ACTION_UPDATE - Y
     */
    @Test
    public void testIcatAdminUpdateDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for update DatafileParameter on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.UPDATE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for update keyword
     *
     * MANAGE_USERS - Y
     */
    @Test
    public void testIcatAdminManageUsersOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for ManageUsers on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);

        Datafile dataset = getDatafile(true);

        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dataset, AccessType.MANAGE_USERS, em);
        //no exception
        assertTrue("This should be true", true);
    }

    /**
     * Tests icat admin on valid Datafile for update keyword
     *
     * ACTION_REMOVE - N
     */
    @Test(expected = InsufficientPrivilegesException.class)
    public void testIcatAdminRemoveDatafileParameterOnDatafile() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for remove DatafileParameter on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);


        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setDatafile(dataset);

        try {
            GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.REMOVE, em);
        } catch (InsufficientPrivilegesException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }

    /**
     * Tests icat admin on valid Datafile for update keyword
     *
     * ACTION_REMOVE - Y is same create id as user
     */
    @Test
    public void testIcatAdminRemoveDatafileParameterOnDatafile2() throws ICATAPIException {
        log.info("Testing  user: " + ICAT_ADMIN_USER + " for remove DatafileParameter on Datafile Id: " + VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);


        Datafile dataset = getDatafile(true);
        DatafileParameter dsp = new DatafileParameter();
        dsp.setCreateId(ICAT_ADMIN_USER);
        dsp.setDatafile(dataset);


        GateKeeper.performAuthorisation(ICAT_ADMIN_USER, dsp, AccessType.REMOVE, em);

        //no exception
        assertTrue("This should be true", true);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestGateKeeperIcatAdminDatafile.class);
    }
}
