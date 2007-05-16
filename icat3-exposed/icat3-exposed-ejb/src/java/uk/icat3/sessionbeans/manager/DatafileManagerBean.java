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
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.ManagerUtil;
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
    
    /**
     * 
     * @param sessionId 
     * @param datafileId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFile(userId, datafileId, manager);
    }
    
    /**
     * 
     * @param sessionId 
     * @param datafileIds 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFiles(userId, datafileIds, manager);
    }
    
    
    /**
     * 
     * @param sessionId 
     * @param dataFile 
     * @param datasetId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.ValidationException 
     * @return 
     */
    public Long createDataFile(String sessionId, Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //add the dataset, this checks permssions
        Datafile newDataFile = DataFileManager.createDataFile(userId, dataFile, datasetId, manager);
        
        //return new id
        return newDataFile.getId();
    }
    
    /**
     * 
     * @param sessionId 
     * @param dataFiles 
     * @param datasetId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.ValidationException 
     * @return 
     */
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
    
    /**
     * 
     * @param sessionId 
     * @param datafileId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     */
    public void deleteDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, datafileId, manager);
    }
    
    /**
     * 
     * @param sessionId 
     * @param dataFile 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
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
     * 
     * @param sessionId 
     * @param datafileId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     */
    public void removeDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.deleteDataFile(userId, datafileId, manager);
    }
    
    /**
     * 
     * @param sessionId 
     * @param dataFile 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
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
     * 
     * @param sessionId 
     * @param dataFile 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.ValidationException 
     */
    public void updateDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //delete the dataset, this checks permsisions
        DataFileManager.updateDataFile(userId, dataFile, manager);
    }
    
    /**
     * 
     * @param sessionId 
     * @param dataFileParameter 
     * @param datafileId 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.ValidationException 
     */
    public void addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get file, checks read access
        DataFileManager.addDataFileParameter(userId, dataFileParameter, datafileId, manager);
    }    
       
    
    /**
     *
     * @param sessionId
     * @param dataSetParameter
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     */
    public void modifyDataFileParameter(String sessionId, DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataFileManager.updateDatafileParameter(userId, dataFileParameter, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param datasetParameterPK
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     */
    public void removeDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatafileParameter datafileParameter = ManagerUtil.find(DatafileParameter.class, datafileParameterPK, manager);
        
        DataFileManager.removeDatafileParameter(userId, datafileParameter, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param datasetParameterPK
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     */
    public void deleteDataFileParameter(String sessionId, DatasetParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatafileParameter datafileParameter = ManagerUtil.find(DatafileParameter.class, datafileParameterPK, manager);
        
        DataFileManager.deleteDatafileParameter(userId, datafileParameter, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleId
     * @param datasetId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     */
    public void setDataSetSample(String sessionId, Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.setDataSetSample(userId, sampleId, datasetId, manager);
    }
}
