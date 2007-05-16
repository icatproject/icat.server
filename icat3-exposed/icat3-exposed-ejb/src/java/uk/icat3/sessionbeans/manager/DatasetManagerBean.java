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
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService(targetNamespace="uk.ac.stfc.manager")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatasetManagerBean extends EJBObject implements DatasetManagerLocal {
    
    static Logger log = Logger.getLogger(DatasetManagerBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of DatasetManagerBean */
    public DatasetManagerBean() {}
    
    /**
     *
     * @param sessionId
     * @param datasetId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSet(userId, datasetId, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param datasetIds
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Dataset> getDatasets(String sessionId, Collection<Long> datasetIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSets(userId, datasetIds, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param datasetId
     * @param includes
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @return
     */
    @WebMethod(operationName="getDatasetIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludesResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataSet = DataSetManager.getDataSet(userId, datasetId, manager);
        //now set the investigation includes for JAXB web service
        dataSet.setDatasetInclude(includes);
        
        return dataSet;
    }
    
    /**
     *
     * @param sessionId
     * @param investigationId
     * @param dataSet
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     * @return
     */
    public Long createDataSet(String sessionId, Long investigationId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataset = DataSetManager.createDataSet(userId, dataSet, investigationId, manager);
        
        return dataset.getId();
    }
    
    /**
     *
     * @param sessionId
     * @param investigationId
     * @param dataSets
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.ValidationException
     * @return
     */
    public Collection<Long> createDataSets(String sessionId, Long investigationId, Collection<Dataset> dataSets) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Collection<Long> ids = new ArrayList<Long>();
        for(Dataset dataset : dataSets){
            Dataset datasetReturned = DataSetManager.createDataSet(userId, dataset, investigationId, manager);
            ids.add(datasetReturned.getId());
        }
        
        return ids;
    }
    
    /**
     *
     * @param sessionId
     * @param dataSetId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    public void removeDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.removeDataSet(sessionId, dataSetId, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param dataSetId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    public void deleteDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.deleteDataSet(sessionId, dataSetId, manager);
    }
    
    /**
     * 
     * @param sessionId 
     * @param dataSet 
     * @throws uk.icat3.exceptions.SessionException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     */
    public void modifyDataSet(String sessionId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.updateDataSet(sessionId, dataSet, manager);
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
    public void addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.addDataSetParameter(userId, dataSetParameter, datasetId, manager);
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
    public void modifyDataSetParameter(String sessionId, DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DataSetManager.updateDataSetParameter(userId, dataSetParameter, manager);
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
    public void removeDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatasetParameter datasetParameter = ManagerUtil.find(DatasetParameter.class, datasetParameterPK, manager);
        
        DataSetManager.removeDataSetParameter(userId, datasetParameter, manager);
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
    public void deleteDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        DatasetParameter datasetParameter = ManagerUtil.find(DatasetParameter.class, datasetParameterPK, manager);
        
        DataSetManager.deleteDataSetParameter(userId, datasetParameter, manager);
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
