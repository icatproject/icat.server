/*
 * TestAll.java
 *
 * Created on 07 March 2007, 14:44
 *
 * JUNIT Test Suite that creates a connection to a specified ICAT3 JUNIT
 * test database schema and executes a number of scripts in order to
 * initialise the database for the JUNIT tests that are to be
 * subsequently called.  Add each JUNIT class (that is to be run) to the
 * @Suite.SuiteClasses annotation.
 *
 * @author df01
 * @version 1.0
 */
package uk.icat3;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.datafilemanager.TestDatafile;
import uk.icat3.datafilemanager.TestDatafileParameter;
import uk.icat3.datasetmanager.TestDataset;
import uk.icat3.datasetmanager.TestDatasetParameter;
import uk.icat3.investigationmanager.TestInvestigation;
import uk.icat3.investigationmanager.TestInvestigator;
import uk.icat3.investigationmanager.TestKeyword;
import uk.icat3.investigationmanager.TestManagerUtil;
import uk.icat3.investigationmanager.TestPublication;
import uk.icat3.investigationmanager.TestSample;
import uk.icat3.investigationmanager.TestSampleParameter;
import uk.icat3.search.TestDatafileSearch;
import uk.icat3.search.TestDatasetSearch;
import uk.icat3.search.TestInvalidUser;
import uk.icat3.search.TestInvestigationSearch;
import uk.icat3.search.TestKeywordSearch;
import uk.icat3.security.TestGateKeeperAdminInvestigation;
import uk.icat3.security.TestGateKeeperCreatorInvestigation;
import uk.icat3.security.TestGateKeeperDeleterInvestigation;
import uk.icat3.security.TestGateKeeperDownloaderInvestigation;
import uk.icat3.security.TestGateKeeperIcatAdminInvestigation;
import uk.icat3.security.TestGateKeeperReaderInvestigation;
import uk.icat3.security.TestGateKeeperUpdaterInvestigation;
import uk.icat3.util.ExecuteDatabaseScript;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    
               
            TestSample.class,
            TestKeyword.class,
            TestPublication.class,
            TestInvestigator.class,
            TestInvestigation.class,
            TestSampleParameter.class,
           // TestManagerUtil.class,
            
            TestDataset.class,
            TestDatasetParameter.class,
            
            TestDatafile.class,
            TestDatafileParameter.class,
            
            TestKeywordSearch.class,
            TestInvalidUser.class,
            
            TestDatafileSearch.class,
            TestDatasetSearch.class,
            TestInvestigationSearch.class,
            
            TestGateKeeperAdminInvestigation.class,
            TestGateKeeperCreatorInvestigation.class,
            TestGateKeeperDeleterInvestigation.class,
            TestGateKeeperDownloaderInvestigation.class,
            TestGateKeeperIcatAdminInvestigation.class,
            TestGateKeeperReaderInvestigation.class,
            TestGateKeeperUpdaterInvestigation.class
})
        public class TestAll {
    
    
    public static Test suite() {
        
          //icat scratch
        ExecuteDatabaseScript script = new ExecuteDatabaseScript("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(HOST=elektra.dl.ac.uk)(PROTOCOL=tcp)(PORT=1521))(CONNECT_DATA=(SID=minerva2)))", "icat_scratch", "c1sco");
        
        script.execute("database/ICAT3API_DropTables[v1].sql", ";");
        script.execute("database/ICAT3API_CreateSchema[v1].sql", ";");
        script.execute("database/ICAT3API_InsertListTables[v1].sql", ";");
        script.execute("database/ICAT3API_JUNIT_InsertTestData[v1].sql", ";");
        
        return new JUnit4TestAdapter(TestAll.class);
    }
}
