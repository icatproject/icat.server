/*
 * DatasetManagerBean.java
 *
 * Created on 26 March 2007, 15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DataSetManager;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatasetManagerBean extends EJBObject implements DatasetManagerLocal {
    
    static Logger log = Logger.getLogger(DatasetManagerBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of DatasetManagerBean */
    public DatasetManagerBean() {}
    
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataSetManager.getDataSet(userId, datasetId, manager);        
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Dataset dataSet = DataSetManager.getDataSet(userId, datasetId, manager);        
        //now set the investigation includes for JAXB web service
        dataSet.setDatasetInclude(includes);
        
        return dataSet;
    }    
}
