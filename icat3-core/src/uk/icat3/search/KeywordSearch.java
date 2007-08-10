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
import uk.icat3.util.ElementType;
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
     *  This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return list of keywords
     */
    public static Collection<String> getKeywordsForUser(String userId, EntityManager manager){
        return getKeywordsForUser(userId, null, -1, manager);
    }
    
    /**
     *  This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param userId federalId of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @param manager manager object that will facilitate interaction with underlying database
     * @return list of keywords
     */
    //TODO finish this and add type into it
    public static Collection<String> getKeywordsForUser(String userId, KeywordType type, EntityManager manager){
        return getKeywordsForUser(userId, null, -1, manager);
    }
    
    /**
     *  This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return list of keywords
     */
    public static Collection<String> getKeywordsForUser(String userId, String startKeyword, EntityManager manager){
        return getKeywordsForUser(userId, startKeyword, -1, manager);
    }
    
    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param userId federalId of the user.
     * @param startKeyword start keyword to search
     * @param numberReturned number of results returned
     * @param manager manager object that will facilitate interaction with underlying database
     * @return list of keywords
     */
    public static Collection<String> getKeywordsForUser(String userId, String startKeyword, int numberReturned, EntityManager manager){
        log.trace("getKeywordsForUser("+userId+", EntityManager)");
        
        if(startKeyword != null) startKeyword = startKeyword+"%";
        else startKeyword = "%";
        
        if(numberReturned < 0){
            return  (Collection<String>)manager.createNamedQuery(KEYWORDS_FOR_USER).
                    setParameter("objectType", ElementType.INVESTIGATION).
                    setParameter("userId",userId).
                    setParameter("startKeyword", startKeyword).getResultList();
        } else {
            return  (Collection<String>)manager.createNamedQuery(KEYWORDS_FOR_USER).
                    setParameter("objectType", ElementType.INVESTIGATION).
                    setParameter("userId",userId).
                    setParameter("startKeyword", startKeyword).
                    setMaxResults(numberReturned).getResultList();
        }
    }
    
    
    /**
     * This gets all the unique keywords in the database
     *
     * Types,  ALPHA, ALPHA_NUMERIC only work with oracle DBs
     *
     * @param userId federalId of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @param manager manager object that will facilitate interaction with underlying database
     * @return list of keywords
     */
    public static Collection<String> getAllKeywords(String userId, KeywordType type, EntityManager manager){
        log.trace("getAllKeywords("+userId+", EntityManager)");
        Collection<String> keywords  = null;
        
        if(type.toString().equals(KeywordType.ALL.toString())){
            keywords = (Collection<String>)manager.createNamedQuery(ALLKEYWORDS).getResultList();
        } else if(type.toString().equals(KeywordType.ALPHA.toString())){
            keywords = (Collection<String>)manager.createNamedQuery(ALLKEYWORDS_NATIVE_ALPHA).getResultList();
        } else if(type.toString().equals(KeywordType.ALPHA_NUMERIC.toString())){
            keywords = (Collection<String>)manager.createNamedQuery(ALLKEYWORDS_NATIVE_ALPHA_NUMERIC).getResultList();
        } else {
            keywords = (Collection<String>)manager.createNamedQuery(ALLKEYWORDS).getResultList();
        }
        
        return keywords;
    }
    
}
