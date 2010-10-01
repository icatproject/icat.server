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
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
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
    public Collection<Dataset> searchDatasetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getDatasetsBySample(userId, sample, manager);
    }

    /**
     * Search the datasets that match the given name.
     * @param sessionId
     * @param datasetName
     * @return collection of datasets that match the name and have authorisation to access them.
     * @throws SessionException
     */
    @WebMethod()
    public Collection<Dataset> searchDatasetsByName(String sessionId, String datasetName) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        return DatasetSearch.getDatasetsByName(userId, datasetName, manager);
    }

    /**
     * Searches for the datasets that match the dataset parameter condition
     * @param sessionId
     * @param parameterOperable
     * @return collection of datasets that match the parameter condition and have authorisation to access them.
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod()
    public Collection<Dataset> searchDatasetsByParameterOperable (String sessionId, ParameterCondition parameterOperable) throws SessionException, ParameterSearchException {
        String userId = user.getUserIdFromSessionId(sessionId);
        return DatasetSearch.searchByParameterOperable(userId, parameterOperable, manager);
    }

    /**
     * Searches for the datasets that match the dataset parameter conditions
     * @param sessionId
     * @param parameterComparator
     * @return collection of datasets that match the parameter conditions and have authorisation to access them
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod()
    public Collection<Dataset> searchDatasetsByParameter(String sessionId, List<ParameterComparisonCondition> parameterComparator) throws SessionException, ParameterSearchException {
        String userId = user.getUserIdFromSessionId(sessionId);
        return DatasetSearch.searchByParameterListComparators(userId, parameterComparator, manager);
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
