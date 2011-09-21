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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.log4j.Logger;

import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DatasetManager;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@Stateless()
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
        
        return DatasetManager.getDataSet(userId, datasetId, manager);
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
        
        return DatasetManager.getDataSets(userId, datasetIds, manager);
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
        
        Dataset dataSet = DatasetManager.getDataSet(userId, datasetId, includes, manager);
                       
        return dataSet;
    }
    

   
    
}
