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
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
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
public class TestSampleParameter extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestSampleParameter.class);
    private Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameter(true, true);
        
        SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter, VALID_INVESTIGATION_ID, em);
        
        SampleParameter modified = em.find(SampleParameter.class,sampleParameterInserted.getSampleParameterPK()  );
        
        checkSampleParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertNotNull("Must be numeric value", modified.getNumericValue());
        assertNull("String value must be null", modified.getStringValue());
    }
        
    /**
     * Tests creating a file
     */
    @Test
    public void modifySampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedError = "unit test error "+random.nextInt();
        //create invalid sampleParameter, no name
        
        SampleParameter modifiedSampleParameter = getSampleParameter(true, true);
        SampleParameter duplicateSampleParameter = getSampleParameterDuplicate(true);
        modifiedSampleParameter.setError(modifiedError);
        //set investigation id
        Sample sample = em.find(Sample.class, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
        modifiedSampleParameter.setSample(sample);
        modifiedSampleParameter.setSampleParameterPK(duplicateSampleParameter.getSampleParameterPK());
        
        InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, modifiedSampleParameter, em);
        
        SampleParameter modified = em.find(SampleParameter.class, duplicateSampleParameter.getSampleParameterPK()  );
        
        assertEquals("error must be "+modifiedError+" and not "+modified.getError(), modified.getError(), modifiedError);
        
        checkSampleParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter duplicateSampleParameter = getSampleParameterDuplicate(true);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSampleParameter, VALID_INVESTIGATION_ID, em);
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
    public void deleteSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameterDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter,  AccessType.DELETE, em);
        
        SampleParameter modified = em.find(SampleParameter.class,validSampleParameter.getSampleParameterPK());
        
        checkSampleParameter(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void undeleteSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeelting sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameterDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter,  AccessType.DELETE, em);
        
        SampleParameter modified = em.find(SampleParameter.class,validSampleParameter.getSampleParameterPK());
        
        checkSampleParameter(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        deleteSampleParameter();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter duplicateSampleParameter = getSampleParameterDuplicate(true);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSampleParameter, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
        
       /* SampleParameter modified = em.find(SampleParameter.class,sampleParameterInserted.getSampleParameterPK()  );
        
        checkSampleParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());*/
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter duplicateSampleParameter = getSampleParameterDuplicate(true);
        duplicateSampleParameter.setDeleted(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicateSampleParameter, AccessType.REMOVE, em);
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
    public void removeActualSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter duplicateSampleParameter = getSampleParameterDuplicate(true);
        duplicateSampleParameter.setDeleted(false);
        duplicateSampleParameter.setCreateId(ICAT_ADMIN_USER);
        
        
        InvestigationManager.deleteInvestigationObject(ICAT_ADMIN_USER, duplicateSampleParameter, AccessType.REMOVE, em);
        
        SampleParameter modified = em.find(SampleParameter.class,duplicateSampleParameter.getSampleParameterPK()  );
        
        assertNull("SampleParameter must not be found in DB "+duplicateSampleParameter, modified);
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void addSampleParameterNew() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding new sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameterPK PK = new SampleParameterPK("silly sample units", "silly sample name", VALID_INVESTIGATION_ID);
        SampleParameter validSampleParameter = new SampleParameter(PK);
        validSampleParameter.setNumericValue(3d);
        
        SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter, VALID_INVESTIGATION_ID, em);
        
        SampleParameter modified = em.find(SampleParameter.class,sampleParameterInserted.getSampleParameterPK()  );
        
        checkSampleParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertNotNull("Must be numeric value", modified.getNumericValue());
        assertNull("String value must be null", modified.getStringValue());
        
        removeActualSampleParameter();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addSampleParameterInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameter(true, true);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(INVALID_USER, validSampleParameter, VALID_INVESTIGATION_ID, em);
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
    public void addSampleParameterInvalidInvestigation() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sampleParameter to invalid investigation Id");
        
        SampleParameter validSampleParameter  = getSampleParameter(true,true);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter, random.nextLong(), em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void addSampleParameterInvalidSampleId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sampleParameter to invalid investigation Id");
        
        SampleParameter validSampleParameter  = getSampleParameter(true,true);
        validSampleParameter.getSampleParameterPK().setSampleId(456787L);
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter, VALID_INVESTIGATION_ID, em);
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
    public void addInvalidSampleParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter invalidSampleParameter = getSampleParameter(false, true);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidSampleParameter, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidSampleParameter2() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter invalidSampleParameter = getSampleParameter(true, false);
        //string value only allowed
        invalidSampleParameter.setNumericValue(45d);
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidSampleParameter, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'string value only'", ex.getMessage().contains("string value only"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidSampleParameter3() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter invalidSampleParameter = getSampleParameter(true, true);
        //string value only allowed
        invalidSampleParameter.setStringValue("45d");
        
        try {
            SampleParameter sampleParameterInserted = (SampleParameter)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidSampleParameter, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'numeric value only'", ex.getMessage().contains("numeric value only"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteSampleParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameter(true, true);
        validSampleParameter.setSampleParameterPK(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter,  AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeSampleParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameter(true, true);
        validSampleParameter.setSampleParameterPK(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter,  AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a file, no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updateSampleParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        SampleParameter validSampleParameter  = getSampleParameter(true, true);
        validSampleParameter.setSampleParameterPK(null);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, validSampleParameter,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
   // @Test(expected=InsufficientPrivilegesException.class)
    public void modifySampleParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a props sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter propsSampleParameter = getSampleParameterDuplicate(false);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSampleParameter, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void deleteSampleParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter propsSampleParameter = getSampleParameterDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSampleParameter, AccessType.DELETE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void removeSampleParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props sampleParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid sampleParameter, no name
        SampleParameter propsSampleParameter = getSampleParameterDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsSampleParameter, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    @Test
    public void removeParameter(){
        Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.facilityAcquired = 'N' order by d.modTime desc");
        for(Parameter  parameter : parameters){
            if(parameter.getCreateId().equals("SAMPLE_PARAMETER_ADDED")){
                log.info("Removing added parameter: "+parameter );
                em.remove(parameter);
            }
        }
    }
    
    static Parameter getParameter(boolean numeric){
        Parameter parameter = null;
        if(numeric){
            Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.numericValue = 'Y' AND d.isSampleParameter = 'Y'");
            if(parameters.size() == 0){
                log.trace("Adding new parameter");
                Parameter param = new Parameter("units","name");
                param.setIsSampleParameter("Y");
                param.setNumericValue("Y");
                param.setSearchable("Y");
                param.setIsDatasetParameter("N");
                param.setIsDatafileParameter("N");
                param.setCreateId("SAMPLE_PARAMETER_ADDED");
                em.persist(param);
                parameter =  param;
            } else {
                parameter = parameters.iterator().next();
            }
        } else {
            Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.numericValue = 'N' AND d.isSampleParameter = 'Y'");
            if(parameters.size() == 0){
                log.trace("Adding new parameter");
                Parameter param = new Parameter("units string","name");
                param.setIsSampleParameter("Y");
                param.setIsDatasetParameter("N");
                param.setIsDatafileParameter("N");
                param.setNumericValue("N");
                param.setSearchable("Y");
                param.setCreateId("SAMPLE_PARAMETER_ADDED");
                em.persist(param);
                parameter =  param;
            } else {
                parameter = parameters.iterator().next();
            }
        }
        log.trace(parameter);
        return parameter;
    }
    
    static SampleParameter getSampleParameterDuplicate(boolean last){
        SampleParameter sampleParameter = null;
        if(!last){
            Collection<SampleParameter> sampleParameters = (Collection<SampleParameter>)executeListResultCmd("select d from SampleParameter d where d.facilityAcquired = 'Y' AND d.markedDeleted = 'N'");
            sampleParameter = sampleParameters.iterator().next();
        } else {
            Collection<SampleParameter> sampleParameters = (Collection<SampleParameter>)executeListResultCmd("select d from SampleParameter d where d.facilityAcquired = 'N' order by d.modTime desc");
            sampleParameter = sampleParameters.iterator().next();
        }
        log.trace(sampleParameter);
        return sampleParameter;
    }
    
    static SampleParameter getSampleParameter(boolean valid, boolean numeric){
        Parameter parameter = getParameter(numeric);
        if(valid){
            //create valid sampleParameter
            SampleParameter sampleParameter = new SampleParameter(parameter.getParameterPK().getUnits(),parameter.getParameterPK().getName(), VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            if(numeric){
                sampleParameter.setNumericValue(45d);
            } else {
                sampleParameter.setStringValue("string value");
            }
            sampleParameter.getSampleParameterPK().setSampleId(VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            return sampleParameter;
        } else {
            //create invalid sampleParameter
            //(parameter.getParameterPK().getUnits(),parameter.getParameterPK().getName(), VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            SampleParameter sampleParameter = new SampleParameter();
            return sampleParameter;
        }
    }
    
    private void checkSampleParameter(SampleParameter sampleParameter){
        assertNotNull("PK cannot be null", sampleParameter.getSampleParameterPK());
        assertNotNull("PK units cannot be null", sampleParameter.getSampleParameterPK().getUnits());
        assertNotNull("PK name cannot be null", sampleParameter.getSampleParameterPK().getName());
        
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, sampleParameter.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, sampleParameter.getModId(), VALID_USER_FOR_INVESTIGATION);
        //assertEquals("Investigation id must be "+VALID_INVESTIGATION_ID, sampleParameter.getSampleParameterPK().getSampleId(), VALID_INVESTIGATION_ID);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestSampleParameter.class);
    }
}
