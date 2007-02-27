/*
 * InvestigationSearch.java
 *
 * Created on 20 February 2007, 11:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import static uk.icat3.util.Queries.*;
/**
 * This is the service to allows access to search through the icat schema.
 * Checks are made through SQL and JPQL for access rights to view investigations
 *
 * @author Glen Drinkwater
 */
public class InvestigationSearch {
    
    
    // Global class logger
    static Logger log = Logger.getLogger(InvestigationSearch.class);
    
    
    //used for type of user search
    private enum SearchType { SURNAME, USERID };
    
   
    
    private static Collection<Long>  searchByKeywordRtnIdImpl(String userId, String keyword, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeyword("+userId+", "+keyword+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<Long> investigationsId = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigationsId = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID).setParameter(1,userId).setParameter(2,"%"+keyword+"%").setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            //list all Investigation ids that the users has access to
            investigationsId = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID).setParameter(1,userId).setParameter(2,"%"+keyword+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        return investigationsId;
    }
    
    /**
     *
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId userId of the user.  Could be USERID , username or federal ID
     * @param keyword
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Long> searchByKeywordRtnId(String userId, String keyword, EntityManager manager) throws InsufficientPrivilegesException {
        //search and return all investigations
        return  searchByKeywordRtnIdImpl(userId, keyword, -1, -1, manager);
    }
    
    /**
     *
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId
     * @param keyword
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    private static Collection<Investigation>  searchByKeywordImpl(String userId, String keyword, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeyword("+userId+", "+keyword+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD).setParameter(1,userId).setParameter(2,"%"+keyword+"%").setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            //list all Investigation ids that the users has access to
            investigations = manager.createNamedQuery(INVESTIGATION_NATIVE_LIST_BY_KEYWORD).setParameter(1,userId).setParameter(2,"%"+keyword+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        return investigations;
    }
    
    /**
     *
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId userId of the user.  Could be USERID , username or federal ID
     * @param keyword
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeyword(String userId, String keyword, EntityManager manager) throws InsufficientPrivilegesException {
        //search and return all investigations
        return  searchByKeywordImpl(userId, keyword, -1, -1, manager);
    }
    
    /**
     *
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId userId of the user.  Could be USERID , username or federal ID
     * @param keyword
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeyword(String userId, String keyword, int startIndex, int number_results, EntityManager manager) {
        return  searchByKeywordImpl(userId, keyword, startIndex, number_results, manager);
    }
    
    
    /**
     *
     * Searches the investigations the user has access to view user id or surname
     *
     * @param userId
     * @param surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    private  static Collection<Investigation> searchByUserSurnameImpl(String userId, String searchString, SearchType searchType, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByUserImpl("+userId+", "+searchType+", "+searchString+", "+startIndex+", "+number_results+", EntityManager)");
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            
            //get all, maybe should limit this to 500?
            if(searchType == searchType.SURNAME){
                
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_SURNAME).setParameter("userId",userId).setParameter("surname","%"+searchString+"%").setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            } else {
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).setParameter("userId",userId).setParameter("userIdSearched","%"+searchString+"%").setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            }
        } else {
            if(searchType == searchType.SURNAME){
                //list all Investigation ids that the users has access to
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_SURNAME).setParameter("userId",userId).setParameter("surname","%"+searchString+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            } else {
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).setParameter("userId",userId).setParameter("userIdSearched","%"+searchString+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            }
        }
        return investigations;
    }
    
    
    /**
     *
     * Searches the investigations the user has access to view by surname
     *
     * @param userId userId of the user.
     * @param surname
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserSurname(String userId, String surname, EntityManager manager)  {
        //search and return all investigations
        return  searchByUserSurnameImpl(userId, surname, SearchType.SURNAME, -1, -1, manager);
    }
    
    
    /**
     *
     ** Searches the investigations the user has access to view by surname
     *
     * @param userId userId of the user.
     * @param surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserSurname(String userId, String surname, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserSurnameImpl(userId, surname, SearchType.SURNAME, startIndex, number_results, manager);
    }
    
    /**
     *
     * Searches the investigations the user has access to view by user id
     * @param userId userId of the user.
     * @param searchUserId Could be DN , username or federal ID
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, EntityManager manager) {
        //search and return all investigations
        return  searchByUserSurnameImpl(userId, searchUserId, SearchType.USERID, -1, -1, manager);
    }
    
    
    /**
     *
     * Searches the investigations the user has access to view by user id
     *
     * @param userId userId of the user.
     * @param searchUserId  Could be DN , username or federal ID
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserSurnameImpl(userId, searchUserId, SearchType.USERID, startIndex, number_results, manager);
    }
    
    
    /**
     *
     *  Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId
     * @param advanDTO
     * @param startIndex
     * @param number_results
     * @param manager
     * @return collection of {@link Investigation} investigation objects
     */
    private static Collection<Investigation> searchByAdvancedImpl(String userId, AdvancedSearchDTO advanDTO, int startIndex, int number_results, EntityManager manager){
        if(advanDTO == null) throw new IllegalArgumentException("AdvancedSearchDTO cannot be null");
        log.trace("searchByAdvancedImpl("+userId+", "+advanDTO);
        
        Query query = manager.createNamedQuery(ADVANCED_SEARCH);
        query = query.setParameter("userId",userId);
        
        //add all of the advanced search criteria
        //  query = query.setParameter("year",advanDTO.getYear());
        query = query.setParameter("investigationName",advanDTO.getInvestigationName());
        query = query.setParameter("sampleName",advanDTO.getSampleName());
        query = query.setParameter("investigatorName",advanDTO.getSampleType());
        query = query.setParameter("startDate",advanDTO.getYearRangeStart());
        query = query.setParameter("endDate",advanDTO.getYearRangeEnd());
        query = query.setParameter("instrument",advanDTO.getInstrument());
        
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            return query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            return query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
    }
    
    /**
     *
     *    Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId
     * @param advanDTO
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDTO advanDTO, int startIndex, int number_results, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, startIndex, number_results, manager);
    }
    
    /**
     *
     *  Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId
     * @param advanDTO
     * @param manager
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDTO advanDTO, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, -1, -1, manager);
    }
    
    
    /**
     *
     *  Gets all the investigations associated with that user
     *
     * @param userId
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     *
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, int startIndex, int number_results, EntityManager manager){
        log.trace("getUserInvestigations("+userId+", "+startIndex+", "+number_results+", EnitiyManager)");
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            return  manager.createNamedQuery(INVESTIGATIONS_FOR_USER).setParameter("userId",userId).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            return  manager.createNamedQuery(INVESTIGATIONS_FOR_USER).setParameter("userId",userId).setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
    }
    
    /**
     * Gets all the investigations associated with that user
     *
     * @param userId
     * @param manager
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, EntityManager manager){
        return getUsersInvestigations(userId,-1, -1, manager);
    }
    
    public static Collection<Long> getUsersInvestigationsRtnId(String userId, EntityManager manager){
        log.trace("getUsersInvestigationsRtnId("+userId+", EnitiyManager)");
        
        return  manager.createNamedQuery(INVESTIGATIONS_FOR_USER_RTN_ID).setParameter("userId",userId).getResultList();
        
    }
    
    
    /**
     * Search by keywords
     *
     * @param userId userId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator LogicalOperator, either AND or OR, default AND
     * @param include Set of information to return with investigations, ie their keywords, investigators, datasets, default none.  Having more information returned means the query will take longer.
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param use_security search all investigations regardless of who owns it, default true
     * @param startIndex start index of the results found, default 0
     * @param number_results number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator,  InvestigationInclude include, boolean fuzzy, boolean use_security, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeyword("+userId+", "+keywords+", "+operator +", "+include+", "+fuzzy+", "+use_security+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<Investigation> investigations = null;
        
        //dynamically create the SQL
        String SQL = INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL;
        
        int i  = 2;
        if(fuzzy){
            for(String keyword : keywords){
                if(i == 2) SQL = SQL + "NAME LIKE ?"+(i++);
                else  SQL = SQL +" OR NAME LIKE ?"+(i++);
                
            }
        } else {
            for(String keyword : keywords){
                if(i == 2) SQL = SQL + "NAME = ?"+(i++);
                else  SQL = SQL +" OR NAME = ?"+(i++);
            }
        }
        
        //need to do this if used a EJB cos of the hashcode difference if LogicalOperator is serialized
        //so == and equals do not work, only on string of object
        if(operator.toString().equals(LogicalOperator.AND.toString())) {
            SQL = SQL +" GROUP BY ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT," +
                    " RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, " +
                    "FACILITY_CYCLE HAVING Count(*) = ?number_keywords";        }
        
        log.info("DYNAMIC SQL GENERATED: "+SQL);
        
        //set query with investigation as entity object
        Query query = manager.createNativeQuery(SQL, Investigation.class);
        
        //use security??
        if(use_security) query = query.setParameter("userId",userId);
        else query = query.setParameter("userId","%");
        
        //set keywords
        int j = 2;
        for(String keyword : keywords){
            if(fuzzy) query = query.setParameter(j++,"%"+keyword+"%");
            else query.setParameter(j++,keyword);
        }
        
        //add in the number of keywords
        query.setParameter("number_keywords",keywords.size());
        
        //run query
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations =  query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            investigations = query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        
        ManagerUtil.getInvestigationInformation(investigations,include);
        
        return investigations;
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, boolean fuzzy, EntityManager manager)  {
        //secuirty on, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, includes, fuzzy ,true , -1, -1, manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, boolean fuzzy, EntityManager manager)  {
        //secuirty on, AND, no includes
        return searchByKeywords(userId, keywords, LogicalOperator.AND, InvestigationInclude.NONE, fuzzy ,true , -1, -1, manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, InvestigationInclude.NONE, false ,true ,-1 , -1,manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, includes, false ,true ,-1 , -1,manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, EntityManager manager)  {
        //exact match, security true, AND
        return searchByKeywords(userId, keywords, operator, InvestigationInclude.NONE, false ,true ,-1 , -1,manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, LogicalOperator operator, EntityManager manager)  {
        //exact match, security true, AND
        return searchByKeywords(userId, keywords, operator, includes,  false ,true ,-1 , -1,manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, boolean fuzzy, EntityManager manager)  {
        //exact match, security true,
        return searchByKeywords(userId, keywords, operator, InvestigationInclude.NONE, fuzzy ,true ,-1 , -1,manager);
    }
    
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude includes, boolean fuzzy, EntityManager manager)  {
        //exact match, security true,
        return searchByKeywords(userId, keywords, operator, includes, fuzzy ,true ,-1 , -1,manager);
    }
    
}
