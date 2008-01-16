/*
 * TestAll.java
 *
 * Created on 22 March 2007, 09:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.userdefault.operations.TestAdminUser;
import uk.icat3.userdefault.operations.TestInvalidUser;
import uk.icat3.userdefault.operations.TestSuperUser;
import uk.icat3.userdefault.operations.TestValidUser;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestInvalidUser.class,
    TestAdminUser.class,
    TestValidUser.class,
    TestSuperUser.class
})
public class TestAll {
    
    
    public static Test suite() {
        
        return new JUnit4TestAdapter(TestAll.class);
    }
}
