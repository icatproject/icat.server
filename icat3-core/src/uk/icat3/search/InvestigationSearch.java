/*
 * InvestigationSearch.java
 *
 * Created on 20 February 2007, 11:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import static uk.icat3.util.Queries.*;

/**
 * This is the service to allows access to search through the icat schema.
 * Checks are made through SQL and JPQL for access rights to view investigations
 *
 * @author Glen Drinkwater
 */
public class InvestigationSearch extends ManagerUtil {
    
    // Global class logger
    static Logger log = Logger.getLogger(InvestigationSearch.class);
    
    //used for type of user search
    private enum SearchType { SURNAME, USERID };
    
    
    /**
     * Searches a single keyword for a users and returns all the Id of the investigations
     *
     * @param userId federalId of the user.
     * @param keyword keywords to search
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection investigation ids
     */
    public static Collection<Long>  searchByKeywordRtnId(String userId, String keyword, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeyword("+userId+", "+keyword+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<BigDecimal> investigationsId = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigationsId = manager.createNamedQuery(INVESTIGATION_LIST_BY_KEYWORD_RTN_ID).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter(1,userId).setParameter(2,"%"+keyword+"%").
                    setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            //list all Investigation ids that the users has access to
            investigationsId = manager.createNamedQuery(INVESTIGATION_LIST_BY_KEYWORD_RTN_ID).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter(1,userId).setParameter(2,"%"+keyword+"%").
                    setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        //turn into longs
        Collection<Long> investigationsIds = new ArrayList<Long>();
        for(BigDecimal bd : investigationsId){
            investigationsIds.add(bd.longValue());
        }
        return investigationsIds;
    }
    
    /**
     * Searches a single keyword for a user and returns all the Id of the investigations
     *
     * @param userId federalId of the user.
     * @param keyword keyword to search
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of investigation ids
     */
    public static Collection<Long> searchByKeywordRtnId(String userId, String keyword, EntityManager manager)  {
        //search and return all investigations
        return  searchByKeywordRtnId(userId, keyword, -1, -1, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId federalId of the user.
     * @param keyword keyword to search
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
            investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_KEYWORD).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter("userId",userId).setParameter("keyword","%"+keyword+"%").
                    setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            //list all Investigation ids that the users has access to
            investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_KEYWORD).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter("userId",userId).setParameter("keyword","%"+keyword+"%").
                    setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        return investigations;
    }
    
