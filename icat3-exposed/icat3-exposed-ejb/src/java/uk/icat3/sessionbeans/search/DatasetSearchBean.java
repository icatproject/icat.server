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
import javax.persistence.EntityManager;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatasetSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless
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
    public Collection<Dataset> getBySampleName(String sessionId, String sampleName) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getBySampleName(userId, sampleName, manager);
    }
    
    
}
