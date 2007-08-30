/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.datafilemanager;

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
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.exposed.util.BaseTestClassTX;
import uk.icat3.exposed.util.TestUserLocal;
import uk.icat3.sessionbeans.manager.DatafileManagerBean;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import static uk.icat3.exposed.util.TestConstants.*;

/**
 * Opens a new entitymanager and tranaction for each method and tests for get/ remove/ delete/ modify/ insert
 * for data files for valid and invalid users on the DataFileManager class
 *
 * @author gjd37
 */
public class TestDatafile extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatafile.class);
    private static Random random = new Random();
    
    private static DatafileManagerBean icat = new DatafileManagerBean();
    private static UserSessionLocal tul = new TestUserLocal();
    
    
    /**
     * Tests creating a file
     */
    @Test
    public void createDatafile() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for creating a datafile for datafile id: "+VALID_INVESTIGATION_ID);
        
        Datafile dataFile = getDatafile(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Datafile dataFileInserted = icat.createDataFile(VALID_SESSION, dataFile, VALID_INVESTIGATION_ID);
        
        checkDatafile(dataFileInserted);
        assertFalse("Deleted must be false", dataFileInserted.isDeleted());
        assertNotNull("Format cannot be null", dataFileInserted.getDatafileFormat());
    }
    
    /**
     * Tests deleting a file, marks it as deleted Y
     */
    @Test
    public void deleteDatafile() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafileDuplicate(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.deleteDataFile(VALID_SESSION, validDatafile.getId());
        
        Datafile modified = em.find(Datafile.class,validDatafile.getId() );
        
        checkDatafile(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        //for(Investigator investigator : modified.getInvestigatorCollection()){
        //  assertTrue("investigator must be deleted", investigator.isDeleted());
        //}
    }
    
    
    /**
     * Tests removing a file, removes it from DB
     */
    @Test
    public void removeDatafile() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION_ICAT_ADMIN +"  for rmeoving dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile duplicateDatafile = getDatafileDuplicate(true);
        duplicateDatafile.setDeleted(false);
        duplicateDatafile.setCreateId(VALID_ICAT_ADMIN_FOR_INVESTIGATION);
        
        Collection<Long> longs =  addAuthorisation(duplicateDatafile.getId(), duplicateDatafile.getDataset().getId(), VALID_ICAT_ADMIN_FOR_INVESTIGATION, ElementType.DATAFILE, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.removeDataFile(VALID_SESSION_ICAT_ADMIN, duplicateDatafile.getId());
        
        Datafile modified = em.find(Datafile.class,duplicateDatafile.getId() );
        assertNull("Datafile must not be found in DB "+duplicateDatafile, modified);
        
        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
        
        it = longs.iterator();
        assertNull("IcatAuthorisation["+it.next()+"] must not be found in DB ", icatAuth);
    }
    
    /**
     * Tests creating a file for invalid user, should throw InsufficientPrivilegesException that contains
     * message with 'does not have permission' in it
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  session: "+ INVALID_SESSION +"  for adding dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Datafile dataFileInserted = icat.createDataFile(INVALID_SESSION, validDatafile, VALID_DATASET_ID_FOR_INVESTIGATION);
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
        log.info("Testing  session: "+ VALID_SESSION +"  for adding invalid dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile invalidDatafile = getDatafile(false);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Datafile dataFileInserted = icat.createDataFile(VALID_SESSION, invalidDatafile, VALID_INVESTIGATION_ID);
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
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting a props dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile propsDatafile = getDatafileDuplicate(false);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deleteDataFile(VALID_SESSION, propsDatafile.getId());
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
        log.info("Testing  session: "+ VALID_SESSION +"  for removing a props dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid dataFile, no name
        Datafile propsDatafile = getDatafileDuplicate(false);
        log.trace(propsDatafile);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deleteDataFile(VALID_SESSION, propsDatafile.getId());
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
        log.info("Testing  session: "+ VALID_SESSION +"  for creating data file with in valid parameter for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Datafile file = getDatafile(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        //cannot add param with data file because dont know data file id yet
        DatafileParameter param = TestDatafileParameter.getDatafileParameter(true, true);
        param.setDatafile(file);
        
        file.addDataFileParameter(param);
        
        try {
            Datafile dataFile = icat.createDataFile(VALID_SESSION, file, VALID_INVESTIGATION_ID);
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
        log.info("Testing  session: "+ VALID_SESSION +"  for creating a whole data set for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Datafile file = getDatafile(true);
       /* DatafileParameter param = TestDatafileParameter.getDatafileParameter(true, true);
        param.setDatafile(file);
        
        file.addDataFileParamaeter(param);*/
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Datafile dataFile = icat.createDataFile(VALID_SESSION, file, VALID_INVESTIGATION_ID);
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
        
        removeDatafile();
    }
    
    /**
     * Tests creating a collection of files
     */
    @Test
    public void testAddValidDatafiles() throws ICATAPIException {
        log.info("Testing session: "+ VALID_SESSION +" for add a files for datset Id: "+VALID_INVESTIGATION_ID);
        
        //create valid file
        Datafile file1 = getDatafile(true);
        
        //find dataFile type
        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
        
        dataFiles.add(file1);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Collection<Datafile> dataFilesCreated =  icat.createDataFiles(VALID_SESSION, dataFiles, VALID_INVESTIGATION_ID);
        
        for(Datafile file  : dataFilesCreated){
            Datafile modified = em.find(Datafile.class, file.getId());
            checkDatafile(modified);
            assertNotNull("Format cannot be null", modified.getDatafileFormat());
            assertFalse("Deleted must be false", modified.isDeleted());
        }
        
        removeDatafile();
    }
    
    /**
     * Tests getting a propagated file
     */
    @Test
    public void getDatafile() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Datafile dataFileGot = icat.getDatafile(VALID_SESSION, VALID_DATA_SET_ID);
        
        checkDatafileProps(dataFileGot);
        assertFalse("Deleted must be false", dataFileGot.isDeleted());
    }
    
    /**
     * Tests getting a file for invalid user, should throw InsufficientPrivilegesException will message containing 'does not have permission'
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void getDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  session: "+ INVALID_SESSION +"  for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Datafile dataFileGot = icat.getDatafile(INVALID_SESSION, VALID_DATA_FILE_ID);
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
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deleteDataFile(VALID_SESSION, validDatafile.getId());
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
        log.info("Testing  session: "+ VALID_SESSION +"  for removing dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.removeDataFile(VALID_SESSION, validDatafile.getId());
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
        log.info("Testing  session: "+ VALID_SESSION +"  for removing dataFile to dataFile Id: "+VALID_INVESTIGATION_ID);
        
        Datafile validDatafile  = getDatafile(true);
        validDatafile.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.modifyDataFile(VALID_SESSION, validDatafile);
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
        log.info("Testing  session: "+ VALID_SESSION +"  for get a dataFile for dataFile id: "+VALID_INVESTIGATION_ID);
        Collection<Long> dsIds = new ArrayList<Long>();
        dsIds.add(VALID_DATA_SET_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Collection<Datafile> dataFilesGot = icat.getDatafiles(VALID_SESSION, dsIds);
        
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
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertEquals("createId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertEquals("modId must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("dataFile id must be not null", file.getId());
        assertEquals("dataFile must be "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, file.getDataset().getInvestigation().getId());
        
        return true;
    }
    
    /**
     * Tests creating a invalid file for valid user, should throw ValidationException that contains
     * message with 'cannot be null' in it
     */
    //@Test(expected=ValidationException.class)
    public void testCreateInValidDatafile() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for creating a file");
        
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
    //@Test(expected=InsufficientPrivilegesException.class)
    public void testCreateInValidDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for creating a file");
        
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
     * Creates a datafile which is either valid or not
     */
    private Datafile getDatafile(boolean valid){
        if(valid){
            //create valid dataFile
            //create valid file
            Datafile file = new Datafile();
            Collection<DatafileFormat> datafileFormat = (Collection<DatafileFormat>)executeListResultCmd("select d from DatafileFormat d");
            file.setDatafileFormat(datafileFormat.iterator().next());
            file.setName("unit test create data set");
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
