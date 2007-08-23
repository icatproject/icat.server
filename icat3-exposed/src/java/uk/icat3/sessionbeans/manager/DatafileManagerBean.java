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
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless()
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatafileManagerBean extends EJBObject implements DatafileManagerLocal {
    
    static Logger log = Logger.getLogger(DatafileManagerBean.class);
    
    /** Creates a new instance of DatasetManagerBean */
    public DatafileManagerBean() {}
    
    /**
     * Gets a data file object from a data file id, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileId Id of data file
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Datafile}
     */
    @WebMethod()
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFile(userId, datafileId, manager);
    }
    
    /**
     * Gets a collection of data file object from a collection of data file ids, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileIds collection of data file ids
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Datafile} objects
     */
    @WebMethod()
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFiles(userId, datafileIds, manager);
    }
    
    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be created
     * @param datasetId Id of data set
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return the created {@link Datafile} primary key
     */
    @WebMethod()
    public Datafile createDataFile(String sessionId, Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //add the dataset, this checks permssions
        Datafile newDataFile = DataFileManager.createDataFile(userId, dataFile, datasetId, manager);
        
        //return new id
        return newDataFile;
    }
    
    /**
     * Creates a collection of data files, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFiles collection of objects to be created
     * @param datasetId Id of data set
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return the collection of created {@link Datafile} primary keys
     */
    @WebMethod()
    public Collection<Datafile> createDataFiles(String sessionId, Collection<Datafile> dataFiles, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Collection<Datafile> ids = new ArrayList<Datafile>();
        //add the dataset, this checks permissions
        for(Datafile file : dataFiles){
            Datafile newDataFile = DataFileManager.createDataFile(userId, file, datasetId, manager);
            ids.add(newDataFile);
        }
        
        //return ids
        return ids;
    }
    
    /**
     * Deletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param datafileId id to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, datafileId, manager);
    }
    
    /**
     * Deletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataFile objectto be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod(operationName="deleteDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObjectResponse")
    public void deleteDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, dataFile, manager);
    }
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param datafileId id be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.removeDataFile(userId, datafileId, manager);
    }
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod(operationName="removeDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObjectResponse")
    public void removeDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.removeDataFile(userId, dataFile, manager);
    }
    
    /**
     * Updates data file depending on whether the user has permission to update this data file.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void modifyDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.updateDataFile(userId, dataFile, manager);
    }
    
    /**
     * Adds a data file paramter object to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatafileParameter} created
     */
    @WebMethod()
    public DatafileParameter addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get file, checks read access
        return DataFileManager.addDataFileParameter(userId, dataFileParameter, datafileId, manager);
    }
    
    /**
     * Adds a collection of data file paramter objects to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameters object to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatafileParameter} created
     */
    @WebMethod()
    public Collection<DatafileParameter> addDataFileParameters(String sessionId, Collection<DatafileParameter> dataFileParameters, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get file, checks read access
        return DataFileManager.addDataFileParameters(userId, dataFileParameters, datafileId, manager);
    }
    
    
    /**
     * Updates the data file paramter object, depending if the user has access to update the data file parameter.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void modifyDataFileParameter(String sessionId, DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataFileManager.updateDatafileParameter(userId, dataFileParameter, manager);
    }
    
    /**
     * Removes (from the database) a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatafileParameter datafileParameter = ManagerUtil.find(DatafileParameter.class, datafileParameterPK, manager);
        
        DataFileManager.removeDatafileParameter(userId, datafileParameter, manager);
    }
    
    /**
     * Deletes a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatafileParameter datafileParameter = ManagerUtil.findObject(DatafileParameter.class, datafileParameterPK, manager);
        
        DataFileManager.deleteDatafileParameter(userId, datafileParameter, manager);
    }
    
    
    ////////////////////////////////////   Authorisation Section //////////////////////////////////////////////
    /**
     * Gets all the IcatAuthorisations for a dataFile id of all of the users
     *
     * @param sessionId session id of the user.
     * @param dataFileId Datafile id of the authorisations you want
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid     * 
     * @return Collection of {@link IcatAuthorisation}>s of the datafile id
     */
    @WebMethod()
    public Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long dataFileId) throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getAuthorisations(userId, dataFileId, manager);
    }
    
    /**
     * Adds a role to a datafile Id for a user (fedId) depending weather user  from session id has permission to do it
     *
     * @param sessionId session id of the user.    
     * @param toAddUserId federal Id of user to add
     * @param toAddRole new role for federal Id
     * @param dataFileId datafile Id of the item to add the role to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid      
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding 
     * @return {@link IcatAuthorisation}s of the datafile id
     */
    @WebMethod()
    public IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long dataFileId ) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.addAuthorisation(userId, toAddUserId, toAddRole, dataFileId, manager);
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
        
        DataFileManager.deleteAuthorisation(userId, authorisationId, manager);
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
        
        DataFileManager.removeAuthorisation(userId, authorisationId, manager);
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
        
        DataFileManager.updateAuthorisation(userId, toChangetoRole, authorisationId, manager);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
}
