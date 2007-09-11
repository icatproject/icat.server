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
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatasetSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless
@PermitAll
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DatasetSearchBean extends EJBObject implements DatasetSearchLocal {
    
    /** Creates a new instance of DatasetSearchBean */
    public DatasetSearchBean() {
    }
    
    /**
     * From a sample name, return all the samples a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sampleName
     * @throws uk.icat3.exceptions.SessionException
     * @return collection
     */
    @WebMethod()
    public Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getSamplesBySampleName(userId, sampleName, manager);
    }
    
    /**
     * From a sample, return all the datafiles a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sample
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @return collection
     */
    @WebMethod()
    public Collection<Dataset> searchDataSetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getDatasetsBySample(userId, sample, manager);
    }
    
    /**
     *  List all the valid avaliable types for datasets
     *
     * @param sessionId
     * @return collection of types
     * @throws uk.icat3.exceptions.SessionException 
     */
    @WebMethod()
    public Collection<String> listDatasetTypes(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetTypes(manager);
    }
    
    /**
     * List all the valid avaliable status for datasets
     *
     * @param sessionId
     * @return collection of status
     */
    @WebMethod()
    public Collection<String> listDatasetStatus(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetStatus(manager);
    }
    
    
}
