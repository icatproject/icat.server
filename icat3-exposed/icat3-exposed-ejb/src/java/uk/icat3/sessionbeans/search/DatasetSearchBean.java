/*
 * DatasetSearchBean.java
 *
 * Created on 27 March 2007, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;


import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatasetSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless
@WebService()
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DatasetSearchBean extends EJBObject implements DatasetSearchLocal {
    
    /** Creates a new instance of DatasetSearchBean */
    public DatasetSearchBean() {
    }
    
    /**
     * From a sample name, return all the datasets a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sampleName
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    public Collection<Dataset> searchBySampleName(String sessionId, String sampleName) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getBySampleName(userId, sampleName, manager);
    }
    
    /**
     *  List all the valid avaliable types' for datasets
     *
     * @param sessionid
     * @return collection of types'
     */
    public Collection<DatasetType> listDatasetTypes(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetTypes(manager);
    }
    
    /**
     * List all the valid avaliable status' for datasets
     *
     * @param sessionid
     * @return collection of status'
     */
    public Collection<DatasetStatus> listDatasetStatus(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetStatus(manager);
    }
    
    
}
