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
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
public class DataSetManager extends ManagerUtil {
    
    static Logger log = Logger.getLogger(DataSetManager.class);
    
    
    ////////////////   Delete commands       ///////////////////////////
    
    /**
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set
     *
     * @param userId
     * @param dataSet
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        
        deleteDataSet(userId, dataSet.getId(), manager);
        
    }
    
    /**
     *
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID.
     *
     * @param userId
     * @param dataSetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSet("+userId+", "+dataSetId+", EntityManager)");
        
        Dataset dataset = checkDataSet(dataSetId, manager);
               
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.DELETE, manager);
        
        //delete dataset (Cascade is true);
        //TODO might have to remove all the datafiles first
        //manager.remove(dataset);
        //not deleting anymore, jsut changing deleted to Y
        
        log.info("Deleting: "+dataset);
        dataset.setCascadeDeleted(true);
        
    }
    
    /**
     * Deletes the collection of data set for a user depending if the users id has delete permissions to delete the data sets from the
     * data set IDs
     *
     * @param userId
     * @param dataSetIds
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSets("+userId+", "+dataSetIds+", EntityManager)");
        
        for(Long dataSetId : dataSetIds){
            deleteDataSet(userId, dataSetId, manager);
        }
    }
    
    
    //TODO:  added boolean to avoid erausre name clash with above method
    /**
     *
     * Deletes the collection of data sets for a user depending if the user's id has delete permissions to delete the data sets
     *
     * @param userId
     * @param dataSets
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static boolean deleteDataSets(String userId, Collection<Dataset> dataSets, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataSets("+userId+", "+dataSets+", EntityManager)");
        
        for(Dataset dataSet : dataSets){
            deleteDataSet(userId, dataSet, manager);
        }
        
        return true;
    }
    ////////////////   End of delete commands       ///////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    
    /**
     * Updates / Adds a data set depending on whether the user has permission to update this data set or its investigation
     *
     * @param userId
     * @param dataSet
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void updateDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateDataSet("+userId+", "+dataSet+", EntityManager)");
        
        //check to see if DataSet exists, dont need the returned dataset as merging
        if(dataSet.getId() != null){
            checkDataSet(dataSet.getId(), manager);
        }
        
        //check if valid dataset
        dataSet.isValid(manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataSet, AccessType.UPDATE, manager);
        
        //if null then update
        if(dataSet.getId() != null){
            manager.merge(dataSet);
        } else {
            //new dataset, set createid
            dataSet.setCreateId(userId);
            manager.persist(dataSet);
        }
    }
    
    /**
     * Adds a data set to the list a data sets for a investigation, depending if the user has update permission on the investigation
     *
     * @param userId
     * @param dataSet
     * @param investigationId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void addDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("addDataSet("+userId+", "+dataSet+" "+investigationId+", EntityManager)");
        InvestigationManager.addDataSet(userId,dataSet,investigationId, manager);
    }
    
    /**
     * Adds a collection of data sets to the list a data sets for a investigation, depending if the user has update permission on the investigation
     *
     * @param userId
     * @param dataSets
     * @param investigationId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void addDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("addDataSets("+userId+", "+dataSets+" "+investigationId+", EntityManager)");
        
        InvestigationManager.addDataSets(userId, dataSets, investigationId, manager);
    }
    ///////////////   End of add/Update Commands    ///////////////////
    
    
    
    ////////////////////    Get Commands    /////////////////////////
    
    /**
     * Gets the data sets objects from a list of data set ids, depending if the users has access to read the data sets.
     * Also gets extra information regarding the data set.  See {@link DatasetInclude}
     *
     * @param userId
     * @param dataSetIds
     * @param includes
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, DatasetInclude includes,  EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
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
    
    /**
     *  Gets the data sets objects from a list of data set ids, depending if the users has access to read the data sets
     *
     * @param userId
     * @param dataSetIds
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        return getDataSets(userId, dataSetIds,DatasetInclude.NONE, manager);
    }
    
    /**
     * Gets the data set object from a list of data set ids, depending if the user has access to read the data set.
     *
     * @param userId
     * @param dataSetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static Collection<Dataset> getDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        Collection<Long> datasets = new ArrayList<Long>();
        datasets.add(dataSetId);
        
        return getDataSets(userId, datasets, DatasetInclude.NONE, manager);
    }
    
    ////////////////////    End of get Commands    /////////////////////////
    
}
