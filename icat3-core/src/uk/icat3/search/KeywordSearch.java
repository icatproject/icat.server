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
import java.util.HashSet;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
public class KeywordSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(KeywordSearch.class);
    
    /**
     * 
     * @param userId 
     * @param manager 
     * @return 
     */
    public static Collection<String> getKeywordsForUser(String userId, EntityManager manager){
        log.trace("getKeywordsForUser("+userId+", EntityManager)");
        
        Collection<Investigation> investigations = manager.createNamedQuery(Queries.INVESTIGATIONS_BY_USER).setParameter("userId",userId).getResultList();
        
        //Turn into String Array
        Collection<String> keywords = new HashSet<String>();
        for(Investigation investigation : investigations){
            for(Keyword keyword : investigation.getKeywordCollection()){
                keywords.add(keyword.getKeywordPK().getName());
            }
        }
        return keywords;
        
    }
    
    /**
     * 
     * @param manager 
     * @return 
     */
    public static Collection<String> getAllKeywords(EntityManager manager){
        log.trace("getAllKeywords(EntityManager)");
        Collection<String> keywords = manager.createNamedQuery(Queries.ALLKEYWORDS_NATIVE).getResultList();
        
        return keywords;
    }
    
}
