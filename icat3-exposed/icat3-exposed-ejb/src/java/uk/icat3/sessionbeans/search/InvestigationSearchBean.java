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
     * @param sessionId
     * @param keywords
     * @param manager
     * @return
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
    
}
