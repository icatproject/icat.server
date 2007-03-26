/*
 * Search.java
 *
 * Created on 12 March 2007, 08:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.ac.cclrc.dpal.DPAccessLayer;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordSearch;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import static uk.icat3.util.Queries.*;

/** 
 * TEST CLASS FOR ME
 *
 * @author gjd37
 */

@Stateless(mappedName="SearchICATEJB")
@WebService()
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Search extends EJBObject implements SearchLocal, SearchRemote{
    
    
    @EJB
    UserSessionLocal user;
    
    static Logger log = Logger.getLogger(Search.class);
    
    /**
     * This searches all DB for investigations with all the keywords that the user can see
     * @param sessionId
     * @param keywords
     * @param manager
     * @return
     */
    @WebMethod
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException {
        log.trace("searchByKeywords("+sessionId+", "+keywords+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, fuzzy, true, 0, 500, manager);
        //return InvestigationSearch.searchByKeywords(userId, keywords, manager);
    }
    
   /* @WebMethod
    public Collection<uk.ac.cclrc.dpal.beans.Investigation> searchByKeywordsDPAL(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException {
        log.trace("searchByKeywordsDPAL("+sessionId+", "+keywords+")");
        try {
            
            DPAccessLayer dpal = new DPAccessLayer("icat3") ;
            
            //for user bean get userId
            String userId = user.getUserId(sessionId);
            
            //now do the search using the core API
            return dpal.getInvestigations((ArrayList)keywords, userId, uk.ac.cclrc.dpal.enums.LogicalOperator.AND, fuzzy, 500, true);
        } catch (Exception ex) {
            throw new SessionException(ex.getMessage());
            //return InvestigationSearch.searchByKeywords(userId, keywords, manager);
        }
    }*/
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Investigation> searchByKeyword(String sessionId, String keywords) throws SessionException {
        log.trace("searchByKeyword("+sessionId+", "+keywords+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the core API
        Collection<Investigation> invests = InvestigationSearch.searchByKeyword(userId, keywords, manager);
        return invests;
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword) throws SessionException {
        log.trace("searchByKeywordsRtnId("+sessionId+", "+keyword+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByKeywordRtnId(userId, keyword,  manager);
        
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Investigation> testThisException(String sessionId, String keyword) throws SessionException {
        log.trace("searchByKeywordsRtnId("+sessionId+", "+keyword+")");
        
        //for user bean get userId
        String userId = "JAMES-JAMES";
     
        Collection<Investigation> investigationsId = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD).setParameter("2",keyword).setParameter("1","JAMES-JAMES").setMaxResults(300).setFirstResult(0).getResultList();
       
       //Collection<Investigation> investigationsId = manager.createNativeQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL,Investigation.class).setParameter("2","isis").setParameter("1","JAMES-JAMES").setMaxResults(300).setFirstResult(0).getResultList();
      //Collection<Investigation> investigationsId = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD+"test").setMaxResults(300).setFirstResult(0).getResultList();
      
        return investigationsId;
        
    }
    
    @WebMethod
    public Collection<String> listInstruments(String sessionId) throws SessionException {
        log.trace("listInstruments("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.listAllInstruments(userId, manager);
        
    }
    
    @WebMethod
    public Collection<Investigation> searchMyInvestigations(String sessionId) throws SessionException {
        log.trace("searchUser("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, manager);
        
    }
    
    @WebMethod
    public Collection<Investigation> searchUser(String sessionId, String userSearch) throws SessionException {
        log.trace("searchUser("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
        
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<String> searchUserKeywords(String sessionId) throws SessionException {
        log.trace("searchUser("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, "", 500, manager);
    }
}


