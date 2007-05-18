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
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.search.DatasetSearch;
import uk.icat3.sessionbeans.manager.DatafileManagerLocal;
import uk.icat3.sessionbeans.manager.DatasetManagerLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerLocal;
import uk.icat3.sessionbeans.search.DatafileSearchLocal;
import uk.icat3.sessionbeans.search.DatasetSearchLocal;
import uk.icat3.sessionbeans.search.InvestigationSearchLocal;
import uk.icat3.sessionbeans.search.KeywordSearchLocal;
import uk.icat3.user.UserDetails;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService(/*serviceName="ICATService", name="ICATServices", */ targetNamespace="client.icat3.uk")
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
    public String login(String username, String password) throws SessionException{
        return user.login(username, password);
    }
     
     
    @WebMethod(operationName="loginLifetime")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginLifetime")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginLifetimeResponse")
    public String login(String username, String password, int lifetime) throws SessionException{
        return user.login(username, password, lifetime);
    }
     
    @WebMethod(operationName="loginAdmin")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdmin")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdminResponse")
    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException{
        return user.login(adminUsername, AdminPassword, runAsUser);
    }
     
     
    @WebMethod(operationName="loginCredentials")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentials")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentialsResponse")
    public String login(String credential) throws SessionException{
        return user.login(credential);
    }
     
    @WebMethod()
    public boolean logout(String sessionId){
        return user.logout(sessionId);
    }
     
    @WebMethod()
    public UserDetails getUserDetails(String sessionId, String usersName) throws SessionException, NoSuchUserException{
        return this.user.getUserDetails(sessionId, usersName);
    }
    ///////////////////////////     End of UserSession methods  //////////////////////////////////
     
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Searches  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
     
    ///////////////////////////     KeywordSearch methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<String> getKeywordsForUser(String sessionId) throws SessionException{
        return keywordSearchLocal.getKeywordsForUser(sessionId);
    }
     
    @WebMethod(operationName="getKeywordsForUserMax")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.getKeywordsForUserMax")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.getKeywordsForUserMaxResponse")
    public Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned) throws SessionException{
        return keywordSearchLocal.getKeywordsForUser(sessionId, startKeyword, numberReturned);
    }
     
    @WebMethod()
    public Collection<String> getAllKeywords(String sessionId, KeywordType type) throws SessionException{
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
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords);
    }
     
    @WebMethod(operationName="searchByKeywordsPagnation")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnation")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnationResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, startIndex, numberOfResults);
    }
     
    @WebMethod(operationName="searchByKeywordsPagnationAndFuzzy")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnationAndFuzzy")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnationAndFuzzyResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, include, fuzzy, startIndex, numberOfResults);
    }
     
    @WebMethod(operationName="searchByKeywordsPagnationFuzzyAndInclude")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnationFuzzyAndInclude")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsPagnationFuzzyAndIncludeResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include,  int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, include, startIndex, numberOfResults);
    }
     
    @WebMethod(operationName="searchByKeywordsAll")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsAll")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByKeywordsAllResponse")
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException {
        return investigationSearchLocal.searchByKeywords(sessionId, keywords, operator, include, fuzzy, startIndex, numberOfResults);
    }
     
    @WebMethod()
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException {
        return investigationSearchLocal.getMyInvestigations(sessionId);
    }
     
    @WebMethod()
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch);
    }
     
    @WebMethod(operationName="searchByUserIDPagnation")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserIDPagnation")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserIDPagnationResponse")
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch, int startIndex, int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserID(sessionId, userSearch, startIndex, number_results);
    }
     
    @WebMethod()
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname);
    }
     
    @WebMethod(operationName="searchByUserSurnamePagnation")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserSurnamePagnation")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.searchByUserSurnamePagnationResponse")
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname, int startIndex, int number_results) throws SessionException {
        return investigationSearchLocal.searchByUserSurname(sessionId, surname, startIndex, number_results);
    }
     
    @WebMethod()
    public Collection<String> listAllInstruments(String sessionId) throws SessionException {
        return investigationSearchLocal.listAllInstruments(sessionId);
    }
    ///////////////////////////     End of Investigation Search methods  /////////////////////////////////////////
     
     
    ///////////////////////////     Dataset Search methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<Dataset> searchBySampleName(String sessionId, String sampleName) throws SessionException {
        return datasetSearchLocal.searchBySampleName(sessionId, sampleName);
    }
     
    @WebMethod()
    public Collection<DatasetType> listDatasetTypes(String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetTypes(sessionId);
    }
     
    @WebMethod()
    public Collection<DatasetType> listDatasetTypes2(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
     
        return DatasetSearch.listDatasetTypes(manager);
    }
     
    @WebMethod()
    public Collection<DatasetStatus> listDatasetStatus(String sessionId) throws SessionException {
        return datasetSearchLocal.listDatasetStatus(sessionId);
    }
    ///////////////////////////     End of Dataset Search methods  /////////////////////////////////////////
     
    ///////////////////////////     Datafile Search methods  /////////////////////////////////////////
    @WebMethod()
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, Long startRun, Long endRun) throws SessionException {
        return datafileSearchLocal.searchByRunNumber(sessionId, instruments, startRun, endRun);
    }
    ///////////////////////////     End of Datafile Search methods  /////////////////////////////////////////
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Manager   ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////     Investigation Manager methods  /////////////////////////////////////////
    @WebMethod(/*operationName="getInvestigation"*/)
   // @RequestWrapper(className="uk.icat3.sessionbeans.getInvestigationDefault")
    //@ResponseWrapper(className="uk.icat3.sessionbeans.getInvestigationDefaultResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId,investigationId);             
    }        
    
    @WebMethod(operationName="getInvestigationIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.getInvestigationIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.getInvestigationIncludesResponse")
    public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        return investigationManagerLocal.getInvestigation(sessionId,investigationId, includes);
    }
   
    @WebMethod()
    public void addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addKeyword(sessionId, keyword, investigationId);
    }
   
    @WebMethod()
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeKeyword(sessionId, keywordPK);
    }
   
    @WebMethod()
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteKeyword(sessionId, keywordPK);
    }
   
    @WebMethod()
    public void addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addInvestigator(sessionId, investigator, investigationId);
    }
   
    @WebMethod()
    public void modifyInvestigator(String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifyInvestigator(sessionId, investigator);
    }
   
    @WebMethod()
    public void addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.addSample(sessionId, sample, investigationId);
    }
   
    @WebMethod()
    public void removeSample(String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSample(sessionId, sampleId);
    }
   
    @WebMethod()
    public void deleteSample(String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteSample(sessionId, sampleId);
    }
   
    @WebMethod()
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifySample(sessionId, sample);
    }
   
    @WebMethod()
    public void removeSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSampleParameter(sessionId, sampleParameterPK);
    }
   
    @WebMethod()
    public void deleteSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteSampleParameter(sessionId, sampleParameterPK);
    }
   
    @WebMethod()
    public void modifySampleParameter(String sessionId, SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.modifySampleParameter(sessionId, sampleParameter);
    }
   
    @WebMethod()
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.deleteInvestigator(sessionId, investigatorPK);
    }
   
    @WebMethod()
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeInvestigator(sessionId, investigatorPK);
    }
    ///////////////////////////     End of Investigation Manager methods  /////////////////////////////////////////
   
}
