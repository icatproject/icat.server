/*
 * AllOperationsBean.java
 *
 * Created on 17 May 2007, 10:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.data.DownloadInfo;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.KeywordDetails;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.sessionbeans.data.DownloadManagerLocal;
import uk.icat3.sessionbeans.interceptor.*;
import uk.icat3.sessionbeans.manager.DatafileManagerLocal;
import uk.icat3.sessionbeans.manager.DatasetManagerLocal;
import uk.icat3.sessionbeans.manager.FacilityManagerLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerLocal;
import uk.icat3.sessionbeans.manager.XMLIngestionManagerLocal;
import uk.icat3.sessionbeans.search.DatafileSearchLocal;
import uk.icat3.sessionbeans.search.DatasetSearchLocal;
import uk.icat3.sessionbeans.search.InvestigationSearchLocal;
import uk.icat3.sessionbeans.search.KeywordSearchLocal;
import uk.icat3.sessionbeans.search.ParameterSearchLocal;
import uk.icat3.sessionbeans.search.SampleSearchLocal;
import uk.icat3.sessionbeans.util.Constants;
import uk.icat3.user.UserDetails;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 *
 * @author gjd37
 */
@Stateless
@WebService(serviceName = "ICATService", targetNamespace = "client.icat3.uk")
@Interceptors(value = ArgumentValidator.class)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class ICAT extends EJBObject implements ICATLocal {

    static Logger log = Logger.getLogger(ICAT.class);
    ///////////////////////  Inject all the EJBs   //////////////////////////
    @EJB
    protected DatafileManagerLocal datafileManagerLocal;
    @EJB
    protected DatasetManagerLocal datasetManagerLocal;
    @EJB
    protected InvestigationManagerLocal investigationManagerLocal;
    @EJB
    protected DatafileSearchLocal datafileSearchLocal;
    @EJB
    protected DatasetSearchLocal datasetSearchLocal;
    @EJB
    protected SampleSearchLocal sampleSearchLocal;
    @EJB
    protected InvestigationSearchLocal investigationSearchLocal;
    @EJB
    protected KeywordSearchLocal keywordSearchLocal;
    @EJB
    protected XMLIngestionManagerLocal xmlIngestionManagerLocal;
    @EJB
    protected DownloadManagerLocal downloadManagerLocal;
    @EJB
    protected FacilityManagerLocal facilityManagerLocal;
    @EJB
    protected ParameterSearchLocal parameterSearchLocal;
    ///////////////////////  End of Inject all the EJBs   ///////////////////////

    /** Creates a new instance of AllOperationsBean */
    public ICAT() {
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   Session  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////     UserSession methods  /////////////////////////////////////////
    /**
     * Logs in, defaults to 2 hours
     *
     * @param username
     * @param password
     * @return
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    @ExcludeClassInterceptors
    public String login(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws SessionException {
        return user.login(username, password);
    }

    /**
     * Logs in for a certain amount of time
     *
     * @param username
     * @param password
     * @param lifetime
     * @return
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "loginLifetime")
    @ExcludeClassInterceptors
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.loginLifetime")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.loginLifetimeResponse")
    public String login(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password,
            @WebParam(name = "lifetime") int lifetime) throws SessionException {
        return user.login(username, password, lifetime);

    }

    /**
     * Logs out
     *
     * @param sessionId
     * @return
     */
    @WebMethod
    @Interceptors(LogoutInterceptor.class)
    public boolean logout(
            @WebParam(name = "sessionId") String sessionId) {
        return user.logout(sessionId);
    }

    @WebMethod(operationName = "getUserDetails")
    public UserDetails getUserDetails(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "usersName") String usersName) throws SessionException, NoSuchUserException {
        return this.user.getUserDetails(sessionId, usersName);

    }

    @WebMethod()
    public boolean isSessionValid(
            @WebParam(name = "sessionId") String sessionId) {
        try {
            return this.user.isSessionValid(sessionId);
        } catch (Exception e) {
            return false;
        }
    }

    ///////////////////////////     End of UserSession methods  //////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Searches  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////     KeywordSearch methods  /////////////////////////////////////////
    /**
     *  This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId federalId of the user.
     * @return list of keywords
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    public Collection<String> getKeywordsForUser(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return keywordSearchLocal.getKeywordsForUser(sessionId);
    }

    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId federalId of the user.
     * @param startKeyword start keyword to search
     * @param numberReturned number of results found returned
     * @return list of keywords
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "getKeywordsForUserStartWithMax")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserStartWithMax")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserStartWithMaxResponse")
    public Collection<String> getKeywordsForUser(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "startKeyword") String startKeyword,
            @WebParam(name = "numberReturned") int numberReturned) throws SessionException {
        return keywordSearchLocal.getKeywordsForUser(sessionId, startKeyword, numberReturned);
    }

    /**
     * This gets all the keywords avaliable for that user that they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId federalId of the user.
     * @param numberReturned number of results found returned
     * @return list of keywords
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "getKeywordsForUserMax")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserMax")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserMaxResponse")
    public Collection<String> getKeywordsForUser(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "numberReturned") int numberReturned) throws SessionException {
        return keywordSearchLocal.getKeywordsForUser(sessionId, numberReturned);
    }

    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId session id of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @throws uk.icat3.exceptions.SessionException
     * @return list of keywords
     */
    @WebMethod(operationName = "getKeywordsForUserType")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserType")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserTypeResponse")
    public Collection<String> getKeywordsForUser(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keywordType") KeywordType type) throws SessionException {
        return keywordSearchLocal.getKeywordsForUser(sessionId, type);
    }

    /**
     * This gets all the unique keywords in the database
     *
     * Types,  ALPHA, ALPHA_NUMERIC only work with oracle DBs
     *
     * @param sessionId sessionId of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @return list of keywords
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    public Collection<String> getAllKeywords(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "type") KeywordType type) throws SessionException {
        return keywordSearchLocal.getAllKeywords(sessionId, type);
    }
    ///////////////////////////     End of KeywordSearch methods  /////////////////////////////////////////

    ///////////////////////////     Investigation Search methods  /////////////////////////////////////////
    /**
     * This searches all DB for investigations with the advanced search criteria
     *
     * @param sessionId session id of the user.
     * @param advancedSearch advanced Search details to search with
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod
    @Interceptors(AdvancedSearchInterceptor.class)
    public Collection<Investigation> searchByAdvanced(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "advancedSearchDetails") AdvancedSearchDetails advancedSearch) throws SessionException {
        return investigationSearchLocal.searchByAdvanced(sessionId, advancedSearch);
    }

    /**
     * This searches all DB for investigations with the advanced search criteria
     *
     * @param sessionId session id of the user.
     * @param advancedSearch advanced Search details to search with
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName = "searchByAdvancedPagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByAdvancedPagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByAdvancedPaginationResponse")
    @Interceptors(AdvancedSearchInterceptor.class)
    public Collection<Investigation> searchByAdvanced(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "advancedSearchDetails") AdvancedSearchDetails advancedSearch,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByAdvanced(sessionId, advancedSearch, startIndex, numberOfResults);
    }

    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param investigationIncludes {@link InvestigationInclude}
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    /*@WebMethod(operationName = "searchByKeywordsFuzzyAndInclude")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsFuzzyAndInclude")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(
    @WebParam(name = "sessionId") String sessionId,
    @WebParam(name = "keywords") Collection<String> keywords,
    @WebParam(name = "investigationInclude") InvestigationInclude investigationIncludes,
    @WebParam(name = "fuzzy") boolean fuzzy) throws SessionException {
    return investigationSearchLocal.searchByKeywords(sessionId, keywords, investigationIncludes, fuzzy);
    }*/
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator, fuzzy false, no includes
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "searchByKeywords")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywords")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsResponse")
    @Interceptors(KeywordsInterceptor.class)
    public Collection<Investigation> searchByKeywords(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keywords") Collection<String> keywords) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords);
    }

    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator, fuzzy false, no includes
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    /* @WebMethod(operationName = "searchByKeywordsPagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationResponse")
    public Collection<Investigation> searchByKeywords(
    @WebParam(name = "sessionId") String sessionId,
    @WebParam(name = "keywords") Collection<String> keywords,
    @WebParam(name = "startIndex") int startIndex,
    @WebParam(name = "numberOfResults") int numberOfResults) throws SessionException {
    return investigationSearchLocal.searchByKeywords(sessionId, keywords, startIndex, numberOfResults);
    }*/
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param investigationIncludes {@link InvestigationInclude}
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    /*  @WebMethod(operationName = "searchByKeywordsPaginationFuzzyAndInclude")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationFuzzyAndInclude")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(
    @WebParam(name = "sessionId") String sessionId,
    @WebParam(name = "keywords") Collection<String> keywords,
    @WebParam(name = "investigationInclude") InvestigationInclude investigationIncludes,
    @WebParam(name = "fuzzy") boolean fuzzy,
    @WebParam(name = "startIndex") int startIndex,
    @WebParam(name = "numberOfResults") int numberOfResults) throws SessionException {
    return investigationSearchLocal.searchByKeywords(sessionId, keywords, investigationIncludes, fuzzy, startIndex, numberOfResults);
    }*/
    /**
     * Search by a collection of keywords for investigations that user has access to view, searching by fuzzy is true, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param investigationIncludes {@link InvestigationInclude}
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    /*  @WebMethod(operationName = "searchByKeywordsPaginationInclude")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationInclude")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationIncludeResponse")
    public Collection<Investigation> searchByKeywords(
    @WebParam(name = "sessionId") String sessionId,
    @WebParam(name = "keywords") Collection<String> keywords,
    @WebParam(name = "investigationInclude") InvestigationInclude investigationIncludes,
    @WebParam(name = "startIndex") int startIndex,
    @WebParam(name = "numberOfResults") int numberOfResults) throws SessionException {
    return investigationSearchLocal.searchByKeywords(sessionId, keywords, investigationIncludes, startIndex, numberOfResults);
    }*/
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param sessionId sessionId of the user.
     * @param keywordsDetails details of keyword search
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "searchByKeywordsAll")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByKeywordsAllResponse")
    @Interceptors(KeywordsInterceptor.class)
    public Collection<Investigation> searchByKeywords(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keywordDetails") KeywordDetails details,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, details, startIndex, numberOfResults);
    }

    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod
    @Interceptors(ViewMyInvestigationsInterceptor.class)
    public Collection<Investigation> getMyInvestigations(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.getMyInvestigations(sessionId);
    }

    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @param investigationIncludes
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod(operationName = "getMyInvestigationsIncludes")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getMyInvestigationsIncludes")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getMyInvestigationsIncludesResponse")
    @Interceptors(ViewMyInvestigationsInterceptor.class)
    public Collection<Investigation> getMyInvestigations(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationInclude") InvestigationInclude investigationIncludes) throws SessionException {
        return investigationSearchLocal.getMyInvestigations(sessionId, investigationIncludes);
    }

    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @param investigationIncludes
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    @WebMethod(operationName = "getMyInvestigationsIncludesPagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getMyInvestigationsIncludesPagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getMyInvestigationsIncludesPaginationResponse")
    @Interceptors(ViewMyInvestigationsInterceptor.class)
    public Collection<Investigation> getMyInvestigations(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationInclude") InvestigationInclude investigationIncludes,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int number_results) throws SessionException {
        return investigationSearchLocal.getMyInvestigations(sessionId, investigationIncludes, startIndex, number_results);
    }

    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param userSearch Could be DN , username or federal ID
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod
    public Collection<Investigation> searchByUserID(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "userSearch") String userSearch) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch);
    }

    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param userSearch Could be DN , username or federal ID
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName = "searchByUserIDPagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByUserIDPagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByUserIDPaginationResponse")
    public Collection<Investigation> searchByUserID(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "userSearch") String userSearch,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch, startIndex, number_results);
    }

    /**
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of
     */
    @WebMethod
    public Collection<Investigation> searchByUserSurname(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "surname") String surname) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname);
    }

    /**
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of
     */
    @WebMethod(operationName = "searchByUserSurnamePagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByUserSurnamePagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByUserSurnamePaginationResponse")
    public Collection<Investigation> searchByUserSurname(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "surname") String surname,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname, startIndex, number_results);
    }

    /**
     *  Lists all the instruments in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of instruments
     */
    @WebMethod
    public Collection<String> listInstruments(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.listInstruments(sessionId);
    }

    /**
     *  Lists all the instruments in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of instruments
     */
    @WebMethod
    public Collection<Instrument> getAllInstruments(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.getAllInstruments(sessionId);
    }

    /**
     *  Lists all the rols in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod
    public Collection<IcatRole> listRoles(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.listRoles(sessionId);
    }

    /**
     *  Lists all the parameters in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod
    public Collection<Parameter> listParameters(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.listParameters(sessionId);
    }

    /**
     *  Lists all the FacilityCycles in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of FacilityCycles
     */
    @WebMethod
    public Collection<FacilityCycle> listFacilityCycles(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return facilityManagerLocal.listAllFacilityCycles(sessionId);

    }

    /**
     *  Lists all the inv types in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod
    public Collection<String> listInvestigationTypes(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.listInvestigationTypes(sessionId);
    }
    ///////////////////////////     End of Investigation Search methods  /////////////////////////////////////////

    ///////////////////////////     Dataset Search methods  /////////////////////////////////////////
    /**
     * From a sample name, return all the samples a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sampleName
     * @throws uk.icat3.exceptions.SessionException
     * @return collection of sample
     */
    @WebMethod
    @Interceptors(SampleSearchInterceptor.class)
    public Collection<Sample> searchSamplesBySampleName(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleName") String sampleName) throws SessionException {
        return datasetSearchLocal.searchSamplesBySampleName(sessionId, sampleName);
    }

    /**
     * From a sample, return all the datafiles a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sample
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @return collection of Data sets
     */
    @WebMethod
    @Interceptors(SampleSearchInterceptor.class)
    public Collection<Dataset> searchDatasetsBySample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sample") Sample sample) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return datasetSearchLocal.searchDataSetsBySample(sessionId, sample);
    }

    /**
     *  List all the valid avaliable types for datasets
     *
     * @param sessionId
     * @return collection of types
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    public Collection<String> listDatasetTypes(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetTypes(sessionId);
    }

    /**
     * List all the valid avaliable status for datasets
     *
     * @param sessionId
     * @return collection of status
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    public Collection<String> listDatasetStatus(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetStatus(sessionId);
    }
    ///////////////////////////     End of Dataset Search methods  /////////////////////////////////////////

    ///////////////////////////     Datafile Search methods  /////////////////////////////////////////
    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId sessionId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @return collection of datafiles returned from search
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    @Interceptors(RunNumberSearchInterceptor.class)
    public Collection<Datafile> searchByRunNumber(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "instruments") Collection<String> instruments,
            @WebParam(name = "startRun") float startRun,
            @WebParam(name = "endRun") float endRun) throws SessionException {
        return datafileSearchLocal.searchByRunNumber(sessionId, instruments, startRun, endRun);
    }

    /**
     * Searches database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId sessionId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @return collection of datafiles returned from search
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod(operationName = "searchByRunNumberPagination")
    @RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByRunNumberPagination")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.searchByRunNumberPaginationResponse")
    @Interceptors(RunNumberSearchInterceptor.class)
    public Collection<Datafile> searchByRunNumber(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "instruments") Collection<String> instruments,
            @WebParam(name = "startRun") float startRun,
            @WebParam(name = "endRun") float endRun,
            @WebParam(name = "startIndex") int startIndex,
            @WebParam(name = "numberOfResults") int number_results) throws SessionException {
        return datafileSearchLocal.searchByRunNumber(sessionId, instruments, startRun, endRun, startIndex, number_results);
    }

    /**
     *  List all the valid avaliable formats for datafiles
     *
     * @param sessionId
     * @return collection of types
     * @throws uk.icat3.exceptions.SessionException
     */
    @WebMethod
    public Collection<DatafileFormat> listDatafileFormats(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return datafileSearchLocal.listDatafileFormats(sessionId);
    }
    ///////////////////////////     End of Datafile Search methods  /////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Manager   ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////     Investigation Manager methods  /////////////////////////////////////////
    /**
     * Returns a {@link Investigation} investigation from a {@link Investigation} id
     * if the user has access to the investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigationId Id of investigations
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Investigation} object
     */
    @WebMethod
    @Interceptors(ViewInvestigationsInterceptor.class)
    public Investigation getInvestigation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId, investigationId);
    }

    /**
     * Returns a {@link Investigation} investigation from a {@link Investigation} id
     * if the user has access to the investigation.
     * Also gets extra information regarding the investigation.  See {@link InvestigationInclude}
     *
     * @param sessionId sessionid of the user.
     * @param investigationId Id of investigations
     * @param includes information that is needed to be returned with the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Investigation} object
     */
    @WebMethod(operationName = "getInvestigationIncludes")
    @RequestWrapper(className = "uk.icat3.sessionbeans.getInvestigationIncludes")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.getInvestigationIncludesResponse")
    @Interceptors(ViewInvestigationsInterceptor.class)
    public Investigation getInvestigation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationId") Long investigationId,
            @WebParam(name = "investigationInclude") InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId, investigationId, includes);
    }

    /**
     * Returns a list of {@link Investigation} investigations from a list of {@link Investigation} investigation ids
     * if the user has access to the investigations.
     *
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    @Interceptors(ViewInvestigationsInterceptor.class)
    public Collection<Investigation> getInvestigations(@WebParam(name = "userId") String userId,
            @WebParam(name = "investigationIds") Collection<Long> investigationIds) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigations(userId, investigationIds, InvestigationInclude.NONE);
    }

    /**
     * Returns a list of {@link Investigation} investigations from a list of {@link Investigation} investigation ids
     * if the user has access to the investigations.
     * Also gets extra information regarding the investigation.  See {@link InvestigationInclude}
     *
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @param includes information that is needed to be returned with the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod(operationName = "getInvestigationsIncludes")
    @RequestWrapper(className = "uk.icat3.sessionbeans.getInvestigationsIncludes")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.getInvestigationsIncludesResponse")
    @Interceptors(ViewInvestigationsInterceptor.class)
    public Collection<Investigation> getInvestigations(@WebParam(name = "userId") String userId,
            @WebParam(name = "investigationIds") Collection<Long> investigationIds,
            @WebParam(name = "investigationInclude") InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigations(userId, investigationIds, includes);
    }

    /**
     * Creates a {@link Investigation} investigation from a {@link Investigation} object.
     *
     * @param sessionId sessionid of the user.
     * @param investigation object to be created
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @return {@link Investigation} object created
     */
    @WebMethod
    public Investigation createInvestigation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigation") Investigation investigation) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return investigationManagerLocal.createInvestigation(sessionId, investigation);
    }

    /**
     * Deletes a {@link Investigation} investigation from a {@link Investigation} object
     * if the user has access to delete the investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigationId id of investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteInvestigation(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deleteInvestigation(sessionId, investigationId);
    }

    /**
     * Removes a {@link Investigation} investigation from a {@link Investigation} object.
     * if the user has access to remove the investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigationId id of investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeInvestigation(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removeInvestigation(sessionId, investigationId);
    }

    /**
     * Adds keyword to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keyword {@link Keyword} object to be updated
     * @param investigationId id of the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return keyword
     */
    @WebMethod
    public Keyword addKeyword(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keyword") Keyword keyword,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.addKeyword(sessionId, keyword, investigationId);
    }

    /**
     * Adds investigator to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigator {@link Investigator} object to be updated
     * @param investigationId id of the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return investigator
     */
    @WebMethod
    public Investigator addInvestigator(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigator") Investigator investigator,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.addInvestigator(sessionId, investigator, investigationId);
    }

    /**
     * Adds a sample to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sample {@link Sample} object to be updated
     * @param investigationId id of the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return sample
     */
    @WebMethod
    public Sample addSample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sample") Sample sample,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.addSample(sessionId, sample, investigationId);
    }

    /**
     * Adds publication to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publication {@link Publication} object to be updated
     * @param investigationId id of the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return publication that was added
     */
    @WebMethod
    public Publication addPublication(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "publication") Publication publication,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.addPublication(sessionId, publication, investigationId);
    }

    /**
     * Adds a sample parameter to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameter {@link SampleParameter} object to be updated
     * @param investigationId id of the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return sampleparameter that was added
     */
    @WebMethod
    public SampleParameter addSampleParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleParameter") SampleParameter sampleParameter,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.addSampleParameter(sessionId, sampleParameter, investigationId);
    }

    /**
     * Deletes/Undeletes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteInvestigator(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigatorPK") InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deleteInvestigator(sessionId, investigatorPK);
    }

    /**
     * Deletes/Undeletess/Undeletes the keyword from investigation, depending on whether the user has permission to delete this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteKeyword(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keywordPK") KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deleteKeyword(sessionId, keywordPK);
    }

    /**
     * Deletes/Undeletes the publication from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publicationId ID of object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deletePublication(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "publicationId") Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deletePublication(sessionId, publicationId);
    }

    /**
     * Deletes/Undeletes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteSample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleId") Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deleteSample(sessionId, sampleId);
    }

    /**
     * Removes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteSampleParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleParameterPK") SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.deleteSampleParameter(sessionId, sampleParameterPK);
    }

    /**
     * Modifies the investigation, depending on whether the user has permission to update this Investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigation {@link Investigation} object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyInvestigation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigaion") Investigation investigation) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.modifyInvestigation(sessionId, investigation);
    }

    /**
     * Modifies the investigator of the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigator {@link Investigator} object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyInvestigator(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigator") Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.modifyInvestigator(sessionId, investigator);
    }

    /**
     * Modifies the sample from the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sample {@link Sample} object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifySample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sample") Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.modifySample(sessionId, sample);
    }

    /**
     * Modifies the publication of the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publication {@link Publication} object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyPublication(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "publication") Publication publication) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.modifyPublication(sessionId, publication);
    }

    /**
     * Modifies the sample parameter from the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameter {@link SampleParameter} object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifySampleParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleParameter") SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.modifySampleParameter(sessionId, sampleParameter);
    }

    /**
     * Removes the keyword from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeKeyword(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "keywordPK") KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removeKeyword(sessionId, keywordPK);
    }

    /**
     * Removes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeInvestigator(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "investigatorPK") InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removeInvestigator(sessionId, investigatorPK);
    }

    /**
     * Removes the publication from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publicationId id of object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removePublication(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "publicationId") Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removePublication(sessionId, publicationId);
    }

    /**
     * Removes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeSample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleId") Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removeSample(sessionId, sampleId);
    }

    /**
     * Deletes/Undeletes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeSampleParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleParameterPK") SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        investigationManagerLocal.removeSampleParameter(sessionId, sampleParameterPK);
    }
    ///////////////////////////     End of Investigation Manager methods  /////////////////////////////////////////

    ///////////////////////////     Dataset Manager methods  /////////////////////////////////////////
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
    @Interceptors(ViewDatasetsInterceptor.class)
    public Dataset getDataset(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return datasetManagerLocal.getDataset(sessionId, datasetId);
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
    @WebMethod(operationName = "getDatasetIncludes")
    @RequestWrapper(className = "uk.icat3.sessionbeans.manager.getDatasetIncludes")
    @ResponseWrapper(className = "uk.icat3.sessionbeans.manager.getDatasetIncludesResponse")
    @Interceptors(ViewDatasetsInterceptor.class)
    public Dataset getDataset(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetId") Long datasetId,
            @WebParam(name = "datasetInclude") DatasetInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return datasetManagerLocal.getDataset(sessionId, datasetId, includes);
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
    @Interceptors(ViewDatasetsInterceptor.class)
    public Collection<Dataset> getDatasets(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetIds") Collection<Long> datasetIds) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return datasetManagerLocal.getDatasets(sessionId, datasetIds);
    }

    /**
     * Creates a data set, depending if the user has create permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be created
     * @param investigationId id of investigations to added the dataset to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Dataset} that was created
     */
    @WebMethod
    public Dataset createDataSet(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSet") Dataset dataSet,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.createDataSet(sessionId, dataSet, investigationId);
    }

    /**
     * Creates a collection of data sets, depending if the user has update permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSets collection of the datasets
     * @param investigationId id of investigations to added the datasets to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Dataset}s that were created
     */
    @WebMethod
    public Collection<Dataset> createDataSets(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSets") Collection<Dataset> dataSets,
            @WebParam(name = "investigationId") Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.createDataSets(sessionId, dataSets, investigationId);
    }

    /**
     * Deletes/Undeletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID. Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSet(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSetId") Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.deleteDataSet(sessionId, dataSetId);
    }

    /**
     * Deletes/Undeletes the data set paramter, depending if the users has access to remove the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSetParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetParameterPK") DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.deleteDataSetParameter(sessionId, datasetParameterPK);
    }

    /**
     * Updates a data set depending on whether the user has permission to update this data set or its investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataSet(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSet") Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.modifyDataSet(sessionId, dataSet);
    }

    /**
     * Modifies a data set paramter, depending if the users has access to update the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataSetParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSetParameter") DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.modifyDataSetParameter(sessionId, dataSetParameter);
    }

    /**
     * Sets the dataset sample id, depending if the users has access to update the data set
     *
     * @param sessionId session id of the user.
     * @param sampleId Id of sample
     * @param datasetId Id of dataset
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void setDataSetSample(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "sampleId") Long sampleId,
            @WebParam(name = "datafileId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.setDataSetSample(sessionId, sampleId, datasetId);
    }

    /**
     * Adds a data set paramter to a dataset, depending if the users has access to create the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @param datasetId id of dataset to add to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return DatasetParameter that was added
     */
    @WebMethod
    public DatasetParameter addDataSetParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSetParameter") DatasetParameter dataSetParameter,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.addDataSetParameter(sessionId, dataSetParameter, datasetId);
    }

    /**
     * Adds a collection of data set paramters to a dataset, depending if the users has access to create the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameters objecst to be created
     * @param datasetId id of dataset to add to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return DatasetParameter that was added
     */
    @WebMethod
    public Collection<DatasetParameter> addDataSetParameters(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSetParameters") Collection<DatasetParameter> dataSetParameters,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.addDataSetParameters(sessionId, dataSetParameters, datasetId);
    }

    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSet(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataSetId") Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSet(sessionId, dataSetId);
    }

    /**
     * Removes the data set paramter, depending if the users has access to delete the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSetParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetParameterPK") DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSetParameter(sessionId, datasetParameterPK);
    }
    ///////////////////////////     End of Dataset Manager methods  /////////////////////////////////////////

    ///////////////////////////     Datafile Manager methods  /////////////////////////////////////////
    /**
     * Gets a data file object from a data file id, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileId Id of data file
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link Datafile}
     */
    @WebMethod
    @Interceptors(ViewDatafilesInterceptor.class)
    public Datafile getDatafile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return datafileManagerLocal.getDatafile(sessionId, datafileId);
    }

    /**
     * Gets a collection of data file object from a collection of data file ids, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileIds collection of data file ids
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Datafile} objects
     */
    @WebMethod
    @Interceptors(ViewDatafilesInterceptor.class)
    public Collection<Datafile> getDatafiles(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileIds") Collection<Long> datafileIds) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return datafileManagerLocal.getDatafiles(sessionId, datafileIds);
    }

    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be created
     * @param datasetId Id of data set
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return the created {@link Datafile} primary key
     */
    @WebMethod
    public Datafile createDataFile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFile") Datafile dataFile,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datafileManagerLocal.createDataFile(sessionId, dataFile, datasetId);
    }

    /**
     * Creates a collection of data files, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFiles collection of objects to be created
     * @param datasetId Id of data set
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return the collection of created {@link Datafile} primary keys
     */
    @WebMethod
    public Collection<Datafile> createDataFiles(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFiles") Collection<Datafile> dataFiles,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datafileManagerLocal.createDataFiles(sessionId, dataFiles, datasetId);
    }

    /**
     * Deletes/Undeletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param datafileId id to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataFile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.deleteDataFile(sessionId, datafileId);
    }

    /**
     * Updates data file depending on whether the user has permission to update this data file.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataFile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFile") Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datafileManagerLocal.modifyDataFile(sessionId, dataFile);
    }

    /**
     * Adds a data file paramter object to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatafileParameter} created
     */
    @WebMethod
    public DatafileParameter addDataFileParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFileParameter") DatafileParameter dataFileParameter,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datafileManagerLocal.addDataFileParameter(sessionId, dataFileParameter, datafileId);
    }

    /**
     * Adds a collection data file paramter objects to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameters objects to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return {@link DatafileParameter} created
     */
    @WebMethod
    public Collection<DatafileParameter> addDataFileParameters(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFileParameters") Collection<DatafileParameter> dataFileParameters,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datafileManagerLocal.addDataFileParameters(sessionId, dataFileParameters, datafileId);
    }

    /**
     * Updates the data file paramter object, depending if the user has access to update the data file parameter.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void modifyDataFileParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "dataFileParameter") DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datafileManagerLocal.modifyDataFileParameter(sessionId, dataFileParameter);
    }

    /**
     * Deletes/Undeletes a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataFileParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileParameterPK") DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.deleteDataFileParameter(sessionId, datafileParameterPK);
    }

    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param datafileId id be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataFile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFile(sessionId, datafileId);
    }

    /**
     * Removes (from the database) a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataFileParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileParameterPK") DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFileParameter(sessionId, datafileParameterPK);
    }
    ///////////////////////////     End of Datafile Manager methods  /////////////////////////////////////////

    ////////////////////////////////////   Authorisation Section methods //////////////////////////////////////////////
    /**
     * Gets all the IcatAuthorisations for a dataFile id of all of the users
     *
     * @param sessionId session id of the user.
     * @param elementId elementId of the authorisations (ie either Investigation, Dataset, Datafile
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid     *
     * @return Collection of {@link IcatAuthorisation}>s of the datafile id
     */
    @WebMethod
    public Collection<IcatAuthorisation> getAuthorisations(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "elementId") Long elementId,
            @WebParam(name = "elementType") ElementType elementType) throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException {

        if (elementType == ElementType.INVESTIGATION) {
            return investigationManagerLocal.getAuthorisations(sessionId, elementId);
        } else if (elementType == ElementType.DATASET) {
            return datasetManagerLocal.getAuthorisations(sessionId, elementId);
        } else if (elementType == ElementType.DATAFILE) {
            return datafileManagerLocal.getAuthorisations(sessionId, elementId);
        } else {
            throw new SessionException("ElementType " + elementType + " not supported. Only INVESTIGATION, DATASET and DATAFILE ElementTypes supported.");
        }
    }

    /**
     * Adds a role to a datafile Id for a user (fedId) depending weather user  from session id has permission to do it
     *
     * @param sessionId session id of the user.
     * @param toAddUserId federal Id of user to add
     * @param toAddRole new role for federal Id
     * @param elementId elementId Id of the item to add the role to
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding
     * @return {@link IcatAuthorisation}s of the datafile id
     */
    @WebMethod
    public IcatAuthorisation addAuthorisation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "toAddFedId") String toAddUserId,
            @WebParam(name = "toAddRole") String toAddRole,
            @WebParam(name = "elementId") Long elementId,
            @WebParam(name = "elementType") ElementType elementType) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException {
        if (elementType == ElementType.INVESTIGATION) {
            return investigationManagerLocal.addAuthorisation(sessionId, toAddUserId, toAddRole, elementId);
        } else if (elementType == ElementType.DATASET) {
            return datasetManagerLocal.addAuthorisation(sessionId, toAddUserId, toAddRole, elementId);
        } /*else if (elementType == ElementType.DATAFILE) {
        return datafileManagerLocal.addAuthorisation(sessionId, toAddUserId, toAddRole, elementId);
        }*/ else {
            throw new SessionException("ElementType " + elementType + " not supported"); //should never be thrown //changed now, thrown for datafile
        }
    }

    /**
     * Deletes/Undeletes a IcatAuthorisation
     *
     * @param sessionId session id of the user.
     * @param authorisationId id of the authorisation to delete
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteAuthorisation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "authorisationId") Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException {
        investigationManagerLocal.deleteAuthorisation(sessionId, authorisationId);
    }

    /**
     * Removes a IcatAuthorisation
     *
     * @param sessionId session id of the user.
     * @param authorisationId id of the authorisation to remove
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeAuthorisation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "authorisationId") Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException {
        investigationManagerLocal.removeAuthorisation(sessionId, authorisationId);
    }

    /**
     * Changes a IcatAuthorisation role for a authorisation id
     *
     * @param sessionId session id of the user.
     * @param toChangetoRole role to change to
     * @param authorisationId id of the authorisation to remove
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding
     */
    @WebMethod
    public void updateAuthorisation(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "toChangetoRole") String toChangetoRole,
            @WebParam(name = "authorisationId") Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException {
        investigationManagerLocal.updateAuthorisation(sessionId, toChangetoRole, authorisationId);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////     XML Ingest      //////////////////////////////////////////////////////////////
    @WebMethod
    public Long[] ingestMetadata(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "xml") String xml) throws SessionException, ValidationException, InsufficientPrivilegesException, ICATAPIException {
        return xmlIngestionManagerLocal.ingestMetadata(sessionId, xml);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////     Data download        ///////////////////////////////////////////////////////////
    /**
     * Downloads a datafile
     *
     * @param sessionId session id of the user.
     * @param datafileId datafile id that is to be downloaded
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object                 
     * @return Url of the file
     */
    @WebMethod
    @Interceptors(DownloadInterceptor.class)
    public
    @WebResult(name = "URL")
    String downloadDatafile(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileId") Long datafileId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return downloadManagerLocal.downloadDatafile(sessionId, datafileId);
    }

    /**
     * Downloads a dataset
     *
     * @param sessionId session id of the user.
     * @param datasetId dataset id that is to be downloaded
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object               
     * @return Url of the zipped dataset
     */
    @WebMethod
    @Interceptors(DownloadInterceptor.class)
    public
    @WebResult(name = "URL")
    String downloadDataset(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return downloadManagerLocal.downloadDataset(sessionId, datasetId);
    }

    /**
     * Downloads a collection of datafiles
     *
     * @param sessionId session id of the user.
     * @param datafileIds Collection of the datafile ids that are to be downloaded
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object               
     * @return Url of the zipped datafiles
     */
    @WebMethod
    @Interceptors(DownloadInterceptor.class)
    public
    @WebResult(name = "URL")
    String downloadDatafiles(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileIds") Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return downloadManagerLocal.downloadDatafiles(sessionId, datafileIds);
    }

    /**
     * Checks if user has access to download the files.  This will be called from the 
     * data service to check that the request coming in is valid with ICAT
     *      
     * @param sessionId session id of the user.
     * @param datafileIds ids of the files that are to be downloaded 
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object             
     * @return DownloadInfo downloadinfo 
     */
    @WebMethod
    public
    @WebResult(name = "downloadInfo")
    DownloadInfo checkDatafileDownloadAccess(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datafileIds") Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return downloadManagerLocal.checkDatafileDownloadAccess(sessionId, datafileIds);
    }

    /**
     * Checks if user has access to download the dataset.  This will be called from the 
     * data service to check that the request coming in is valid with ICAT
     *      
     * @param sessionId session id of the user.
     * @param datasetId id of the dataset that are to be downloaded 
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object             
     * @return DownloadInfo downloadinfo 
     */
    @WebMethod
    public
    @WebResult(name = "downloadInfo")
    DownloadInfo checkDatasetDownloadAccess(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "datasetId") Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        return downloadManagerLocal.checkDatasetDownloadAccess(sessionId, datasetId);
    }

    /**
     * Returns the current version of the ICAT API in use
     * @param sessionId session id of the user.
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return String the Current ICAT API Version (manually updated)
     */
    @WebMethod(operationName = "getICATAPIVersion")
    public String getICATAPIVersion(@WebParam(name = "sessionId") String sessionId) throws SessionException, InsufficientPrivilegesException {
        return Constants.CURRENT_ICAT_API_VERSION;
    }

    /**
     * Returns the FacilityUser for the given userId
     * @param sessionId sessionId of the user
     * @param facilityUserId the id of the user to retrieve
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return FacilityUser the FacilityUser requested
     */
    @WebMethod(operationName = "getFacilityUserByFacilityUserId")
    public FacilityUser getFacilityUserByFacilityUserId(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "facilityUserId") String facilityUserId) throws SessionException {
        return facilityManagerLocal.getFacilityUserByFacilityUserId(sessionId, facilityUserId);
    }

    /**
     * Returns the FacilityUser for the given userId
     * @param sessionId sessionId of the user
     * @param facilityUserId the id of the user to retrieve
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return FacilityUser the FacilityUser requested
     */
    @WebMethod(operationName = "getFacilityUserByFederalId")
    public FacilityUser getFacilityUserByFederalId(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "federalId") String federalId) throws SessionException, NoSuchObjectFoundException {
        return facilityManagerLocal.getFacilityUserByFederalId(sessionId, federalId);
    }

    /**
     * Return the investigation matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logical condition
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchInvestigationByParameterCondition(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "logicalCondition") ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {

        return investigationSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }
    ///////////////////////////////////////////////////////////////////////
    //            PARAMETER CONDITION METHODS                 //
    ///////////////////////////////////////////////////////////////////////

    /**
     * Return datafiles matched by a logical condition.
     * 
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of datafiles
     * 
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatafileByParameterCondition(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return datafileSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Return datasets matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatasetByParameterCondition(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return datasetSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Return samples matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of samples
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchSampleByParameterCondition(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return sampleSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }
    ///////////////////////////////////////////////////////////////////////
    //            PARAMETER COMPARISON CONDITION METHODS                 //
    ///////////////////////////////////////////////////////////////////////

    /**
     * Return the investigation matched by a comparison(s)
     *
     * @param sessionId Session identification
     * @param comparisionCondition Comparison
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchInvestigationByParameterComparison(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "comparisionCondition") ParameterComparisonCondition... comparisionCondition) throws SessionException, ParameterSearchException, RestrictionException {

        return investigationSearchLocal.searchByParameterComparison(sessionId, comparisionCondition);
    }

    /**
     * Return datafiles matched by comparison(s).
     *
     * @param sessionId Session identification
     * @param comparison Comparison
     * @return Collection of datafiles
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatafileByParameterComparison(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "comparison") ParameterComparisonCondition... comparison) throws SessionException, ParameterSearchException, RestrictionException {

        return datafileSearchLocal.searchByParameterComparison(sessionId, comparison);
    }

    /**
     * Return datasets matched by comparison(s).
     *
     * @param sessionId Session identification
     * @param comparison Comparison
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatasetByParameterComparison(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "comparison") ParameterComparisonCondition... comparison) throws SessionException, ParameterSearchException, RestrictionException {

        return datasetSearchLocal.searchByParameterComparison(sessionId, comparison);
    }

    /**
     * Return samples matched by comparison(s).
     *
     * @param sessionId Session identification
     * @param comparison Comparison
     * @return Collection of samples
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchSampleByParameterComparison(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "comparison") ParameterComparisonCondition... comparison) throws SessionException, ParameterSearchException, RestrictionException {

        return sampleSearchLocal.searchByParameterComparison(sessionId, comparison);
    }

    ///////////////////////////////////////////////////////////////////////
    //            PARAMETER SEARCH METHODS                               //
    ///////////////////////////////////////////////////////////////////////
    /**
     * Return the investigation matched by a parameter(s)
     *
     * @param sessionId Session identification
     * @param parameterSearch Parameter
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchInvestigationByParameter(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "parameters") ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {
        return investigationSearchLocal.searchByParameter(sessionId, parameters);
    }

    /**
     * Return datafiles matched by parameter(s).
     *
     * @param sessionId Session identification
     * @param parameters Parameters
     * @return Collection of datafiles
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatafileByParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameters") ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {

        return datafileSearchLocal.searchByParameter(sessionId, parameters);
    }

    /**
     * Return datasets matched by parameter(s).
     *
     * @param sessionId Session identification
     * @param parameters Parameters
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatasetByParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameters") ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {

        return datasetSearchLocal.searchByParameter(sessionId, parameters);
    }

    /**
     * Return samples matched by parameter(s).
     *
     * @param sessionId Session identification
     * @param parameters Parameters
     * @return Collection of samples
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchSampleByParameter(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameters") ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {

        return sampleSearchLocal.searchByParameter(sessionId, parameters);
    }

    ///////////////////////////////////////////////////////////////////////
    //            GET PARAMETER METHODS                                  //
    ///////////////////////////////////////////////////////////////////////
    /**
     * Returns parameters matched by name and units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param sessionId Session identification
     * @param name Parameter name
     * @param units Parameter units
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name and units
     *
     * @throws SessionException
     */
    @WebMethod
    public Collection<Parameter> getParameterByNameUnits(@WebParam(name = "sesssionId") String sesssionId, @WebParam(name = "name") String name, @WebParam(name = "units") String units) throws SessionException {
        return parameterSearchLocal.getParameterByNameUnits(sesssionId, name, units);
    }

    /**
     * Returns parameters matched by name. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param sessionId Session identification
     * @param name Parameter name
     * @return Paremeter collection matched by name
     * @throws SessionException
     */
    @WebMethod
    public Collection<Parameter> getParameterByName(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "name") String name) throws SessionException {
        return parameterSearchLocal.getParameterByName(sessionId, name);
    }

    /**
     * Returns parameters matched by Restriction. 
     *
     * This method aims to add extra options to getParameter method. With RestrictionCondition is
     * possible to make an insensitive search or set a maximun of results. The
     * Restrictions Attributes uses for this methods are PARAMETER_NAME and
     * PARAMETER_UNITS. Othes attributes are forbidden, due to there is no relation
     * between PARAMETER and other objects. If user wants to make a search using parameters,
     * ParameterCondition methods mut be used.
     *
     * @param sessionId Session identification
     * @param condition Restriction condition
     * @return Paremeter collection matched by condition
     * @throws SessionException
     */
    @WebMethod
    public Collection getParameterByRestriction(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "condition") RestrictionCondition condition) throws SessionException, RestrictionException {
        return parameterSearchLocal.getParameterByRestriction(sessionId, condition);
    }

    /**
     * Returns parameters matched by units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%units%' behavior).
     *
     * @param sessionId Session identification
     * @param units Parameter units
     * @return Paremeter collection matched by units
     * @throws SessionException
     */
    @WebMethod(operationName = "getParameterByUnits")
    public Collection<Parameter> getParameterByUnits(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "units") String units) throws SessionException {
        return parameterSearchLocal.getParameterByUnits(sessionId, units);
    }

    ///////////////////////////////////////////////////////////////////////
    //            RESTRICTION and PARAMETER CONDITION METHODS            //
    ///////////////////////////////////////////////////////////////////////
    @WebMethod
    public Collection searchDatasetByParameterRestriction(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameterCondition") ParameterCondition parameterCondition, @WebParam(name = "restrictions") RestrictionCondition restrictions) throws SessionException, ParameterSearchException, RestrictionException {
        return datasetSearchLocal.searchByParameterCondition(sessionId, parameterCondition, restrictions);
    }

    @WebMethod
    public Collection searchSampleByParameterRestriction(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameterCondition") ParameterCondition parameterCondition, @WebParam(name = "restrictions") RestrictionCondition restrictions) throws SessionException, ParameterSearchException, RestrictionException {
        return sampleSearchLocal.searchByParameterCondition(sessionId, parameterCondition, restrictions);
    }

    @WebMethod
    public Collection searchDatafileByParameterRestriction(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameterCondition") ParameterCondition parameterCondition, @WebParam(name = "restrictions") RestrictionCondition restrictions) throws SessionException, ParameterSearchException, RestrictionException {
        return datafileSearchLocal.searchByParameterCondition(sessionId, parameterCondition, restrictions);
    }

    @WebMethod
    public Collection searchInvestigationByParameterRestriction(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "parameterCondition") ParameterCondition parameterCondition, @WebParam(name = "restriction") RestrictionCondition restrictions) throws SessionException, ParameterSearchException, RestrictionException {
        return investigationSearchLocal.searchByParameterCondition(sessionId, parameterCondition, restrictions);
    }

    ///////////////////////////////////////////////////////////////////////
    //            RESTRICTION CONDITION METHODS                          //
    ///////////////////////////////////////////////////////////////////////
    @WebMethod
    public Collection searchInvestigationByRestriction(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return investigationSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchDatasetByRestriction(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return datasetSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchDatafileByRestriction(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return datafileSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection<Sample> searchSampleByRestriction(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return sampleSearchLocal.searchByRestriction(sessionId, restriction);
    }

    ///////////////////////////////////////////////////////////////////////
    //            RESTRICTION COMPARISON CONDITION METHODS               //
    ///////////////////////////////////////////////////////////////////////
    @WebMethod
    public Collection searchInvestigationByRestrictionComparasion(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionComparisonCondition... restriction) throws SessionException, RestrictionException, DatevalueException {
        return investigationSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchDatasetByRestrictionComparison(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionComparisonCondition... restriction) throws SessionException, RestrictionException, DatevalueException {
        return datasetSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchDatafileByRestrictionComparison(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionComparisonCondition... restriction) throws SessionException, RestrictionException, DatevalueException {
        return datafileSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchSampleByRestrictionComparison(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionComparisonCondition... restriction) throws SessionException, RestrictionException, DatevalueException {
        return sampleSearchLocal.searchByRestriction(sessionId, restriction);
    }

    ///////////////////////////////////////////////////////////////////////
    //            RESTRICTION LOGICAL CONDITION METHODS                  //
    ///////////////////////////////////////////////////////////////////////
    @WebMethod
    public Collection searchSampleByRestrictionLogical(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionLogicalCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return sampleSearchLocal.searchByRestriction(sessionId, restriction);
    }

    @WebMethod
    public Collection searchDatasetByRestrictionLogical(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionLogicalCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return datasetSearchLocal.searchByRestriction(sessionId, restriction);
    }

    /**
     * Returns Investigations matched by a restriction logical condition.
     * 
     * @param sessionId Session identification
     * @param restriction Restriction condition
     * @return Investigations matcheb by restriciton condition
     * @throws SessionException
     * @throws RestrictionException
     * @throws DatevalueException
     */
    @WebMethod
    public Collection searchInvestigationByRestrictionLogical(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionLogicalCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return investigationSearchLocal.searchByRestriction(sessionId, restriction);
    }

    /**
     * Returns datafile matched by a restriction logical condition
     *
     * @param sessionId Session identification
     * @param restriction Restriction condition
     * @return Datafiles mathed by restriction condition
     * 
     * @throws SessionException
     * @throws RestrictionException
     * @throws DatevalueException
     */
    @WebMethod
    public Collection searchDatafileByRestrictionLogical(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionLogicalCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return datafileSearchLocal.searchByRestriction(sessionId, restriction);
    }

    ///////////////////////////////////////////////////////////////////////
    //            PARAMETER LOGICAL CONDITION METHODS                    //
    ///////////////////////////////////////////////////////////////////////
    /**
     * Return the investigation matched by a parameter logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logical condition
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchInvestigationByParameterLogical(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "logicalCondition") ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {

        return investigationSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Return datafiles matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of datafiles
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatafileByParameterLogical(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return datafileSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Return datasets matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchDatasetByParameterLogical(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return datasetSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Return samples matched by a logical condition.
     * 
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * 
     * @return Collection of samples
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    @WebMethod
    public Collection searchSampleByParameterLogical(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "logicalCondition") ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        return sampleSearchLocal.searchByParameterCondition(sessionId, logicalCondition);
    }

    /**
     * Searchs for facility users which match with restriction contidion.
     *
     * @param sessionId Session identification
     * @param restriction Restriction condition
     * 
     * @return Collection of Facility Users
     *
     * @throws SessionException
     * @throws RestrictionException
     * @throws DatevalueException
     */
    @WebMethod
    public Collection<FacilityUser> searchFacilityUserByRestriction(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "restriction") RestrictionCondition restriction) throws SessionException, RestrictionException, DatevalueException {
        return facilityManagerLocal.searchByRestriction(sessionId, restriction);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
}
