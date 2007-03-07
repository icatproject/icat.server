/*
 * KeywordSearch.java
 *
 * Created on 22 February 2007, 08:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.util.KeywordType;
import static uk.icat3.util.Queries.*;
/**
 *
 * @author gjd37
 */
public class KeywordSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(KeywordSearch.class);
    
    /**
     *
     *  This gets all the keywords in the database.
     *
     * @param userId
     * @param manager
     * @return
     */
    public static Collection<String> getKeywordsForUser(String userId, EntityManager manager){
        log.trace("getKeywordsForUser("+userId+", EntityManager)");
        
        return getKeywordsForUser(userId, null, manager);
        
    }
    
    /**
     *
     *  This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param userId
     * @param manager
     * @return
     */
    public static Collection<String> getKeywordsForUser(String userId, String startKeyword, EntityManager manager){
        log.trace("getKeywordsForUser("+userId+", EntityManager)");
        
        if(startKeyword != null) startKeyword = startKeyword+"%";
        Collection<String> keywords = manager.createNamedQuery(KEYWORDS_FOR_USER).setParameter("userId",userId).setParameter("startKeyword", startKeyword).getResultList();
        
        return keywords;
    }
    
    
    /**
     * This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations.
     *
     * Types,  ALPHA, ALPHA_NUMERIC only work with oracle DBs
     *
     * @return
     * @param userId
     * @param type ALL, ALPHA, ALPHA_NUMERIC
     * @param manager
     */
    public static Collection<String> getAllKeywords(String userId, KeywordType type, EntityManager manager){
        log.trace("getAllKeywords("+userId+", EntityManager)");
        Collection<String> keywords  = null;
        
        if(type.toString().equals(KeywordType.ALL.toString())){
            keywords = manager.createNamedQuery(ALLKEYWORDS).getResultList();
        } else if(type.toString().equals(KeywordType.ALPHA.toString())){
            keywords = manager.createNamedQuery(ALLKEYWORDS_NATIVE_ALPHA).getResultList();
        } else if(type.toString().equals(KeywordType.ALPHA_NUMERIC.toString())){
            keywords = manager.createNamedQuery(ALLKEYWORDS_NATIVE_ALPHA_NUMERIC).getResultList();
        } else {
            keywords = manager.createNamedQuery(ALLKEYWORDS).getResultList();
        }
     
        return keywords;
    }
    
}
