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
import java.util.Date;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Instrument;
import uk.icat3.util.BaseTestClass;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;
import static uk.icat3.util.Util.*;

/**
 *
 * @author gjd37
 */
public class TestInvestigationSearch extends BaseTestClass {

    private static Logger log = Logger.getLogger(TestInvestigationSearch.class);
    @Test
    public void testGetAllUsersInvestigationsUser() {
        log.info("Testing valid user, getAllInvestigations: " + VALID_USER_FOR_INVESTIGATION);

        log.debug("Testing user investigations: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> invs = em.createQuery(Queries.INVESTIGATIONS_BY_USER_JPQL).
                setParameter("objectType", ElementType.INVESTIGATION).
                setParameter("userId", VALID_USER_FOR_INVESTIGATION).getResultList();

        log.trace("Investigations for user " + VALID_USER_FOR_INVESTIGATION + " is " + invs.size() + ": " + invs);

        assertNotNull("Must not be an empty collection", invs);
        assertEquals("Collection 'all Investigations' should be 3", 3, invs.size());
    }

    @Test
    public void testUsersInvestigationInvalidUser() {
        log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

        log.debug("Testing user investigations: " + INVALID_USER);
        Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
        log.trace("Investigations for user " + INVALID_USER + " is " + userInvestigations.size());

        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0, userInvestigations.size());
    }

