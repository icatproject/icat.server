/*
 * InvestigationSearchBean.java
 *
 * Created on 26 March 2007, 15:42
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
public class InvestigationSearchBean extends EJBObject implements InvestigationSearchLocal {
    
    static Logger log = Logger.getLogger(InvestigationSearchBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of InvestigationSearchBean */
    public InvestigationSearchBean() {
    }
    
    /**
     * This searches all DB for investigations with all the keywords that the user can see
     *
     * @param sessionId session id of the user.
     * @param keywords Collection of keywords to search on
     * @param include Set of information to return with investigations, ie their keywords, investigators, datasets, default none.  Having more information returned means the query will take longer.
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //now do the search using the  API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, include, fuzzy, manager);
    }
    
    /**
     * Lists all the investigations for the current user
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    @WebMethod
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.getUsersInvestigations(userId, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param searchUserId Could be DN , username or federal ID
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    @WebMethod
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return InvestigationSearch.searchByUserID(userId, userSearch, manager);
    }
    
    
}
