/*
 * DatafileSearchBean.java
 *
 * Created on 27 March 2007, 10:36
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
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatafileSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DatafileSearchBean extends EJBObject implements DatafileSearchLocal {
    
    /** Creates a new instance of DatafileSearchBean */
    public DatafileSearchBean() {
    }
    
    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId federalId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @return collection of datafiles returned from search
     */
    @WebMethod()
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatafileSearch.searchByRunNumber(userId, instruments, startRun, endRun, manager);
    }
    
    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId sessionId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @return collection of datafiles returned from search
     */
    @WebMethod(operationName="searchByRunNumberPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByRunNumberPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByRunNumberPaginationResponse")
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatafileSearch.searchByRunNumber(userId, instruments, startRun, endRun, startIndex, number_results, manager);
    }
    
    /**
     *  List all the valid avaliable formats for datafiles
     *
     * @param sessionId
     * @return collection of types
     * @throws uk.icat3.exceptions.SessionException 
     */
    @WebMethod()
    public Collection<DatafileFormat> listDatafileFormats(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatafileSearch.listDatafileFormats(manager);
    }
}
