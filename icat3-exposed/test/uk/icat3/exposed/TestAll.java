package uk.icat3.exposed;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.exposed.datafilemanager.TestDatafile;
import uk.icat3.exposed.datafilemanager.TestDatafileParameter;
import uk.icat3.exposed.datasetmanager.TestDataset;
import uk.icat3.exposed.datasetmanager.TestDatasetParameter;
import uk.icat3.exposed.investigationmanager.TestInvestigation;
import uk.icat3.exposed.investigationmanager.TestInvestigator;
import uk.icat3.exposed.investigationmanager.TestKeyword;
import uk.icat3.exposed.investigationmanager.TestPublication;
import uk.icat3.exposed.investigationmanager.TestSample;
import uk.icat3.exposed.investigationmanager.TestSampleParameter;


@RunWith(Suite.class)
@Suite.SuiteClasses({
            TestSample.class,
            TestKeyword.class,
            TestPublication.class,
            TestInvestigator.class,
            TestInvestigation.class,
            TestSampleParameter.class,
            
            TestDataset.class,
            TestDatasetParameter.class,
            
            TestDatafile.class,
            TestDatafileParameter.class
            
            //TestKeywordSearch.class,
            //TestInvalidUser.class
})
public class TestAll {
       
    
    public static Test suite() {              
        
        return new JUnit4TestAdapter(TestAll.class);
    }
}
