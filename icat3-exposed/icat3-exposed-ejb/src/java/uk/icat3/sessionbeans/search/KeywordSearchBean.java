/*
 * DatafileSearchBean.java
 *
 * Created on 27 March 2007, 10:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;


import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.KeywordSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.KeywordType;

/**
 *
 * @author gjd37
 */
@Stateless
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class KeywordSearchBean extends EJBObject implements KeywordSearchLocal {
    
    // Global class logger
    static Logger log = Logger.getLogger(KeywordSearchBean.class);
    
    /** Creates a new instance of DatafileSearchBean */
    public KeywordSearchBean() {
    }
    
    /**
     *
     * @param sessionId
     * @param userId
     * @return
     */
    public Collection<String> getKeywordsForUser(String sessionId) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, manager);
        
    }
    
    /**
     *
     * @param sessionId
     * @param startKeyword
     * @param numberReturned
     * @return
     */
    public Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, startKeyword, numberReturned, manager);
        
    }
    
    /**
     *
     * @param sessionId
     * @param type
     * @return
     */
    public Collection<String> getAllKeywords(String sessionId, KeywordType type) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getAllKeywords(userId, type, manager);
        
    }
}
