/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Sample;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.exceptions.*;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatasetSearch extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestDatasetSearch.class);
    
    
    /**
     * Tests dataset types
     */
    @Test
    public void testlistDatasetTypes(){
        log.info("Testing valid user for all dataset types: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> types = DatasetSearch.listDatasetTypes(em);
        
        Collection<DatasetType> typesInDB = (Collection<DatasetType>)executeListResultCmd("SELECT d FROM DatasetType d where d.markedDeleted = 'N'");
        
        assertNotNull("Must not be an null collection of types ", types);
        assertEquals("Number of datasettypes searched is different to number in DB",typesInDB.size(),types.size());
    }
    
    /**
     * Tests dataset status'
     */
    @Test
    public void testlistDatasetStatus(){
        log.info("Testing valid user for all dataset status: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> status = DatasetSearch.listDatasetStatus(em);
        
        Collection<DatasetStatus> statusInDB = (Collection<DatasetStatus>)executeListResultCmd("SELECT d FROM DatasetStatus d where d.markedDeleted = 'N'");
        
        assertNotNull("Must not be an null collection of DatasetStatus ", status);
        assertEquals("Number of DatasetStatus searched is different to number in DB",statusInDB.size(),status.size());
    }
    
    /**
     * Tests dataset getSamplesBySampleName
     */
    @Test
    public void testgetSamplesBySampleName(){
        log.info("Testing valid user for get sample by sample name: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Sample> samples = DatasetSearch.getSamplesBySampleName(VALID_USER_FOR_INVESTIGATION, VALID_SAMPLE_NAME, em);
        
        //TODO dynamic find this, at the moment its one dataset
        assertNotNull("Must not be an null collection of samples ", samples);
        assertEquals("Number of samples searched for "+VALID_SAMPLE_NAME+" should be 2",2,samples.size());
        //assertEquals("Sample id should be 3",samples.iterator().next().getId(), VALID_SAMPLE_ID_FOR_INVESTIGATION_ID);
    }
    
    /**
     * Tests dataset getSamplesBySampleName
     */
    @Test
    public void testgetSamplesBySampleNameInvalidUser(){
        log.info("Testing invalid user for get sample by sample name: "+INVALID_USER);
        
        Collection<Sample> samples = DatasetSearch.getSamplesBySampleName(INVALID_USER, VALID_SAMPLE_NAME, em);
        
        //TODO dynamic find this, at the moment its one dataset
        assertNotNull("Must not be an null collection of samples ", samples);
        assertEquals("Number of samples searched for "+VALID_SAMPLE_NAME+" should be 0",0,samples.size());
        
    }
    
    /**
     * Tests dataset getDatasetsBySample
     */
    @Test
    public void testgetDatasetsBySample() throws ICATAPIException{
        log.info("Testing valid user for get datasets by sample : "+VALID_USER_FOR_INVESTIGATION);
        
        Sample sample = ManagerUtil.find(Sample.class, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, em);
        
        Collection<Dataset> datasets = DatasetSearch.getDatasetsBySample(VALID_USER_FOR_INVESTIGATION, sample, em);
        
        //TODO dynamic find this, at the moment its one dataset
        assertNotNull("Must not be an null collection of Datasets ", datasets);
        assertEquals("Number of Dataset searched for "+VALID_SAMPLE_NAME+" should be 1",1,datasets.size());
        assertEquals("Datasets id should be 2",datasets.iterator().next().getId(), VALID_DATA_SET_ID);
    }
    
    /**
     * Tests dataset getDatasetsBySample
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void testgetDatasetsBySampleInvalidUser() throws ICATAPIException{
        log.info("Testing invalid user for get datasets by sample : "+INVALID_USER);
        
        Sample sample = ManagerUtil.find(Sample.class, VALID_SAMPLE_ID_FOR_INVESTIGATION_ID, em);
        
        
        try {
            Collection<Dataset> datasets = DatasetSearch.getDatasetsBySample(INVALID_USER, sample, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            
            throw ex;
        }
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestDatasetSearch.class);
    }
    
}
