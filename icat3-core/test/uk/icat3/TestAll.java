/*
 * TestAll.java
 *
 * Created on 07 March 2007, 14:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.search.TestInvestigationSearch;
import uk.icat3.search.TestNothing;
import uk.icat3.util.ExecuteDatabaseScript;
import uk.icat3.util.BaseTestClass;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestInvestigationSearch.class,
    TestNothing.class
})
public class TestAll {
    public static Test suite() {
        ExecuteDatabaseScript script = new ExecuteDatabaseScript("jdbc:oracle:thin:@murray.nd.rl.ac.uk:1521:prop", "icat3junit", "icat3junit");
        script.execute("database/ICAT3API_DropTables[v1].sql", ";");
        script.execute("database/ICAT3API_CreateSchema[v1].sql", ";");
        script.execute("database/ICAT3API_InsertListTables[v1].sql", ";");
        script.execute("database/ICAT3API_JUNIT_InsertTestData[v1].sql", ";");
        
        return new JUnit4TestAdapter(TestAll.class);
    }
}
