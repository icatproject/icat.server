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
import uk.icat3.exceptions.LoginException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.user.UserManager;
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
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include) throws LoginException {
        log.trace("searchByKeywords("+sessionId+", "+keywords+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String userId = userManager.getUserIdFromSessionId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND,include, false, true,0, 500, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Investigation> searchByKeyword(String sessionId, String keywords) throws LoginException {
        log.trace("searchByKeyword("+sessionId+", "+keywords+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        // UserManager userManager = new UserManager(managerUser);
        //String userId = userManager.getUserIdFromSessionId(sessionId);
        String userId = user.getUserId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeyword(userId, keywords, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword) throws LoginException {
        log.trace("searchByKeywordsRtnId("+sessionId+", "+keyword+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        //  UserManager userManager = new UserManager(managerUser);
        // String userId = userManager.getUserIdFromSessionId(sessionId);
        String userId = user.getUserId(sessionId);
        
       /* Collection<Long> ids = new ArrayList<Long>();
        Collection<String> key = new ArrayList<String>();
        key.add(keyword);
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(userId, key,LogicalOperator.AND,InvestigationInclude.NONE, false, true,0, 500, manager);
        
        for(Investigation investigation :investigations){
            ids.add(investigation.getId());
        }
        
        //now do the search using the core API
        return ids;*/
        return InvestigationSearch.searchByKeywordRtnId(userId, keyword,  manager);
        
    }
    
    @WebMethod
    public Collection<String> listInstruments(String sessionId) throws LoginException {
        log.trace("listInstruments("+sessionId+")");
        
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String userId = userManager.getUserIdFromSessionId(sessionId);
        
        
        return InvestigationSearch.listAllInstruments(userId, manager);
        
    }
    
    @WebMethod
    public Collection<Investigation> searchUser(String sessionId, String userSearch) throws LoginException {
        log.trace("searchUser("+sessionId+")");
        
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
       // UserManager userManager = new UserManager(managerUser);
        //String userId = userManager.getUserIdFromSessionId(sessionId);
        String userId = user.getUserId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
        
    }
}


