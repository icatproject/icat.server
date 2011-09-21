package uk.icat3.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.icat3.util.TestConstants.ICAT_ADMIN_USER;
import static uk.icat3.util.TestConstants.INVALID_USER;
import static uk.icat3.util.TestConstants.VALID_DATAFILE_NAME1;
import static uk.icat3.util.TestConstants.VALID_INSTRUMENT1;
import static uk.icat3.util.TestConstants.VALID_INVESTIGATION_SURNAME;
import static uk.icat3.util.TestConstants.VALID_KEYWORD;
import static uk.icat3.util.TestConstants.VALID_KEYWORD2;
import static uk.icat3.util.TestConstants.VALID_KEYWORD3;
import static uk.icat3.util.TestConstants.VALID_RUN_NUMBER_RANGE_END;
import static uk.icat3.util.TestConstants.VALID_RUN_NUMBER_RANGE_START;
import static uk.icat3.util.TestConstants.VALID_SURNAME_FOR_INVESTIGATION;
import static uk.icat3.util.TestConstants.VALID_USER_FOR_INVESTIGATION;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.manager.RuleManager;
import uk.icat3.util.BaseClassTransaction;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

public class TestInvestigationSearch extends BaseClassTransaction {

	@BeforeClass
	public static void authz() throws Exception {
		RuleManager.addUserGroupMember(VALID_USER_FOR_INVESTIGATION, VALID_USER_FOR_INVESTIGATION, em);
		RuleManager.addUserGroupMember(VALID_USER_FOR_INVESTIGATION, ICAT_ADMIN_USER, em);
		RuleManager.addUserGroupMember(VALID_USER_FOR_INVESTIGATION, "gjd37", em);
		RuleManager.addRule(VALID_USER_FOR_INVESTIGATION, "Investigation", "CRUD", null, em);
	}

	private static Logger log = Logger.getLogger(TestInvestigationSearch.class);

	@Test
	public void testUsersInvestigationInvalidUser() {
		log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

		log.debug("Testing user investigations: " + INVALID_USER);
		Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
		log.trace("Investigations for user " + INVALID_USER + " is " + userInvestigations.size());

		assertNotNull("Must not be an empty collection", userInvestigations);
		assertEquals(0, userInvestigations.size());
	}

	@Test
	public void testUsersInvestigationLimitInvalidUser() {
		log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

		log.debug("Testing user investigations limit 12: " + INVALID_USER);
		Collection<Investigation> userInvestigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, 0, 12,
				em);
		log.trace("Investigations for user " + INVALID_USER + " is " + userInvestigations.size());

