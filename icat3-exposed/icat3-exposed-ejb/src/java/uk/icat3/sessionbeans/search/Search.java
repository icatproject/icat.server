/*
 * Search.java
 *
 * Created on 12 March 2007, 08:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordSearch;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */

@Stateless()
@WebService()
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Search extends EJBObject {
    
    
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
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include) throws SessionException {
        log.trace("searchByKeywords("+sessionId+", "+keywords+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND,include, false, true,0, 500, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Investigation> searchByKeyword(String sessionId, String keywords) throws SessionException {
        log.trace("searchByKeyword("+sessionId+", "+keywords+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeyword(userId, keywords, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword) throws SessionException {
        log.trace("searchByKeywordsRtnId("+sessionId+", "+keyword+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        return InvestigationSearch.searchByKeywordRtnId(userId, keyword,  manager);
        
    }
    
    @WebMethod
    public Collection<String> listInstruments(String sessionId) throws SessionException {
        log.trace("listInstruments("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        return InvestigationSearch.listAllInstruments(userId, manager);
        
    }
    
    @WebMethod
    public Collection<Investigation> searchUser(String sessionId, String userSearch) throws SessionException {
        log.trace("searchUser("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
        
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<String> searchUserKeywords(String sessionId) throws SessionException {
        log.trace("searchUser("+sessionId+")");
        
        //for user bean get userId
        String userId = user.getUserId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, "", 500, manager);          
    }
}


