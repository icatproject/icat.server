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
import uk.icat3.entity.IcatRole;
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
public class TestGateKeeperSuperUserInvestigation extends TestGateKeeperUtil {

    private static Logger log = Logger.getLogger(TestGateKeeperCreatorInvestigation.class);
    private Random random = new Random();

    /**
     * Tests super user on valid investigation for read
     *
     * ACTION_SELECT  - Y
     */
    @Test
    public void testSuperUserReadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for reading investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.READ, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for delete
     *
     * ACTION_DELETE - 'Y'
     */
    @Test
    public void testSuperUserDeleteOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for deleting investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.DELETE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests admin on valid investigation for delete
     *
     * ACTION_DELETE - Y
     */
    @Test
    public void testAdminDeleteOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for deleting investigation not FA Id: " + VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);

        Investigation investigation = getInvestigationNotFA_Acquired();

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.DELETE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for download
     *
     * ACTION_DOWNLOAD - Y
     */
    @Test
    public void testSuperUserDownloadOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for download investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.DOWNLOAD, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for remove 
     *
     * ACTION_REMOVE_ROOT - Y
     */
    @Test
    public void testSuperUserRemoveOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for remove investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.REMOVE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for remove 
     *
     * ACTION_REMOVE_ROOT - Y
     */
    @Test
    public void testSuperUserRemoveOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for remove investigation Id: " + VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);

        Investigation investigation = getInvestigationNotFA_Acquired();

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.REMOVE, em);
        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for remove 
     *
     * ACTION_REMOVE_ROOT - Y 
     */
    @Test
    public void testSuperUserRemoveOnInvestigationNotFASameCreateId() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for remove investigation Id: " + VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);

        Investigation investigation = getInvestigationNotFA_Acquired();
        investigation.setCreateId(SUPER_USER);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.REMOVE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for update
     *
     * ACTION_UPDATE -  'Y'
     */
    @Test
    public void testSuperUserUpdateOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for update investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.UPDATE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests admin on valid investigation for update
     *
     * ACTION_UPDATE - Y (But no on SET_FA = 'Y')
     */
    @Test
    public void testAdminUpdateOnInvestigationNotFA() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for update investigation not FA Id: " + VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);

        Investigation investigation = getInvestigationNotFA_Acquired();

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.UPDATE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for insert (cos investigation this test insert root)
     *
     * ACTION_ROOT_INSERT - Y
     */
    @Test
    public void testSuperUserInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for insert on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for insert )
     *
     * ACTION_ROOT_INSERT - Y 
     */
    @Test
    public void testSuperUserInvestigationInsertOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for insert on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(false);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    /**
     * Tests super user on valid investigation for update
     *
     * ACTION_SET_FA - Y
     */
    @Test
    public void testSuperUserSetFAOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for set FA on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);

        IcatRole role = GateKeeper.performAuthorisation(SUPER_USER, investigation, AccessType.SET_FA, em);

        //no exception
        assertTrue("This should be true", true);
        assertTrue("Role is super", role.getRole().equals("SUPER"));
    }

    //now try for insert and remove of none investigation objects to test insert and remove
    /**
     * Tests super user on valid investigation for insert keyword
     *
     * ACTION_INSERT - Y
     */
    @Test
    public void testSuperUserInsertKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for insert keyword on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);

        GateKeeper.performAuthorisation(SUPER_USER, keyword, AccessType.CREATE, em);

        //no exception
        assertTrue("This should be true", true);        
    }

    /**
     * Tests super user on valid investigation for update keyword
     *
     * ACTION_UPDATE - Y
     */
    @Test
    public void testSuperUserUpdateKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for update keyword on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);

        GateKeeper.performAuthorisation(SUPER_USER, keyword, AccessType.UPDATE, em);

        //no exception
        assertTrue("This should be true", true);        
    }

    /**
     * Tests super user on valid investigation for update keyword
     *
     * ACTION_REMOVE - Y
     */
    @Test
    public void testSuperUserRemoveKeywordOnInvestigation() throws ICATAPIException {
        log.info("Testing  user: " + SUPER_USER + " for remove keyword on investigation Id: " + VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN);

        Investigation investigation = getInvestigation(true);
        Keyword keyword = new Keyword();
        keyword.setInvestigation(investigation);

        GateKeeper.performAuthorisation(SUPER_USER, keyword, AccessType.REMOVE, em);

        //no exception
        assertTrue("This should be true", true);        
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestGateKeeperSuperUserInvestigation.class);
    }
}
