package uk.icat3.sessionbeans;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.log4j.Logger;

import uk.icat3.data.DownloadInfo;
import uk.icat3.entity.Application;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Facility;
import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.FacilityInstrumentScientist;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.InputDatafile;
import uk.icat3.entity.InputDataset;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Job;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.OutputDatafile;
import uk.icat3.entity.OutputDataset;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Publication;
import uk.icat3.entity.RelatedDatafiles;
import uk.icat3.entity.Rule;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.Shift;
import uk.icat3.entity.SoftwareVersion;
import uk.icat3.entity.Study;
import uk.icat3.entity.StudyInvestigation;
import uk.icat3.entity.StudyStatus;
import uk.icat3.entity.Topic;
import uk.icat3.entity.TopicList;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.KeywordDetails;
import uk.icat3.sessionbeans.data.DownloadManagerLocal;
import uk.icat3.sessionbeans.interceptor.AdvancedSearchInterceptor;
import uk.icat3.sessionbeans.interceptor.DownloadInterceptor;
import uk.icat3.sessionbeans.interceptor.KeywordsInterceptor;
import uk.icat3.sessionbeans.interceptor.LogoutInterceptor;
import uk.icat3.sessionbeans.interceptor.RunNumberSearchInterceptor;
import uk.icat3.sessionbeans.interceptor.SampleSearchInterceptor;
import uk.icat3.sessionbeans.interceptor.ViewDatafilesInterceptor;
import uk.icat3.sessionbeans.interceptor.ViewDatasetsInterceptor;
import uk.icat3.sessionbeans.interceptor.ViewInvestigationsInterceptor;
import uk.icat3.sessionbeans.interceptor.ViewMyInvestigationsInterceptor;
import uk.icat3.sessionbeans.manager.BeanManagerLocal;
import uk.icat3.sessionbeans.manager.DatafileManagerLocal;
import uk.icat3.sessionbeans.manager.DatasetManagerLocal;
import uk.icat3.sessionbeans.manager.FacilityManagerLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerLocal;
import uk.icat3.sessionbeans.manager.RuleManagerLocal;
import uk.icat3.sessionbeans.manager.XMLIngestionManagerLocal;
import uk.icat3.sessionbeans.search.DatafileSearchLocal;
import uk.icat3.sessionbeans.search.DatasetSearchLocal;
import uk.icat3.sessionbeans.search.InvestigationSearchLocal;
import uk.icat3.sessionbeans.search.KeywordSearchLocal;
import uk.icat3.sessionbeans.search.ParameterSearchLocal;
import uk.icat3.sessionbeans.search.SearchLocal;
import uk.icat3.sessionbeans.util.Constants;
import uk.icat3.user.UserDetails;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.Queries;

