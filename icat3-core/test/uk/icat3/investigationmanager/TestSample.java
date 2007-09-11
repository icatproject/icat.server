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
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
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
public class TestSample extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestSample.class);
    private static Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSample(true);
        
        Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample, VALID_INVESTIGATION_ID, em);
        
        Sample modified = em.find(Sample.class,sampleInserted.getId() );
        
        checkSample(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void modifySample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedName = "Modified Name "+random.nextInt();
        //create invalid sample, no name
        Sample modifiedSample = getSample(true);
        Sample duplicateSample = getSampleDuplicate(true);
        
        modifiedSample.setSafetyInformation(modifiedName);
        //set investigation id
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        modifiedSample.setInvestigationId(in);
        modifiedSample.setId(duplicateSample.getId());
        
        
        InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, modifiedSample, em);
        
        Sample modified = em.find(Sample.class,duplicateSample.getId() );
        
        assertEquals("SafetyInformation must be "+modifiedName+" and not "+modified.getSafetyInformation(), modified.getSafetyInformation(), modifiedName);
        
        checkSample(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample duplicateSample = getSampleDuplicate(true);
        
        try {
            Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSample, VALID_INVESTIGATION_ID, em);
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
    public void deleteSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSampleDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample,  AccessType.DELETE, em);
        
        Sample modified = em.find(Sample.class,validSample.getId() );
        
        checkSample(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void undeleteSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSampleDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample,  AccessType.DELETE, em);
        
        Sample modified = em.find(Sample.class,validSample.getId() );
        
        checkSample(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        deleteSample();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample duplicateSample = getSampleDuplicate(true);
        try{
            Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSample, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            throw ex;
        }
       /* Sample modified = em.find(Sample.class,sampleInserted.getId() );
        
        checkSample(modified);
        assertFalse("Deleted must be false", modified.isDeleted());*/
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample duplicateSample = getSampleDuplicate(true);
        duplicateSample.setDeleted(false);
        
        try{
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSample, AccessType.REMOVE, em);
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
    public void removeActualSample() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for removing actual sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample duplicateSample = getSampleDuplicate(true);
        duplicateSample.setDeleted(false);
        duplicateSample.setCreateId(ICAT_ADMIN_USER);
        
        InvestigationManager.deleteInvestigationObject(ICAT_ADMIN_USER, duplicateSample, AccessType.REMOVE, em);
        
        Sample modified = em.find(Sample.class,duplicateSample.getId() );
        
        assertNull("Sample must not be found in DB "+duplicateSample, modified);
    }
    
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeLinkedSample() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for removing linked sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample duplicateSample = em.find(Sample.class, 3L);
        duplicateSample.setDeleted(false);
        duplicateSample.setCreateId(ICAT_ADMIN_USER);
        
        try{
            InvestigationManager.deleteInvestigationObject(ICAT_ADMIN_USER, duplicateSample, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'linked'", ex.getMessage().contains("linked"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addSampleInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSample(true);
        
        try {
            Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(INVALID_USER, validSample, VALID_INVESTIGATION_ID, em);
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
    public void addSampleInvalidInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sample to invalid investigation Id");
        
        Sample validSample  = getSample(true);
        
        try {
            Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample, random.nextLong(), em);
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
    public void addInvalidSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample invalidSample = getSample(false);
        
        try {
            Sample sampleInserted = (Sample)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidSample, VALID_INVESTIGATION_ID, em);
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
    public void modifySampleProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a props sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample propsSample = getSampleDuplicate(false);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSample, em);
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
    public void deleteSampleProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample propsSample = getSampleDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSample, AccessType.DELETE, em);
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
    public void removeSampleProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sample, no name
        Sample propsSample = getSampleDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSample, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteSampleNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSample(true);
        validSample.setId(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample,  AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests removing a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeSampleNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSample(true);
        validSample.setId(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample,  AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests updating a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updatingSampleNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing sample to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Sample validSample  = getSample(true);
        validSample.setId(null);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSample, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    static Sample getSampleDuplicate(boolean last){
        Sample sample = null;
        if(!last){
            Collection<Sample> samples = (Collection<Sample>)executeListResultCmd("select d from Sample d where d.facilityAcquired = 'Y' AND d.markedDeleted = 'N'");
            if(samples.isEmpty()) throw new RuntimeException("No props samples found in DB");
            sample = samples.iterator().next();
        } else {
            Collection<Sample> samples = (Collection<Sample>)executeListResultCmd("select d from Sample d where d.facilityAcquired = 'N' order by d.modTime desc");
            if(samples.isEmpty()) throw new RuntimeException("No none props samples found in DB");
            sample = samples.iterator().next();
        }
        log.trace(sample);
        return sample;
    }
    
    static Sample getSample(boolean valid){
        if(valid){
            //create valid sample
            Sample sample = new Sample();
            sample.setInstance("valid junit test instance "+random.nextInt());
            sample.setName("valid name "+random.nextInt());
            sample.setSafetyInformation("valid safety "+random.nextInt());
            return sample;
        } else {
            //create invalid sample
            Sample sample = new Sample();
            sample.setInstance("invalid junit test instance "+random.nextInt());
            return sample;
        }
    }
    
    private void checkSample(Sample sample){
        assertNotNull("Name cannot be null", sample.getName());
        assertNotNull("Safety cannot be null", sample.getSafetyInformation());
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, sample.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, sample.getModId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Investigation id must be "+VALID_INVESTIGATION_ID, sample.getInvestigationId().getId(), VALID_INVESTIGATION_ID);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestSample.class);
    }
}
