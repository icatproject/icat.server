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
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.manager.DataFileManager;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatafileManager extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatafileManager.class);
    
    /**
     * Tests creating a file
     */
    //@Test
    public void testCreateValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test create data file "+ram.nextLong());
        
        Datafile datafile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        validateFile(datafile);
    }
    /**
     * Tests creating a file
     */
   // @Test
    public void testAddValidDatafiles() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for add a files for daatset Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file1 = new Datafile();
        Random ram = new Random();
        file1.setName("unit test addDatafiles "+ram.nextLong());
        
        Datafile file2 = new Datafile();
        file2.setName("unit test addDatafiles "+ram.nextLong());
        
        Collection<Datafile> datafiles = new ArrayList<Datafile>();
        datafiles.add(file1);
        datafiles.add(file2);
        
        DataFileManager.addDataFiles(VALID_USER_FOR_INVESTIGATION, datafiles, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        for(Datafile file  : datafiles){
            //get the file by searching through the DB
            Datafile datafileFound = (Datafile)executeSingleResultCmd("select d from Datafile d where d.name = '"+file.getName()+"'");
            validateFile(datafileFound);
        }
    }
    
    
    private boolean validateFile(Datafile file){
        assertTrue("datafile must be in db", em.contains(file));
        assertTrue("File must be not deleted", file.getDeleted().equalsIgnoreCase("N"));
        
        assertNotNull("createId must be not null", file.getCreateId());
        assertSame("createId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getCreateId());
        
        assertNotNull("modId must be not null", file.getModId());
        assertSame("modId must be "+VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, file.getModId());
        
        assertNotNull("dataset id must be not null", file.getDatasetId());
        assertEquals("dataset must be "+VALID_DATASET_ID_FOR_INVESTIGATION, VALID_DATASET_ID_FOR_INVESTIGATION, file.getDatasetId().getId());
        
        return true;
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=ValidationException.class)
    public void testCreateInValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file");
        
        //create invalid file, no name
        Datafile file = new Datafile();
        try {
            Datafile datafile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
        
    }
    
     /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void testAddInValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding a file");
        
        //create invalid file, no name
        Datafile file = new Datafile();
        try {
            DataFileManager.addDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
        
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void testCreateInValidDatafileInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for creating a file");
        
        //create invalid file, no name
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test "+ram.nextLong());
        try {
            Datafile datafile = DataFileManager.createDataFile(INVALID_USER, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
        
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatafileManager.class);
    }
    
}
