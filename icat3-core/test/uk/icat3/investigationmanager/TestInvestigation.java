/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestInvestigation extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvestigation.class);
    private static Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding investigation");
        
        Investigation validInvestigation  = getInvestigation(true);
        
        Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,investigationInserted.getId());
        
        checkInvestigation(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        
        //check that investigator is there
        for(Investigator investigator : modified.getInvestigatorCollection()){
            log.trace(investigator.getInvestigation());
            log.trace(investigator.getInvestigatorPK().getFacilityUserId());
            assertFalse("investigator must not be deleted", investigator.isDeleted());
            assertEquals("investigator must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, investigator.getInvestigatorPK().getFacilityUserId());
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigation(true);
        
        try {
            Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, duplicateInvestigation,  em);
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
    public void deleteInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigationDuplicate(true);
        
        InvestigationManager.deleteInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,validInvestigation.getId() );
        
        checkInvestigation(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        for(Investigator investigator : modified.getInvestigatorCollection()){
            assertTrue("investigator must be deleted", investigator.isDeleted());
        }
    }
    
    
    /**
     * Tests creating a file
     */
    @Test
    public void removeInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        
        InvestigationManager.removeInvestigation(VALID_USER_FOR_INVESTIGATION, duplicateInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,duplicateInvestigation.getId() );
        
        assertNull("Investigation must not be found in DB "+duplicateInvestigation, modified);
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void addInvestigationInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        
        try {
            Investigation investigationInserted = (Investigation)InvestigationManager.addInvestigationObject(INVALID_USER, validInvestigation, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation invalidInvestigation = getInvestigation(false);
        
        try {
            Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, invalidInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void deleteInvestigationProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation propsInvestigation = getInvestigationDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsInvestigation, AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be modified'", ex.getMessage().contains("cannot be modified"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeInvestigationProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation propsInvestigation = getInvestigationDuplicate(false);
        log.trace(propsInvestigation);
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsInvestigation, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be modified'", ex.getMessage().contains("cannot be modified"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void getInvestigations() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for gettings investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        Collection<Long> investigations = new ArrayList<Long>();
        investigations.add(VALID_INVESTIGATION_ID);
        investigations.add(VALID_INVESTIGATION_ID);
        
        Collection<Investigation> investigationsFound = InvestigationManager.getInvestigations(VALID_USER_FOR_INVESTIGATION, investigations,em);
        
        for(Investigation investigation : investigationsFound){
            assertNotNull("investigation title cannot be null", investigation.getTitle());
            assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
            assertNotNull("investigation id cannot be null", investigation.getId());
            assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
            //    assertFalse("Deleted must be false", investigation.isDeleted());
            //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void getInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for gettings investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation investigation = InvestigationManager.getInvestigation(VALID_USER_FOR_INVESTIGATION, VALID_INVESTIGATION_ID,em);
        
        //checkInvestigation(investigation);
        assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
        // assertFalse("Deleted must be false", investigation.isDeleted());
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
    }
    
    
    /**
     * Tests deleting a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteInvestigationNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        try {
            InvestigationManager.deleteInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests remove a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeInvestigationNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        try {
            InvestigationManager.removeInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updateInvestigationNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for updating investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        try {
            InvestigationManager.updateInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    private Investigation getInvestigationDuplicate(boolean last){
        Investigation investigation = null;
        if(!last){
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.createId LIKE '%PROP%'");
            investigation = investigations.iterator().next();
        } else {
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
            investigation = investigations.iterator().next();
            if(investigation == null) throw new RuntimeException("No investigation found");
        }
        log.trace(investigation);
        return investigation;
    }
    
    private Investigation getInvestigation(boolean valid){
        if(valid){
            //create valid investigation
            Investigation investigation = new Investigation();
            
            investigation.setTitle("investigation "+random.nextInt());
            investigation.setInvNumber("11");
            investigation.setInvType(new InvestigationType("experiment"));
            
            return investigation;
        } else {
            //create invalid investigation
            Investigation investigation = new Investigation();
            return investigation;
        }
    }
    
    private void checkInvestigation(Investigation investigation){
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        assertEquals("Create id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, investigation.getCreateId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, investigation.getModId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigation.class);
    }
}
