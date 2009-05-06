/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestInvestigator extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvestigator.class);
    private Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addInvestigator() throws ICATAPIException {
        log.info("Testing user: "+VALID_USER_FOR_INVESTIGATION+ " for adding investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigator(true);
        
        Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator, VALID_INVESTIGATION_ID, em);
        
        Investigator modified = em.find(Investigator.class,investigatorInserted.getInvestigatorPK() );
        
        checkInvestigator(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void modifyInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedRole = "Modfied Role "+random.nextInt();
        //create invalid investigator, no name
        Investigator modifiedInvestigator = getInvestigator(true);
        Investigator duplicateInvestigator = getInvestigatorDuplicate(true);
        modifiedInvestigator.setRole(modifiedRole);
        
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        modifiedInvestigator.setInvestigation(in);
        modifiedInvestigator.setInvestigatorPK(duplicateInvestigator.getInvestigatorPK());
        
        InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, modifiedInvestigator, em);
        
        Investigator modified = em.find(Investigator.class,duplicateInvestigator.getInvestigatorPK() );
        
        assertEquals("Role must be "+modifiedRole+" and not "+modified.getRole(), modified.getRole(), modifiedRole);
        
        checkInvestigator(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator duplicateInvestigator = getInvestigatorDuplicate(true);
        
        try {
            Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateInvestigator, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void deleteInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigatorDuplicate(true);
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator,  AccessType.DELETE, em);
        
        Investigator modified = em.find(Investigator.class,validInvestigator.getInvestigatorPK() );
        
        checkInvestigator(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void undeleteInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigatorDuplicate(true);
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator,  AccessType.DELETE, em);
        
        Investigator modified = em.find(Investigator.class,validInvestigator.getInvestigatorPK() );
        
        checkInvestigator(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        deleteInvestigator();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator duplicateInvestigator = getInvestigatorDuplicate(true);
        try{
            Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateInvestigator, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
        
        /*Investigator modified = em.find(Investigator.class,investigatorInserted.getInvestigatorPK() );
         
        checkInvestigator(modified);
        assertFalse("Deleted must be false", modified.isDeleted());*/
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator duplicateInvestigator = getInvestigatorDuplicate(true);
        duplicateInvestigator.setDeleted(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateInvestigator, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void removeActualInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator duplicateInvestigator = getInvestigatorDuplicate(true);
        duplicateInvestigator.setDeleted(false);
        duplicateInvestigator.setCreateId(ICAT_ADMIN_USER);
        
        InvestigationManager.deleteInvestigationObject(ICAT_ADMIN_USER, duplicateInvestigator, AccessType.REMOVE, em);
        
        Investigator modified = em.find(Investigator.class,duplicateInvestigator.getInvestigatorPK() );
        assertNull("Investigator must not be found in DB "+duplicateInvestigator, modified);
        
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addInvestigatorInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigator(true);
        
        try {
            Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(INVALID_USER, validInvestigator, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void addInvestigatorInvalidInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding investigator to invalid investigation Id");
        
        Investigator validInvestigator  = getInvestigator(true);
        
        try {
            Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator, random.nextLong(), em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator invalidInvestigator = getInvestigator(false);
        
        try {
            Investigator investigatorInserted = (Investigator)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidInvestigator, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'exist'", ex.getMessage().contains("exist"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void modifyInvestigatorProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a props investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator propsInvestigator = getInvestigatorDuplicate(false);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsInvestigator, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void deleteInvestigatorProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator propsInvestigator = getInvestigatorDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsInvestigator, AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeInvestigatorProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigator, no name
        Investigator propsInvestigator = getInvestigatorDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsInvestigator, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a investigator, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteInvestigatorNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigator(true);
        validInvestigator.setInvestigatorPK(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator,  AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a investigator, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeInvestigatorNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigator(true);
        validInvestigator.setInvestigatorPK(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator,  AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a investigator, no Id
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void updateInvestigatorNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for update investigator to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigator validInvestigator  = getInvestigator(true);
        validInvestigator.setInvestigatorPK(null);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, validInvestigator,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    
    private Investigator getInvestigatorDuplicate(boolean last){
        Investigator investigator = null;
        if(!last){
            Collection<Investigator> investigators = (Collection<Investigator>)executeListResultCmd("select d from Investigator d where d.facilityAcquired = 'Y'");
            investigator = investigators.iterator().next();
        } else {
            Collection<Investigator> investigators = (Collection<Investigator>)executeListResultCmd("select d from Investigator d where d.facilityAcquired = 'N' order by d.modTime desc");
            investigator = investigators.iterator().next();
        }
        log.trace(investigator);
        return investigator;
    }
    
    private Investigator getInvestigator(boolean valid){
        if(valid){
            //create valid investigator
            Investigator investigator = new Investigator(VALID_USER,  VALID_INVESTIGATION_ID);
            
            return investigator;
        } else {
            //create invalid investigator
            Investigator investigator = new Investigator(INVALID_USER, VALID_INVESTIGATION_ID);
            return investigator;
        }
    }
    
    private void checkInvestigator(Investigator investigator){
        assertNotNull("PK cannot be null", investigator.getInvestigatorPK());
        assertNotNull("FacilityUserId cannot be null", investigator.getInvestigatorPK().getFacilityUserId());
        assertNotNull("PK InvestigationId cannot be null", investigator.getInvestigatorPK().getInvestigationId());
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, investigator.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, investigator.getModId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Investigation id must be "+VALID_INVESTIGATION_ID, investigator.getInvestigation().getId(), VALID_INVESTIGATION_ID);
        assertEquals("PK Investigation id must be "+VALID_INVESTIGATION_ID, investigator.getInvestigatorPK().getInvestigationId(), VALID_INVESTIGATION_ID);
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigator.class);
    }
}
