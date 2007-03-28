/*
 * TestDatasetManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.datafilemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.ValidationException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatasetManager extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatasetManager.class);
    
    /**
     * Tests creating a file
     */
    // @Test
    public void testCreateValidDataset() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a set for investigation id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Dataset file = new Dataset();
        Random ram = new Random();
        file.setName("unit test create data set "+ram.nextLong());
        
        //find dataset type
        Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
        
        file.setDatasetType(datasetType.iterator().next());
        
        
        Dataset dataset = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, file, VALID_INVESTIGATION_ID, em);
        
        validateFile(dataset);
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
        
        DataSetManager.addDataSets(VALID_USER_FOR_INVESTIGATION, datasets, VALID_INVESTIGATION_ID, em);
        
        for(Dataset file  : datasets){
            //get the file by searching through the DB
            Dataset datasetFound = (Dataset)executeSingleResultCmd("select d from Dataset d where d.name = '"+file.getName()+"'");
            validateFile(datasetFound);
        }
    }
    
    
    private boolean validateFile(Dataset file){
        assertTrue("dataset must be in db", em.contains(file));
        assertTrue("File must be not deleted", file.getDeleted().equalsIgnoreCase("N"));
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertSame("createId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertSame("modId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("investigation id must be not null", file.getInvestigationId());
        assertEquals("investigation must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getInvestigationId().getId());
        
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
            DataSetManager.addDataSet(INVALID_USER, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
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
    @Test(expected=ValidationException.class)
    public void testAddInValidSampleToDatasetValidUser() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding sample");
        
        try {
             DataSetManager.setDataSetSample(VALID_USER_FOR_INVESTIGATION, 3045395454L,VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, em);
            
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());            
            throw ex;
        }
    }
    
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatasetManager.class);
    }
    
}
