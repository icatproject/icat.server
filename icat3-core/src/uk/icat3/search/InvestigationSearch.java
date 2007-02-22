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
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.Queries;

/**
 * This is the service to allows access to the search throught that icat schema.
 * Checks are made through SQL and JPQL for access rights to view investigation
 *
 * @author Glen Drinkwater
 */
public class InvestigationSearch {
    
    
    // Global class logger
    static Logger log = Logger.getLogger(InvestigationSearch.class);
    
    
    //used for type of user search
    private enum SearchType { SURNAME, USERID };
    
    /**
     *
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId
     * @param keyword
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @return collection of {@link Investigation} investigation objects
     */
    private static Collection<Investigation>  searchByKeywordImpl(String userId, String keyword, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeyword("+userId+", "+keyword+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD).setParameter(1,userId).setParameter(2,"%"+keyword+"%").getResultList();
        } else {
            //list all Investigation ids that the users has access to
            investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD).setParameter(1,userId).setParameter(2,"%"+keyword+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
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
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
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
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
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
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @return collection of {@link Investigation} investigation objects
     */
    private  static Collection<Investigation> searchByUserImpl(String userId, String searchString, SearchType searchType, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByUserImpl("+userId+", "+searchType+", "+searchString+", "+startIndex+", "+number_results+", EntityManager)");
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            if(searchType == searchType.SURNAME){
                investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_SURNAME).setParameter(1,userId).setParameter(2,"%"+searchString+"%").getResultList();
            } else {
                investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_USERID).setParameter(1,userId).setParameter(2,"%"+searchString+"%").getResultList();
            }
        } else {
            if(searchType == searchType.SURNAME){
                //list all Investigation ids that the users has access to
                investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_SURNAME).setParameter(1,userId).setParameter(2,"%"+searchString+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            } else {
                investigations = manager.createNamedQuery(Queries.INVESTIGATION_NATIVE_LIST_BY_USERID).setParameter(1,userId).setParameter(2,"%"+searchString+"%").setMaxResults(number_results).setFirstResult(startIndex).getResultList();
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
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    public static Collection<Investigation> searchByUser(String userId, String surname, EntityManager manager) throws InsufficientPrivilegesException {
        //search and return all investigations
        return  searchByUserImpl(userId, surname, SearchType.SURNAME, -1, -1, manager);
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
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    public static Collection<Investigation> searchByUser(String userId, String surname, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserImpl(userId, surname, SearchType.SURNAME, startIndex, number_results, manager);
    }
    
    /**
     *
     * Searches the investigations the user has access to view user id
     *
     * @param userId userId of the user.
     * @param searchUserId  Could be DN , username or federal ID
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, EntityManager manager) throws InsufficientPrivilegesException {
        //search and return all investigations
        return  searchByUserImpl(userId, searchUserId, SearchType.USERID, -1, -1, manager);
    }
    
    
    /**
     *
     * Searches the investigations the user has access to view user id
     *
     * @param userId userId of the user.
     * @param searchUserId  Could be DN , username or federal ID
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserImpl(userId, searchUserId, SearchType.USERID, startIndex, number_results, manager);
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
     * @return
     */
    private static Collection<Investigation> searchByAdvancedImpl(String userId, AdvancedSearchDTO advanDTO,int startIndex, int number_results, EntityManager manager){
        if(advanDTO == null) throw new IllegalArgumentException("AdvancedSearchDTO cannot be null");
        log.trace("searchByAdvancedImpl("+userId+", "+advanDTO);
        
        Query query = manager.createNamedQuery(Queries.ADVANCED_SEARCH);
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
            return query.getResultList();
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
     * @param startIndex
     * @param number_results
     * @param manager
     * @return
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDTO advanDTO,int startIndex, int number_results, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, startIndex, number_results, manager);
    }
    
    /**
     *
     *  Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId
     * @param advanDTO
     * @param manager
     * @return
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDTO advanDTO, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, -1, -1, manager);
    }
    
    
    /**
     *
     *  Gets all the investigations associated with that user
     *
     * @param userId
     * @param startIndex
     * @param number_results
     * @param manager
     * @return
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, int startIndex, int number_results, EntityManager manager){
        log.trace("getUserInvestigations("+userId+", "+startIndex+", "+number_results+", EnitiyManager)");
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            return  manager.createNamedQuery(Queries.INVESTIGATIONS_FOR_USER).setParameter("userId",userId).getResultList();
        } else {
            return  manager.createNamedQuery(Queries.INVESTIGATIONS_FOR_USER).setParameter("userId",userId).setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
    }
    
    /**
     * Gets all the investigations associated with that user
     *
     * @param userId
     * @param manager
     * @return
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, EntityManager manager){
        return getUsersInvestigations(userId,-1, -1, manager);
    }
    
}
