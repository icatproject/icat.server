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
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.sessionbeans.manager.DatafileManagerLocal;
import uk.icat3.sessionbeans.manager.DatasetManagerLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerLocal;
import uk.icat3.sessionbeans.search.DatafileSearchLocal;
import uk.icat3.sessionbeans.search.DatasetSearchLocal;
import uk.icat3.sessionbeans.search.InvestigationSearchLocal;
import uk.icat3.sessionbeans.search.KeywordSearchLocal;
import uk.icat3.user.UserDetails;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService(/*serviceName="ICATService", name="ICATServices",*/ targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
    protected InvestigationSearchLocal investigationSearchLocal;
    
    @EJB
    protected KeywordSearchLocal keywordSearchLocal;
    ///////////////////////  End of Inject all the EJBs   ///////////////////////
    
    
    
    /** Creates a new instance of AllOperationsBean */
    public ICAT() {
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   Session  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////     UserSession methods  /////////////////////////////////////////
    @WebMethod()
    @ExcludeClassInterceptors
    public String login(@WebParam(name="username") String username, @WebParam(name="password") String password) throws SessionException{
        return user.login(username, password);
    }
    
    @WebMethod(operationName="loginLifetime")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginLifetime")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginLifetimeResponse")
    public String login(@WebParam(name="username") String username, @WebParam(name="password") String password, int lifetime) throws SessionException{
        return user.login(username, password, lifetime);
    }
    
    @WebMethod(operationName="loginAdmin")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdmin")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdminResponse")
    public String login(@WebParam(name="adminUsername") String adminUsername, @WebParam(name="AdminPassword") String AdminPassword, @WebParam(name="runAsUser") String runAsUser) throws SessionException{
        return user.login(adminUsername, AdminPassword, runAsUser);
    }
    
    @WebMethod(operationName="loginCredentials")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentials")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentialsResponse")
    public String login(@WebParam(name="credential") String credential) throws SessionException{
        return user.login(credential);
    }
    
    @WebMethod()
    public boolean logout(@WebParam(name="sessionId") String sessionId){
        return user.logout(sessionId);
    }
    
    @WebMethod()
    public UserDetails getUserDetails(@WebParam(name="sessionId") String sessionId, @WebParam(name="usersName") String usersName) throws SessionException, NoSuchUserException{
        return this.user.getUserDetails(sessionId, usersName);
    }
    ///////////////////////////     End of UserSession methods  //////////////////////////////////
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Searches  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////     KeywordSearch methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<String> getKeywordsForUser(@WebParam(name="sessionId") String sessionId) throws SessionException{
        return keywordSearchLocal.getKeywordsForUser(sessionId);
    }
    
    @WebMethod(operationName="getKeywordsForUserMax")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.getKeywordsForUserMax")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.getKeywordsForUserMaxResponse")
    public Collection<String> getKeywordsForUser(@WebParam(name="sessionId") String sessionId, String startKeyword, int numberReturned) throws SessionException{
        return keywordSearchLocal.getKeywordsForUser(sessionId, startKeyword, numberReturned);
    }
    
    @WebMethod()
    public Collection<String> getAllKeywords(@WebParam(name="sessionId") String sessionId, KeywordType type) throws SessionException{
        return keywordSearchLocal.getAllKeywords(sessionId, type);
    }
    ///////////////////////////     End of KeywordSearch methods  /////////////////////////////////////////
    
    
    ///////////////////////////     Investigation Search methods  /////////////////////////////////////////
    @WebMethod(operationName="searchByKeywordsFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsFuzzyResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, include, fuzzy);
    }
    
    @WebMethod(operationName="searchByKeywords")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywords")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsResponse")
    public Collection<Investigation> searchByKeywords(@WebParam(name="sessionId") String sessionId, Collection<String> keywords) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords);
    }
    
    @WebMethod(operationName="searchByKeywordsPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationResponse")
    public Collection<Investigation> searchByKeywords(@WebParam(name="sessionId") String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, startIndex, numberOfResults);
    }
    
    @WebMethod(operationName="searchByKeywordsPaginationAndFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationAndFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationAndFuzzyResponse")
    public Collection<Investigation> searchByKeywords(@WebParam(name="sessionId") String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, include, fuzzy, startIndex, numberOfResults);
    }
    
    @WebMethod(operationName="searchByKeywordsPaginationFuzzyAndInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationFuzzyAndInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPaginationFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(@WebParam(name="sessionId") String sessionId, Collection<String> keywords, InvestigationInclude include,  int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, include, startIndex, numberOfResults);
    }
    
    @WebMethod(operationName="searchByKeywordsAll")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsAllResponse")
    public Collection<Investigation> searchByKeywords(@WebParam(name="sessionId") String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, operator, include, fuzzy, startIndex, numberOfResults);
    }
    
    @WebMethod()
    public Collection<Investigation> getMyInvestigations(@WebParam(name="sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.getMyInvestigations(sessionId);
    }
    
    @WebMethod()
    public Collection<Investigation> searchByUserID(@WebParam(name="sessionId") String sessionId, String userSearch) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch);
    }
    
    @WebMethod(operationName="searchByUserIDPagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserIDPagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserIDPaginationResponse")
    public Collection<Investigation> searchByUserID(@WebParam(name="sessionId") String sessionId, String userSearch, int startIndex, int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch, startIndex, number_results);
    }
    
    @WebMethod()
    public Collection<Investigation> searchByUserSurname(@WebParam(name="sessionId") String sessionId, String surname) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname);
    }
    
    @WebMethod(operationName="searchByUserSurnamePagination")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserSurnamePagination")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserSurnamePaginationResponse")
    public Collection<Investigation> searchByUserSurname(@WebParam(name="sessionId") String sessionId, String surname, int startIndex, int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname, startIndex, number_results);
    }
    
    @WebMethod()
    public Collection<String> listAllInstruments(@WebParam(name="sessionId") String sessionId) throws SessionException {
        return investigationSearchLocal.listAllInstruments(sessionId);
    }
    ///////////////////////////     End of Investigation Search methods  /////////////////////////////////////////
    
    
    ///////////////////////////     Dataset Search methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<Dataset> searchBySampleName(@WebParam(name="sessionId") String sessionId, String sampleName) throws SessionException {
        return datasetSearchLocal.searchBySampleName(sessionId, sampleName);
    }
    
    @WebMethod()
    public Collection<DatasetType> listDatasetTypes(@WebParam(name="sessionId") String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetTypes(sessionId);
    }
    
    @WebMethod()
    public Collection<DatasetStatus> listDatasetStatus(@WebParam(name="sessionId") String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetStatus(sessionId);
    }
    ///////////////////////////     End of Dataset Search methods  /////////////////////////////////////////
    
    ///////////////////////////     Datafile Search methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<Datafile> searchByRunNumber(@WebParam(name="sessionId") String sessionId, Collection<String> instruments, Long startRun, Long endRun) throws SessionException {
        return datafileSearchLocal.searchByRunNumber(sessionId, instruments, startRun, endRun);
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
    @WebMethod(/*operationName="getInvestigation"*/)
    // @RequestWrapper(className="uk.icat3.sessionbeans.getInvestigationDefault")
    //@ResponseWrapper(className="uk.icat3.sessionbeans.getInvestigationDefaultResponse")
    public Investigation getInvestigation(@WebParam(name="sessionId") String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId,investigationId);
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
    @WebMethod(operationName="getInvestigationIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.getInvestigationIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.getInvestigationIncludesResponse")
    public Investigation getInvestigation(@WebParam(name="sessionId") String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId,investigationId, includes);
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
     */
    @WebMethod()
    public void addKeyword(@WebParam(name="sessionId") String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addKeyword(sessionId, keyword, investigationId);
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
    @WebMethod()
    public void removeKeyword(@WebParam(name="sessionId") String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeKeyword(sessionId, keywordPK);
    }
    
    /**
     * Deletes the keyword from investigation, depending on whether the user has permission to delete this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteKeyword(@WebParam(name="sessionId") String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteKeyword(sessionId, keywordPK);
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
     */
    @WebMethod()
    public void addInvestigator(@WebParam(name="sessionId") String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addInvestigator(sessionId, investigator, investigationId);
    }
    
    /**
     * Deletes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteInvestigator(@WebParam(name="sessionId") String sessionId, InvestigatorPK investigatorPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteInvestigator(sessionId, investigatorPK);
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
    @WebMethod()
    public void removeInvestigator(@WebParam(name="sessionId") String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeInvestigator(sessionId, investigatorPK);
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
    @WebMethod()
    public void modifyInvestigator(@WebParam(name="sessionId") String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifyInvestigator(sessionId, investigator);
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
     */
    @WebMethod()
    public void addSample(@WebParam(name="sessionId") String sessionId, Sample sample, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addSample(sessionId, sample, investigationId);
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
    @WebMethod()
    public void removeSample(@WebParam(name="sessionId") String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSample(sessionId, sampleId);
    }
    
    /**
     * Deletes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteSample(@WebParam(name="sessionId") String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteSample(sessionId, sampleId);
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
    @WebMethod()
    public void modifySample(@WebParam(name="sessionId") String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifySample(sessionId, sample);
    }
    
    /**
     * Deletes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeSampleParameter(@WebParam(name="sessionId") String sessionId,SampleParameterPK sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSampleParameter(sessionId, sampleParameterPK);
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
    @WebMethod()
    public void deleteSampleParameter(@WebParam(name="sessionId") String sessionId,SampleParameterPK sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteSampleParameter(sessionId, sampleParameterPK);
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
    @WebMethod()
    public void modifySampleParameter(@WebParam(name="sessionId") String sessionId,SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifySampleParameter(sessionId, sampleParameter);
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
    public Dataset getDataset(@WebParam(name="sessionId") String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
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
    @WebMethod(operationName="getDatasetIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getDatasetIncludesResponse")
    public Dataset getDataset(@WebParam(name="sessionId") String sessionId, Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
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
    public Collection<Dataset> getDatasets(@WebParam(name="sessionId") String sessionId, Collection<Long> datasetIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
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
    public Dataset createDataSet(@WebParam(name="sessionId") String sessionId, Long investigationId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
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
    public Collection<Dataset> createDataSets(@WebParam(name="sessionId") String sessionId, Collection<Dataset> dataSets, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.createDataSets(sessionId, dataSets, investigationId);
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
    public void removeDataSet(@WebParam(name="sessionId") String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSet(sessionId, dataSetId);
    }
    
    /**
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID. Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSet(@WebParam(name="sessionId") String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.deleteDataSet(sessionId, dataSetId);
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
    public void modifyDataSet(@WebParam(name="sessionId") String sessionId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.modifyDataSet(sessionId, dataSet);
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
     */
    @WebMethod
    public DatasetParameter addDataSetParameter(@WebParam(name="sessionId") String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datasetManagerLocal.addDataSetParameter(sessionId, dataSetParameter, datasetId);
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
    public void modifyDataSetParameter(@WebParam(name="sessionId") String sessionId, DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.modifyDataSetParameter(sessionId, dataSetParameter);
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
    public void removeDataSetParameter(@WebParam(name="sessionId") String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSetParameter(sessionId, datasetParameterPK);
    }
    
    /**
     * Deleted the data set paramter, depending if the users has access to remove the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void deleteDataSetParameter(@WebParam(name="sessionId") String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.deleteDataSetParameter(sessionId, datasetParameterPK);
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
    public void setDataSetSample(@WebParam(name="sessionId") String sessionId, Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datasetManagerLocal.setDataSetSample(sessionId, sampleId, datasetId);
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
    @WebMethod()
    public Datafile getDatafile(@WebParam(name="sessionId") String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
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
    @WebMethod()
    public Collection<Datafile> getDatafiles(@WebParam(name="sessionId") String sessionId, Collection<Long> datafileIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
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
    @WebMethod()
    public Datafile createDataFile(@WebParam(name="sessionId") String sessionId, Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
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
    @WebMethod()
    public Collection<Datafile> createDataFiles(@WebParam(name="sessionId") String sessionId, Collection<Datafile> dataFiles, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return datafileManagerLocal.createDataFiles(sessionId, dataFiles, datasetId);
    }
    
    /**
     * Deletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param datafileId id to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteDataFile(@WebParam(name="sessionId") String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.deleteDataFile(sessionId, datafileId);
    }
    
    /**
     * Deletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataFile objectto be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod(operationName="deleteDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.deleteDataFileObjectResponse")
    public void deleteDataFile(@WebParam(name="sessionId") String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.deleteDataFile(sessionId, dataFile);
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
    @WebMethod()
    public void removeDataFile(@WebParam(name="sessionId") String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFile(sessionId, datafileId);
    }
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod(operationName="removeDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObjectResponse")
    public void removeDataFile(@WebParam(name="sessionId") String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFile(sessionId, dataFile);
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
    @WebMethod()
    public void updateDataFile(@WebParam(name="sessionId") String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datafileManagerLocal.updateDataFile(sessionId, dataFile);
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
    @WebMethod()
    public DatafileParameter addDataFileParameter(@WebParam(name="sessionId") String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        return  datafileManagerLocal.addDataFileParameter(sessionId, dataFileParameter, datafileId);
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
    @WebMethod()
    public void modifyDataFileParameter(@WebParam(name="sessionId") String sessionId, DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        datafileManagerLocal.modifyDataFileParameter(sessionId, dataFileParameter);
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
    @WebMethod()
    public void removeDataFileParameter(@WebParam(name="sessionId") String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFileParameter(sessionId, datafileParameterPK);
    }
    
    
    /**
     * Deletes a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deleteDataFileParameter(@WebParam(name="sessionId") String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.deleteDataFileParameter(sessionId, datafileParameterPK);
    }
    ///////////////////////////     End of Datafile Manager methods  /////////////////////////////////////////
    
}
