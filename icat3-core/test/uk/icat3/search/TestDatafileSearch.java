/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.ArrayList;
import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Instrument;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.exceptions.*;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatafileSearch extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatafileSearch.class);
    
    /**
     * Tests datafiles
     */
    @Test
    public void testsearchByRunNumber(){
        log.info("Testing valid user for search By Run Number: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        Collection<Datafile> files = DatafileSearch.searchByRunNumber(VALID_USER_FOR_INVESTIGATION, instruments, 0L, 100000L, em);
        
        assertNotNull("Must not be an null collection of types ", files);
        assertEquals("Number of datasettypes searched is different to number in DB", 3 , files.size());
        
        log.trace("Number datafiles returned is "+files.size());
        for (Datafile datafile : files) {
            log.trace(datafile + " "+datafile.getName());
            
        }
    }
    
    /**
     * Tests datafiles
     */
    @Test
    public void testsearchByRunNumberMultipleInstruments(){
        log.info("Testing valid user for search By Run Number: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        instruments.add("SXD-invalid");
        Collection<Datafile> files = DatafileSearch.searchByRunNumber(VALID_USER_FOR_INVESTIGATION, instruments, 0L, 100000L, em);
        
        assertNotNull("Must not be an null collection of types ", files);
        assertEquals("Number of datasettypes searched is different to number in DB", 3 , files.size());
        
        log.trace("Number datafiles returned is "+files.size());
        for (Datafile datafile : files) {
            log.trace(datafile + " "+datafile.getName());
            
        }
    }
    
    /**
     * Tests datafiles, limit by 2
     */
    @Test
    public void testsearchByRunNumberLimit(){
        log.info("Testing valid user for search By Run Number: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        Collection<Datafile> files = DatafileSearch.searchByRunNumber(VALID_USER_FOR_INVESTIGATION, instruments, 0L, 100000L, 0,1,em);
        
        assertNotNull("Must not be an null collection of types ", files);
        assertEquals("Number of datasettypes searched is different to number in DB", 1 , files.size());
        
        log.trace("Number datafiles returned is "+files.size());
        for (Datafile datafile : files) {
            log.trace(datafile + " "+datafile.getName());
            
        }
    }
    
    /**
     * Tests datafiles, invalid instrument
     */
    @Test
    public void testsearchByRunNumberInvalidInstrument(){
        log.info("Testing valid user, invalid instrument, for search By Run Number: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("fsdfsdfsdfsdfsdfsdfsd");
        
        Collection<Datafile> files = DatafileSearch.searchByRunNumber(VALID_USER_FOR_INVESTIGATION, instruments, 0f, 100000f, em);
        
        assertNotNull("Must not be an null collection of types ", files);
        assertEquals("Number of datasettypes searched is different to number in DB", 0 , files.size());
        
        log.trace("Number datafiles returned is "+files.size());
        for (Datafile datafile : files) {
            log.trace(datafile + " "+datafile.getName());
            
        }
    }
    
    /**
     * Tests datafiles, limited run range
     */
    @Test
    public void testsearchByRunNumberLimitRange(){
        log.info("Testing valid user, limit range, for search By Run Number: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        
        //limit by 1200, miss one of the three out so only two returned
        Collection<Datafile> files = DatafileSearch.searchByRunNumber(VALID_USER_FOR_INVESTIGATION, instruments, 1070f, 100000f, em);
        
        assertNotNull("Must not be an null collection of types ", files);
        assertEquals("Number of datasettypes searched is different to number in DB", 1 , files.size());
        
        log.trace("Number datafiles returned is "+files.size());
        for (Datafile datafile : files) {
            log.trace(datafile + " "+datafile.getName());
            
        }
    }
    
     /**
     * Tests dataset types
     */
    //@Test
    public void testlistDatafileFormats(){
        log.info("Testing valid user for all DatafileFormats: "+VALID_USER_FOR_INVESTIGATION);
        Collection<DatafileFormat> types = DatafileSearch.listDatafileFormats(em);
        
        Collection<DatafileFormat> typesInDB = (Collection<DatafileFormat>)executeListResultCmd("SELECT d FROM DatafileFormat d where d.markedDeleted = 'N'");
        
        assertNotNull("Must not be an null collection of types ", types);
        assertEquals("Number of DatafileFormats searched is different to number in DB",typesInDB.size(),types.size());
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatafileSearch.class);
    }
}
