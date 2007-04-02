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
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
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
        
        Dataset dataset = find(Dataset.class, dataSetId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.DELETE, manager);
        
        //delete dataset (Cascade is true);
        //TODO might have to remove all the datafiles first
        //manager.remove(dataset);
        //not deleting anymore, jsut changing deleted to Y
        
        log.info("Deleting: "+dataset);
        dataset.setCascade(Cascade.DELETE, Boolean.TRUE);
        
    }
    
    /**
     *
     * Removes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID.
     *
     * @param userId
     * @param dataSetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("removeDataSet("+userId+", "+dataSetId+", EntityManager)");
        
        Dataset dataset = find(Dataset.class, dataSetId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.DELETE, manager);
        
        log.info("Removing: "+dataset);
        
        //dataset.getInvestigationId().getDatasetCollection().remove(dataset);
        
        manager.remove(dataset);
        
    }
    
    /**
     *
     * Removes a collection of data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID.
     *
     * @param userId
     * @param dataSetIds collection of datasets
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static boolean removeDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("removeDataSet("+userId+", "+dataSetIds+", EntityManager)");
        
        for(Long dataSetId : dataSetIds){
            removeDataSet(userId, dataSetId, manager);
        }
        return true;
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
     * @param validate does the whole of dataset need validating
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Dataset updateDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateDataSet("+userId+", "+dataSet+", EntityManager)");
        
        Dataset datasetManaged = null;
        
        //check to see if DataSet exists, dont need the returned dataset as merging
        if(dataSet.getId() != null){
           datasetManaged = find(Dataset.class, dataSet.getId(), manager);
        }
        
        //check if valid dataset
        dataSet.isValid(manager);
        //check if unique
        if(!dataSet.isUnique(manager)) throw new ValidationException(dataSet+" is not unique.");
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataSet, AccessType.UPDATE, manager);
        
        //if null then update
        if(dataSet.getId() != null){
            datasetManaged.setModId(userId);
            datasetManaged.merge(dataSet);
            return datasetManaged;
        } else {
            //new dataset, set createid, this sets mod id and modtime
            dataSet.setCascade(Cascade.MOD_AND_CREATE_IDS, userId);
            dataSet.setCascade(Cascade.REMOVE_ID, Boolean.TRUE);
            manager.persist(dataSet);
            return dataSet;
        }
    }
    
    /**
     * Creates a data set, depending if the user has update permission on the data set associated with the investigation
     *
     * @param userId
     * @param dataSet
     * @param investigationId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Dataset createDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createDataFile("+userId+", "+dataSet+" "+investigationId+", EntityManager)");
        
        //check investigation exists
        Investigation investigation  = find(Investigation.class, investigationId, manager);
        dataSet.setInvestigationId(investigation);
        dataSet.setId(null);
        
        return updateDataSet(userId, dataSet, manager);
    }
    
    /**
     * Creates a collection of data sets, depending if the user has update permission on the data set associated with the investigation
     *
     * @param userId
     * @param dataSets collection of the datasets
     * @param investigationId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Collection<Dataset> createDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createDataFile("+userId+", "+dataSets+" "+investigationId+", EntityManager)");
        
        Collection<Dataset> datasetsCreated = new ArrayList<Dataset>();
        for(Dataset dataset : dataSets){
            Dataset datasetCreated = createDataSet(userId, dataset, investigationId, manager);
            datasetsCreated.add(datasetCreated);
        }
        
        return datasetsCreated;
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
            Dataset dataset  = find(Dataset.class, dataSetId, manager);
            
            //check user has read access
            GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
            
            //add to arraylist
            datasets.add(dataset);
        }
        
        //add include information
        getDatasetInformation(datasets, includes);
        
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
    public static Dataset getDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("getDataSet("+userId+", "+dataSetId+" EntityManager)");
        
        Collection<Long> datasets = new ArrayList<Long>();
        datasets.add(dataSetId);
        
        Collection<Dataset> datasetsReturned = getDataSets(userId, datasets, DatasetInclude.NONE, manager);
        return datasetsReturned.iterator().next();
    }
    
    ////////////////////    End of get Commands    /////////////////////////
    
    
    /////////////////////   Util commands /////////////////////////
    
    public static void setDataSetSample(String userId, Long sampleId, Long datasetid, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("setDataSetSample("+userId+", "+sampleId+", "+datasetid+", EntityManager)");
        
        //check valid sample id
        Sample sampleRef = find(Sample.class,sampleId, manager);
        
        //get dataset,
        Dataset datasetManaged = find(Dataset.class, datasetid, manager);
        
        outer: if(sampleId != null){
            Collection<Sample> samples = datasetManaged.getInvestigationId().getSampleCollection();
            for(Sample sample : samples){
                if(sample.getId().equals(sampleId)){
                    //invest has for this sample in
                    break outer;
                }
            }
            //if here not got sample in
            throw new ValidationException("Sample[id="+sampleId+"] is not associated with Dataset[id="+datasetManaged.getId()+ "]'s invesigation.");
        }
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, datasetManaged, AccessType.UPDATE, manager);
        
        //add the dataset parameter to the dataset
        datasetManaged.setSampleId(sampleId);
        
    }
    
    public static DatasetParameter addDataSetParameter(String userId, DatasetParameter dataSetParameter, Long datasetId,  EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataSetParameter("+userId+", "+dataSetParameter+", "+datasetId+", EntityManager)");
        
        //check if param already in DB
        if(manager.contains(dataSetParameter))  throw new ValidationException("DatasetParameter: "+dataSetParameter.getDatasetParameterPK().getName()+" with units: "+dataSetParameter.getDatasetParameterPK().getUnits()+" is already is a parameter of the dataset.");
        
        //get dataset,
        Dataset dataset = find(Dataset.class, datasetId, manager);
        
        //set id for dataSetParameter
        dataSetParameter.setDataset(dataset);
        dataSetParameter.setCreateId(userId);
        
        //check is valid, check parent dataset is in the private key
        dataSetParameter.isValid(manager);
        
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, dataSetParameter, AccessType.CREATE, manager);
        
        manager.persist(dataSetParameter);
        
        return dataSetParameter;
    }
    
    public static DatasetParameter addDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataSetParameter("+userId+", "+dataSetParameter+", EntityManager)");
        
        Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        return  addDataSetParameter(userId, dataSetParameter, datasetId, manager);
    }
    
    public static void removeDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("removeDataSetParameter("+userId+", "+dataSetParameter+", EntityManager)");
        
        Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        
        //find the dataset
        DatasetParameter dataSetParameterManaged = find(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);
        
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, dataSetParameterManaged, AccessType.REMOVE, manager);
        
        dataSetParameterManaged.setDataset(null);
        
        manager.remove(dataSetParameterManaged);
    }
    
    public static void deleteDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("deleteDataSetParameter("+userId+", "+dataSetParameter+", EntityManager)");
        
        Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        
        //find the dataset
        DatasetParameter dataSetParameterManaged = find(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);
        
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, dataSetParameterManaged, AccessType.DELETE, manager);
        
        dataSetParameterManaged.setDeleted(true);
    }
    
    public static void updateDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("updateDataSetParameter("+userId+", "+dataSetParameter+", EntityManager)");
        
        DatasetParameter dataSetParameterFound = find(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);
        
        //ok, now check permissions on found data set
        GateKeeper.performAuthorisation(userId, dataSetParameterFound, AccessType.UPDATE, manager);
        
        //update model with changed wanted
        dataSetParameterFound.merge(dataSetParameter);
        
        //check is valid, check parent dataset is in the private key
        dataSetParameter.isValid(manager);
    }
}


