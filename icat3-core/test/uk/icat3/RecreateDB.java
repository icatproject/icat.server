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
import uk.icat3.util.ExecuteDatabaseScript;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   
})
public class RecreateDB {
    
    
    public static Test suite() {
        
        //icat unit test
       // ExecuteDatabaseScript script = new ExecuteDatabaseScript("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(HOST=elektra.dl.ac.uk)(PROTOCOL=tcp)(PORT=1521))(CONNECT_DATA=(SID=minerva2)))", "icat_apitest", "bb8isb4ck");
        
        //icat scratch
        ExecuteDatabaseScript script = new ExecuteDatabaseScript("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(HOST=elektra.dl.ac.uk)(PROTOCOL=tcp)(PORT=1521))(CONNECT_DATA=(SID=minerva2)))", "icat_scratch", "c1sco");
        
        script.execute("database/ICAT3API_DropTables[v1].sql", ";");
        script.execute("database/ICAT3API_CreateSchema[v1].sql", ";");
        script.execute("database/ICAT3API_InsertListTables[v1].sql", ";");
        script.execute("database/ICAT3API_JUNIT_InsertTestData[v1].sql", ";");
        
        return new JUnit4TestAdapter(TestAll.class);
    }
}
