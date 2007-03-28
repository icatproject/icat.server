/*
 * DatasetManagerBean.java
 *
 * Created on 26 March 2007, 15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

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
import uk.icat3.entity.Sample;
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
@WebService()
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatasetManagerBean extends EJBObject implements DatasetManagerLocal {
    
    static Logger log = Logger.getLogger(DatasetManagerBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of DatasetManagerBean */
    public DatasetManagerBean() {}
    
    
    @WebMethod
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getDataset")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getDatasetResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSet(userId, datasetId, manager);
    }
    
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
    
    @WebMethod
    public Long createDataSet(String sessionId, Long investigationId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataset = DataSetManager.createDataSet(userId, dataSet, investigationId, manager);
        
        return dataset.getId();
    }
    
    public void addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //check is valid
        dataSetParameter.isValid(manager);
        
        //get dataset, checks read access
        Dataset dataset = DataSetManager.getDataSet(userId, datasetId, manager);
        
        //add the dataset parameter to the dataset
        dataset.addDataSetParamaeter(dataSetParameter);
        
        //update this, this also checks permissions,  no need to validate cos just loaded from DB
        DataSetManager.updateDataSet(userId, dataset, manager, false);
    }
    
    public void setDataSetSample(String sessionId, Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
                
        DataSetManager.setDataSetSample(userId, sampleId, datasetId, manager);        
    }
}
