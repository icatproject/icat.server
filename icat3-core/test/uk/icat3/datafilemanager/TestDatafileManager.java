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
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.ValidationException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Parameter;
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
    // @Test
    public void testCreateValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test create data file "+ram.nextLong());
        
        
        Datafile datafile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        validateFile(datafile);
        
        
        ////new test with format
        
        //create valid file
        Datafile file2 = new Datafile();
        file2.setName("unit test format "+ram.nextLong());
        
        //now add valid format aswell
        Collection<DatafileFormat> datafileFormats = (Collection<DatafileFormat>)executeListResultCmd("select d from DatafileFormat d");
        if(datafileFormats.size() == 0) throw new ICATAPIException("No DatafileFormats found");
        
        
        DatafileFormat ft = datafileFormats.iterator().next();
        log.debug("Setting datafile format as "+ft);
        file2.setDatafileFormat(ft);
        
        Datafile datafile2 = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file2, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
        assertNotNull("Format cannot be null", datafile2.getDatafileFormat());
        assertNotNull("Format name cannot be null", datafile2.getDatafileFormat().getDatafileFormatPK().getName());
        assertNotNull("Format version cannot be null", datafile2.getDatafileFormat().getDatafileFormatPK().getVersion());
        
        validateFile(datafile);
        
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=ValidationException.class)
    public void testCreateValidDatafileInvalidFormat() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test create data file "+ram.nextLong());
        
        DatafileFormat format = new DatafileFormat("version","name");
        file.setDatafileFormat(format);
        
        try {
            Datafile datafile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    //@Test(expected=ValidationException.class)
    public void testCreateValidDatafileInvalidParameterCollection() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test invalid parameter "+ram.nextLong());
        
        DatafileParameter invalidParameter = new DatafileParameter("units","name", VALID_DATASET_ID_FOR_INVESTIGATION);
        Collection<DatafileParameter> coll = new ArrayList<DatafileParameter>();
        coll.add(invalidParameter);
        
        file.setDatafileParameterCollection(coll);
        
        try {
            Datafile datafile = DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    @Test
    public void testCreateValidDatafileValidParameterCollection() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for creating a file Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid file
        Datafile file = new Datafile();
        Random ram = new Random();
        file.setName("unit test valid parameter "+ram.nextLong());
        
        Collection<Parameter> parameters = (Collection<Parameter>)executeListResultCmd("select d from Parameter d where d.isDatafileParameter = 'Y'");
        if(parameters.size() == 0) throw new ICATAPIException("No DatafileParameter found");
        Parameter parameter = parameters.iterator().next() ;
        
        DatafileParameter dfp = new DatafileParameter(parameter.getParameterPK().getUnits(),parameter.getParameterPK().getName(),VALID_DATASET_ID_FOR_INVESTIGATION);
        Collection<DatafileParameter> coll = new ArrayList<DatafileParameter>();
        coll.add(dfp);
        
        file.setDatafileParameterCollection(coll);
        
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
        
        DataFileManager.createDataFiles(VALID_USER_FOR_INVESTIGATION, datafiles, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        
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
    //@Test(expected=ValidationException.class)
    public void testAddInValidDatafile() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding a file");
        
        //create invalid file, no name
        Datafile file = new Datafile();
        try {
            DataFileManager.createDataFile(VALID_USER_FOR_INVESTIGATION, file, VALID_DATASET_ID_FOR_INVESTIGATION, em);
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
