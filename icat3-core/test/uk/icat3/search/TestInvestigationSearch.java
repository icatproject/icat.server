/*
 * TestInvestigationSearch.java
 *
 * Created on 22 February 2007, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.ArrayList;
import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Instrument;
import uk.icat3.util.BaseTestClass;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
public class TestInvestigationSearch extends BaseTestClass{
    
    private static Logger log = Logger.getLogger(TestInvestigationSearch.class);
    
    @Test
    public void testUsersInvestigationInvalidUser(){
        log.info("Testing invalid user, getUsersInvestigations: "+INVALID_USER);
        
        
        log.debug("Testing user investigations: "+INVALID_USER);
        Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
        log.trace("Investigations for user "+INVALID_USER+" is "+userInvestigations.size());
        
        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0 , userInvestigations.size());
    }
    
    @Test
    public void testUsersInvestigationLimitInvalidUser(){
        log.info("Testing invalid user, getUsersInvestigations: "+INVALID_USER);
        
        
        log.debug("Testing user investigations limit 12: "+INVALID_USER);
        Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, 0, 12, em);
        log.trace("Investigations for user "+INVALID_USER+" is "+userInvestigations.size());
        
        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0 , userInvestigations.size());
        
    }
    
    @Test
    public void testUsersInvestigationInvalidUserRtnId(){
        log.info("Testing invalid user, getUsersInvestigations: "+INVALID_USER);
        
        
        log.debug("Testing user investigations id: "+INVALID_USER);
        Collection<Long> userInvestigations = InvestigationSearch.getUsersInvestigationsRtnId(INVALID_USER, em);
        log.trace("Investigations for user "+INVALID_USER+" is "+userInvestigations.size());
        
        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0 , userInvestigations.size());
    }
    
    @Test
    public void testUsersInvestigationBySurnameInvalidUser(){
        log.info("Testing invalid user, getUsersInvestigations: "+INVALID_USER);
        
        log.debug("Testing user investigations: "+INVALID_USER);
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER, VALID_INVESTIGATION_SURNAME, em);
        log.trace("Investigations for user "+VALID_INVESTIGATION_SURNAME+" is "+investigationsSurname.size());
        
        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero size", 0 , investigationsSurname.size());
    }
    
    @Test
    public void testUsersInvestigationBySurnameLimitInvalidUser(){
        log.info("Testing invalid user, getUsersInvestigations: "+INVALID_USER);
        
        log.debug("Testing user investigations: "+INVALID_USER);
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER, VALID_INVESTIGATION_SURNAME, 0, 12, em);
        log.trace("Investigations for user "+VALID_INVESTIGATION_SURNAME+" is "+investigationsSurname.size());
        
        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero size", 0 , investigationsSurname.size());
    }
    
    @Test
    public void testListInstruments(){
        log.info("Testing invalid user, list all instruments:");
        
        Collection<Instrument> instrumentsInDB = (Collection<Instrument>)executeListResultCmd("SELECT DISTINCT i FROM Instrument i");
        
        
        Collection<Instrument> instruments = InvestigationSearch.listAllInstruments(em);
        log.trace("Instruments are "+instruments.size());
        
        assertNotNull("Must not be an empty collection", instruments);
        assertEquals("Collection 'instruments' should be size: "+instrumentsInDB.size(), instrumentsInDB.size(), instruments.size());
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void testSearchByKeyword() throws ICATAPIException {
        log.info("Testing valid user, keyword: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION, VALID_KEYWORD ,em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByKeywordLimit() throws ICATAPIException {
        log.info("Testing valid user, keyword: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION, VALID_KEYWORD ,0,1, em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByKeywordInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, keyword: "+INVALID_USER);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD ,em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
    }
    
    @Test
    public void testSearchByKeywordInvalidUserLimit() throws ICATAPIException {
        log.info("Testing invalid user, keyword: "+INVALID_USER);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD , 0 ,100,em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void testSearchByKeywords() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByKeywordsIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    
    //TODO this should fail as the search does not work for more than one keyword fuzzy search and AND
    @Test
    public void testSearchByKeywordsMultipleFuzzy() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        
        //this should bring back zero rows
        keywords.add(VALID_KEYWORD.substring(1,2));
        keywords.add("z");
        
        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,true , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size()); //TODO Failure comes back one should be 0
        checkInvestigations(investigations);
        
        //fuzzy false, this should work
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,false , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsFuzzy() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(1,3));
        
        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,true , em);
        log.trace("Investigations found with "+VALID_KEYWORD.substring(1,3)+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,false , em);
        log.trace("Investigations found with "+VALID_KEYWORD.substring(1,3)+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsFuzzyIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(1,3));
        
        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, true , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , InvestigationInclude.DATASETS_AND_DATAFILES, false , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    //TODO this should fail as the search does not work for more than one keyword fuzzy search and AND
    
    @Test
    public void testSearchByKeywordsMultipleFuzzyIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(1,2));
        
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, true , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.add("z");
        
        //fuzzy true
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, true , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size()); //TODO Failure comes back 1 should be 0
        checkInvestigations(investigations);
        
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , InvestigationInclude.DATASETS_AND_DATAFILES, false , em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsLogical() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,LogicalOperator.AND, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.add("z");
        
        //check AND
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);
        
        //check OR
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,LogicalOperator.OR, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByKeywordsLogicalIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.add("z");
        
        //check AND
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);
        
        
        //check OR
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords ,LogicalOperator.OR, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    //TODO this should fail as the search does not work for more than one keyword fuzzy search and AND
    
    @Test
    public void testSearchByKeywordsAll() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(1,2));
        
        //AND fuzzy
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, true , true, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.add(VALID_KEYWORD2);
        //OR fuzzy
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, true , true, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        
        keywords.clear();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        //AND fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false , true, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.add("z");
        //OR fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords , LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, false , true, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        
        //////////////////////now check security section
        //OR fuzzy false no security
        
        keywords.clear();
        keywords.add(VALID_KEYWORD);
        
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords , LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, false , false, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        
        //AND
        keywords.add(VALID_KEYWORD2);
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords , LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false , false, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        
        keywords.add("z");
        //AND
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords , LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false , false, 0, 1000, em);
        log.trace("Investigations found with "+keywords+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    //////////////////////////////////////////////////////////////////////////
    
    @Test
    public void testSearchByKeywordsInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, keywords: "+INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsInvalidUserIncludes() throws ICATAPIException {
        log.info("Testing invalid user, keywords: "+INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsFuzzyInvalidUser() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(1,3));
        
        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,true , em);
        log.trace("Investigations found with "+VALID_KEYWORD.substring(1,3)+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);
        
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,false , em);
        log.trace("Investigations found with "+VALID_KEYWORD.substring(1,3)+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testSearchByKeywordsInvalidUserLogical() throws ICATAPIException {
        log.info("Testing invalid user, keywords: "+INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,LogicalOperator.AND, em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
        //check OR
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords ,LogicalOperator.OR, em);
        log.trace("Investigations found with "+VALID_KEYWORD+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
    }
    
    /////////////////////////////////////////////////////////////////////////////
    
    
    ///////////////////////////User ID /////////////////////////
    @Test
    public void testSearchByUserID() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(VALID_USER_FOR_INVESTIGATION, VALID_FEDID_FOR_INVESTIGATION , em);
        log.trace("Investigations found with "+VALID_FEDID_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByInvalidUserID() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(VALID_USER_FOR_INVESTIGATION, "dsdfsdsdsds" , em);
        log.trace("Investigations found with dsdfsdsdsds : " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zeri", 0, investigations.size());
        
    }
    
    /////////////////////////////////////////////////////////////////////////////
    
    
    ///////////////////////////Surname /////////////////////////
    @Test
    public void testSearchBySurname() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION , em);
        log.trace("Investigations found with "+VALID_SURNAME_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchBySurnameLimit() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION , 0,100, em);
        log.trace("Investigations found with "+VALID_SURNAME_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testSearchByInvalidSurname() throws ICATAPIException {
        log.info("Testing valid user, keywords: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, "asdsdasdasdasdas" , em);
        log.trace("Investigations found with asdsdasdasdasdas: " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Tests instruments
     */
    @Test
    public void testlistInstruments(){
        log.info("Testing valid user for all instruments: "+VALID_USER_FOR_INVESTIGATION);
        Collection<Instrument> instruments = InvestigationSearch.listAllInstruments(em);
        
        Collection<Instrument> instrumentsInDB = (Collection<Instrument>)executeListResultCmd("SELECT i FROM Instrument i");
        
        assertNotNull("Must not be an null collection of instruments ", instruments);
        assertEquals("Number of instruments searched is different to number in DB",instrumentsInDB.size(),instruments.size());
    }
    /////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////// My Investigations ///////////////////////////////////////////////////
    @Test
    public void testMyInvestigations() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, em);
        log.trace("Investigations found with "+VALID_USER_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testMyInvestigationsIncludes() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, InvestigationInclude.ALL, em);
        log.trace("Investigations found with "+VALID_USER_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testMyInvestigationsIncludesLimit() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: "+VALID_USER_FOR_INVESTIGATION);
        
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, InvestigationInclude.ALL, 0,1, em);
        log.trace("Investigations found with "+VALID_USER_FOR_INVESTIGATION+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    @Test
    public void testMyInvestigationsIncludesLimitInvalidUser() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: "+INVALID_USER);
        
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, InvestigationInclude.ALL, 0,1, em);
        log.trace("Investigations found with "+INVALID_USER+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    @Test
    public void testMyInvestigationsInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, My Investigations: "+INVALID_USER);
        
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
        log.trace("Investigations found with "+INVALID_USER+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
    }
    
    /////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////// My Investigations ///////////////////////////////////////////////////
    @Test
    public void testAdvanced() throws ICATAPIException {
        log.info("Testing invalid user, My Investigations: "+VALID_USER_FOR_INVESTIGATION);
        
        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setInvestigationName("SrF2 calibration  w=-25.3");
        
        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        //add sample name
        asd.setSampleName("SrF2 calibration  w=-25.3");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        
        asd.setKeywords(keywords);
        //add keywords
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        keywords.clear();
        keywords.add("sdsdsdsdsds");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        asd.setInstruments(instruments);
        //reset keywords
        keywords.clear();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);
        //add instruments
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        asd.setDatafileName("asknfasopfosdmfsdf");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
        asd.setDatafileName("SXD01256.RAW");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        asd.setExperimentNumber("3434343434");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        
        asd.setExperimentNumber("32");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with "+asd+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
        
        
        Collection<String> investigators = new ArrayList<String>();
        investigators.add(VALID_FEDID_FOR_INVESTIGATION);
        AdvancedSearchDetails asd2 = new AdvancedSearchDetails();
        asd2.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd2, em);
        log.trace("Investigations found with "+asd2+ ": " +investigations.size());
        
        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    
    
    /////////////////////////////////////////////////////////////////////////////
    
    
    
    
    private static void checkInvestigations(Collection<Investigation> investigations) throws ICATAPIException {
        for (Investigation investigation : investigations) {
            if(investigation.getInvestigatorCollection() == null){
                log.debug("investigation has no investigators so ok");
                continue;
            }
            boolean found = false;
            investigator: for (Investigator investigator : investigation.getInvestigatorCollection()) {
                if(investigator.getFacilityUser().getFederalId().equals(VALID_USER_FOR_INVESTIGATION)) {
                    log.debug("Investigation ok");
                    found = true;
                    break investigator;
                } else log.trace(investigation+" investigator "+investigator.getFacilityUser().getFacilityUserId()+" does not match");
            }
            if(!found){
                //if gets here throw exception
                throw new ICATAPIException(VALID_USER_FOR_INVESTIGATION+" is not a part of this investigation and should not have been found");
            }
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigationSearch.class);
    }
}
