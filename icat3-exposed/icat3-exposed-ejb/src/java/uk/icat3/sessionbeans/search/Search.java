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
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.LoginException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.exception.LoginError;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */

@Stateless()
@WebService()
public class Search extends EJBObject implements SearchLocal {
    
    static Logger log = Logger.getLogger(Search.class);
    
    /**
     * This searches all DB for investigations with all the keywords that the user can see
     * @param sessionId
     * @param keywords
     * @param manager
     * @return
     */
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include) throws LoginException {
        log.trace("searchByKeywords("+sessionId+", "+keywords+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String userId = userManager.getUserIdFromSessionId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND,include, false, true,0, 500, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword, InvestigationInclude include) throws LoginException {
        log.trace("searchByKeywords("+sessionId+", "+keyword+")");
      
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String userId = userManager.getUserIdFromSessionId(sessionId);
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywordRtnId(userId, keyword, manager);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public String login(String username, String password) throws LoginException {
        log.trace("login("+username+", "+password+")");
      
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String sessionId = userManager.login(username,password);
        
        return sessionId;
    }
    
    
}


