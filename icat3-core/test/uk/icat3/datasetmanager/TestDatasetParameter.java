/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.datasetmanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatasetParameter extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatasetParameter.class);
    private Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        DatasetParameter validDatasetParameter  = getDatasetParameter(true, true);
        
        DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        DatasetParameter modified = em.find(DatasetParameter.class,datasetParameterInserted.getDatasetParameterPK() );
        
        checkDatasetParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertNotNull("Must be numeric value", modified.getNumericValue());
        assertNull("String value must be null", modified.getStringValue());
    }
            
    /**
     * Tests creating a file
     */
    @Test
    public void modifyDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedError = "unit test error "+random.nextInt();
        //create invalid datasetParameter, no name
        DatasetParameter modifiedDatasetParameter = getDatasetParameter(true, true);
        DatasetParameter duplicateDatasetParameter = getDatasetParameterDuplicate(true);
        Dataset ds  =em.find(Dataset.class, VALID_DATA_SET_ID);
        modifiedDatasetParameter.setError(modifiedError);
        modifiedDatasetParameter.setDataset(ds);
        modifiedDatasetParameter.setDatasetParameterPK(duplicateDatasetParameter.getDatasetParameterPK());
        
        DataSetManager.updateDataSetParameter(VALID_USER_FOR_INVESTIGATION, modifiedDatasetParameter, em);
        
        DatasetParameter modified = em.find(DatasetParameter.class, duplicateDatasetParameter.getDatasetParameterPK() );
        
        assertEquals("error must be "+modifiedError+" and not "+modified.getError(), modified.getError(), modifiedError);
        
        checkDatasetParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter duplicateDatasetParameter = getDatasetParameterDuplicate(true);
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, duplicateDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
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
    public void deleteDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        DatasetParameter validDatasetParameter  = getDatasetParameterDuplicate(true);
        
        DataSetManager.deleteDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter,  em);
        
        DatasetParameter modified = em.find(DatasetParameter.class,validDatasetParameter.getDatasetParameterPK());
        
        checkDatasetParameter(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void undeleteDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        DatasetParameter validDatasetParameter  = getDatasetParameterDuplicate(true);
        
        DataSetManager.deleteDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter,  em);
        
        DatasetParameter modified = em.find(DatasetParameter.class,validDatasetParameter.getDatasetParameterPK());
        
        checkDatasetParameter(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        deleteDatasetParameter();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter duplicateDatasetParameter = getDatasetParameterDuplicate(true);
        try{
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, duplicateDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
       /* DatasetParameter modified = em.find(DatasetParameter.class,datasetParameterInserted.getDatasetParameterPK() );
        
        checkDatasetParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());*/
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter duplicateDatasetParameter = getDatasetParameterDuplicate(true);
        
        //TODO remove
        duplicateDatasetParameter.setDeleted(false);
        
        try {
            DataSetManager.removeDataSetParameter(VALID_USER_FOR_INVESTIGATION, duplicateDatasetParameter, em);
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
    public void removeActualDatasetParameter() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter duplicateDatasetParameter = getDatasetParameterDuplicate(true);
        
        //TODO remove
        duplicateDatasetParameter.setDeleted(false);
        duplicateDatasetParameter.setCreateId(ICAT_ADMIN_USER);
        
       /* Collection<Long> longs =  addAuthorisation(duplicateDatasetParameter.getDataset().getId(), duplicateDatasetParameter.getDataset().getInvestigation().getId(), ICAT_ADMIN_USER, ElementType.DATASET, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();*/
        
        DataSetManager.removeDataSetParameter(ICAT_ADMIN_USER, duplicateDatasetParameter, em);
        
        DatasetParameter modified = em.find(DatasetParameter.class,duplicateDatasetParameter.getDatasetParameterPK() );
        assertNull("DatasetParameter must not be found in DB "+duplicateDatasetParameter, modified);
        
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void addDatasetParameterNew() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding new datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        DatasetParameterPK PK = new DatasetParameterPK("silly file units", "silly file name", VALID_DATASET_ID_FOR_INVESTIGATION);
        DatasetParameter validDatasetParameter = new DatasetParameter(PK);
        validDatasetParameter.setNumericValue(3d);
        
        DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        DatasetParameter modified = em.find(DatasetParameter.class,datasetParameterInserted.getDatasetParameterPK() );
        
        checkDatasetParameter(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertNotNull("Must be numeric value", modified.getNumericValue());
        assertNull("String value must be null", modified.getStringValue());
        
        removeActualDatasetParameter();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addDatasetParameterInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        DatasetParameter validDatasetParameter  = getDatasetParameter(true, true);
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(INVALID_USER, validDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
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
    public void addDatasetParameterInvalidDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding datasetParameter to invalid investigation Id");
        
        DatasetParameter validDatasetParameter  = getDatasetParameter(true,true);
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter, random.nextLong(), em);
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
    public void addDatasetParameterInvalidDatasetId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding datasetParameter to invalid investigation Id");
        
        DatasetParameter validDatasetParameter  = getDatasetParameter(true,true);
        validDatasetParameter.getDatasetParameterPK().setDatasetId(456787L);
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, validDatasetParameter, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not correspond'", ex.getMessage().contains("does not correspond"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidDatasetParameter() throws ICATAPIException, Exception {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter invalidDatasetParameter = getDatasetParameter(false, true);
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, invalidDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        }catch (Exception ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidDatasetParameter2() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter invalidDatasetParameter = getDatasetParameter(true, false);
        //string value only allowed
        invalidDatasetParameter.setNumericValue(45d);
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, invalidDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
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
    public void addInvalidDatasetParameter3() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter invalidDatasetParameter = getDatasetParameter(true, true);
        //string value only allowed
        invalidDatasetParameter.setStringValue("45d");
        
        try {
            DatasetParameter datasetParameterInserted = (DatasetParameter)DataSetManager.addDataSetParameter(VALID_USER_FOR_INVESTIGATION, invalidDatasetParameter, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'numeric value only'", ex.getMessage().contains("numeric value only"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void modifyDatasetParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a props datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter propsDatasetParameter = getDatasetParameterDuplicate(false);
        
        try {
            DataSetManager.updateDataSetParameter(VALID_USER_FOR_INVESTIGATION, propsDatasetParameter, em);
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
    public void deleteDatasetParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter propsDatasetParameter = getDatasetParameterDuplicate(false);
        
        try {
            DataSetManager.deleteDataSetParameter(VALID_USER_FOR_INVESTIGATION, propsDatasetParameter, em);
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
    public void removeDatasetParameterProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetParameter, no name
        DatasetParameter propsDatasetParameter = getDatasetParameterDuplicate(false);
        
        try {
            DataSetManager.removeDataSetParameter(VALID_USER_FOR_INVESTIGATION, propsDatasetParameter, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests a removing , no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeDatafileParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a datafileParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetarameter, no name
        DatasetParameter datasetParameter = getDatasetParameter(true, true);
        datasetParameter.setDatasetParameterPK(null);
        
        try {
            DataSetManager.removeDataSetParameter(VALID_USER_FOR_INVESTIGATION, datasetParameter, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests delete a a dataset , no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteDatafileParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a datasetParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetarameter, no name
        DatasetParameter datasetParameter = getDatasetParameter(true, true);
        datasetParameter.setDatasetParameterPK(null);
        
        try {
            DataSetManager.deleteDataSetParameter(VALID_USER_FOR_INVESTIGATION, datasetParameter, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a dataset , no Id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updateDatasetParameterNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a datafileParameter to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid datasetarameter, no name
        DatasetParameter datasetParameter = getDatasetParameter(true, true);
        datasetParameter.setDatasetParameterPK(null);
        
        try {
            DataSetManager.updateDataSetParameter(VALID_USER_FOR_INVESTIGATION, datasetParameter, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    @Test
    public void removeParameter(){
        log.info("Removing parameters");
        Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
        log.info("Found: "+parameters.size());
        for(Parameter  parameter : parameters){
            if(parameter.getCreateId().equals("DATASET_PARAMETER_ADDED")){
                log.info("Removing added parameter: "+parameter );
                em.remove(parameter);
            }
        }
    }
    
    @Test //TODO the last method would not execute in TestingAll so added this dummy, seems to work
    public void dummy(){
        Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
        for(Parameter  parameter : parameters){
            if(parameter.getCreateId().equals("DATASET_PARAMETER_ADDED")){
                log.info("Removing added parameter: "+parameter );
                em.remove(parameter);
            }
        }
    }
    
    
    
    
    
    static Parameter getParameter(boolean numeric){
        Parameter parameter = null;
        if(numeric){
            Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.numericValue = 'Y' AND d.isDatasetParameter = 'Y'");
            if(parameters.size() == 0){
                log.trace("Adding new parameter");
                Parameter param = new Parameter("units","name");
                param.setIsSampleParameter("N");
                param.setNumericValue("Y");
                param.setSearchable("Y");
                param.setIsDatasetParameter("Y");
                param.setIsDatafileParameter("N");
                param.setCreateId("DATASET_PARAMETER_ADDED");
                param.setVerified(true);
                em.persist(param);
                parameter =  param;
            } else {
                parameter = parameters.iterator().next();
            }
        } else {
            Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.numericValue = 'N' AND d.isDatasetParameter = 'Y'");
            if(parameters.size() == 0){
                log.trace("Adding new parameter");
                Parameter param = new Parameter("units string","name");
                param.setIsSampleParameter("N");
                param.setIsDatasetParameter("Y");
                param.setIsDatafileParameter("N");
                param.setNumericValue("N");
                param.setSearchable("Y");
                param.setCreateId("DATASET_PARAMETER_ADDED");
                em.persist(param);
                parameter =  param;
            } else {
                parameter = parameters.iterator().next();
            }
        }
        log.trace(parameter);
        return parameter;
    }
    
    static DatasetParameter getDatasetParameterDuplicate(boolean last){
        DatasetParameter datasetParameter = null;
        if(!last){
            Collection<DatasetParameter> datasetParameters = (Collection<DatasetParameter>)executeListResultCmd("select d from DatasetParameter d where d.facilityAcquired = 'Y'");
            datasetParameter = datasetParameters.iterator().next();
        } else {
            Collection<DatasetParameter> datasetParameters = (Collection<DatasetParameter>)executeListResultCmd("select d from DatasetParameter d where d.facilityAcquired = 'N' order by d.modTime desc");
            datasetParameter = datasetParameters.iterator().next();
        }
        log.trace(datasetParameter);
        return datasetParameter;
    }
    
    static DatasetParameter getDatasetParameter(boolean valid, boolean numeric){
        Parameter parameter = getParameter(numeric);
        if(valid){
            //create valid datasetParameter
            DatasetParameter datasetParameter = new DatasetParameter(parameter.getParameterPK().getUnits(),parameter.getParameterPK().getName(), VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            if(numeric){
                datasetParameter.setNumericValue(45d);
            } else {
                datasetParameter.setStringValue("string value");
            }
            datasetParameter.getDatasetParameterPK().setDatasetId(VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            return datasetParameter;
        } else {
            //create invalid datasetParameter
            //(parameter.getParameterPK().getUnits(),parameter.getParameterPK().getName(), VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
            DatasetParameter datasetParameter = new DatasetParameter();
            return datasetParameter;
        }
    }
    
    private void checkDatasetParameter(DatasetParameter datasetParameter){
        assertNotNull("PK cannot be null", datasetParameter.getDatasetParameterPK());
        assertNotNull("PK units cannot be null", datasetParameter.getDatasetParameterPK().getUnits());
        assertNotNull("PK name cannot be null", datasetParameter.getDatasetParameterPK().getName());
        
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, datasetParameter.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, datasetParameter.getModId(), VALID_USER_FOR_INVESTIGATION);
        //assertEquals("Dataset id must be "+VALID_INVESTIGATION_ID, datasetParameter.getDatasetParameterPK().getDatasetId(), VALID_INVESTIGATION_ID);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatasetParameter.class);
    }
}
