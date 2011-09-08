package uk.icat3;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import uk.icat3.manager.TestDatafileManager;
import uk.icat3.manager.TestDatasetManager;
import uk.icat3.manager.TestInvestigationManager;
import uk.icat3.manager.TestInvestigatorManager;
import uk.icat3.manager.TestParameterManager;
import uk.icat3.search.TestDatasetSearch;
import uk.icat3.search.TestGet;
import uk.icat3.search.TestInvestigationSearch;
import uk.icat3.search.TestSearch;
import uk.icat3.security.TestDagHandler;
import uk.icat3.security.TestEntityInfo;
import uk.icat3.security.TestGateKeeper;
import uk.icat3.security.parser.TestRestriction;
import uk.icat3.security.parser.TestTokenizer;

@RunWith(Suite.class)
@Suite.SuiteClasses({

	TestRestriction.class,
	TestTokenizer.class,
	TestEntityInfo.class,
	TestDagHandler.class,
	TestSearch.class,
	TestGateKeeper.class,
	TestDatasetSearch.class,
	TestInvestigationSearch.class, 
	TestParameterManager.class,
	TestInvestigatorManager.class,
	TestInvestigationManager.class,
	TestDatasetManager.class,
	TestDatafileManager.class,
	TestGet.class
		
})
public class TestAll {
}
