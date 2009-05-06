/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.BaseTestClass;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatasetInclude;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestEntityBaseBean  extends BaseTestClass {
    
    private static Logger log = Logger.getLogger(TestEntityBaseBean.class);
    private Random random = new Random();
    
    /**
     * Tests own dataset as unique
     */
    @Test
    public void merge() throws ICATAPIException {
        Dataset dataset = new Dataset();
        
        dataset.setDescription("A");
        
        Dataset dataset2 = new Dataset();
        
        dataset2.merge(dataset);
        log.trace("desc: "+dataset2.getDescription());
        assertEquals("This should be same", dataset2.getDescription(), "A");
        assertEquals("This should be same", dataset2.getDescription(), dataset.getDescription());
    }
    
    @Test
    public void merge2() throws ICATAPIException {
        Dataset dataset = new Dataset();
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        dataset.setInvestigation(in);
        
        dataset.setDescription("A");
        
        Dataset dataset2 = new Dataset();
        
        dataset2.merge(dataset);
        log.trace("desc: "+dataset2.getDescription());
        assertNull("Investigation should be null", dataset2.getInvestigationId());
        assertEquals("This should be same", dataset2.getDescription(), "A");
        assertEquals("This should be same", dataset2.getDescription(), dataset.getDescription());
    }

    /*
    @Test
    public void cascadeRemoveDeletednvestigationObject() throws ICATAPIException {
        //get a investigation with deleted datasets
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        
        assertEquals("Datasets number must be 2", in.getDatasetCollection().size(), 2);
        assertEquals("Keywords number must be 2", in.getKeywordCollection().size(), 2);
        assertEquals("Investigators number must be 2", in.getInvestigatorCollection().size(), 2);
        assertEquals("Publications number must be 2", in.getPublicationCollection().size(), 2);
        assertEquals("Sample number must be 2", in.getSampleCollection().size(), 2);
        for (Sample sample : in.getSampleCollection()) {
            assertEquals("Sample parameter number must be 1", sample.getSampleParameterCollection().size(), 1);
            
        }
        
        in.setCascade(Cascade.REMOVE_DELETED_ITEMS, Boolean.TRUE, em);
        
        assertEquals("Datasets number must be 1 after removing deleted items", in.getDatasetCollection().size(), 1);
        for (Dataset ds : in.getDatasetCollection()) {
            assertEquals("Datafiless number must be 1 after removing deleted items", ds.getDatafileCollection().size(), 1);
        }
        
        assertEquals("Keywords number must be 1 after removing deleted items", in.getKeywordCollection().size(), 1);
        assertEquals("Investigators number must be 0 after removing deleted items", in.getInvestigatorCollection().size(), 1);
        assertEquals("Publications number must be 0 after removing deleted items", in.getPublicationCollection().size(), 0);
        assertEquals("Sample number must be 1 after removing deleted items", in.getSampleCollection().size(), 1);
        for (Sample sample : in.getSampleCollection()) {
            assertEquals("Sample parameter number must be 0", sample.getSampleParameterCollection().size(), 0);
            
        }
    }
    */
    
    @Test
    public void cascadeRemoveDeletedDatasetd() throws ICATAPIException {
        //get a investigation with deleted datasets
        Dataset ds = em.find(Dataset.class, VALID_INVESTIGATION_ID);
        
        ds.setDatasetInclude(DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
        assertEquals("datafile number must be 3", ds.getDatafileCollection().size(), 3);
        
        ds.setCascade(Cascade.REMOVE_DELETED_ITEMS, Boolean.TRUE, em);
        
        assertEquals("datafile number must be 2 after removing deleted items", 2, ds.getDatafileCollection().size());
    }
    
    @Test(expected=InsufficientPrivilegesException.class)
    public void cascadeDeleteInvestigation()  throws ICATAPIException {
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID_TO_DE_DELETED);
        
        boolean delete = true;
         
        in.setCascade(Cascade.DELETE, Boolean.valueOf(delete), em, ICAT_ADMIN_USER);
        
        for(Dataset ds : in.getDatasetCollection()){
            assertEquals("dataset: "+ds+" is marked deleted", ds.isDeleted(),Boolean.valueOf(delete));
            for (Datafile df : ds.getDatafileCollection()) {
                assertEquals("datafile: "+df+" is marked deleted", df.isDeleted(), Boolean.valueOf(delete));
                for (DatafileParameter dfp : df.getDatafileParameterCollection()) {
                    assertEquals("datafileparameter: "+dfp+" is marked deleted", dfp.isDeleted(),Boolean.valueOf(delete));
                }
            }
            for (DatasetParameter dsp : ds.getDatasetParameterCollection()) {
                assertEquals("datasetparameter: "+dsp+" is marked deleted", dsp.isDeleted(), Boolean.valueOf(delete));
            }
        }
        for(Keyword keyword : in.getKeywordCollection()){
            assertEquals("keyword: "+keyword+" is marked deleted", keyword.isDeleted(), Boolean.valueOf(delete));
        }
        for(Publication publication : in.getPublicationCollection()){
            assertEquals("Publication: "+publication+" is marked deleted", publication.isDeleted(), Boolean.valueOf(delete));
        }
        for(Sample sample : in.getSampleCollection()){
            assertEquals("Sample: "+sample+" is marked deleted", sample.isDeleted(), Boolean.valueOf(delete));
        }
        
        delete = false;
         
        in.setCascade(Cascade.DELETE, Boolean.valueOf(delete), em, ICAT_ADMIN_USER);
        
        for(Dataset ds : in.getDatasetCollection()){
            assertEquals("dataset: "+ds+" is marked deleted", ds.isDeleted(),Boolean.valueOf(delete));
            for (Datafile df : ds.getDatafileCollection()) {
                assertEquals("datafile: "+df+" is marked deleted", df.isDeleted(), Boolean.valueOf(delete));
                for (DatafileParameter dfp : df.getDatafileParameterCollection()) {
                    assertEquals("datafileparameter: "+dfp+" is marked deleted", dfp.isDeleted(),Boolean.valueOf(delete));
                }
            }
            for (DatasetParameter dsp : ds.getDatasetParameterCollection()) {
                assertEquals("datasetparameter: "+dsp+" is marked deleted", dsp.isDeleted(), Boolean.valueOf(delete));
            }
        }
        for(Keyword keyword : in.getKeywordCollection()){
            assertEquals("keyword: "+keyword+" is marked deleted", keyword.isDeleted(), Boolean.valueOf(delete));
        }
        for(Publication publication : in.getPublicationCollection()){
            assertEquals("Publication: "+publication+" is marked deleted", publication.isDeleted(), Boolean.valueOf(delete));
        }
        for(Sample sample : in.getSampleCollection()){
            assertEquals("Sample: "+sample+" is marked deleted", sample.isDeleted(), Boolean.valueOf(delete));
        }
    }
    
    @Test
    public void cascadeRemoveDeletedDatafile() throws ICATAPIException {
        //get a investigation with deleted datasets
        Datafile df= em.find(Datafile.class, VALID_INVESTIGATION_ID);
        
        df.setCascade(Cascade.DELETE, Boolean.TRUE, em, VALID_FACILITY_USER_FOR_INVESTIGATION);
        
        for (DatafileParameter dfp : df.getDatafileParameterCollection()) {
            assertTrue("datafileparameter: "+dfp+" is marked deleted", dfp.isDeleted());
            assertEquals("datafileparameter: "+dfp+" is modif "+VALID_FACILITY_USER_FOR_INVESTIGATION,VALID_FACILITY_USER_FOR_INVESTIGATION, dfp.getModId());
            
        }
    }
    
    @Test(expected=InsufficientPrivilegesException.class)
    public void cascadeRemoveDeletedDataset() throws ICATAPIException {
        
        boolean delete = true;
        
        //get a investigation with deleted datasets
        Dataset ds = em.find(Dataset.class, VALID_DATASET_ID_TO_DE_DELETED);
        
        ds.setCascade(Cascade.DELETE, Boolean.valueOf(delete), em, ICAT_ADMIN_USER);
        
        for (Datafile df : ds.getDatafileCollection()) {
            assertEquals("datafile: "+df+" is marked deleted", df.isDeleted(), delete);
            //if(df.getId() != 56L) assertEquals("datafile: "+df+" is modid "+VALID_FACILITY_USER_FOR_INVESTIGATION,VALID_FACILITY_USER_FOR_INVESTIGATION, df.getModId());
            for (DatafileParameter dfp : df.getDatafileParameterCollection()) {
                assertEquals("datafileparameter: "+dfp+" is marked deleted", dfp.isDeleted(), delete);
                assertEquals("datafileparameter: "+dfp+" is modif "+VALID_FACILITY_USER_FOR_INVESTIGATION,VALID_FACILITY_USER_FOR_INVESTIGATION, dfp.getModId());
                
            }
        }
        
        for (DatasetParameter dsp : ds.getDatasetParameterCollection()) {
            assertEquals("datasetparameter: "+dsp+" is marked deleted", dsp.isDeleted(), delete);
            assertEquals("datasetparameter: "+dsp+" is modif "+VALID_FACILITY_USER_FOR_INVESTIGATION,VALID_FACILITY_USER_FOR_INVESTIGATION, dsp.getModId());
            
        }
        
        delete = false;
        ds.setCascade(Cascade.DELETE, Boolean.valueOf(delete), em, ICAT_ADMIN_USER);
        
        for (Datafile df : ds.getDatafileCollection()) {
            assertEquals("datafile: "+df+" is marked deleted", df.isDeleted(), delete);
            for (DatafileParameter dfp : df.getDatafileParameterCollection()) {
                assertEquals("datafileparameter: "+dfp+" is marked deleted", dfp.isDeleted(), delete);
            }
        }
        
        for (DatasetParameter dsp : ds.getDatasetParameterCollection()) {
            assertEquals("datasetparameter: "+dsp+" is marked deleted", dsp.isDeleted(), delete);
        }
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestEntityBaseBean.class);
    }
}
