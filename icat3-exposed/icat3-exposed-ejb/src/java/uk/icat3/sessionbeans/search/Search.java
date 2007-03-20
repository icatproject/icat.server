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
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.sessionbeans.EJBObject;
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
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include)  {
        log.trace("searchByKeywords("+sessionId+", "+keywords+", EntityManager)");
        String userId = null;
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        
        //but force user instead
        userId = "JAMES-JAMES";
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND,include,false, true,0, 100, manager);
    }
    
     @WebMethod
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword, InvestigationInclude include)  {
        log.trace("searchByKeywords("+sessionId+", "+keyword+", EntityManager)");
        String userId = null;
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        
        //but force user instead
        userId = "JAMES-JAMES";
        
        //now do the search using the core API
        return InvestigationSearch.searchByKeywordRtnId(userId, keyword, manager);
    }
    
    
}


