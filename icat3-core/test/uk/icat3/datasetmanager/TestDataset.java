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
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
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
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateDataset() throws ICATAPIException, Exception {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset duplicateDataset = getDataset(true);
        
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
    @Test
    public void deleteDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        Dataset validDataset  = getDatasetDuplicate(true);
        
        DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, validDataset, em);
        
        Dataset modified = em.find(Dataset.class,validDataset.getId() );
        
        checkDataset(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        //for(Investigator investigator : modified.getInvestigatorCollection()){
        //  assertTrue("investigator must be deleted", investigator.isDeleted());
        //}
    }
    
    
    /**
     * Tests creating a file
     */
    @Test
    public void removeDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset duplicateDataset = getDatasetDuplicate(true);
        
        DataSetManager.removeDataSet(VALID_USER_FOR_INVESTIGATION, duplicateDataset, em);
        
        Dataset modified = em.find(Dataset.class,duplicateDataset.getId() );
        
        assertNull("Dataset must not be found in DB "+duplicateDataset, modified);
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
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
    @Test(expected=ValidationException.class)
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
    @Test(expected=InsufficientPrivilegesException.class)
    public void deleteDatasetProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset propsDataset = getDatasetDuplicate(false);
        
        try {
            DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, propsDataset, em);
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
    public void removeDatasetProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props dataset to dataset Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataset, no name
        Dataset propsDataset = getDatasetDuplicate(false);
        log.trace(propsDataset);
        try {
            DataSetManager.deleteDataSet(VALID_USER_FOR_INVESTIGATION, propsDataset, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be modified'", ex.getMessage().contains("cannot be modified"));
            throw ex;
        }
    }
    
    // @Test
    public void createWholeValidDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a whole data set for dataset id: "+VALID_INVESTIGATION_ID);
        
        //create valid set
        Dataset set = new Dataset();
        Random ram = new Random();
        set.setName("complete dataset:  "+ram.nextLong());
        
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        set.setDatasetType(datasetType.iterator().next());
        
        //add status
        Collection<DatasetStatus> datasetStstus = (Collection<DatasetStatus>)executeListResultCmd("select d from DatasetStatus d");
        set.setDatasetStatus(datasetStstus.iterator().next());
        
        //add a file
        Datafile file = new Datafile();
        file.setName("whole file: "+ram.nextLong());
        
        set.addDataFile(file);
        
        Dataset dataset = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, set, VALID_INVESTIGATION_ID, em);
        log.info("Dataset: "+dataset);
        checkDataset(dataset);
    }
    
    /**
     * Tests creating a file
     */
    // @Test
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
        file1.setDatasetType(added);
        file2.setDatasetType(added);
        
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        datasets.add(file1);
        datasets.add(file2);
        
        DataSetManager.createDataSets(VALID_USER_FOR_INVESTIGATION, datasets, VALID_INVESTIGATION_ID, em);
        
        for(Dataset file  : datasets){
            //get the file by searching through the DB
            Dataset datasetFound = (Dataset)executeSingleResultCmd("select d from Dataset d where d.name = '"+file.getName()+"'");
            checkDataset(datasetFound);
        }
    }
    
    
    private boolean checkDataset(Dataset file){
        assertTrue("dataset must be in db", em.contains(file));
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertEquals("createId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertEquals("modId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("dataset id must be not null", file.getId());
        assertEquals("dataset must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getInvestigationId().getId());
        
        return true;
    }
    
    /**
     * Tests creating a invalid file, no anme and type
     */
    // @Test(expected=ValidationException.class)
    public void testCreateInValidDatafile() throws ICATAPIException {
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
        
        file.setDatasetType(datasetType.iterator().next());
        
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
    //  @Test(expected=InsufficientPrivilegesException.class)
    public void testCreateInValidDatasetInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for creating a file");
        
        //create invalid file, no name
        Dataset file = new Dataset();
        Random ram = new Random();
        file.setName("unit test not allowed in "+ram.nextLong());
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        
        file.setDatasetType(datasetType.iterator().next());
        
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
    // @Test(expected=ValidationException.class)
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
            file.setDatasetType(datasetType.iterator().next());
            file.setName("unit test create data set");
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
            Collection<Dataset> datasets = (Collection<Dataset>)executeListResultCmd("select d from Dataset d where d.createId LIKE '%PROP%'");
            dataset = datasets.iterator().next();
        } else {
            Collection<Dataset> datasets = (Collection<Dataset>)executeListResultCmd("select d from Dataset d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
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
