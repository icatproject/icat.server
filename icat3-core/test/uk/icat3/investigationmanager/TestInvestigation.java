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
import java.util.Iterator;
import java.util.Random;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import uk.icat3.util.Queries;
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
        validInvestigation.setInvAbstract("Valid length");
        
        Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,investigationInserted.getId());
        
        checkInvestigation(modified);
        checkInvestigationPermissions(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertFalse("FA must be false", modified.isFacilityAcquiredSet());
        
        
        //check that icat authorisation is there
        for(Keyword keyword : modified.getKeywordCollection()){
            
            assertFalse("keyword must not be deleted", keyword.isDeleted());
            assertEquals("keyword must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, keyword.getModId());
        }
    }
    
    @Test
    public void modifyInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying investigation");
        
        String invAbstract = "Valid length modified";
        Investigation validInvestigation  = getInvestigation(true);
        Investigation duplicateInvestigation  = getInvestigationDuplicate(true);
        validInvestigation.setInvAbstract(invAbstract);
        validInvestigation.setVisitId(""+new Random().nextInt());
        validInvestigation.setFacility("ISIS");
        validInvestigation.setId(duplicateInvestigation.getId());
        
        Investigation investigationInserted = (Investigation)InvestigationManager.updateInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,investigationInserted.getId());
        
        assertEquals("Abstract must be "+invAbstract+" and not "+modified.getInvAbstract(), modified.getInvAbstract(), invAbstract);
        
        
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
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        em.clear(); //Just to seperate out the entity query connection
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
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
    public void undeleteInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigationDuplicate(true);
        
        InvestigationManager.deleteInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,validInvestigation.getId() );
        
        checkInvestigation(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        //check deep delete
        for(Investigator investigator : modified.getInvestigatorCollection()){
            assertTrue("investigator must be undeleted", !investigator.isDeleted());
        }
        
        deleteInvestigation();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void getDeletedInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for gettings deleted investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigationDuplicate(true);
        
        try {
            Investigation investigation = InvestigationManager.getInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation.getId(), em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    //TODO add deleted investigation here
    @Test(expected=ValidationException.class)
    public void addDeletedInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigationDuplicate(true);
        em.clear();
        try {
            Investigation investigation = InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
   // @Test(expected=InsufficientPrivilegesException.class)
    public void removeInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        
        //TODO remove
        duplicateInvestigation.setDeleted(false);
        
        try {
            InvestigationManager.removeInvestigation(VALID_USER_FOR_INVESTIGATION, duplicateInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
        
        // Investigation modified = em.find(Investigation.class,duplicateInvestigation.getId());
        
        //assertNull("Investigation must not be found in DB "+duplicateInvestigation, modified);
    }
    
    /**
     * Tests creating a file
     */
   @Test
    public void removeActualInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        //change so can delete
        duplicateInvestigation.setDeleted(false);
        duplicateInvestigation.setCreateId(ICAT_ADMIN_USER);
        
        Collection<Long> longs =  addAuthorisation(duplicateInvestigation.getId(), null, ICAT_ADMIN_USER, ElementType.INVESTIGATION, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        InvestigationManager.removeInvestigation(ICAT_ADMIN_USER, duplicateInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,duplicateInvestigation.getId());
        
        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
        IcatAuthorisation childIcatAuth = em.find(IcatAuthorisation.class,it.next());
        
        assertNull("Investigation must not be found in DB ", modified);
        assertNull("IcatAuthorisation[main] must not be found in DB ", icatAuth);
        assertNull("IcatAuthorisation[child] must not be found in DB ", childIcatAuth);
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addInvestigationInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        
        try {
            Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(INVALID_USER, validInvestigation, em);
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
   // @Test(expected=ValidationException.class)
    public void addValidInvestigationInvalidAbstract() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding valid investigation with too big abstract to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation invalidInvestigation = getInvestigation(true);
        
        StringBuilder builder  = new StringBuilder();
        for(int i = 0; i  < 4000; i ++ ){
            builder.append(i);
            if(builder.toString().length() > 4000) break;
        }
        invalidInvestigation.setInvAbstract(builder.toString());
        
        try {
            Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, invalidInvestigation, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be more than'", ex.getMessage().contains("cannot be more than"));
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
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
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
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
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
            assertNotNull("investigation create time cannot be null", investigation.getCreateTime());
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
        assertFalse("Deleted must be false", investigation.isDeleted());
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
    
    /**
     * Tests creating a file
     */
    @Test
    public void addInvestigationWithDatasetAndDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding investigation");
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setInvAbstract("Valid length");
        
        Dataset ds = new Dataset();
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        ds.setDatasetType(datasetType.iterator().next().getName());
        ds.setName("unit test create data set");
                
        Datafile file = new Datafile();
        Collection<DatafileFormat> datafileFormat = (Collection<DatafileFormat>)executeListResultCmd("select d from DatafileFormat d");
        file.setDatafileFormat(datafileFormat.iterator().next());
        file.setName("unit test create data set");
        
        ds.addDataFile(file);
        validInvestigation.addDataSet(ds);
        
        Investigation investigationInserted = (Investigation)InvestigationManager.createInvestigation(VALID_USER_FOR_INVESTIGATION, validInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,investigationInserted.getId());
        
        checkInvestigation(modified);
        checkInvestigationPermissions(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertFalse("FA must be false", modified.isFacilityAcquiredSet());
        
        //check that icat authorisation is there
        for(Dataset ds2 : modified.getDatasetCollection()){
            assertFalse("Dataset must not be deleted", ds2.isDeleted());
            assertEquals("Dataset must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, ds2.getModId());
        }
    }
    
    @Test
    public void removeActualInvestigationWithDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        //change so can delete
        duplicateInvestigation.setDeleted(false);
        duplicateInvestigation.setCreateId(ICAT_ADMIN_USER);
        
        Collection<Long> longs =  addAuthorisation(duplicateInvestigation.getId(), null, ICAT_ADMIN_USER, ElementType.INVESTIGATION, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        InvestigationManager.removeInvestigation(ICAT_ADMIN_USER, duplicateInvestigation, em);
        
        Investigation modified = em.find(Investigation.class,duplicateInvestigation.getId());
        
        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
        IcatAuthorisation childIcatAuth = em.find(IcatAuthorisation.class,it.next());
        
        assertNull("Investigation must not be found in DB ", modified);
        assertNull("IcatAuthorisation[main] must not be found in DB ", icatAuth);
        assertNull("IcatAuthorisation[child] must not be found in DB ", childIcatAuth);
    }
    
    private Investigation getInvestigationDuplicate(boolean last){
        Investigation investigation = null;
        if(!last){
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.facilityAcquired = 'Y'");
            investigation = investigations.iterator().next();
        } else {
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.facilityAcquired = 'N' order by d.modTime desc");
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
            int i = random.nextInt();
            investigation.setTitle("investigation "+ i);
            investigation.setInvNumber("" + i);
            investigation.setInvType("experiment");
            
            return investigation;
        } else {
            //create invalid investigation
            Investigation investigation = new Investigation();
            return investigation;
        }
    }
    
    /**
     * Checks the investigation found by the search has authroisation to read this investigation
     */
    private static void checkInvestigationPermissions(Investigation investigation) throws ICATAPIException {
        IcatAuthorisation icatAuthorisation = null;
        
        boolean found = false;
        
        Query query = em.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_INVESTIGATION);
        query.setParameter("elementType", ElementType.INVESTIGATION).
                setParameter("elementId", investigation.getId()).               
                setParameter("userId", VALID_USER_FOR_INVESTIGATION);
        
        try{
            icatAuthorisation = (IcatAuthorisation)query.getSingleResult();
            log.trace("Found stage 1 (normal): "+icatAuthorisation);
            assertTrue("Found investigation level authorisation", true);
            
            assertTrue("icatAuthorisation must have elementType INVESTIGATION", icatAuthorisation.getElementType() == ElementType.INVESTIGATION);
            assertFalse("Deleted must be false", icatAuthorisation.isDeleted());
            assertFalse("FA must be false", icatAuthorisation.isFacilityAcquiredSet());
            assertTrue("parent must have elementType null", icatAuthorisation.getParentElementType() == null);
            assertTrue("parent id must be null", icatAuthorisation.getParentElementId() == null);
            assertTrue("id must be "+investigation.getId(), icatAuthorisation.getElementId() == investigation.getId());
            assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, icatAuthorisation.getCreateId(), VALID_USER_FOR_INVESTIGATION);
            assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, icatAuthorisation.getModId(), VALID_USER_FOR_INVESTIGATION);
            
            //now find child
            IcatAuthorisation child = ManagerUtil.find(IcatAuthorisation.class, icatAuthorisation.getUserChildRecord(), em);
            
            assertTrue("child must have elementTyep DATASET", child.getElementType() == ElementType.DATASET);
            assertFalse("Deleted must be false", child.isDeleted());
            assertFalse("FA must be false", child.isFacilityAcquiredSet());
            assertTrue("parent must have elementType INVESTIGATION", child.getParentElementType() == ElementType.INVESTIGATION);
            assertTrue("parent id must be "+investigation.getId(), child.getParentElementId() == investigation.getId());
            assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, child.getCreateId(), VALID_USER_FOR_INVESTIGATION);
            assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, child.getModId(), VALID_USER_FOR_INVESTIGATION);
            
        } catch(NoResultException nre){
            //try find ANY
            log.trace("Could not find child level authorisation");
            assertTrue("Could not find child level authorisation: "+icatAuthorisation.getUserChildRecord(), false);
        }
    }
    
    private void checkInvestigation(Investigation investigation){
        assertNotNull("createTime must be not null", investigation.getCreateTime());
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, investigation.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, investigation.getModId(), VALID_USER_FOR_INVESTIGATION);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigation.class);
    }
}
