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
import uk.icat3.investigationmanager.TestEntityBaseBean;
import uk.icat3.investigationmanager.TestInvestigation;
import uk.icat3.investigationmanager.TestInvestigator;
import uk.icat3.investigationmanager.TestKeyword;
import uk.icat3.investigationmanager.TestManagerUtil;
import uk.icat3.investigationmanager.TestPublication;
import uk.icat3.investigationmanager.TestSample;
import uk.icat3.investigationmanager.TestSampleParameter;
import uk.icat3.search.TestDatasetSearch;
import uk.icat3.search.TestInvalidUser;
import uk.icat3.search.TestInvestigationSearch;
import uk.icat3.search.TestKeywordSearch;
import uk.icat3.security.TestGateKeeperAdminDatafile;
import uk.icat3.security.TestGateKeeperAdminDataset;
import uk.icat3.security.TestGateKeeperAdminInvestigation;
import uk.icat3.security.TestGateKeeperCreatorDatafile;
import uk.icat3.security.TestGateKeeperCreatorDataset;
import uk.icat3.security.TestGateKeeperCreatorInvestigation;
import uk.icat3.security.TestGateKeeperDeleterDatafile;
import uk.icat3.security.TestGateKeeperDeleterDataset;
import uk.icat3.security.TestGateKeeperDeleterInvestigation;
import uk.icat3.security.TestGateKeeperDownloaderDatafile;
import uk.icat3.security.TestGateKeeperDownloaderDataset;
import uk.icat3.security.TestGateKeeperDownloaderInvestigation;
import uk.icat3.security.TestGateKeeperIcatAdminDatafile;
import uk.icat3.security.TestGateKeeperIcatAdminDataset;
import uk.icat3.security.TestGateKeeperIcatAdminInvestigation;
import uk.icat3.security.TestGateKeeperReaderDatafile;
import uk.icat3.security.TestGateKeeperReaderDataset;
import uk.icat3.security.TestGateKeeperReaderInvestigation;
import uk.icat3.security.TestGateKeeperUpdaterDatafile;
import uk.icat3.security.TestGateKeeperUpdaterDataset;
import uk.icat3.security.TestGateKeeperUpdaterInvestigation;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestInvestigation.class,
    TestInvestigator.class,
    TestKeyword.class,

    TestPublication.class,
    TestSample.class,
    TestSampleParameter.class,
    TestDataset.class,
    TestDatasetParameter.class,
    TestDatafile.class,
    TestDatafileParameter.class,
    TestEntityBaseBean.class,
    TestManagerUtil.class,

    TestDatasetSearch.class,
    TestInvalidUser.class,
    TestKeywordSearch.class,

    /*
     * Need modification...
     * TestDatafileSearch.class,
     */

   // TestInvestigationListSearch.class,  //*
    TestInvestigationSearch.class,      //*

    TestGateKeeperAdminInvestigation.class,
    TestGateKeeperCreatorInvestigation.class,
    TestGateKeeperDeleterInvestigation.class,
    TestGateKeeperDownloaderInvestigation.class,
    TestGateKeeperIcatAdminInvestigation.class,
    TestGateKeeperReaderInvestigation.class,
    TestGateKeeperUpdaterInvestigation.class,

    TestGateKeeperAdminDataset.class,
    TestGateKeeperCreatorDataset.class,
    TestGateKeeperDeleterDataset.class,
    TestGateKeeperDownloaderDataset.class,
    TestGateKeeperIcatAdminDataset.class,
    TestGateKeeperReaderDataset.class,
    TestGateKeeperUpdaterDataset.class,

    TestGateKeeperAdminDatafile.class,
    TestGateKeeperCreatorDatafile.class,
    TestGateKeeperDeleterDatafile.class,
    TestGateKeeperDownloaderDatafile.class,
    TestGateKeeperIcatAdminDatafile.class,
    TestGateKeeperReaderDatafile.class,
    TestGateKeeperUpdaterDatafile.class
            
})
        public class TestAll {
    
    
    public static Test suite() {
        return new JUnit4TestAdapter(TestAll.class);
    }
}
