    /*
     * DatasetManagerBean.java
     *
     * Created on 26 March 2007, 15:30
     *
     * To change this template, choose Tools | Template Manager
     * and open the template in the editor.
     */

package uk.icat3.sessionbeans.manager;

import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@Stateless()
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatasetManagerBean extends EJBObject implements DatasetManagerLocal {
    
    static Logger log = Logger.getLogger(DatasetManagerBean.class);
    
    /** Creates a new instance of DatasetManagerBean */
    public DatasetManagerBean() {}
    
    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     *
     * @param sessionId session id of the user.
     * @param datasetId Id of object
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Dataset}
     */
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSet(userId, datasetId, manager);
    }
    
    /**
     * Gets the data set object from a from a list of data set ids, depending if the user has access to read the data sets.
     *
     * @param sessionId session id of the user.
     * @param datasetIds Id of object
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Dataset}s
     */
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Dataset> getDatasets(String sessionId, Collection<Long> datasetIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSets(userId, datasetIds, manager);
    }
    
    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     * Also gets extra information regarding the data set.  See {@link DatasetInclude}
     *
     * @param sessionId session id of the user.
     * @param datasetId Id of object
     * @param includes other information wanted with the data set
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Dataset}
     */
    @WebMethod(operationName="getDatasetIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludesResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataSet = DataSetManager.getDataSet(userId, datasetId, includes, manager);
                       
        return dataSet;
    }
    
    /**
     * Creates a data set, depending if the user has create permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be created
     * @param investigationId id of investigations to added the dataset to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Dataset} that was created
     */
    @WebMethod
    public Dataset createDataSet(String sessionId, Dataset dataSet, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataset = DataSetManager.createDataSet(userId, dataSet, investigationId, manager);
        
        return dataset;
    }
    
    /**
     * Creates a collection of data sets, depending if the user has update permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSets collection of the datasets
     * @param investigationId id of investigations to added the datasets to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Dataset}s that were created
     */
    @WebMethod
    public Collection<Dataset> createDataSets(String sessionId, Collection<Dataset> dataSets, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Collection<Dataset> ids = new ArrayList<Dataset>();
        for(Dataset dataset : dataSets){
            Dataset datasetReturned = DataSetManager.createDataSet(userId, dataset, investigationId, manager);
            ids.add(datasetReturned);
        }
        
        return ids;
    }
    
    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.removeDataSet(userId, dataSetId, manager);
    }
    
    /**
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID. Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.deleteDataSet(userId, dataSetId, manager);
    }
    
    /**
     * Updates a data set depending on whether the user has permission to update this data set or its investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataSet(String sessionId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.updateDataSet(userId, dataSet, manager);
    }
    
    /**
     * Adds a data set paramter to a dataset, depending if the users has access to create the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @param datasetId id of dataset to add to    
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatasetParameter} that was created
     */
    @WebMethod
    public DatasetParameter addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.addDataSetParameter(userId, dataSetParameter, datasetId, manager);
    }
    
     /**
     * Adds a collection of data set paramters to a dataset, depending if the users has access to create the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameters object to be created
     * @param datasetId id of dataset to add to    
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatasetParameter} that was created
     */
    @WebMethod
    public Collection<DatasetParameter> addDataSetParameters(String sessionId, Collection<DatasetParameter> dataSetParameters, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.addDataSetParameters(userId, dataSetParameters, datasetId, manager);
    }
    
    /**
     * Modifies a data set paramter, depending if the users has access to update the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataSetParameter(String sessionId, DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.updateDataSetParameter(userId, dataSetParameter, manager);
    }
    
    /**
     * Removes the data set paramter, depending if the users has access to delete the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatasetParameter datasetParameter = ManagerUtil.find(DatasetParameter.class, datasetParameterPK, manager);
        
        DataSetManager.removeDataSetParameter(userId, datasetParameter, manager);
    }
    
    /**
     * Deleted the data set paramter, depending if the users has access to remove the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatasetParameter datasetParameter = ManagerUtil.findObject(DatasetParameter.class, datasetParameterPK, manager);
        
        DataSetManager.deleteDataSetParameter(userId, datasetParameter, manager);
    }
    
    /**
     * Sets the dataset sample id, depending if the users has access to update the data set
     *
     * @param sessionId session id of the user.
     * @param sampleId Id of sample
     * @param datasetId Id of dataset
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void setDataSetSample(String sessionId, Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.setDataSetSample(userId, sampleId, datasetId, manager);
    }
    
     ////////////////////////////////////   Authorisation Section //////////////////////////////////////////////
     /**
     * Gets all the IcatAuthorisations for a dataSetId id of all of the users
     *
     * @param sessionId session id of the user.
     * @param dataSetId Dataset id of the authorisations you want
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid     * 
     * @return Collection of {@link IcatAuthorisation}>s of the datafile id
     */
    @WebMethod()
    public Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long dataSetId) throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getAuthorisations(userId, dataSetId, manager);
    }
    
     /**
     * Adds a role to a dataset Id for a user (fedId) depending weather user  from session id has permission to do it
     *
     * @param sessionId session id of the user.    
     * @param toAddUserId federal Id of user to add
     * @param toAddRole new role for federal Id
     * @param dataSetId dataset Id of the item to add the role to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid      
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding 
     * @return {@link IcatAuthorisation}s of the datafile id
     */
    @WebMethod()
    public IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long dataSetId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.addAuthorisation(userId, toAddUserId, toAddRole, dataSetId, manager);
    }
    
    /**
     * Deletes a IcatAuthorisation 
     *
     * @param sessionId session id of the user.     
     * @param authorisationId id of the authorisation to delete
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     */
    @WebMethod()
    public void deleteAuthorisation(String sessionId, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.deleteAuthorisation(userId, authorisationId, manager);
    }
    
    /**
     * Removes a IcatAuthorisation 
     *
     * @param sessionId session id of the user.     
     * @param authorisationId id of the authorisation to remove
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     */
    @WebMethod()
    public void removeAuthorisation(String sessionId, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.removeAuthorisation(userId, authorisationId, manager);
    }
    
    /**
     * Changes a IcatAuthorisation role for a authorisation id
     *
     * @param sessionId session id of the user.     
     * @param toChangetoRole role to change to
     * @param authorisationId id of the authorisation to remove
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding     
     */
    @WebMethod()
    public void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.updateAuthorisation(userId, toChangetoRole, authorisationId, manager);
    }    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
}
