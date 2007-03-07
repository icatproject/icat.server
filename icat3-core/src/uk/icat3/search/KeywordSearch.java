/*
 * KeywordSearch.java
 *
 * Created on 22 February 2007, 08:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import javax.persistence.EntityManager;
import oracle.sql.ARRAY;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.util.Queries;
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
     * investigations or public investigations
     *
     * @param manager
     * @return
     */
    public static Collection<String> getAllKeywords(String userId, EntityManager manager){
        log.trace("getAllKeywords("+userId+", EntityManager)");
        Collection<String> keywords = manager.createNamedQuery(ALLKEYWORDS).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        
        return keywords;
    }    
}