    @Test
    public void testUsersInvestigationLimitInvalidUser() {
        log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

        log.debug("Testing user investigations limit 12: " + INVALID_USER);
        Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, 0, 12, em);
        log.trace("Investigations for user " + INVALID_USER + " is " + userInvestigations.size());

        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0, userInvestigations.size());
    }

    @Test
    public void testUsersInvestigationInvalidUserRtnId() {
        log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

        log.debug("Testing user investigations id: " + INVALID_USER);
        Collection<Long> userInvestigations = InvestigationSearch.getUsersInvestigationsRtnId(INVALID_USER, em);
        log.trace("Investigations for user " + INVALID_USER + " is " + userInvestigations.size());

        assertNotNull("Must not be an empty collection", userInvestigations);
        assertEquals("Collection 'userInvestigations' should be zero size", 0, userInvestigations.size());
    }

    @Test
    public void testInvestigationBySurnameInvalidUser() {
        log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

        log.debug("Testing user investigations: " + INVALID_USER);
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER, VALID_INVESTIGATION_SURNAME, em);
        log.trace("Investigations for user " + VALID_INVESTIGATION_SURNAME + " is " + investigationsSurname.size() + ":  " + investigationsSurname);

        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero", 0, investigationsSurname.size());
    }

    @Test
    public void testInvestigationBySurnameSuperUser() {
        log.info("Testing super user, searchByUserSurname: " + SUPER_USER);

        log.debug("Testing user investigations: " + SUPER_USER);
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(SUPER_USER, VALID_INVESTIGATION_SURNAME, em);
        log.trace("Investigations for user " + VALID_INVESTIGATION_SURNAME + " is " + investigationsSurname.size());

        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero size", 1, investigationsSurname.size());
    }

    @Test
    public void testInvestigationBySurnameLimitInvalidUser() {
        log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

        log.debug("Testing user investigations: " + INVALID_USER);
        Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER, VALID_INVESTIGATION_SURNAME, 0, 12, em);
        log.trace("Investigations for user " + VALID_INVESTIGATION_SURNAME + " is " + investigationsSurname.size());

        assertNotNull("Must not be an empty collection", investigationsSurname);
        assertEquals("Collection 'investigationsUser' should be zero ", 0, investigationsSurname.size());
    }

    @Test
    public void testListInstruments() {
        log.info("Testing invalid user, list all instruments:");

        Collection<Instrument> instrumentsInDB = (Collection<Instrument>) executeListResultCmd("SELECT DISTINCT i FROM Instrument i WHERE i.markedDeleted = 'N'");


        Collection<String> instruments = InvestigationSearch.listAllInstruments(em);
        log.trace("Instruments are " + instruments.size());

        assertNotNull("Must not be an empty collection", instruments);
        assertEquals("Collection 'instruments' should be size: " + instrumentsInDB.size(), instrumentsInDB.size(), instruments.size());
    }

    @Test
    public void testListRole() {
        log.info("Testing invalid user, list all roles:");

        Collection<Instrument> rolesInDB = (Collection<Instrument>) executeListResultCmd("SELECT DISTINCT i FROM IcatRole i WHERE i.markedDeleted = 'N'");


        Collection<IcatRole> roles = InvestigationSearch.listAllRoles(em);
        log.trace("Roles are " + roles.size());

        assertNotNull("Must not be an empty collection", roles);
        assertEquals("Collection 'roles' should be size: " + rolesInDB.size(), rolesInDB.size(), roles.size());
    }

    @Test
    public void testListParameters() {
        log.info("Testing invalid user, list all Parameters:");

        Collection<Instrument> parametersInDB = (Collection<Instrument>) executeListResultCmd("SELECT DISTINCT p FROM Parameter p WHERE p.markedDeleted = 'N'");


        Collection<Parameter> parameters = InvestigationSearch.listAllParameters(em);
        log.trace("Parameters are " + parameters.size());

        assertNotNull("Must not be an empty collection", parameters);
        assertEquals("Collection 'parameters' should be size: " + parametersInDB.size(), parametersInDB.size(), parameters.size());
    }

    @Test
    public void testListTypes() {
        log.info("Testing invalid user, list all investigation types:");

        Collection<Instrument> typesInDB = (Collection<Instrument>) executeListResultCmd("SELECT DISTINCT i FROM InvestigationType i WHERE i.markedDeleted = 'N'");


        Collection<String> types = InvestigationSearch.listAllInvestigationTypes(em);
        log.trace("InvestigationType are " + types.size());

        assertNotNull("Must not be an empty collection", types);
        assertEquals("Collection 'types' should be size: " + typesInDB.size(), typesInDB.size(), types.size());
    }
    //////////////////////////////////////////
    @Test
    public void testSearchByDeletedKeyword() throws ICATAPIException {
        log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION, "deleted keyword", em);
        log.trace("Investigations found with 'deleted keyword': " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be 0", 0, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeyword() throws ICATAPIException {
        log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION, VALID_KEYWORD, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be 1", investigations.size(), 1);
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordSuperUser() throws ICATAPIException {
        log.info("Testing super user, keyword: " + SUPER_USER);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(SUPER_USER, VALID_KEYWORD, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be 1", investigations.size(), 1);

    }

    @Test
    public void testSearchByKeywordLimit() throws ICATAPIException {
        log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION, VALID_KEYWORD, 0, 1, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", investigations.size(), 1);
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, keyword: " + INVALID_USER);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", investigations.size(), 0);
    }

    @Test
    public void testSearchByKeywordInvalidUserLimit() throws ICATAPIException {
        log.info("Testing invalid user, keyword: " + INVALID_USER);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD, 0, 100, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", investigations.size(), 0);
    }
    ///////////////////  Keywords fuzzy etc    //////////////////////////
    @Test
    public void testSearchByDeletedKeywords() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add("deleted");

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with 'deleted' : " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 0, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordsWithAnd() throws ICATAPIException {
        log.info("Testing valid user, with AND in keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(" ANd");
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add(" ANd");
        keywords.add(VALID_KEYWORD2);

        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywords() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordsSuperUser() throws ICATAPIException {
        log.info("Testing super user, keywords: " + SUPER_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(SUPER_USER, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
    }

    @Test
    public void testSearchByKeywordsAllSuperUser() throws ICATAPIException {
        log.info("Testing super user, keywords: " + SUPER_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add("*ll*");

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(SUPER_USER, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be three", 3, investigations.size());
    }

    @Test
    public void testSearchByKeywordsIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordsMultipleFuzzy() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();

        //this should bring back zero rows
        keywords.add(VALID_KEYWORD.substring(1, 5));
        keywords.add("x");

        //fuzzy false
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        keywords.clear();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

        //fuzzy true
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);



    }

    @Test
    public void testSearchByKeywordsFuzzy() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add(VALID_KEYWORD.substring(0, 5));
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsFuzzyIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add(VALID_KEYWORD.substring(0, 5));
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsMultipleFuzzyIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");


        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add("x");
        keywords.add(VALID_KEYWORD.substring(0, 5));
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        keywords.clear();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");
        //fuzzy true
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

    }

    @Test
    public void testSearchByKeywordsLogical() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.AND, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.add("z");

        //check AND
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        //check OR
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.OR, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordsLogicalIncludes() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.add("z");

        //check AND
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);


        //check OR
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.OR, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByKeywordsAll() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add("*" + VALID_KEYWORD.substring(1, 5) + "*");

        //AND fuzzy
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, true, true, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.add(VALID_KEYWORD2);
        //OR fuzzy
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, true, true, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        keywords.clear();
        keywords.add(VALID_KEYWORD);
        keywords.add(" and ");
        keywords.add(VALID_KEYWORD2);

        //AND fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false, true, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.add("z");
        //OR fuzzy false
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, false, true, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());

        ////////////now check security section
        //OR fuzzy false no security

        keywords.clear();
        keywords.add(VALID_KEYWORD);

        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, false, false, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());

        //AND
        keywords.add(VALID_KEYWORD2);
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false, false, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());

        keywords.add("z");
        //AND
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, false, false, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsCaseSensitive() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(0, 5).toUpperCase() + "*");

        //AND fuzzy, case sensitive
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, true, true, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        //AND fuzzy, case insensitive
        investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, true, false, 0, 1000, em);
        log.trace("Investigations found with " + keywords + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

    }
    //////////////////////////////////////
    @Test
    public void testSearchByKeywordsInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, keywords: " + INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsInvalidUserIncludes() throws ICATAPIException {
        log.info("Testing invalid user, keywords: " + INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsFuzzyInvalidUser() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

        //fuzzy true
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add(VALID_KEYWORD.substring(0, 5));
        //fuzzy false
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
        log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testSearchByKeywordsInvalidUserLogical() throws ICATAPIException {
        log.info("Testing invalid user, keywords: " + INVALID_USER);
        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        //check AND
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.AND, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        //check OR
        investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.OR, em);
        log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
    }
    ///////////////////////////////////////
    ///////////////User ID /////////////
    @Test
    public void testSearchByUserID() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(VALID_USER_FOR_INVESTIGATION, VALID_FEDID_FOR_INVESTIGATION, em);
        log.trace("Investigations found with " + VALID_FEDID_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByUserIDSuperUser() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + SUPER_USER);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(SUPER_USER, VALID_FEDID_FOR_INVESTIGATION, em);
        log.trace("Investigations found with " + VALID_FEDID_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());

    }

    @Test
    public void testSearchByInvalidUserID() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(VALID_USER_FOR_INVESTIGATION, "dsdfsdsdsds", em);
        log.trace("Investigations found with dsdfsdsdsds : " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }
    ///////////////////////////////////////
    ///////////////Surname /////////////
    @Test
    public void testSearchBySurname() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION, em);
        log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }
    @Test
    public void testSearchBySurnameLower() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION.toLowerCase(), em);
        log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION.toLowerCase() + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchBySurnameLimit() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION, 0, 100, em);
        log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testSearchByInvalidSurname() throws ICATAPIException {
        log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(VALID_USER_FOR_INVESTIGATION, "asdsdasdasdasdas", em);
        log.trace("Investigations found with asdsdasdasdasdas: " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }
    ///////////////////////////////////////
    /**
     * Tests instruments
     */
    @Test
    public void testlistInstruments() {
        log.info("Testing valid user for all instruments: " + VALID_USER_FOR_INVESTIGATION);
        Collection<String> instruments = InvestigationSearch.listAllInstruments(em);

        Collection<Instrument> instrumentsInDB = (Collection<Instrument>) executeListResultCmd("SELECT i FROM Instrument i");

        assertNotNull("Must not be an null collection of instruments ", instruments);
        assertEquals("Number of instruments searched is different to number in DB", instrumentsInDB.size(), instruments.size());
    }
    ///////////////////////////////////////
    ////////////// My Investigations ///////////////////////////
    @Test
    public void testMyInvestigations() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, em);
        log.trace("Investigations found with " + VALID_USER_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testMyInvestigationsIncludes() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, InvestigationInclude.ALL, em);
        log.trace("Investigations found with " + VALID_USER_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testMyInvestigationsIncludesLimit() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(VALID_USER_FOR_INVESTIGATION, InvestigationInclude.ALL, 0, 1, em);
        log.trace("Investigations found with " + VALID_USER_FOR_INVESTIGATION + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testMyInvestigationsIncludesLimitInvalidUser() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + INVALID_USER);

        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, InvestigationInclude.ALL, 0, 1, em);
        log.trace("Investigations found with " + INVALID_USER + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }

    @Test
    public void testMyInvestigationsInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, My Investigations: " + INVALID_USER);

        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
        log.trace("Investigations found with " + INVALID_USER + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

    }
    ///////////////////////////////////////
    ////////////// Advanced ///////////////////////////
    @Test
    public void testAdvancedDeleted() throws ICATAPIException {
        log.info("Testing invalid user, : " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        Collection<String> keywords = new ArrayList<String>();
        keywords.add("deleted keyword");
        asd.setKeywords(keywords);

        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an null collection", investigations);
        assertEquals("Size should be 0", 0, investigations.size());
        checkInvestigations(investigations);
    }

    //@Test
    public void testAdvanced() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();

        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be 3", 3, investigations.size());
        checkInvestigations(investigations);

        //test with name
        asd.setInvestigationType("experiment");

        //test with name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 3, investigations.size());
        checkInvestigations(investigations);

        //test with name
        asd.setInvestigationName("Investigation without any investigator");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        //test with name fuzzy
        asd.setInvestigationName("Investigation without any investigator*");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add sample name
        asd.setSampleName("SrF2 calibration  w=-25.");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        //add sample name fuzzy
        asd.setSampleName("SrF2 calibration  w=-25.*");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add visit id
        asd.setVisitId("12");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add grant id
        asd.setGrantId(15L);
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add abstract
        asd.setInvestigationAbstract("false ab");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 0, investigations.size());
        checkInvestigations(investigations);

        //add abstract fuzzy
        asd.setInvestigationAbstract("*abstrac*");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add abstract
        asd.setInvestigationAbstract("test abstract");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add bcatInvStr
        asd.setBackCatalogueInvestigatorString("false name");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 0, investigations.size());
        checkInvestigations(investigations);

        //add bcatInvStr fuzzy
        asd.setBackCatalogueInvestigatorString("damia*");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add bcatInvStr
        asd.setBackCatalogueInvestigatorString("damian");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        Collection<String> keywords = new ArrayList<String>();
        keywords.add("shull");


        asd.setKeywords(keywords);
        //add keywords
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add("shul*");
        //add valid keyword fuzzy
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());

        keywords.clear();
        keywords.add("deleted");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        keywords.clear();
        keywords.add("deleted");
        keywords.add(" and ");
        keywords.add("dvveletedddddd");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());


        Collection<String> instruments = new ArrayList<String>();
        instruments.add(VALID_INSTRUMENT1);
        asd.setInstruments(instruments);
        //reset keywords
        keywords.clear();
        keywords.add(VALID_KEYWORD3);

        //add instruments
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        instruments.add("false");
        instruments.add("SXD");
        asd.setInstruments(instruments);
        //reset keywords
        keywords.clear();
        keywords.add("shul*");

        //add instruments
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size() + " : " + investigations);

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        asd.setDatafileName("asknfasopfosdmfsdf");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setDatafileName("SXD015554.RAW");

        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setDatafileName("SXD015554.RA*");

        //add datafile name fuzzy
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setExperimentNumber("3434343434");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setExperimentNumber("12345");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        Collection<String> investigators = new ArrayList<String>();
        investigators.add("djdjdjdjkdjdjdjd");
        asd.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);


        investigators.clear();
        investigators.add(VALID_SURNAME_FOR_INVESTIGATION);
        asd.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        investigators.clear();
        investigators.add(VALID_SURNAME_FOR_INVESTIGATION.substring(0, 3) + "*");
        asd.setInvestigators(investigators);
        //add investigators fuzzy
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        asd.setRunStart(1000d);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(2000.0);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(1001.0);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

    }

    //@Test
    public void testAdvancedCaseInsensitive() throws ICATAPIException {
        log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);


        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setCaseSensitive(false);

        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be 3", 3, investigations.size());
        checkInvestigations(investigations);

        //test with name
        asd.setInvestigationType("experiment");

        //test with name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 3, investigations.size());
        checkInvestigations(investigations);

        //test with name
        asd.setInvestigationName("INVESTIGATION without any investigators");

        //test with name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add sample name
        asd.setSampleName("SrF2 CALIBRATION  w=-25.3");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add visit id
        asd.setVisitId("12");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add grant id
        asd.setGrantId(15L);
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add abstract
        asd.setInvestigationAbstract("FALSE ab");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 0, investigations.size());
        checkInvestigations(investigations);

        //add abstract
        asd.setInvestigationAbstract("test ABSTRACT");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add abstract fuzzy
        asd.setInvestigationAbstract("test *BSTRAC*");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        //add bcatInvStr
        asd.setBackCatalogueInvestigatorString("FALSE name");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 0, investigations.size());
        checkInvestigations(investigations);

        //add bcatInvStr
        asd.setBackCatalogueInvestigatorString("DAMIAN");
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        Collection<String> keywords = new ArrayList<String>();
        keywords.add("SHULL");


        asd.setKeywords(keywords);
        //add keywords
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add("DELETED");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        keywords.clear();
        keywords.add("DELETED");
        keywords.add(" AND");
        keywords.add("DELETEDdddddd");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());


        Collection<String> instruments = new ArrayList<String>();
        instruments.add("SXD");
        asd.setInstruments(instruments);
        //reset keywords
        keywords.clear();
        keywords.add("SHUL*");

        //add instruments
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        instruments.add("false");
        instruments.add("SXD");
        asd.setInstruments(instruments);
        //reset keywords
        keywords.clear();
        keywords.add("SHUL*");

        //add instruments
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size() + " : " + investigations);

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        asd.setDatafileName("ASKnfasopfosdmfsdf");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setDatafileName("sxd015554.ra*");

        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setExperimentNumber("3434343434");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setExperimentNumber("12345");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        Collection<String> investigators = new ArrayList<String>();
        investigators.add("DJDJDjdjkdjdjdjd");
        asd.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);


        investigators.clear();
        investigators.add(VALID_SURNAME_FOR_INVESTIGATION.toUpperCase());
        asd.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);


        asd.setRunStart(1000d);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(2000.0);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(1001.0);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testAdvancedDatafileName() throws ICATAPIException {
        log.info("Testing valid user, datafile name: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setCaseSensitive(true);
        asd.setDatafileName(VALID_DATAFILE_NAME1);


        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced("gjd37", asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testAdvancedCreateTime() throws ICATAPIException {
        log.info("Testing valid user, create time: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setDateRangeStart(new Date(1, 1, 1));  //120 = 2020
        asd.setDateRangeEnd(new Date());

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testAdvancedCreateTime2() throws ICATAPIException {
        log.info("Testing valid user, create time: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setDateRangeStart(new Date(60, 1, 1));  //120 = 2020

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
        checkInvestigations(investigations);
    }

    @Test
    public void testAdvancedCreateTimeSuperUser() throws ICATAPIException {
        log.info("Testing super user, create time: " + SUPER_USER);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setDateRangeStart(new Date(1, 1, 1));  //120 = 2020
        asd.setDateRangeEnd(new Date());

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(SUPER_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
    }

    //@Test
    public void testAdvancedCreateTimeFacilityScientist() throws ICATAPIException {
        log.info("Testing facility_scientist user, create time: facility_scientist");

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setDateRangeStart(new Date(1, 1, 1));  //120 = 2020
        asd.setDateRangeEnd(new Date());

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced("facility_scientist", asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size() + ": " + investigations);

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
    }

    @Test
    public void testAdvancedRunNumber() throws ICATAPIException {
        log.info("Testing valid user, run number: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setRunEnd(66660d);
        // asd.setRunStart(1d);

        log.trace("RunNumber? " + asd.hasRunNumber());

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());
        log.trace(investigations);
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be two", 2, investigations.size());
    }

    @Test
    public void testAdvancedRunNumberInstrument() throws ICATAPIException {
        log.info("Testing valid user, run number: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setRunEnd(VALID_RUN_NUMBER_RANGE_END);
        asd.setRunStart(VALID_RUN_NUMBER_RANGE_START);
        Collection<String> ins = new ArrayList<String>();
        ins.add(VALID_INSTRUMENT1);
        asd.setInstruments(ins);

        log.trace("RunNumber? " + asd.hasRunNumber());

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());
        log.trace(investigations);
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be one", 1, investigations.size());
    }

    //@Test
    public void testAdvancedDF() throws ICATAPIException {
        log.info("Testing valid user, DF: " + VALID_USER_FOR_INVESTIGATION);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setDatafileName("sXD01064.RAW");
        asd.setCaseSensitive(false);

        long time = System.currentTimeMillis();

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());
        log.trace(investigations);
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        AdvancedSearchDetails asd2 = new AdvancedSearchDetails();
        asd2.setDatafileName("SXD01064.RAW");
        asd2.setCaseSensitive(true);

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be two", 1, investigations.size());
        
        time = System.currentTimeMillis();

        //test with name
        investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd2, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());
        log.trace(investigations);
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be two", 1, investigations.size());
    }

    @Test
    public void testAdvancedInvalidUser() throws ICATAPIException {
        log.info("Testing invalid user, My Investigations: " + INVALID_USER);

        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.setInvestigationName("SrF2 calibration  w=-25.3");

        //test with name
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        //add sample name
        asd.setSampleName("SrF2 calibration  w=-25.3");
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        Collection<String> keywords = new ArrayList<String>();
        keywords.add(VALID_KEYWORD);
        keywords.add(VALID_KEYWORD2);

        asd.setKeywords(keywords);
        //add keywords
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        keywords.clear();
        keywords.add("sdsdsdsdsds");
        //add invalid keyword
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

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
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        asd.setDatafileName("asknfasopfosdmfsdf");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setDatafileName("SXD01256.RAW");
        //add datafile name
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        asd.setExperimentNumber("3434343434");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());

        asd.setExperimentNumber("32");
        //add ex number
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);


        Collection<String> investigators = new ArrayList<String>();
        investigators.add(VALID_SURNAME_FOR_INVESTIGATION);
        asd.setInvestigators(investigators);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);


        asd.setRunStart(1000d);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(2000d);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);

        asd.setRunEnd(1001d);
        //add investigators
        investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
        log.trace("Investigations found with " + asd + ": " + investigations.size());

        assertNotNull("Must not be an empty collection", investigations);
        assertEquals("Size should be zero", 0, investigations.size());
        checkInvestigations(investigations);
    }
    ///////////////////////////////////////
    @Test(expected=ICATAPIException.class)
    public void testCheckInvestigation() throws ICATAPIException {
        Investigation inv = em.find(Investigation.class, 6L);

        Collection<Investigation> invs = new ArrayList<Investigation>();
        invs.add(inv);

        checkInvestigations(invs);
    }

    /**
     * Checks the investigation found by the search has authroisation to read this investigation
     */
    private static void checkInvestigations(Collection<Investigation> investigations) throws ICATAPIException {
        IcatAuthorisation icatAuthorisation = null;
        for (Investigation investigation : investigations) {
            boolean found = false;

            Query query = em.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_INVESTIGATION);
            query.setParameter("elementType", ElementType.INVESTIGATION).
                    setParameter("elementId", investigation.getId()).
                    setParameter("userId", VALID_USER_FOR_INVESTIGATION);

            try {
                icatAuthorisation = (IcatAuthorisation) query.getSingleResult();
                log.trace("Found stage 1 (normal): " + icatAuthorisation);
                found = true;
            } catch (NoResultException nre) {

                //try find ANY
                log.trace("None found, searching for ANY in userId");
                query.setParameter("userId", "ANY");

                try {
                    icatAuthorisation = (IcatAuthorisation) query.getSingleResult();
                    log.trace("Found stage 2 (ANY): " + icatAuthorisation);
                    found = true;
                } catch (NoResultException nre2) {
                    log.debug("None found for : UserId: " + VALID_USER_FOR_INVESTIGATION + ", type: " + ElementType.INVESTIGATION + ", elementId: " + investigation.getId() + ", throwing exception");
                    throw new InsufficientPrivilegesException();
                }
            }

            if (!found) {
                //if gets here throw exception
                throw new ICATAPIException(VALID_USER_FOR_INVESTIGATION + " is not a part of this investigation and should not have been found");
            } else {
                //found but check role
                if (!parseBoolean(icatAuthorisation.getRole().getActionCanSelect())) {
                    //if gets here throw exception
                    throw new ICATAPIException(VALID_USER_FOR_INVESTIGATION + " is not a part of this investigation and should not have been found");
                }
            }

        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestInvestigationSearch.class);
    }
}
