/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestManagerUtil extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestManagerUtil.class);
    private Random random = new Random();
    
    /**
     * Tests own dataset as unique
     */
    @Test
    public void datasetUnique() throws ICATAPIException {
        Dataset dataset = getDatasetDuplicate(false);
        
        boolean unique = ManagerUtil.isUnique(dataset,em);
        
        assertTrue("This should be unique", unique);
    }
    
    /**
     * Tests new dataset as unique
     */
    @Test
    public void datasetUnique2() throws ICATAPIException {
        Dataset dataset = getDatasetDuplicate(false);
        
        Dataset datasetDu  = new Dataset();
        datasetDu.merge(dataset);
        
        //set things
        datasetDu.setId(null);
        datasetDu.setInvestigationId(dataset.getInvestigationId());
        datasetDu.setInvestigation(dataset.getInvestigation());
        log.trace(datasetDu.getName()+" "+datasetDu.getId());
        
        boolean unique = ManagerUtil.isUnique(datasetDu,em);
        
        assertFalse("This should not be unique", unique);
    }
    
    /**
     * Tests new dataset as unique
     */
    @Test
    public void datasetUnique3() throws ICATAPIException {
        Dataset dataset = getDataset(true);
        
        Dataset added = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, dataset, VALID_INVESTIGATION_ID, em);
        log.trace("Added: "+added);
        
        //Investigation investigation = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        //added dataset
        Dataset dataset2 = getDataset(true);
        dataset2.setInvestigation(added.getInvestigation());
        
        boolean unique = ManagerUtil.isUnique(dataset2,em);
        //remove dataset
        Dataset found = em.find(Dataset.class, added.getId());        
        em.remove(found);
        
        assertFalse("This should not be unique", unique);
    }
    
     @Test
    public void addData() throws ICATAPIException {
           Dataset dataset = getDataset(true);
        
        Dataset added = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, dataset, VALID_INVESTIGATION_ID, em);
        log.trace("Added: "+added);
     }
    
    /**
     * Tests new dataset as unique
     */
//    @Test
    public void datasetUnique4() throws ICATAPIException {
                      
        Dataset dataset_ = getDataset(true);
        dataset_.setName("differnet");
        Dataset added2 = DataSetManager.createDataSet(VALID_USER_FOR_INVESTIGATION, dataset_, VALID_INVESTIGATION_ID, em);
        log.trace("Added: "+added2);
        
        //added dataset
        Dataset dataset2 = em.find(Dataset.class, added2.getId());
        log.trace("Checking uniqueness on: "+dataset2.getId());
        dataset2.setInvestigationId(added2.getInvestigationId());
        
        dataset2.setName("unit test create data set");
        
        boolean unique = ManagerUtil.isUnique(dataset2,em);
        
        assertFalse("This should not be unique", unique);
    }
    
    @Test
    public void clearData() throws ICATAPIException {
      
        Collection<Dataset> datasets = (Collection<Dataset>)executeListResultCmd("select d from Dataset d");
        for (Dataset dataset : datasets) {
            log.trace(dataset);
        }
        
        Dataset dataset = getDatasetDuplicate(true);
        
        //remove datasets
        Dataset found = em.find(Dataset.class, dataset.getId());
        if(found != null) em.remove(found);
        
    }
    
    private Dataset getDataset(boolean valid){
        if(valid){
            //create valid dataset
            //create valid file
            Dataset file = new Dataset();
            Collection<DatasetType> datasetType = (Collection<DatasetType>)executeListResultCmd("select d from DatasetType d");
            file.setDatasetType(datasetType.iterator().next().getName());
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
        return new JUnit4TestAdapter(TestManagerUtil.class);
    }
}
