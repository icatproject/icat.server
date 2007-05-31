/*
 * InvestigationSearchBean.java
 *
 * Created on 26 March 2007, 15:42
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
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InvestigationSearchBean extends EJBObject implements InvestigationSearchLocal {
    
    static Logger log = Logger.getLogger(InvestigationSearchBean.class);
    private boolean SECURITY_ON = true;
    
    /** Creates a new instance of InvestigationSearchBean */
    public InvestigationSearchBean() {
    }
    
    /**
     * This searches all DB for investigations with all the keywords that the user can see
     *
     * @param sessionId session id of the user.
     * @param keywords Collection of keywords to search on
     * @param include Set of information to return with investigations, ie their keywords, investigators, datasets, default none.  Having more information returned means the query will take longer.
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName="searchByKeywordsFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsFuzzyResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, fuzzy, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod(operationName="searchByKeywords")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywords")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod(operationName="searchByKeywordsPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, InvestigationInclude.NONE, false, SECURITY_ON, startIndex, numberOfResults, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param include
     * @param fuzzy
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod(operationName="searchByKeywordsPaginationAndFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationAndFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationAndFuzzyResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, fuzzy, SECURITY_ON, startIndex, numberOfResults, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param include
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod(operationName="searchByKeywordsPaginationFuzzyAndInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationFuzzyAndInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include,  int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, false, SECURITY_ON, startIndex, numberOfResults, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param operator
     * @param include
     * @param fuzzy
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod(operationName="searchByKeywordsAll")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAllResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords, operator, include, fuzzy, SECURITY_ON, startIndex, numberOfResults, manager);
    }
    
    /**
     * Lists all the investigations for the current user
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod()
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param searchUserId Could be DN , username or federal ID
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod()
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param searchUserId Could be DN , username or federal ID
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName="searchByUserIDPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByUserIDPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByUserIDPaginationResponse")
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch, int startIndex, int number_results) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
    }
    
    /**
     *
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @param manager
     * @return
     */
    @WebMethod()
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserSurname(userId, surname, manager);
        
    }
    
    /**
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @param startIndex
     * @param number_results
     * @param manager
     * @return
     */
    @WebMethod(operationName="searchByUserSurnamePagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByUserSurnamePagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByUserSurnamePaginationResponse")
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname, int startIndex, int number_results) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserSurname(userId, surname, startIndex, number_results, manager);
        
    }
    
    /**
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod()
    public Collection<String> listAllInstruments(String sessionId) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllInstruments(userId, manager);
    }
    
}