		assertNotNull("Must not be an empty collection", userInvestigations);
		assertEquals(0, userInvestigations.size());
	}

	@Test
	public void testInvestigationBySurnameInvalidUser() {
		log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

		log.debug("Testing user investigations: " + INVALID_USER);
		Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER,
				VALID_INVESTIGATION_SURNAME, em);
		log.trace("Investigations for user " + VALID_INVESTIGATION_SURNAME + " is " + investigationsSurname.size()
				+ ":  " + investigationsSurname);

		assertNotNull("Must not be an empty collection", investigationsSurname);
		assertEquals("Collection 'investigationsUser' should be zero", 0, investigationsSurname.size());
	}

	@Test
	public void testInvestigationBySurnameLimitInvalidUser() {
		log.info("Testing invalid user, getUsersInvestigations: " + INVALID_USER);

		log.debug("Testing user investigations: " + INVALID_USER);
		Collection<Investigation> investigationsSurname = InvestigationSearch.searchByUserSurname(INVALID_USER,
				VALID_INVESTIGATION_SURNAME, 0, 12, em);
		log.trace("Investigations for user " + VALID_INVESTIGATION_SURNAME + " is " + investigationsSurname.size());

		assertNotNull("Must not be an empty collection", investigationsSurname);
		assertEquals("Collection 'investigationsUser' should be zero ", 0, investigationsSurname.size());
	}

	@Test
	public void testSearchByDeletedKeyword() throws ICATAPIException {
		log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION,
				"deleted keyword", em);
		log.trace("Investigations found with 'deleted keyword': " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeyword() throws ICATAPIException {
		log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION,
				VALID_KEYWORD, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordLimit() throws ICATAPIException {
		log.info("Testing valid user, keyword: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(VALID_USER_FOR_INVESTIGATION,
				VALID_KEYWORD, 0, 1, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordInvalidUser() throws ICATAPIException {
		log.info("Testing invalid user, keyword: " + INVALID_USER);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
	}

	@Test
	public void testSearchByKeywordInvalidUserLimit() throws ICATAPIException {
		log.info("Testing invalid user, keyword: " + INVALID_USER);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(INVALID_USER, VALID_KEYWORD, 0,
				100, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
	}

	@Test
	public void testSearchByDeletedKeywords() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add("deleted");

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, em);
		log.trace("Investigations found with 'deleted' : " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsWithAnd() throws ICATAPIException {
		log.info("Testing valid user, with AND in keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(" ANd");
		keywords.add(VALID_KEYWORD2);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

		keywords.clear();
		keywords.add(" ANd");
		keywords.add(VALID_KEYWORD2);

		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywords() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsIncludes() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsMultipleFuzzy() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();

		// this should bring back zero rows
		keywords.add(VALID_KEYWORD.substring(1, 5));
		keywords.add("x");

		// fuzzy false
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

		// fuzzy true
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsFuzzy() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

		// fuzzy true
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

		keywords.clear();
		keywords.add(VALID_KEYWORD.substring(0, 5));
		// fuzzy false
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsFuzzyIncludes() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

		// fuzzy true
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

		keywords.clear();
		keywords.add(VALID_KEYWORD.substring(0, 5));
		// fuzzy false
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				InvestigationInclude.DATASETS_AND_DATAFILES, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsMultipleFuzzyIncludes() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, InvestigationInclude.DATASETS_AND_DATAFILES, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

		keywords.clear();
		keywords.add("x");
		keywords.add(VALID_KEYWORD.substring(0, 5));
		// fuzzy false
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				InvestigationInclude.DATASETS_AND_DATAFILES, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");
		// fuzzy true
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				InvestigationInclude.DATASETS_AND_DATAFILES, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsLogical() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		// check AND
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, LogicalOperator.AND, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
		keywords.add("z");

		// check AND
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// check OR
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				LogicalOperator.OR, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsLogicalIncludes() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		// check AND
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

		keywords.add("z");

		// check AND
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				InvestigationInclude.DATASETS_AND_DATAFILES, LogicalOperator.AND, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// check OR
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				LogicalOperator.OR, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	

	@Test
	public void testSearchByKeywordsCaseSensitive() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD.substring(0, 5).toUpperCase() + "*");

		// AND fuzzy, case sensitive
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION,
				keywords, LogicalOperator.AND, InvestigationInclude.DATASETS_AND_DATAFILES, true, true, 0, 1000, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// AND fuzzy, case insensitive
		investigations = InvestigationSearch.searchByKeywords(VALID_USER_FOR_INVESTIGATION, keywords,
				LogicalOperator.OR, InvestigationInclude.DATASETS_AND_DATAFILES, true, false, 0, 1000, em);
		log.trace("Investigations found with " + keywords + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsInvalidUser() throws ICATAPIException {
		log.info("Testing invalid user, keywords: " + INVALID_USER);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsInvalidUserIncludes() throws ICATAPIException {
		log.info("Testing invalid user, keywords: " + INVALID_USER);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords,
				InvestigationInclude.INVESTIGATORS_AND_KEYWORDS, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsFuzzyInvalidUser() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + INVALID_USER);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD.substring(0, 5) + "*");

		// fuzzy true
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add(VALID_KEYWORD.substring(0, 5));
		// fuzzy false
		investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, em);
		log.trace("Investigations found with " + VALID_KEYWORD.substring(1, 3) + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByKeywordsInvalidUserLogical() throws ICATAPIException {
		log.info("Testing invalid user, keywords: " + INVALID_USER);
		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		// check AND
		Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords,
				LogicalOperator.AND, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// check OR
		investigations = InvestigationSearch.searchByKeywords(INVALID_USER, keywords, LogicalOperator.OR, em);
		log.trace("Investigations found with " + VALID_KEYWORD + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
	}



	@Test
	public void testSearchBySurname() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(
				VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION, em);
		log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
	}

	@Test
	public void testSearchBySurnameLower() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(
				VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION.toLowerCase(), em);
		log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION.toLowerCase() + ": "
				+ investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchBySurnameLimit() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(
				VALID_USER_FOR_INVESTIGATION, VALID_SURNAME_FOR_INVESTIGATION, 0, 100, em);
		log.trace("Investigations found with " + VALID_SURNAME_FOR_INVESTIGATION + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testSearchByInvalidSurname() throws ICATAPIException {
		log.info("Testing valid user, keywords: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(
				VALID_USER_FOR_INVESTIGATION, "asdsdasdasdasdas", em);
		log.trace("Investigations found with asdsdasdasdasdas: " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}


	@Test
	public void testMyInvestigationsIncludesLimit() throws ICATAPIException {
		log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

		Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(
				VALID_USER_FOR_INVESTIGATION, InvestigationInclude.ALL, 0, 1, em);
		log.trace("Investigations found with " + VALID_USER_FOR_INVESTIGATION + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testMyInvestigationsIncludesLimitInvalidUser() throws ICATAPIException {
		log.info("Testing valid user, My Investigations: " + INVALID_USER);

		Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER,
				InvestigationInclude.ALL, 0, 1, em);
		log.trace("Investigations found with " + INVALID_USER + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}

	@Test
	public void testMyInvestigationsInvalidUser() throws ICATAPIException {
		log.info("Testing invalid user, My Investigations: " + INVALID_USER);

		Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(INVALID_USER, em);
		log.trace("Investigations found with " + INVALID_USER + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}


	public void testAdvanced() throws ICATAPIException {
		log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

		AdvancedSearchDetails asd = new AdvancedSearchDetails();

		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION,
				asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be 3", 3, investigations.size());

		// test with name
		asd.setInvestigationType("experiment");

		// test with name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 3, investigations.size());

		// test with name
		asd.setInvestigationName("Investigation without any investigator");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// test with name fuzzy
		asd.setInvestigationName("Investigation without any investigator*");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add sample name
		asd.setSampleName("SrF2 calibration  w=-25.");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// add sample name fuzzy
		asd.setSampleName("SrF2 calibration  w=-25.*");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add visit id
		asd.setVisitId("12");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add grant id
		asd.setGrantId(15L);
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add abstract
		asd.setInvestigationAbstract("false ab");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 0, investigations.size());

		// add abstract fuzzy
		asd.setInvestigationAbstract("*abstrac*");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add abstract
		asd.setInvestigationAbstract("test abstract");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add bcatInvStr
		asd.setBackCatalogueInvestigatorString("false name");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 0, investigations.size());

		// add bcatInvStr fuzzy
		asd.setBackCatalogueInvestigatorString("damia*");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add bcatInvStr
		asd.setBackCatalogueInvestigatorString("damian");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		Collection<String> keywords = new ArrayList<String>();
		keywords.add("shull");

		asd.setKeywords(keywords);
		// add keywords
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		keywords.clear();
		keywords.add("shul*");
		// add valid keyword fuzzy
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		keywords.clear();
		keywords.add("deleted");
		// add invalid keyword
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add("deleted");
		keywords.add(" and ");
		keywords.add("dvveletedddddd");
		// add invalid keyword
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		Collection<String> instruments = new ArrayList<String>();
		instruments.add(VALID_INSTRUMENT1);
		asd.setInstruments(instruments);
		// reset keywords
		keywords.clear();
		keywords.add(VALID_KEYWORD3);

		// add instruments
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		instruments.add("false");
		instruments.add("SXD");
		asd.setInstruments(instruments);
		// reset keywords
		keywords.clear();
		keywords.add("shul*");

		// add instruments
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size() + " : " + investigations);

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setDatafileName("asknfasopfosdmfsdf");
		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setDatafileName("SXD015554.RAW");

		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setDatafileName("SXD015554.RA*");

		// add datafile name fuzzy
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setExperimentNumber("3434343434");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setExperimentNumber("12345");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		Collection<String> investigators = new ArrayList<String>();
		investigators.add("djdjdjdjkdjdjdjd");
		asd.setInvestigators(investigators);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		investigators.clear();
		investigators.add(VALID_SURNAME_FOR_INVESTIGATION);
		asd.setInvestigators(investigators);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		investigators.clear();
		investigators.add(VALID_SURNAME_FOR_INVESTIGATION.substring(0, 3) + "*");
		asd.setInvestigators(investigators);
		// add investigators fuzzy
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunStart(1000d);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunEnd(2000.0);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunEnd(1001.0);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

	}

	// @Test
	public void testAdvancedCaseInsensitive() throws ICATAPIException {
		log.info("Testing valid user, My Investigations: " + VALID_USER_FOR_INVESTIGATION);

		AdvancedSearchDetails asd = new AdvancedSearchDetails();
		asd.setCaseSensitive(false);

		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION,
				asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be 3", 3, investigations.size());

		// test with name
		asd.setInvestigationType("experiment");

		// test with name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 3, investigations.size());

		// test with name
		asd.setInvestigationName("INVESTIGATION without any investigators");

		// test with name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add sample name
		asd.setSampleName("SrF2 CALIBRATION  w=-25.3");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add visit id
		asd.setVisitId("12");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add grant id
		asd.setGrantId(15L);
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add abstract
		asd.setInvestigationAbstract("FALSE ab");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 0, investigations.size());

		// add abstract
		asd.setInvestigationAbstract("test ABSTRACT");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add abstract fuzzy
		asd.setInvestigationAbstract("test *BSTRAC*");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		// add bcatInvStr
		asd.setBackCatalogueInvestigatorString("FALSE name");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 0, investigations.size());

		// add bcatInvStr
		asd.setBackCatalogueInvestigatorString("DAMIAN");
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		Collection<String> keywords = new ArrayList<String>();
		keywords.add("SHULL");

		asd.setKeywords(keywords);
		// add keywords
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		keywords.clear();
		keywords.add("DELETED");
		// add invalid keyword
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add("DELETED");
		keywords.add(" AND");
		keywords.add("DELETEDdddddd");
		// add invalid keyword
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		Collection<String> instruments = new ArrayList<String>();
		instruments.add("SXD");
		asd.setInstruments(instruments);
		// reset keywords
		keywords.clear();
		keywords.add("SHUL*");

		// add instruments
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		instruments.add("false");
		instruments.add("SXD");
		asd.setInstruments(instruments);
		// reset keywords
		keywords.clear();
		keywords.add("SHUL*");

		// add instruments
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size() + " : " + investigations);

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setDatafileName("ASKnfasopfosdmfsdf");
		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setDatafileName("sxd015554.ra*");

		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setExperimentNumber("3434343434");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setExperimentNumber("12345");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		Collection<String> investigators = new ArrayList<String>();
		investigators.add("DJDJDjdjkdjdjdjd");
		asd.setInvestigators(investigators);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		investigators.clear();
		investigators.add(VALID_SURNAME_FOR_INVESTIGATION.toUpperCase());
		asd.setInvestigators(investigators);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunStart(1000d);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunEnd(2000.0);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be one", 1, investigations.size());

		asd.setRunEnd(1001.0);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

	}

	@Test
	public void testAdvancedDatafileName() throws ICATAPIException {
		log.info("Testing valid user, datafile name: " + VALID_USER_FOR_INVESTIGATION);

		AdvancedSearchDetails asd = new AdvancedSearchDetails();
		asd.setCaseSensitive(true);
		asd.setDatafileName(VALID_DATAFILE_NAME1);

		long time = System.currentTimeMillis();

		// test with name
		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced("gjd37", asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());

	}



	@Test
	public void testAdvancedRunNumber() throws ICATAPIException {
		log.info("Testing valid user, run number: " + VALID_USER_FOR_INVESTIGATION);

		AdvancedSearchDetails asd = new AdvancedSearchDetails();
		asd.setRunEnd(66660d);
		// asd.setRunStart(1d);

		log.trace("RunNumber? " + asd.hasRunNumber());

		long time = System.currentTimeMillis();

		// test with name
		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION,
				asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());
		log.trace(investigations);
		System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
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

		// test with name
		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION,
				asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());
		log.trace(investigations);
		System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals(0, investigations.size());
	}

	// @Test
	public void testAdvancedDF() throws ICATAPIException {
		log.info("Testing valid user, DF: " + VALID_USER_FOR_INVESTIGATION);

		AdvancedSearchDetails asd = new AdvancedSearchDetails();
		asd.setDatafileName("sXD01064.RAW");
		asd.setCaseSensitive(false);

		long time = System.currentTimeMillis();

		// test with name
		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(VALID_USER_FOR_INVESTIGATION,
				asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());
		log.trace(investigations);
		System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

		AdvancedSearchDetails asd2 = new AdvancedSearchDetails();
		asd2.setDatafileName("SXD01064.RAW");
		asd2.setCaseSensitive(true);

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be two", 1, investigations.size());

		time = System.currentTimeMillis();

		// test with name
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

		// test with name
		Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		// add sample name
		asd.setSampleName("SrF2 calibration  w=-25.3");
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		Collection<String> keywords = new ArrayList<String>();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);

		asd.setKeywords(keywords);
		// add keywords
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		keywords.clear();
		keywords.add("sdsdsdsdsds");
		// add invalid keyword
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		Collection<String> instruments = new ArrayList<String>();
		instruments.add("SXD");
		asd.setInstruments(instruments);
		// reset keywords
		keywords.clear();
		keywords.add(VALID_KEYWORD);
		keywords.add(VALID_KEYWORD2);
		// add instruments
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setDatafileName("asknfasopfosdmfsdf");
		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setDatafileName("SXD01256.RAW");
		// add datafile name
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setExperimentNumber("3434343434");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setExperimentNumber("32");
		// add ex number
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		Collection<String> investigators = new ArrayList<String>();
		investigators.add(VALID_SURNAME_FOR_INVESTIGATION);
		asd.setInvestigators(investigators);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setRunStart(1000d);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setRunEnd(2000d);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

		asd.setRunEnd(1001d);
		// add investigators
		investigations = InvestigationSearch.searchByAdvanced(INVALID_USER, asd, em);
		log.trace("Investigations found with " + asd + ": " + investigations.size());

		assertNotNull("Must not be an empty collection", investigations);
		assertEquals("Size should be zero", 0, investigations.size());

	}

}
