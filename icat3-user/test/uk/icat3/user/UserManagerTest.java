/*
 * UserManagerTest.java
 * JUnit based test
 *
 * Created on 21 February 2007, 13:40
 */

package uk.icat3.user;

import junit.framework.*;
import uk.icat3.user.exceptions.LoginException;
import uk.icat3.user.facility.ISISUser;

/**
 *
 * @author df01
 */
public class UserManagerTest extends TestCase {
    
    String username = null;
    String password = null;
    UserManager instance = new UserManager();
    
    public UserManagerTest(String testName) {        
        super(testName);        
    }

    protected void setUp() throws Exception {
        username = "damian.flannery@rl.ac.uk";
        password = "helloworld";        
    }

    protected void tearDown() throws Exception {
        username = null;
        password = null;
    }

    /**
     * Test of getUserIdFromSessionId method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserIdFromSessionIdWithInvalidSessionId() throws Exception {
        System.out.println("JUNIT____getUserIdFromSessionId");
        
        String sessionId = "";        
        String expResult = "9932";
        
        try {        
            String result = instance.getUserIdFromSessionId(sessionId);
            fail("Shouldn't get here because I passed in an invalid sessionId!");
        } catch (LoginException le) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed as expected, but with an unexpected exception type");
        }
                
    }

    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    public void testValidLogin() throws Exception {
        System.out.println("JUNIT____login");                                        
        String result = instance.login(username, password);
        assertNotNull(result);        
    }

    /**
     * Test of logout method, of class uk.icat3.user.UserManager.
     */
    public void testInvalidLogout() {
        System.out.println("logout");        
        String sessionId = "";
        
        try {
            instance.logout(sessionId);
            assertTrue(true);
        } catch(Exception e) {
            fail("Should not throw an exception");
        }
        
    }

    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserDetailsWithInvalidSessionId() throws Exception {
        System.out.println("JUNIT____getUserDetails");
        
        String sessionId = "";
        String user = "9932";        
        
        UserDetails expResult = null;
        try {
            UserDetails result = instance.getUserDetails(sessionId, user);
            System.out.println(result.getDepartment());
            fail("Should have thrown an exception before here");
        } catch (LoginException le) {
            assertTrue(true);
        } catch(Exception e) {
            fail("Failed as expected, but with the wrong exception type");
        }
        
                
    }
    
}
