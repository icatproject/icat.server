/*
 * InvestigationManagerBean.java
 *
 * Created on 26 March 2007, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import javax.ejb.EJB;
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
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.AccessType;
import uk.icat3.util.InvestigationInclude;

/**
 * This web service exposes the functions that are needed on investigation
 *
 * @author gjd37
 */
@Stateless()
@WebService(/*name="ICATInvestigationManagerService",targetNamespace="client.icat3.uk"*/)
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InvestigationManagerBean extends EJBObject implements InvestigationManagerLocal {
    
    static Logger log = Logger.getLogger(InvestigationManagerBean.class);
    
    /** Creates a new instance of InvestigationManagerBean */
    public InvestigationManagerBean() {}
    
    /**
     *
     * @param sessionId
     * @param investigationId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @return
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
     *
     * @param sessionId
     * @param investigationId
     * @param includes
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @return
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
        investigation.setInvestigationInclude(includes);
        
        return investigation;
    }
    
    /**
     *
     * @param sessionId
     * @param keyword
     * @param investigationId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.addInvestigationObject(userId, keyword, investigationId, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param keywordId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteKeywordImpl(sessionId, keywordPK, AccessType.REMOVE);
    }
    
    /**
     *
     * @param sessionId
     * @param keywordId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteKeywordImpl(sessionId, keywordPK, AccessType.DELETE);
    }
    
    private void deleteKeywordImpl(String sessionId, KeywordPK keywordPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) || !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find investigator
        Keyword keyword = ManagerUtil.find(Keyword.class, keywordPK, manager);
        
        // remove/delete investigator
        InvestigationManager.deleteInvestigationObject(userId, keyword, type, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param investigator
     * @param investigationId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.addInvestigationObject(userId, investigator, investigationId, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param investigatorId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteInvestigatorImpl(sessionId, investigatorPK, AccessType.REMOVE);
    }
    
    /**
     *
     * @param sessionId
     * @param investigator
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void modifyInvestigator(String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, investigator, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param investigatorId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        deleteInvestigatorImpl(sessionId, investigatorPK, AccessType.DELETE);
    }
    
    private void deleteInvestigatorImpl(String sessionId, InvestigatorPK investigatorPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) || !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find investigator
        Investigator investigator = ManagerUtil.find(Investigator.class, investigatorPK, manager);
        
        // remove/delete investigator
        InvestigationManager.deleteInvestigationObject(userId, investigator, type, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sample
     * @param investigationId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException{
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        InvestigationManager.addInvestigationObject(userId, sample, investigationId, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void removeSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleImpl(sessionId, sampleId, AccessType.REMOVE);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void deleteSample(String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleImpl(sessionId, sampleId, AccessType.DELETE);
    }
    
    private void removeSampleImpl(String sessionId, Long sampleId, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) || !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find sample
        Sample sample = ManagerUtil.find(Sample.class, sampleId, manager);
        
        // remove/delete sample
        InvestigationManager.deleteInvestigationObject(userId, sample, type, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sample
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        // for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, sample, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleParameterId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void removeSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleParameterImpl(sessionId, sampleParameterPK, AccessType.REMOVE);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleParameterId
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void deleteSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        removeSampleParameterImpl(sessionId, sampleParameterPK, AccessType.DELETE);
    }
    
    private void removeSampleParameterImpl(String sessionId, SampleParameter sampleParameterPK, AccessType type) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        if(!type.equals(AccessType.DELETE) || !type.equals(AccessType.REMOVE)) throw new IllegalArgumentException("AccessType must be either DELETE or REMOVE");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //find sample
        SampleParameter sampleParameter = ManagerUtil.find(SampleParameter.class, sampleParameterPK, manager);
        
        // remove/delete sample
        InvestigationManager.deleteInvestigationObject(userId, sampleParameter, type, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param sampleParameter
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     */
    @WebMethod()
    public void modifySampleParameter(String sessionId, SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        
        // for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        // modify sample
        InvestigationManager.updateInvestigationObject(userId, sampleParameter, manager);
    }
}
