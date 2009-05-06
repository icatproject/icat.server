/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.datafilemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;

/**
 * Opens a new entitymanager and tranaction for each method and tests for get/ remove/ delete/ modify/ insert
 * for data files for valid and invalid users on the DataFileManager class
 *
 * @author gjd37
 */
public class TestDatafile extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatafile.class);
    private static Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void createDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a datafile for datafile id: "+VALID_INVESTIGATION_ID);
        
        Datafile dataFile = getDatafile(true);
        
        Datafile dataFileInserted = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, dataFile, VALID_INVESTIGATION_ID, em);
        
        checkDatafile(dataFileInserted);
        assertFalse("Deleted must be false", dataFileInserted.isDeleted());
        assertFalse("facility acquired must be false", dataFileInserted.isFacilityAcquiredSet());
        assertNotNull("Format cannot be null", dataFileInserted.getDatafileFormat());
    }
    
    @Test
    public void modifyDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a datafile for datafile id: "+VALID_INVESTIGATION_ID);
        
        String modifiedDesc = "Modfied Desc "+random.nextInt();
        
        Datafile modifiedDatafile = getDatafile(true);
        Datafile duplicateDatafile = getDatafileDuplicate(true);
        
        Dataset ds = em.find(Dataset.class, VALID_INVESTIGATION_ID);
        modifiedDatafile.setDataset(ds);
        modifiedDatafile.setDescription(modifiedDesc);
        modifiedDatafile.setId(duplicateDatafile.getId());
        
        Datafile datafileInserted = DataFileManager.updateDataFile(VALID_USER_FOR_INVESTIGATION, modifiedDatafile, em);
        assertEquals("Desc must be "+modifiedDesc+" and not "+datafileInserted.getDescription(), datafileInserted.getDescription(), modifiedDesc);
        
        
        checkDatafile(datafileInserted);
        assertFalse("Deleted must be false", datafileInserted.isDeleted());
    }
    
    /**
     * Tests deleting a file, marks it as deleted Y
     */
    @Test
    public void deleteDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafileDuplicate(true);
        
        DataFileManager.deleteDataFile(VALID_USER_FOR_INVESTIGATION, validDatafile, em);
        
        Datafile modified = em.find(Datafile.class,validDatafile.getId() );
        
        checkDatafile(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        for(DatafileParameter param : modified.getDatafileParameterCollection()){
            assertTrue("investigator must be deleted", param.isDeleted());
        }
    }
    
     /**
     * Tests deleting a file, marks it as deleted Y
     */
    @Test
    public void undeleteDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafileDuplicate(true);
        
        DataFileManager.deleteDataFile(VALID_USER_FOR_INVESTIGATION, validDatafile, em);
        
        Datafile modified = em.find(Datafile.class,validDatafile.getId() );
        
        checkDatafile(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        //check deep delete
        for(DatafileParameter param : modified.getDatafileParameterCollection()){
            assertTrue("investigator must be undeleted", !param.isDeleted());
        }
    }
    
    /**
     * Tests removing a file, removes it from DB
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile duplicateDatafile = getDatafileDuplicate(true);
        
        //TODO remove
        duplicateDatafile.setDeleted(false);
        
        try {
            DataFileManager.removeDataFile(VALID_USER_FOR_INVESTIGATION, duplicateDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    
    /**
     * Tests creating a file for invalid user, should throw InsufficientPrivilegesException that contains
     * message with 'does not have permission' in it
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        
        try {
            Datafile dataFileInserted = (Datafile)DataFileManager.createDataFile(INVALID_USER, validDatafile, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a invalid file, should throw ValidationException that contains
     * message with 'cannot be null' in it
     */
    @Test(expected=ValidationException.class)
    public void addInvalidDatafile() throws ICATAPIException, Exception {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile invalidDatafile = getDatafile(false);
        
        try {
            Datafile dataFileInserted = (Datafile)DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, invalidDatafile, VALID_INVESTIGATION_ID, em);
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
     * Tests deleting a file thats been made by propogation, should throw InsufficientPrivilegesException that contains
     * message with 'cannot be modified' in it
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void deleteDatafileProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile propsDatafile = getDatafileDuplicate(false);
        
        try {
            DataFileManager.deleteDataFile(VALID_USER_FOR_INVESTIGATION, propsDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests removing a file thats been made by propogation, should throw InsufficientPrivilegesException that contains
     * message with 'cannot be modified' in it
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeDatafileProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile propsDatafile = getDatafileDuplicate(false);
        log.trace(propsDatafile);
        try {
            DataFileManager.deleteDataFile(VALID_USER_FOR_INVESTIGATION, propsDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a valid file but adding a paramter to it.  A paramter must contains the parent datafile id and because
     * this has not been persited yet it is unknown so throws ValidationException with message containing 'parent'
     */
    @Test(expected=ValidationException.class)
    public void createValidDatafileParameter() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating data file with in valid parameter for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Datafile file = getDatafile(true);
        
        //cannot add param with data file because dont know data file id yet
        DatafileParameter param = TestDatafileParameter.getDatafileParameter(true, true);
        param.setDatafile(file);
        
        file.addDataFileParameter(param);
        
        try {
            Datafile dataFile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'parent'", ex.getMessage().contains("parent"));
            throw ex;
        }
        
    }
    
    /**
     * Tests creating a valid file, deletes the file after wards.
     */
    @Test
    public void createWholeValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a whole data set for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Datafile file = getDatafile(true);
//        DatafileParameter param = TestDatafileParameter.getDatafileParameter(true, true);
//        param.setDatafile(file);
//        
//        file.addDataFileParamaeter(param);
        
        Datafile dataFile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_INVESTIGATION_ID, em);
        log.info("Datafile: "+dataFile);
        
        Datafile modified = em.find(Datafile.class, dataFile.getId());
        
        checkDatafile(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        assertNotNull("Format cannot be null", file.getDatafileFormat());
        
        for(DatafileParameter datafileParameter : modified.getDatafileParameterCollection()){
            assertFalse("Deleted must be false", datafileParameter.isDeleted());
            assertNotNull("createId must be not null", datafileParameter.getCreateId());
            assertEquals("createId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, datafileParameter.getCreateId());
            
            assertNotNull("modId must be not null", datafileParameter.getModId());
            assertEquals("modId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, datafileParameter.getModId());
            
            assertNotNull("dataFile id must be not null", datafileParameter.getDatafileParameterPK());
            assertEquals("dataFile must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, datafileParameter.getDatafile().getDataset().getInvestigation().getId());
        }
        
        removeActualDatafile();
    }
    
    /**
     * Tests creating a collection of files
     */
    @Test
    public void testAddValidDatafiles() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for add a files for datset Id: "+VALID_INVESTIGATION_ID);
        
        
        //create valid file
        Datafile file1 = getDatafile(true);
        
        //find dataFile type
        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
        
        dataFiles.add(file1);
        
        Collection<Datafile> dataFilesCreated =  DataFileManager.createDataFiles(VALID_USER_FOR_INVESTIGATION, dataFiles, VALID_INVESTIGATION_ID, em);
        
        for(Datafile file  : dataFilesCreated){
            Datafile modified = em.find(Datafile.class, file.getId());
            checkDatafile(modified);
            assertNotNull("Format cannot be null", modified.getDatafileFormat());
            
        }
        
        removeActualDatafile();
    }
    
    /**
     * Tests getting a propagated file
     */
   /* @Test
    public void getDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        
        
        Datafile dataFileGot = DataFileManager.getDataFile(VALID_USER_FOR_INVESTIGATION, VALID_DATA_SET_ID,  em);
        
        checkDatafileProps(dataFileGot);
        assertFalse("Deleted must be false", dataFileGot.isDeleted());
    }*/
    
    /**
     * Tests getting a file for invalid user, should throw InsufficientPrivilegesException will message containing 'does not have permission'
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void getDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        
        try {
            Datafile dataFileGot = DataFileManager.getDataFile(INVALID_USER, VALID_DATA_FILE_ID,  em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests deleting a file with no primary key
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteDatafileNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        try {
            DataFileManager.deleteDataFile(VALID_USER_FOR_INVESTIGATION, validDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found.'", ex.getMessage().contains("not found."));
            throw ex;
        }
    }
    
    /**
     * Tests removing a file with no primary key
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removingDatafileNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        try {
            DataFileManager.removeDataFile(VALID_USER_FOR_INVESTIGATION, validDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found.'", ex.getMessage().contains("not found."));
            throw ex;
        }
    }
    
    /**
     * Tests update a file with no primary key
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updateDatafileNoId() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        try {
            DataFileManager.updateDataFile(VALID_USER_FOR_INVESTIGATION, validDatafile, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found.'", ex.getMessage().contains("not found."));
            throw ex;
        }
    }
    
    
    /**
     * Tests getting a collection of propagated files.
     */
    @Test
    public void getDatafiles() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        Collection<Long> dsIds = new ArrayList<Long>();
        dsIds.add(VALID_DATA_FILE_ID);
        
        Collection<Datafile> dataFilesGot = DataFileManager.getDataFiles(VALID_USER_FOR_INVESTIGATION, dsIds,  em);
        
        for(Datafile set : dataFilesGot){
            checkDatafileProps(set);
            assertFalse("Deleted must be false", set.isDeleted());
        }
    }
    
    /**
     * Checks that the data file is valid in the DB for extra stuff for propagated fields
     */
    private boolean checkDatafileProps(Datafile file){
        assertTrue("dataFile must be in db", em.contains(file));
        
        if(file.getDatafileFormat() != null){
            assertNotNull("Format name cannot be null", file.getDatafileFormat().getDatafileFormatPK().getName());
            assertNotNull("Format version cannot be null", file.getDatafileFormat().getDatafileFormatPK().getVersion());
        }
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertEquals("createId must be "+VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION, VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        // assertEquals("modId must be "+VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION, VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION, file.getModId());
        
        assertNotNull("dataFile id must be not null", file.getId());
        assertEquals("dataFile must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getDataset().getInvestigation().getId());
        return true;
    }
    
    /**
     * Checks that the data file is valid in the DB
     */
    private boolean checkDatafile(Datafile file){
        assertTrue("dataFile must be in db", em.contains(file));
               
        assertNotNull("createTime must be not null", file.getCreateTime());
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertEquals("createId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertEquals("modId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("dataFile id must be not null", file.getId());
        assertEquals("dataFile must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getDataset().getInvestigation().getId());
        
        return true;
    }
    
    /**
     * Tests creating a invalid file for valid user, should throw ValidationException that contains
     * message with 'cannot be null' in it
     */
    @Test(expected=ValidationException.class)
    public void testCreateInValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file");
        
        //create invalid file, no name
        Datafile file = getDatafile(false);
        try {
            Datafile dataFile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a invalid file for invalid user, should throw ValidationException that contains
     * message with 'cannot be null' in it
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testCreateInValidDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for creating a file");
        
        //create invalid file, no name
        Datafile file = getDatafile(false);
        
        try {
            DataFileManager.createDataFile(INVALID_USER, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests removing a file, removes it from DB
     */
    
    @Test
    public void removeActualDatafile() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile duplicateDatafile = getDatafileDuplicate(true);
        
        //TODO remove
        duplicateDatafile.setDeleted(false);
        duplicateDatafile.setCreateId(ICAT_ADMIN_USER);
        
        Collection<Long> longs =  addAuthorisation(duplicateDatafile.getId(), duplicateDatafile.getDataset().getId(), ICAT_ADMIN_USER, ElementType.DATAFILE, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        
        log.trace("Created two icat auths "+longs);
        DataFileManager.removeDataFile(ICAT_ADMIN_USER, duplicateDatafile, em);
        
        Datafile modified = em.find(Datafile.class,duplicateDatafile.getId() );
        assertNull("Datafile must not be found in DB "+duplicateDatafile, modified);
        
        //Not needed as removing a datafile does not remove icat auths now.
//        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
//        
//        it = longs.iterator();
//        assertNull("IcatAuthorisation["+it.next()+"] must not be found in DB ", icatAuth);
    }
    
    /**
     * Creates a datafile which is either valid or not
     */
    private Datafile getDatafile(boolean valid){
        if(valid){
            //create valid dataFile
            //create valid file
            Datafile file = new Datafile();
            Collection<DatafileFormat> datafileFormat = (Collection<DatafileFormat>)executeListResultCmd("select d from DatafileFormat d");
            file.setDatafileFormat(datafileFormat.iterator().next());
            file.setName("unit test create datafile " + Math.random());
            return file;
        } else {
            //create invalid dataFile
            Datafile file = new Datafile();
            return file;
        }
    }
    
    /**
     * Gets a datafile from the Db so that its a duplicate
     */
    private Datafile getDatafileDuplicate(boolean last){
        Datafile dataFile = null;
        if(!last){
            Collection<Datafile> dataFiles = (Collection<Datafile>)executeListResultCmd("select d from Datafile d where d.facilityAcquired = 'Y'");
            dataFile = dataFiles.iterator().next();
        } else {
            Collection<Datafile> dataFiles = (Collection<Datafile>)executeListResultCmd("select d from Datafile d where d.facilityAcquired = 'N' order by d.modTime desc");
            dataFile = dataFiles.iterator().next();
            if(dataFile == null) throw new RuntimeException("No dataFile found");
        }
        log.trace(dataFile);
        return dataFile;
    }
    
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatafile.class);
    }
    
}
