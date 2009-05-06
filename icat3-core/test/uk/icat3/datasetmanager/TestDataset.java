/*
 * TestDatasetManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.datasetmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDataset extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDataset.class);
    private static Random random = new Random();
    
    
    /**
     * Tests creating a file
     */
    @Test
    public void createDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        Dataset dataset = getDataset(true);
        
        Dataset datasetInserted = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, dataset, VALID_INVESTIGATION_ID, em);
        
        checkDataset(datasetInserted);
        assertFalse("Deleted must be false", datasetInserted.isDeleted());
    }
    
    //@Test
    public void modifyDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        String modifiedDesc = "Modfied Desc "+random.nextInt();
        
        Dataset modifiedDataset = getDataset(true);
        Dataset duplicateDataset = getDatasetDuplicate(true);
        
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        modifiedDataset.setInvestigation(in);
        modifiedDataset.setDescription(modifiedDesc);
        modifiedDataset.setId(duplicateDataset.getId());
        
        Dataset datasetInserted = DataSetManager.updateDataSet(VALID_USER_FOR_INVESTIGATION, modifiedDataset, em);
        assertEquals("Desc must be "+modifiedDesc+" and not "+datasetInserted.getDescription(), datasetInserted.getDescription(), modifiedDesc);
        
        
        checkDataset(datasetInserted);
        assertFalse("Deleted must be false", datasetInserted.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void setDatasetInvalidSampleId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for setting invalid sample id to dataset Id: ");
        
        //create invalid dataset, no name
        Dataset dataset = getDatasetDuplicate(true);
        assertNull("sample must be null", dataset.getSampleId());
        
        try {
            DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, 7684384L, dataset.getId(), em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void setDatasetSample() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for setting sample to dataset Id: ");
        
        //create invalid dataset, no name
        Dataset dataset = getDatasetDuplicate(true);
        assertNull("sample must be null", dataset.getSampleId());
        
        DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, dataset.getId(), em);
        
        Dataset modified = em.find(Dataset.class,dataset.getId() );
        
        checkDataset(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertEquals("Sample id is "+VALID_SAMPLE_ID_FOR_INVESTIGATION_ID,VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, modified.getSampleId());
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=ValidationException.class)
    public void addDuplicateDataset() throws ICATAPIException, Exception {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset duplicateDataset = getDataset(true);
        duplicateDataset.setSampleId(VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
        try {
            Dataset datasetInserted = (Dataset)DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, duplicateDataset, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void deleteDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDatasetDuplicate(true);
        
        DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, validDataset, em);
        
        Dataset modified = em.find(Dataset.class,validDataset.getId() );
        
        checkDataset(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        for(Datafile file : modified.getDatafileCollection()){
            assertTrue("investigator must be deleted", file.isDeleted());
            for(DatafileParameter datafileParameter : file.getDatafileParameterCollection()){
                assertTrue("datafileParameter must be deleted", datafileParameter.isDeleted());
            }
        }
    }
    
     /**
     * Tests creating a file
     */
    //@Test
    public void undeleteDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDatasetDuplicate(true);
        
        DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, validDataset, em);
        
        Dataset modified = em.find(Dataset.class,validDataset.getId() );
        
        checkDataset(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        //check deep delete
        for(Datafile file : modified.getDatafileCollection()){
            assertTrue("investigator must be undeleted", !file.isDeleted());
            for(DatafileParameter datafileParameter : file.getDatafileParameterCollection()){
                assertTrue("datafileParameter must be undeleted", !datafileParameter.isDeleted());
            }
        }
        
        deleteDataset();
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void getDeletedDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a deleted dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDatasetDuplicate(true);
        
        try {
            Dataset datasetGot = DataSetManager.getDataSet(VALID_USER_FOR_INVESTIGATION, validDataset.getId(),  em);
            
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=ValidationException.class)
    public void createDeletedDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDatasetDuplicate(true);
        validDataset.setId(null);
        try{
            Dataset datasetInserted = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, validDataset, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void removeDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset duplicateDataset = getDatasetDuplicate(true);
        
        //TODO remove this
        duplicateDataset.setDeleted(false);
        
        try{
            DataSetManager.removeDataSet(VALID_USER_FOR_INVESTIGATION, duplicateDataset, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void removeActualDataset() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset duplicateDataset = getDatasetDuplicate(true);
        
        //TODO remove this
        duplicateDataset.setDeleted(false);
        duplicateDataset.setCreateId(ICAT_ADMIN_USER);
        
        Collection<Long> longs =  addAuthorisation(duplicateDataset.getId(),duplicateDataset.getInvestigation().getId(), ICAT_ADMIN_USER, ElementType.DATASET, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        
        DataSetManager.removeDataSet(ICAT_ADMIN_USER, duplicateDataset, em);
        
        Dataset modified = em.find(Dataset.class,duplicateDataset.getId() );
        assertNull("Dataset must not be found in DB "+duplicateDataset, modified);
        
        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
        IcatAuthorisation childIcatAuth = em.find(IcatAuthorisation.class,it.next());
        it = longs.iterator();
        assertNull("IcatAuthorisation["+it.next()+"] must not be found in DB ", icatAuth);
        assertNull("IcatAuthorisation["+it.next()+"] must not be found in DB ", childIcatAuth);
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void addDatasetInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDataset(true);
        
        try {
            Dataset datasetInserted = (Dataset)DataSetManager.createDataSet(INVALID_USER, validDataset, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=ValidationException.class)
    public void addInvalidDataset() throws ICATAPIException, Exception {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset invalidDataset = getDataset(false);
        
        try {
            Dataset datasetInserted = (Dataset)DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, invalidDataset, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        } catch (Exception ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void deleteDatasetProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset propsDataset = getDatasetDuplicate(false);
        
        try {
            DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, propsDataset, em);
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
    public void removeDatasetProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset propsDataset = getDatasetDuplicate(false);
        log.trace(propsDataset);
        try {
            DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, propsDataset, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void setDatasetSampleProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for setting sample to dataset Id: "+VALID_DATA_SET_ID);
        
        DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, VALID_DATA_SET_ID, em);
        
        assertTrue("No exception", true);
    }
    
    //@Test
    public void createWholeValidDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a whole data set for dataset id: "+VALID_INVESTIGATION_ID);
        
        //create valid set
        Dataset set = new Dataset();
        Random ram = new Random();
        set.setName("complete dataset:  "+ram.nextLong());
        
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        set.setDatasetType(datasetType.iterator().next().getName());
        
        //add status
        Collection<DatasetStatus> datasetStstus = (Collection<DatasetStatus>)executeListResultCmd("select d from DatasetStatus d");
        set.setDatasetStatus(datasetStstus.iterator().next().getName());
        
        //add a file
        Datafile file = new Datafile();
        file.setName("whole file: "+ram.nextLong());
        
        set.addDataFile(file);
        
        Dataset dataset = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, set, VALID_INVESTIGATION_ID, em);
        log.info("Dataset: "+dataset);
        
        Dataset modified = em.find(Dataset.class, dataset.getId());
        
        checkDataset(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        
        for(Datafile datafile : modified.getDatafileCollection()){
            assertFalse("Deleted must be false", datafile.isDeleted());
            assertNotNull("createId must be not null", datafile.getCreateId());
            assertEquals("createId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, datafile.getCreateId());
            
            assertNotNull("modId must be not null", datafile.getModId());
            assertEquals("modId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, datafile.getModId());
            
            assertNotNull("dataset id must be not null", datafile.getId());
            assertEquals("dataset must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, modified.getInvestigation().getId());
        }
    }
    
    //@Test
    public void removeActualDataset2() throws ICATAPIException{
        removeActualDataset();
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void testAddValidDatafiles() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for add a files for datset Id: "+VALID_INVESTIGATION_ID);
        
        
        //create valid file
        Dataset file1 = new Dataset();
        Dataset file2 = new Dataset();
        Random ram = new Random();
        file1.setName("unit test create data set not allowed "+ram.nextLong());
        file2.setName("unit test create data set not allowed "+ram.nextLong());
        
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        
        DatasetType added =  datasetType.iterator().next();
        file1.setDatasetType(added.getName());
        file2.setDatasetType(added.getName());
        
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        datasets.add(file1);
        datasets.add(file2);
        
        Collection<Dataset> datasetsCreated =  DataSetManager.createDataSets(VALID_USER_FOR_INVESTIGATION, datasets, VALID_INVESTIGATION_ID, em);
        
        for(Dataset file  : datasetsCreated){
            Dataset modified = em.find(Dataset.class, file.getId());
            checkDataset(modified);
        }
    }
    
    //@Test
    public void removeActualDataset3() throws ICATAPIException{
        removeActualDataset();
        removeActualDataset();
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void getDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        
        Dataset datasetGot = DataSetManager.getDataSet(VALID_USER_FOR_INVESTIGATION, VALID_DATA_SET_ID,  em);
        
        checkDatasetProps(datasetGot);
        assertFalse("Deleted must be false", datasetGot.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void getDatasetInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        try {
            Dataset datasetGot = DataSetManager.getDataSet(INVALID_USER, VALID_DATA_SET_ID,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void getDatasets() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        Collection<Long> dsIds = new ArrayList<Long>();
        dsIds.add(VALID_DATA_SET_ID);
        
        Collection<Dataset> datasetsGot = DataSetManager.getDataSets(VALID_USER_FOR_INVESTIGATION, dsIds,  em);
        
        for(Dataset set : datasetsGot){
            checkDatasetProps(set);
            assertFalse("Deleted must be false", set.isDeleted());
        }
    }
    
    
    /**
     * Tests remove a file, no Id
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void removeDatasetNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset validDataset = getDataset(true);
        validDataset.setId(null);
        
        try {
            DataSetManager.removeDataSet(VALID_USER_FOR_INVESTIGATION, validDataset,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests delete a file, no Id
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void deleteDatasetNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset validDataset = getDataset(true);
        validDataset.setId(null);
        
        try {
            DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, validDataset,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a file, no Id
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void updateDatasetNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataset for dataset id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset validDataset = getDataset(true);
        validDataset.setId(null);
        
        try {
            DataSetManager.updateDataSet(VALID_USER_FOR_INVESTIGATION, validDataset,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    
    private boolean checkDatasetProps(Dataset file){
        assertTrue("dataset must be in db", em.contains(file));
        
        assertNotNull("createId must be not null", file.getCreateId());
        
        assertNotNull("createTime must be not null", file.getCreateTime());
        
        assertNotNull("modId must be not null", file.getModId());
        
        assertNotNull("dataset id must be not null", file.getId());
        return true;
    }
    
    private boolean checkDataset(Dataset file){
        assertTrue("dataset must be in db", em.contains(file));
        
        assertNotNull("createTime must be not null", file.getCreateTime());
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertEquals("createId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertEquals("modId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("dataset id must be not null", file.getId());
        assertEquals("dataset must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getInvestigation().getId());
        
        return true;
    }
    
    /**
     * Tests creating a invalid file, no anme and type
     */
    //@Test(expected=ValidationException.class)
    public void testCreateInValidDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file");
        
        //create invalid file, no name
        Dataset file = new Dataset();
        try {
            Dataset dataset = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Tests creating a invalid file, name but no type
     */
    //@Test(expected=ValidationException.class)
    public void testCreateInValidDataset2() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file");
        
        //create invalid file, no name
        Dataset file = new Dataset();
        file.setName("test name");
        try {
            Dataset dataset = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void testAddInValidDatasetInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for creating a file");
        
        //create invalid file, no name
        Dataset file = new Dataset();
        Random ram = new Random();
        file.setName("unit test not allowed in "+ram.nextLong());
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        
        file.setDatasetType(datasetType.iterator().next().getName());
        
        try {
            DataSetManager.createDataSet(INVALID_USER, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void testCreateInValidDatasetInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for creating a file");
        
        //create invalid file, no name
        Dataset file = new Dataset();
        Random ram = new Random();
        file.setName("unit test not allowed in "+ram.nextLong());
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        
        file.setDatasetType(datasetType.iterator().next().getName());
        
        try {
            DataSetManager.createDataSet(INVALID_USER, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //@Test
    public void testAddValidSampleToDatasetValidUser() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sample");
        
        //update this, this also checks permissions, no need to validate cos just loaded from DB
        DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, VALID_DATA_SET_ID, em);
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=NoSuchObjectFoundException.class)
    public void testAddInValidSampleToDatasetValidUser() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sample");
        
        try {
            DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, 3045395454L,VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, em);
            
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    
    private Dataset getDataset(boolean valid){
        if(valid){
            //create valid dataset
            //create valid file
            Dataset file = new Dataset();
            Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
            file.setDatasetType(datasetType.iterator().next().getName());
            file.setName("unit test create data set " + Math.random());
            return file;
        } else {
            //create invalid dataset
            Dataset file = new Dataset();
            return file;
        }
    }
    
    private Dataset getDatasetDuplicate(boolean last){
        Dataset dataset = null;
        if(!last){
            Collection<Dataset> datasets = (Collection<Dataset>)executeListResultCmd("select d from Dataset d where d.facilityAcquired = 'Y'");
            dataset = datasets.iterator().next();
        } else {
            Collection<Dataset> datasets = (Collection<Dataset>)executeListResultCmd("select d from Dataset d where d.facilityAcquired = 'N' order by d.modTime desc");
            dataset = datasets.iterator().next();
            if(dataset == null) throw new RuntimeException("No dataset found");
        }
        log.trace(dataset);
        return dataset;
    }
    
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDataset.class);
    }
    
}
