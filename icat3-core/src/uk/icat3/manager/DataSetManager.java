/*
 * DataSetManager.java
 *
 * Created on 27 February 2007, 13:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
public class DataSetManager {
    
    static Logger log = Logger.getLogger(DataSetManager.class);
    
    
    ////////////////   Delete commands       ///////////////////////////
    
    public static void deleteDataSet(String userId, Dataset dataSet, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        
        deleteDataSet(userId, dataSet.getId(), manager);
        
    }
    
    public static void deleteDataSet(String userId, Long dataSetId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSet("+userId+", "+dataSetId+", EntityManager)");
        
        Dataset dataset = checkDataSet(dataSetId, manager);
        //check if the id exists in the database
        if(dataset == null) throw new EntityNotFoundException("Dataset: id: "+dataSetId+" not found.");
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.DELETE, manager);
        
        //delete dataset (Cascade is true);
        //TODO might have to remove all the datafiles first
        manager.remove(dataset);
        
    }
    
    public static void deleteDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSets("+userId+", "+dataSetIds+", EntityManager)");
        
        for(Long dataSetId : dataSetIds){
            deleteDataSet(userId, dataSetId, manager);
        }
    }
    
    //added boolean to avoid erausre name clash
    //TODO
    public static boolean deleteDataSets(String userId, Collection<Dataset> dataSets, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSets("+userId+", "+dataSets+", EntityManager)");
        
        for(Dataset dataSet : dataSets){
            deleteDataSet(userId, dataSet, manager);
        }
        
        return true;
    }
    ////////////////   End of delete commands       ///////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    
    public static void updateDataSet(String userId, Dataset dataSet, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("updateDataSet("+userId+", "+dataSet+", EntityManager)");
        
        //check to see if DataSet exists, dont need the returned dataset as merging
        checkDataSet(dataSet.getId(), manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataSet, AccessType.UPDATE, manager);
        
        manager.merge(dataSet);
    }
    
    public static void addDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataSet("+userId+", "+dataSet+" "+investigationId+", EntityManager)");
        InvestigationManager.addDataSet(userId,dataSet,investigationId, manager);
    }
    
    public static void addDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataSets("+userId+", "+dataSets+" "+investigationId+", EntityManager)");
        
        InvestigationManager.addDataSets(userId, dataSets, investigationId, manager);
    }
    ///////////////   End of add/Update Commands    ///////////////////
    
    
    
    ////////////////////    Get Commands    /////////////////////////
    
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, DatasetInclude includes,  EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("getDataSet("+userId+", "+dataSetIds+" EntityManager)");
        
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        
        for(Long dataSetId : dataSetIds) {
            
            //check Dataset exist
            Dataset dataset  = checkDataSet(dataSetId, manager);
            
            //check user has read access
            GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
            
            //add to arraylist
            datasets.add(dataset);
        }
        
        //add include information
        ManagerUtil.getDatasetInformation(datasets, includes);
        
        return datasets;
    }
    
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        return getDataSets(userId, dataSetIds,DatasetInclude.NONE, manager);
    }
    
    public static Collection<Dataset> getDataSet(String userId, Long dataSetId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        Collection<Long> datasets = new ArrayList<Long>();
        datasets.add(dataSetId);
        
        return getDataSets(userId, datasets, DatasetInclude.NONE, manager);
    }
    ////////////////////    End of get Commands    /////////////////////////
    
    
    
    
    protected static Dataset checkDataSet(Long datasetId, EntityManager manager) throws EntityNotFoundException {
        Dataset dataset = manager.find(Dataset.class, datasetId);
        //check if the id exists in the database
        if(dataset == null) throw new EntityNotFoundException("Dataset: id: "+datasetId+" not found.");
        
        log.trace("DataSet: id: "+datasetId+" exists in the database");
        
        return dataset;
    }
}