    /**
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId federalId of the user.
     * @param keyword keyword to search
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeyword(String userId, String keyword, EntityManager manager)  {
        //search and return all investigations
        return  searchByKeywordImpl(userId, keyword, -1, -1, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by keyword
     *
     * @param userId federalId of the user.
     * @param keyword keyword to search
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeyword(String userId, String keyword, int startIndex, int number_results, EntityManager manager) {
        return  searchByKeywordImpl(userId, keyword, startIndex, number_results, manager);
    }
    
    /**
     * Searches the investigations the user has access to view federalId or surname
     *
     * @param userId federalId of the user.
     * @param searchString federalId or surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    private  static Collection<Investigation> searchByUserSurnameImpl(String userId, String searchString, SearchType searchType, int startIndex, int number_results, InvestigationInclude include, EntityManager manager)  {
        log.trace("searchByUserImpl("+userId+", "+searchType+", "+searchString+", "+startIndex+", "+number_results+", "+include+", EntityManager)");
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            
            //get all, maybe should limit this to 500?
            if(searchType == searchType.SURNAME){
                log.trace("Searching by SURNAME");
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_SURNAME).
                        setParameter("objectType",ElementType.INVESTIGATION).
                        setParameter("userId",userId).
                        setParameter("surname","%"+searchString.toLowerCase()+"%").
                        setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            } else {
                log.trace("Searching by USERID");
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).
                        setParameter("objectType",ElementType.INVESTIGATION).
                        setParameter("userId",userId).
                        setParameter("federalId","%"+searchString+"%").
                        setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            }
        } else {
            if(searchType == searchType.SURNAME){
                //list all Investigation ids that the users has access to
                log.trace("Searching by SURNAME");
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_SURNAME).
                        setParameter("objectType",ElementType.INVESTIGATION).
                        setParameter("userId",userId).
                        setParameter("surname","%"+searchString.toLowerCase()+"%").
                        setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            } else {
                log.trace("Searching by USERID");
                investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).
                        setParameter("objectType",ElementType.INVESTIGATION).
                        setParameter("userId",userId).
                        setParameter("federalId","%"+searchString+"%").
                        setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            }
        }
        
        //add all the investigation information to the list of investigations
        getInvestigationInformation(userId, investigations,include, manager);
        
        
        return investigations;
    }
    
    /**
     * Searches the investigations the user has access to view by investigator surname
     *
     * @param userId federalId of the user.
     * @param surname investigator surname
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserSurname(String userId, String surname, EntityManager manager)  {
        //search and return all investigations
        return  searchByUserSurnameImpl(userId, surname, SearchType.SURNAME, -1, -1, InvestigationInclude.NONE, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by investigator surname
     *
     * @param userId federalId of the user.
     * @param surname investigator surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserSurname(String userId, String surname, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserSurnameImpl(userId, surname, SearchType.SURNAME, startIndex, number_results, InvestigationInclude.NONE, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by federalId
     *
     * @param userId federalId of the user.
     * @param searchUserId federalId of user
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, EntityManager manager) {
        //search and return all investigations
        return  searchByUserSurnameImpl(userId, searchUserId, SearchType.USERID, -1, -1, InvestigationInclude.NONE, manager);
    }
    
    /**
     * Searches the investigations the user has access to view by federalId
     *
     * @param userId federalId of the user.
     * @param searchUserId federalId of user
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByUserID(String userId, String searchUserId, int startIndex, int number_results, EntityManager manager)  {
        return  searchByUserSurnameImpl(userId, searchUserId, SearchType.USERID, startIndex, number_results,InvestigationInclude.NONE, manager);
    }
    
    /**
     *  Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId federalId of the user.
     * @param advanDTO {@Link AdvancedSearchDetails}
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    private static Collection<Investigation> searchByAdvancedImpl(String userId, AdvancedSearchDetails advanDTO, int startIndex, int number_results, EntityManager manager){
        if(advanDTO == null || !advanDTO.isValid()) throw new IllegalArgumentException("AdvancedSearchDTO cannot be null");
        log.trace("searchByAdvancedImpl("+userId+", "+advanDTO);
        
        Collection<Investigation> investigations = null;
        
        //dynamically create the query
        String JPQL = ADVANCED_SEARCH_JPQL_START;
        
        if(advanDTO.hasSample()){
            log.trace("Searching sample info");
            //  " AND EXISTS (SELECT sample FROM i.sampleCollection sample WHERE sample.name LIKE :sampleName AND " +
            //  " sample.markedDeleted = 'N') "+//iterate, remove if no sample is null
            JPQL += "AND EXISTS (SELECT sample FROM i.sampleCollection sample WHERE sample.name LIKE :sampleName AND " +
                    " sample.markedDeleted = 'N') ";
        }
        
        if(advanDTO.hasInstruments()){
            log.trace("Searching instruments info");
            //add insturments section:
            //" AND i.instrument.name IN(:instrument)  AND i.instrument.markedDeleted = 'N' "+ //expand IN, remove this if instrument null
            JPQL += " AND i.instrument.name IN(";
            //add in the instruments in the IN() cause of JPQL
            int i = 1;
            for(String instrument : advanDTO.getInstruments()){
                if(i == advanDTO.getInstruments().size()) JPQL += ":instrument"+(i++)+"";
                else  JPQL += ":instrument"+(i++)+" , ";
            }
            JPQL += ") AND i.instrument.markedDeleted = 'N' ";
        }
        
        if(advanDTO.hasKeywords()){
            log.trace("Searching keywords info");
            //add keywords section:
            // AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.markedDeleted = 'N' AND kw.keywordPK.name LIKE :keyword1)
            
            int i = 1;
            for(String keyword : advanDTO.getKeywords()){
                JPQL += " AND EXISTS (SELECT kw"+i+" FROM i.keywordCollection kw"+i+" WHERE kw"+i+".markedDeleted = 'N' AND kw"+i+".keywordPK.name LIKE :keyword"+(i++)+") ";
            }
            JPQL += " ";
        }
        
        if(advanDTO.hasInvestigators()){
            log.trace("Searching investigators info");
            //add investigator section:
            //" AND EXISTS ( SELECT inv FROM i.investigatorCollection inv WHERE " +
            //   "LOWER(inv.facilityUser.lastName) LIKE :surname AND inv.markedDeleted = 'N')  "+ //iterate, remove this if investigator null
            
            int i = 1;
            for(String investigators : advanDTO.getInvestigators()){
                JPQL += " AND EXISTS (SELECT inv"+i+" FROM i.investigatorCollection inv"+i+" WHERE inv"+i+".markedDeleted = 'N' AND LOWER(inv"+i+".facilityUser.lastName) LIKE :surname"+(i++)+") ";
            }
            JPQL += " ";
        }
        
        if(advanDTO.hasDataFileParameters()){
            log.trace("Searching data file info");
            //add data file and run number section
            //             " AND EXISTS (SELECT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
            //            " df.id = iadf3.elementId AND iadf3.elementType = :dataFileType AND df.markedDeleted = 'N' " +
            //            " AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')" +
            //            " AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y' " +
            //            " AND df.dataset.investigation = i AND (df.createTime > :lowerTime OR :lowerTime IS NULL AND df.createTime < :upperTime OR :upperTime IS NULL) AND " +
            //            " df.markedDeleted = 'N' AND (df.name = :datafileName OR :datafileName IS NULL))  " ; //remove if all are null
            //
            JPQL += ADVANCED_SEARCH_JPQL_DATAFILE;
        }
        
        if(advanDTO.hasRunNumber()){
            log.trace("Searching run number");
            //add data file and run number section
            //    "EXISTS (SELECT dfp FROM DatafileParameter dfp, IcatAuthorisation ia2 " +
            //    " WHERE dfp.datafile.id = ia2.elementId AND ia2.elementType = :dataFileType AND dfp.markedDeleted = 'N' " +
            //    " AND (ia2.userId = :userId OR ia2.userId = 'ANY')" +
            //    " AND ia2.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND ia2.role.actionCanSelect = 'Y' AND dfp.datafile.dataset.investigation = i AND dfp.numericValue BETWEEN :lower AND :upper AND " +
            //    "dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N')"; //remove this if run number null
            
            JPQL += ADVANCED_SEARCH_JPQL_DATAFILE_PARAMETER;
        }
        
        //set all the paramaters now
        //set query with datafile as entity object
        Query query = manager.createQuery(JPQL);
        
        //sets the paramters
        query = query.setParameter("userId",userId);
        query = query.setParameter("invTitle", advanDTO.getInvestigationName());
        query = query.setParameter("bcatInvStr", advanDTO.getBackCatalogueInvestigatorString());
        query = query.setParameter("invNumber", advanDTO.getExperimentNumber());
        query = query.setParameter("visitId", advanDTO.getVisitId());
        query = query.setParameter("invType", advanDTO.getInvestigationType());
        query = query.setParameter("grantId", advanDTO.getGrantId());
        query = query.setParameter("objectType", ElementType.INVESTIGATION);
        query = query.setParameter("invAbstract",advanDTO.getInvestigationAbstract());
        
        if(advanDTO.hasSample()){
            query = query.setParameter("sampleName", advanDTO.getSampleName());
        }
        
        if(advanDTO.hasDataFileParameters()){
            query = query.setParameter("datafileName", advanDTO.getDatafileName());
            query = query.setParameter("dataFileType", ElementType.DATAFILE);
            query = query.setParameter("upperTime", advanDTO.getYearRangeEnd());
            query = query.setParameter("lowerTime", advanDTO.getYearRangeStart());
        }
        
        //set upper run number
        if(advanDTO.hasRunNumber()){
            query = query.setParameter("upper", advanDTO.getRunEnd());
            query = query.setParameter("lower", advanDTO.getRunStart());
            query = query.setParameter("dataFileType", ElementType.DATAFILE);
        }
        
        //set instruments
        if(advanDTO.hasInstruments()){
            int j = 1;
            for(String instrument : advanDTO.getInstruments()){
                query = query.setParameter("instrument"+j++,instrument);
            }
        }
        
        //set instruments
        if(advanDTO.hasKeywords()){
            int j = 1;
            for(String keyword : advanDTO.getKeywords()){
                if(advanDTO.isFuzzy()) query = query.setParameter("keyword"+j++,"%"+keyword+"%");
                else query = query.setParameter("keyword"+j++,keyword);
            }
        }
        
        //set investigators
        if(advanDTO.hasInvestigators()){
            int j = 1;
            for(String investigator : advanDTO.getInvestigators()){
                if(advanDTO.isFuzzy()) query = query.setParameter("surname"+j++,"%"+investigator.toLowerCase()+"%");
                else query = query.setParameter("surname"+j++,investigator.toLowerCase());
            }
        }
        
        log.trace("DYNAMIC JPQL: "+JPQL);
        
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations = query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            investigations = query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        
        //add all the investigation information to the list of investigations
        getInvestigationInformation(userId, investigations, advanDTO.getInvestigationInclude(), manager);
        
        return investigations;
    }
    
    /**
     * Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId federalId of the user.
     * @param advanDTO {@Link AdvancedSearchDetails}
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDetails advanDTO, int startIndex, int number_results, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, startIndex, number_results, manager);
    }
    
    /**
     * Searches investigations from the ones they can view by the advanced criteria
     *
     * @param userId federalId of the user.
     * @param advanDTO {@Link AdvancedSearchDetails}
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByAdvanced(String userId, AdvancedSearchDetails advanDTO, EntityManager manager){
        return searchByAdvancedImpl(userId, advanDTO, -1, -1, manager);
    }
    
    /**
     *  Gets all the investigations associated with that user, ie. that they are investigator of.
     *
     * @param userId federalId of the user.
     * @param include information that is needed to be returned with the investigation
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, InvestigationInclude include, int startIndex, int number_results, EntityManager manager){
        log.trace("getUserInvestigations("+userId+", "+startIndex+", "+number_results+", EnitiyManager)");
        
        Collection<Investigation> investigations = null;
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter("federalId",userId).
                    setParameter("userId",userId).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            investigations = manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID).
                    setParameter("objectType",ElementType.INVESTIGATION).
                    setParameter("federalId",userId).
                    setParameter("userId",userId).setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        
        //add include information
        getInvestigationInformation(userId, investigations, include, manager);
        
        return investigations;
    }
    
    /**
     *  Gets all the investigations associated with that user, ie. that they are investigator of.
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, EntityManager manager){
        return getUsersInvestigations(userId, InvestigationInclude.NONE, -1, -1, manager);
    }
    
    /**
     *  Gets all the investigations associated with that user, ie. that they are investigator of.
     *
     * @param userId federalId of the user
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, int startIndex, int number_results, EntityManager manager){
        return getUsersInvestigations(userId, InvestigationInclude.NONE, startIndex, number_results, manager);
    }
    
    /**
     *  Gets all the investigations associated with that user, ie. that they are investigator of.
     *
     * @param userId federalId of the user.
     * @param include information that is needed to be returned with the investigation
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getUsersInvestigations(String userId, InvestigationInclude include, EntityManager manager){
        return getUsersInvestigations(userId, include, -1, -1, manager);
    }
    
    /**
     *  Gets all the investigation ids associated with that user, ie. thart they are investigator of.
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation ids
     */
    public static Collection<Long> getUsersInvestigationsRtnId(String userId, EntityManager manager){
        log.trace("getUsersInvestigationsRtnId("+userId+", EnitiyManager)");
        
        return  manager.createNamedQuery(INVESTIGATION_LIST_BY_USERID_RTID).
                setParameter("objectType",ElementType.INVESTIGATION).
                setParameter("userId",userId).
                setParameter("federalId",userId).getResultList();
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator {@link LogicalOperator}, either AND or OR, default AND
     * @param include {@link InvestigationInclude}
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param use_security search all investigations regardless of who owns it, default true
     * @param startIndex start index of the results found, default 0
     * @param number_results number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator,  InvestigationInclude include, boolean fuzzy, boolean use_security, int startIndex, int number_results, EntityManager manager)  {
        log.trace("searchByKeywords("+userId+", "+keywords+", "+operator +", "+include+", fuzzy? "+fuzzy+", secure? "+use_security+", "+startIndex+", "+number_results+", EntityManager)");
        
        Collection<Investigation> investigations = null;
        String JPQL = null;
        
        //dynamically create the SQL
        if(use_security)  JPQL = INVESTIGATION_LIST_BY_KEYWORDS_JPQL;
        else  JPQL = INVESTIGATION_LIST_BY_KEYWORDS_JPQL_NOSECURITY;
        
        // String KEYWORDSEARCH = " EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.markedDeleted = 'N' AND kw.keywordPK.name ";
        // Need to generate this
        // AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.markedDeleted = 'N' AND kw.keywordPK.name LIKE :keyword1)
        
        int i  = 2;
        //check if fuzzy
        if(fuzzy){
            //fuzzy so LIKE
            for(String keyword : keywords){
                if(i == 2) JPQL += " AND EXISTS (SELECT kw"+i+" FROM i.keywordCollection kw"+i+" WHERE kw"+i+".markedDeleted = 'N' AND kw"+i+".keywordPK.name  LIKE :"+(i++)+") ";
                else  JPQL += " "+operator+" EXISTS (SELECT kw"+i+" FROM i.keywordCollection kw"+i+" WHERE kw"+i+".markedDeleted = 'N' AND kw"+i+".keywordPK.name  LIKE :"+(i++)+") ";
                
            }
        } else {
            //none fuzzy, =
            for(String keyword : keywords){
                if(i == 2) JPQL += " AND EXISTS (SELECT kw"+i+" FROM i.keywordCollection kw"+i+" WHERE kw"+i+".markedDeleted = 'N' AND kw"+i+".keywordPK.name = ?"+(i++)+") ";
                else  JPQL += " "+operator+" EXISTS (SELECT kw"+i+" FROM i.keywordCollection kw"+i+" WHERE kw"+i+".markedDeleted = 'N' AND kw"+i+".keywordPK.name = ?"+(i++)+") ";
            }
        }
        
        log.info("DYNAMIC JPQL GENERATED: "+JPQL);
        
        //set query with investigation as entity object
        Query query = manager.createQuery(JPQL);
        
        //use security??
        if(use_security) {
            query.setParameter("objectType",ElementType.INVESTIGATION);
            query.setParameter("userId",userId);
        }
        //else query = query.setParameter("userId","%");
        
        //set keywords
        int j = 2;
        for(String keyword : keywords){
            if(fuzzy) query = query.setParameter(j++,"%"+keyword+"%");
            else query.setParameter(j++,keyword);
        }
        
        //run query
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            investigations = query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            investigations = query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        
        log.trace("number of investigations returned is: "+investigations.size());
        //add all the investigation information to the list of investigations
        getInvestigationInformation(userId, investigations,include, manager);
        
        return investigations;
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param includes {@link InvestigationInclude}
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, boolean fuzzy, EntityManager manager)  {
        //secuirty on, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, includes, fuzzy ,true , -1, -1, manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, boolean fuzzy, EntityManager manager)  {
        //secuirty on, AND, no includes
        return searchByKeywords(userId, keywords, LogicalOperator.AND, InvestigationInclude.NONE, fuzzy ,true , -1, -1, manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, InvestigationInclude.NONE, false ,true ,-1 , -1,manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param includes {@link InvestigationInclude}
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, LogicalOperator.AND, includes, false ,true ,-1 , -1,manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator {@link LogicalOperator}, either AND or OR
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, operator, InvestigationInclude.NONE, false ,true ,-1 , -1,manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param includes {@link InvestigationInclude}
     * @param operator {@link LogicalOperator}, either AND or OR
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, InvestigationInclude includes, LogicalOperator operator, EntityManager manager)  {
        //exact match, secuirty true, AND
        return searchByKeywords(userId, keywords, operator, includes,  false ,true ,-1 , -1,manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator {@link LogicalOperator}, either AND or OR
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, boolean fuzzy, EntityManager manager)  {
        //exact match, secuirty true,
        return searchByKeywords(userId, keywords, operator, InvestigationInclude.NONE, fuzzy ,true ,-1 , -1,manager);
    }
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param userId federalId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator {@link LogicalOperator}, either AND or OR
     * @param includes {@link InvestigationInclude}
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> searchByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude includes, boolean fuzzy, EntityManager manager)  {
        //exact match, secuirty true,
        return searchByKeywords(userId, keywords, operator, includes, fuzzy ,true ,-1 , -1,manager);
    }
    
    /**
     * Lists all the instruments in the database
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return List of {@link Instrument}s
     */
    public static Collection<Instrument> listAllInstruments(EntityManager manager)  {
        log.trace("listAllInstruments(EntityManager)");
        return  manager.createNamedQuery(ALL_INSTRUMENTS).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
    }
    
    /**
     * Lists all the investigation types  in the database
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return List of types
     */
    public static Collection<InvestigationType> listAllInvestigationTypes(EntityManager manager)  {
        log.trace("listAllInvestigationTypes(EntityManager)");
        return  manager.createNamedQuery(ALL_INVESTIGATION_TYPES).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
    }
    
    /**
     * Lists all the user roles in the database
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return List of {@link IcatRole}s
     */
    public static Collection<IcatRole> listAllRoles(EntityManager manager){
        log.trace("listAllRoles(EntityManager)");
        return  manager.createNamedQuery(ALL_ROLES).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
    }
    
    /**
     * Lists all the user roles in the database
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return List of {@link IcatRole}s
     */
    public static Collection<Parameter> listAllParameters(EntityManager manager){
        log.trace("listAllParameters(EntityManager)");
        return  manager.createNamedQuery(ALL_PARAMETERS).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
    }
}
