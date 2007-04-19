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
import javax.ejb.EJB;
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
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService()
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatafileManagerBean extends EJBObject implements DatafileManagerLocal {
    
    static Logger log = Logger.getLogger(DatafileManagerBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of DatasetManagerBean */
    public DatafileManagerBean() {}
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFile(userId, datafileId, manager);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFiles(userId, datafileIds, manager);
    }
    
    
    public Long createDataFile(String sessionId, Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //add the dataset, this checks permssions
        Datafile newDataFile = DataFileManager.createDataFile(userId, dataFile, datasetId, manager);
        
        //return new id
        return newDataFile.getId();
    }
    
    public Collection<Long> createDataFiles(String sessionId, Collection<Datafile> dataFiles, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Collection<Long> ids = new ArrayList<Long>();
        //add the dataset, this checks permissions
        for(Datafile file : dataFiles){
            Datafile newDataFile = DataFileManager.createDataFile(userId, file, datasetId, manager);
            ids.add(newDataFile.getId());
        }
        
        //return ids
        return ids;
    }
    
    public void deleteDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, datafileId, manager);
    }
    
    @WebMethod(operationName="deleteDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObjectResponse")
    public void deleteDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, dataFile, manager);
    }
    
    public void removeDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, datafileId, manager);
    }
    
    @WebMethod(operationName="removeDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObjectResponse")
    public void removeDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.removeDataFile(userId, dataFile, manager);
    }
    
    public void updateDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.updateDataFile(userId, dataFile, manager);
    }
    
    public void addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get file, checks read access
        DataFileManager.addDataFileParameter(userId, dataFileParameter, datafileId, manager);
    }    
}
