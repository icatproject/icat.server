/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.data;


import java.util.ArrayList;
import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.Instrument;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.exceptions.*;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatafileDownload extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatafileDownload.class);
    
    /**
     * Tests datafiles
     */
    @Test
    public void testDataFileDownload(){
        log.info("Testing download datafile "+VALID_DATA_FILE_ID);
        
        
    }
    
   
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatafileDownload.class);
    }
}
