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
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
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
//@WebService(targetNamespace="client.icat3.uk")
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
     * This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId sessionId of the user.
     * @return list of keywords
     */
    @WebMethod()
    public Collection<String> getKeywordsForUser(String sessionId) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, manager);
    }
    
    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId session id of the user.
     * @param startIndex start index of the results found, default 0
     * @param number_results number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @throws uk.icat3.exceptions.SessionException
     * @return list of keywords
     */
    @WebMethod(operationName="getKeywordsForUserStartWithMax")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserStartWithMax")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserStartWithMaxResponse")
    public Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, KeywordType.ALL, startKeyword, numberReturned, manager);
    }
    
    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId session id of the user.
     * @param startIndex start index of the results found, default 0
     * @param number_results number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @throws uk.icat3.exceptions.SessionException
     * @return list of keywords
     */
    @WebMethod(operationName="getKeywordsForUserMax")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserMax")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserMaxResponse")
    public Collection<String> getKeywordsForUser(String sessionId, int numberReturned) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, KeywordType.ALL, null, numberReturned, manager);
    }
    
    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId session id of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @throws uk.icat3.exceptions.SessionException
     * @return list of keywords
     */
    @WebMethod(operationName="getKeywordsForUserType")
    @RequestWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserType")
    @ResponseWrapper(className="uk.icat3.sessionbeans.search.jaxws.getKeywordsForUserMaxType")
     //TODO: this should not be here but in glassfish UR1 and V2 RC1 it throws a
    //Caused by: Exception [TOPLINK-23005] (Oracle TopLink Essentials - 2006.8 (Build 060830)): oracle.toplink.essentials.exceptions.TransactionException
    //Exception Description: Error binding to externally managed transaction
    //Internal Exception: java.lang.IllegalStateException: Operation not allowed
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<String> getKeywordsForUser(String sessionId, KeywordType type) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getKeywordsForUser(userId, type, manager);
    }
    
    /**
     *
     * @param sessionId
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @return collection
     */
    @WebMethod()
    //TODO: this should not be here but in glassfish UR1 and V2 RC1 it throws a
    //Caused by: Exception [TOPLINK-23005] (Oracle TopLink Essentials - 2006.8 (Build 060830)): oracle.toplink.essentials.exceptions.TransactionException
    //Exception Description: Error binding to externally managed transaction
    //Internal Exception: java.lang.IllegalStateException: Operation not allowed
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<String> getAllKeywords(String sessionId, KeywordType type) throws SessionException{
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return KeywordSearch.getAllKeywords(userId, type, manager);
    }
}
