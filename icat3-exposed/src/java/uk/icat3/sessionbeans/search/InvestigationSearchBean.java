/*
 * InvestigationSearchBean.java
 *
 * Created on 26 March 2007, 15:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordDetails;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
@Stateless()
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InvestigationSearchBean extends EJBObject implements InvestigationSearchLocal {
    
    static Logger log = Logger.getLogger(InvestigationSearchBean.class);
    private final boolean SECURITY_ON = true;
    
    /** Creates a new instance of InvestigationSearchBean */
    public InvestigationSearchBean() {
    }
    
    /**
     * This searches all DB for investigations with the advanced search criteria
     *
     * @param sessionId session id of the user.
     * @param advancedSearch advanced Search details to search with
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByAdvanced(String sessionId, AdvancedSearchDetails advancedSearch, int startIndex, int numberOfResults) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the API
        return InvestigationSearch.searchByAdvanced(userId, advancedSearch, startIndex, numberOfResults, manager);
    }
    
    /**
     * This searches all DB for investigations with the advanced search criteria
     *
     * @param sessionId session id of the user.
     * @param advancedSearch advanced Search details to search with
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByAdvanced(String sessionId, AdvancedSearchDetails advancedSearch) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the API
        return InvestigationSearch.searchByAdvanced(userId, advancedSearch, -1, -1, manager);
    }
    
    /**
     * This searches all DB for investigations with all the keywords that the user can see
     *
     * @param sessionId session id of the user.
     * @param keywords Collection of keywords to search on
     * @param include Set of information to return with investigations, ie their keywords, investigators, datasets, default none.  Having more information returned means the query will take longer.
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName="searchByKeywordsInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsIncludeResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
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
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    /*@WebMethod(operationName="searchByKeywordsPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, InvestigationInclude.NONE, false, SECURITY_ON, startIndex, numberOfResults, manager);
    }*/
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param include
     * @param fuzzy
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    /*@WebMethod(operationName="searchByKeywordsPaginationAndFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationAndFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationAndFuzzyResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, fuzzy, SECURITY_ON, startIndex, numberOfResults, manager);
    }*/
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param include
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    /*@WebMethod(operationName="searchByKeywordsPaginationFuzzyAndInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationFuzzyAndInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsPaginationFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include,  int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, false, SECURITY_ON, startIndex, numberOfResults, manager);
    }*/
    
    /**
     *
     * @param sessionId
     * @param keywords
     * @param operator
     * @param include
     * @param fuzzy   
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    /*@WebMethod(operationName="searchByKeywordsAll")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAllResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywords, operator, include, fuzzy, SECURITY_ON, startIndex, numberOfResults, manager);
    }*/
    
      /**
     *
     * @param sessionId
     * @param keywords
     * @param operator
     * @param include
     * @param fuzzy   
     * @param startIndex
     * @param numberOfResults
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod(operationName="searchByKeywordsAll")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.searchByKeywordsAllResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, KeywordDetails keywordDetails, int startIndex, int numberOfResults) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API, always use security
        return InvestigationSearch.searchByKeywords(userId, keywordDetails, startIndex, numberOfResults, manager);
    }
    
    /**
     * Lists all the investigations for the current user
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod()
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, manager);
    }
    
    /**
     * Lists all the investigations for the current user
     *
     * @param sessionId
     * @param include
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod(operationName="getMyInvestigationsInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.getMyInvestigationsInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.getMyInvestigationsIncludeResponse")
    public Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, include, manager);
    }
    
    /**
     * Lists all the investigations for the current user
     *
     * @param sessionId
     * @param include
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod(operationName="getMyInvestigationsIncludePagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.getMyInvestigationsIncludePagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.getMyInvestigationsIncludePaginationResponse")
    public Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include, int startIndex, int number_results) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, include, startIndex, number_results, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param userSearch Could be DN , username or federal ID
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
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
     * @param userSearch Could be DN , username or federal ID
     * @param startIndex
     * @param number_results
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
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
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
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
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
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
     *  Lists all the instruments in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of instruments
     */
    @WebMethod()
    public Collection<String> listInstruments(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllInstruments(manager);
    }
    
    /**
     *  Lists all the rols in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod()
    public Collection<IcatRole> listRoles(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllRoles(manager);
    }
    
    
    /**
     *  Lists all the inv types in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod()
    public Collection<String> listInvestigationTypes(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllInvestigationTypes(manager);
    }
    
    /**
     *  Lists all the parameters in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod()
    public Collection<Parameter> listParameters(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllParameters(manager);
    }

    @WebMethod()
    public Collection searchByParameterCondition (String sessionId, ParameterCondition parameterOperable) throws SessionException, ParameterSearchException, RestrictionException {
        String userId = user.getUserIdFromSessionId(sessionId);
        return InvestigationSearch.searchByParameterCondition(userId, parameterOperable, Queries.NO_RESTRICTION, InvestigationInclude.NONE, manager);
    }

    @WebMethod()
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition... listComparators) throws SessionException, ParameterSearchException, RestrictionException {
        String userId = user.getUserIdFromSessionId(sessionId);
        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : listComparators) {
            list.add(p);
        }
        return InvestigationSearch.searchByParameterComparisonList(userId, list, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, manager);
    }

    @Override
    public Collection searchByParameter(String sessionId, ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);
        
        return InvestigationSearch.searchByParameterList(userId, list, Queries.NO_RESTRICTION, InvestigationInclude.NONE, -1, -1, manager);
    }

    @Override
    public Collection searchByParameterCondition(String sessionId, ParameterCondition logicalCondition, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        return InvestigationSearch.searchByParameterCondition(userId, logicalCondition, restLogCond, InvestigationInclude.NONE, manager);
    }

    @Override
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : comparison) {
            list.add(p);
        }

        return InvestigationSearch.searchByParameterComparisonList(userId, list, restLogCond, InvestigationInclude.NONE, manager);
    }

    @Override
    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);

        return InvestigationSearch.searchByParameterList(userId, list, restLogCond, InvestigationInclude.NONE, manager);
    }

    @Override
    public Collection<Instrument> getAllInstruments(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getAllInstruments(manager);
    }

    @Override
    public Collection<Investigation> searchByRestriction(String sessionId, RestrictionCondition... restricion) throws SessionException, RestrictionException, DatevalueException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restricion)
            restLogCond.add(r);
        return InvestigationSearch.searchByRestriction(userId, restLogCond, manager);
    }

    @Override
    public Collection searchByRestriction(String sessionId, RestrictionCondition restricion) throws SessionException, RestrictionException, DatevalueException {
         //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        return InvestigationSearch.searchByRestriction(userId, restricion, InvestigationInclude.NONE, manager);
    }
}
