/*
 * InvestigationManagerBean.java
 *
 * Created on 26 March 2007, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.AccessType;
import uk.icat3.util.InvestigationInclude;

/**
 * This web service exposes the functions that are needed on investigation
 *
 * @author gjd37
 */
@Stateless()
//@WebService(/*name="ICATInvestigationManagerService",*/targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InvestigationManagerBean extends EJBObject implements InvestigationManagerLocal {
    
    static Logger log = Logger.getLogger(InvestigationManagerBean.class);
    
    /** Creates a new instance of InvestigationManagerBean */
    public InvestigationManagerBean() {}
    
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
    @WebMethod(operationName="getInvestigation")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationDefault")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationDefaultResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationManager.getInvestigation(userId, investigationId, manager);
    }
    
    /**
     * Returns a {@link Investigation} from a {@link Investigation} id
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
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationIncludesResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Investigation investigation = InvestigationManager.getInvestigation(userId, investigationId, manager);
        
        //now set the investigation includes for JAXB web service
        ManagerUtil.getInvestigationInformation(userId, investigation, includes, manager);
        
        return investigation;
    }
    
    /**
     * Returns a Collection of {@link Investigation}s from a Collection of {@link Investigation} ids
     * if the user has access to the investigations.
     * Also gets extra information regarding the investigations.  See {@link InvestigationInclude}
     *
     * @param sessionId sessionid of the user.
     * @param investigationIds Id of investigations
     * @param includes information that is needed to be returned with the investigation
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return Collection of {@link Investigation} objects
     */
    @WebMethod(operationName="getInvestigationsIncludes")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationsIncludes")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.getInvestigationsIncludesResponse")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<Investigation> getInvestigations(String sessionId, Collection<Long> investigationIds, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Collection<Investigation> investigations = InvestigationManager.getInvestigations(userId, investigationIds, manager);
        
        //now set the investigation includes for JAXB web service
        ManagerUtil.getInvestigationInformation(userId, investigations, includes, manager);
        
        return investigations;
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
    @WebMethod(/*operationName="createInvestigation"*/)
    public Investigation createInvestigation(String sessionId, Investigation investigation) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        Investigation investigationcreated = InvestigationManager.createInvestigation(userId, investigation, manager);
        
        return investigationcreated;
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
    @WebMethod()
    public void removeInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.removeInvestigation(userId, investigationId, manager);
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
    @WebMethod()
    public void deleteInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.deleteInvestigation(userId, investigationId, manager);
    }
    
    /**
     * Modifies a {@link Investigation} investigation from a {@link Investigation} object
     * if the user has access to delete the investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigation investigation object to be updated
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     */
    @WebMethod()
    public void modifyInvestigation(String sessionId, Investigation investigation) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.updateInvestigation(userId, investigation, manager);
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
     * @return keyword that was added
     */
    @WebMethod()
    public Keyword addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return (Keyword) InvestigationManager.addInvestigationObject(userId, keyword, investigationId, manager);
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
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteKeywordImpl(sessionId, keywordPK, AccessType.REMOVE);
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
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteKeywordImpl(sessionId, keywordPK, AccessType.DELETE);
    }
    
    private void deleteKeywordImpl(String sessionId, KeywordPK keywordPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) && !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find investigator
        Keyword keyword = ManagerUtil.findObject(Keyword.class, keywordPK, manager);
        
        // remove/delete investigator
        InvestigationManager.deleteInvestigationObject(userId, keyword, type, manager);
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
    @WebMethod()
    public Publication addPublication(String sessionId, Publication publication, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return (Publication) InvestigationManager.addInvestigationObject(userId, publication, investigationId, manager);
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
    @WebMethod()
    public void removePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deletePublicationImpl(sessionId, publicationId, AccessType.REMOVE);
    }
    
    /**
     * Deleted the publication from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publicationId id of object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void deletePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deletePublicationImpl(sessionId, publicationId, AccessType.DELETE);
    }
    
    private void deletePublicationImpl(String sessionId, Long publicationId, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) && !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find publication
        Publication publication = ManagerUtil.findObject(Publication.class, publicationId, manager);
        
        // remove/delete publication
        InvestigationManager.deleteInvestigationObject(userId, publication, type, manager);
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
    @WebMethod()
    public void modifyPublication(String sessionId, Publication publication) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify publication
        InvestigationManager.updateInvestigationObject(userId, publication, manager);
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
     * @return investigator that was added
     */
    @WebMethod()
    public Investigator addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return (Investigator) InvestigationManager.addInvestigationObject(userId, investigator, investigationId, manager);
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
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteInvestigatorImpl(sessionId, investigatorPK, AccessType.REMOVE);
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
    public void modifyInvestigator(String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, investigator, manager);
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
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteInvestigatorImpl(sessionId, investigatorPK, AccessType.DELETE);
    }
    
    private void deleteInvestigatorImpl(String sessionId, InvestigatorPK investigatorPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) && !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find investigator
        Investigator investigator = ManagerUtil.findObject(Investigator.class, investigatorPK, manager);
        
        // remove/delete investigator
        InvestigationManager.deleteInvestigationObject(userId, investigator, type, manager);
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
     * @return sample that was added
     */
    @WebMethod()
    public Sample addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return (Sample)InvestigationManager.addInvestigationObject(userId, sample, investigationId, manager);
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
    public void removeSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleImpl(sessionId, sampleId, AccessType.REMOVE);
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
    public void deleteSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleImpl(sessionId, sampleId, AccessType.DELETE);
    }
    
    private void removeSampleImpl(String sessionId, Long sampleId, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) && !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find sample
        Sample sample = ManagerUtil.findObject(Sample.class, sampleId, manager);
        
        // remove/delete sample
        InvestigationManager.deleteInvestigationObject(userId, sample, type, manager);
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
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        // for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, sample, manager);
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
    public void removeSampleParameter(String sessionId, SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleParameterImpl(sessionId, sampleParameterPK, AccessType.REMOVE);
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
    public void deleteSampleParameter(String sessionId, SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleParameterImpl(sessionId, sampleParameterPK, AccessType.DELETE);
    }
    
    private void removeSampleParameterImpl(String sessionId, SampleParameterPK sampleParameterPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) && !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find sample
        SampleParameter sampleParameter = ManagerUtil.findObject(SampleParameter.class, sampleParameterPK, manager);
        
        // remove/delete sample
        InvestigationManager.deleteInvestigationObject(userId, sampleParameter, type, manager);
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
    public void modifySampleParameter(String sessionId, SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        // for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, sampleParameter, manager);
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
    @WebMethod()
    public SampleParameter addSampleParameter(String sessionId, SampleParameter sampleParameter, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return (SampleParameter) InvestigationManager.addInvestigationObject(userId, sampleParameter, investigationId, manager);
    }
    
    
    ////////////////////////////////////   Authorisation Section //////////////////////////////////////////////
     /**
     * Gets all the IcatAuthorisations for a investigationId of all of the users
     *
     * @param sessionId session id of the user.
     * @param investigationId investigation id of the authorisations you want
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid     * 
     * @return Collection of {@link IcatAuthorisation}>s of the datafile id
     */
    @WebMethod()
    public Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long investigationId) throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationManager.getAuthorisations(userId, investigationId, manager);
    }
    
    /**
     * Adds a role to a investigationIdfor a user (fedId) depending weather user  from session id has permission to do it
     *
     * @param sessionId session id of the user.    
     * @param toAddUserId federal Id of user to add
     * @param toAddRole new role for federal Id
     * @param investigationId investigation Id of the item to add the role to
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid      
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding 
     * @return {@link IcatAuthorisation}s of the datafile id
     */
    @WebMethod()
    public IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long investigationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationManager.addAuthorisation(userId, toAddUserId, toAddRole, investigationId, manager);
    }
    
    /**
     * Deletes a IcatAuthorisation 
     *
     * @param sessionId session id of the user.     
     * @param authorisationId id of the authorisation to delete
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     */
    @WebMethod()
    public void deleteAuthorisation(String sessionId, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.deleteAuthorisation(userId, authorisationId, manager);
    }
    
    /**
     * Removes a IcatAuthorisation 
     *
     * @param sessionId session id of the user.     
     * @param authorisationId id of the authorisation to remove
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     */
    @WebMethod()
    public void removeAuthorisation(String sessionId, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.removeAuthorisation(userId, authorisationId, manager);
    }
    
     /**
     * Changes a IcatAuthorisation role for a authorisation id
     *
     * @param sessionId session id of the user.     
     * @param toChangetoRole role to change to
     * @param authorisationId id of the authorisation to remove
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid         
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding     
     */
    
    @WebMethod()
    public void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.updateAuthorisation(userId, toChangetoRole, authorisationId, manager);
    }    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
}