@Stateless
@WebService(serviceName = "ICATService", targetNamespace = "client.icat3.uk")
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ICAT extends EJBObject {

    static Logger log = Logger.getLogger(ICAT.class);
 
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
    @EJB
    protected SearchLocal searchLocal;
	@EJB
	protected RuleManagerLocal ruleManagerLocal;
	@EJB
	protected BeanManagerLocal beanManagerLocal;


	public ICAT() {
	}
	
    @WebMethod
    public List<?> search(
    		@WebParam(name = "sessionId") String sessionId,
    		@WebParam(name = "query") String query
    		) throws BadParameterException, IcatInternalException, InsufficientPrivilegesException, SessionException {
    	return searchLocal.search(sessionId, query);
    }
    
    @WebMethod
    public Object create(
    		@WebParam(name = "sessionId") String sessionId,
    		@WebParam(name = "bean") EntityBaseBean bean
    ) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException, ObjectAlreadyExistsException, IcatInternalException {
    	return beanManagerLocal.create(sessionId, bean);
    }
    
    @WebMethod
    public void delete(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "bean") EntityBaseBean bean
            ) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException, ObjectAlreadyExistsException, IcatInternalException {
    	beanManagerLocal.delete(sessionId, bean);
    }
    
    @WebMethod
    public void update(
    		@WebParam(name = "sessionId") String sessionId,
    		@WebParam(name = "bean") EntityBaseBean bean
    ) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException, IcatInternalException {
    	beanManagerLocal.update(sessionId, bean);
    }
    
    @WebMethod
    public EntityBaseBean get(
    		@WebParam(name = "sessionId") String sessionId,
    		@WebParam(name = "query") String query,
    		@WebParam(name = "primaryKey") Object primaryKey
    ) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, BadParameterException, IcatInternalException {
    	return beanManagerLocal.get(sessionId, query, primaryKey);
    }

    @WebMethod
    public void dummy(
    	
    		@WebParam Datafile datafile,
    		@WebParam DatafileFormat datafileFormat,
    		@WebParam DatafileParameter datafileParameter,
    		@WebParam Dataset dataset,
    		@WebParam DatasetParameter datasetParameter,
    		@WebParam DatasetStatus datasetStatus,
    		@WebParam DatasetType datasetType,
    		@WebParam Facility facility,
    		@WebParam FacilityCycle facilityCycle,
    		@WebParam FacilityInstrumentScientist facilityInstrumentScientist,
    		@WebParam FacilityUser facilityUser,
    		@WebParam Instrument instrument,
    		@WebParam Investigation investigation,
    		@WebParam InvestigationType investigationType,
    		@WebParam Investigator investigator,
    		@WebParam Keyword keyword,
    		@WebParam Parameter parameter,
    		@WebParam Publication publication,
    		@WebParam RelatedDatafiles relatedDatafiles,
    		@WebParam Sample sample,
    		@WebParam SampleParameter sampleParameter,
    		@WebParam Shift shift,
    		@WebParam SoftwareVersion softwareVersion,
    		@WebParam Study study,
    		@WebParam StudyInvestigation studyInvestigation,
    		@WebParam StudyStatus studyStatus,
    		@WebParam Topic topic,
    		@WebParam TopicList topicList,
    		@WebParam UserGroup userGroup,
    		@WebParam Application application,
    		@WebParam Job job,
    		@WebParam InputDataset inputDataset,
    		@WebParam OutputDataset outputDataset,
    		@WebParam InputDatafile inputDatafile,
    		@WebParam OutputDatafile outputDatafile
    ) {
    	beanManagerLocal.dummy(facility);
    }
	
	@WebMethod(operationName = "loginAdmin")
	@ExcludeClassInterceptors
	public String loginAdmin(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "runAsUserFedId") String runAsUserFedId
	) throws SessionException {
		return user.loginAdmin(sessionId, runAsUserFedId);
	}

	@WebMethod
	public void addUserGroupMember(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "groupName")String name, 
			@WebParam(name = "memberName")String member
	) throws ObjectAlreadyExistsException, SessionException {
		ruleManagerLocal.addUserGroupMember(sessionId, name, member);
	}

	@WebMethod
	public void removeUserGroupMember(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "groupName") String name, 
			@WebParam(name = "memberName") String member
	) throws SessionException, NoSuchObjectFoundException {
		ruleManagerLocal.removeUserGroupMember(sessionId, name, member);
	}

	@WebMethod
	public long addRule(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "groupName")String groupName, 
			@WebParam(name = "what") String what,
			@WebParam(name = "crud") String crud,
			@WebParam(name = "restriction") String restriction
	) throws SessionException, IcatInternalException, BadParameterException {
		return ruleManagerLocal.addRule(sessionId, groupName, what, crud, restriction);
	}

	@WebMethod
	public void removeRule(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "id") Long id 
	) throws SessionException, BadParameterException, NoSuchObjectFoundException {
		ruleManagerLocal.removeRule(sessionId, id);
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
     *  Lists all the rules
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod
    public List<Rule> listRules(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return ruleManagerLocal.listRules(sessionId);
    }
    
    /**
     *  Lists all the UserGroup entities
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    @WebMethod
    public Collection<UserGroup> listUserGroups(
            @WebParam(name = "sessionId") String sessionId) throws SessionException {
        return ruleManagerLocal.listUserGroups(sessionId);
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
 
    @WebMethod
    public Long[] ingestMetadata(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "xml") String xml) throws SessionException, ValidationException, InsufficientPrivilegesException, ICATAPIException {
        return xmlIngestionManagerLocal.ingestMetadata(sessionId, xml);
    }

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

}
