/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.user;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.exceptions.SessionException;

/**
 * Tests the UserManager.
 * @author Mr. Srikanth Nagella
 */
public class UserManagerTest {

    private static String userDB = "uk.icat3.user.UserDB";
    
    public UserManagerTest() {
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getUserIdFromSessionId method, of class UserManager.
     */
    @Test
    public void testGetUserIdFromSessionId() throws Exception {
        String sessionId = "session";
        String expResult = "test";
        UserManager manager = new UserManager(userDB);
        String result = manager.getUserIdFromSessionId(sessionId);
        assertEquals("Invalid getUserIdFromSessionId",expResult, result);
    }

    /**
     * Test of getUserIdFromSessionId method, of class UserManager.
     */
    @Test (expected=SessionException.class)
    public void testGetUserIdFromSessionIdInvalid() throws Exception {
        String sessionId = "invalidsession";
        UserManager manager = new UserManager(userDB);
        String result = manager.getUserIdFromSessionId(sessionId);
        fail("Returned a invalid user.");
    }
    
    /**
     * Test of login method, of class UserManager.
     */
    @Test
    public void testLoginUsernameAndPassword() throws Exception {
        String username = "test";
        String password = "password";
        String expResult = "session";
        UserManager manager = new UserManager(userDB);
        String result = manager.login(username, password);
        assertEquals(expResult, result);
    }

    /**
     * Test of login method, of class UserManager.
     */
    @Test(expected=SessionException.class)
    public void testLoginUsernameAndInvalidPassword() throws Exception {
        String username = "test";
        String password = "pass";
        String expResult = "session";
        UserManager manager = new UserManager(userDB);
        String result = manager.login(username, password);
        fail("Authorised on a invalid password.");
    }

    /**
     * Test of login method, of class UserManager.
     */
    @Test
    public void testLoginUsernamePasswordAndLifetime() throws Exception {
        String username = "test";
        String password = "password";
        int lifetime = 0;
        String expResult = "session";
        UserManager manager = new UserManager(userDB);
        String result = manager.login(username, password,lifetime);
        assertEquals(expResult, result);
    }

    @Test (expected=SessionException.class)
    public void testLoginUsernamePasswordAndLifetimeInvalid() throws Exception {
        String username = "test";
        String password = "pass";
        int lifetime = 0;
        String expResult = "session";
        UserManager manager = new UserManager(userDB);
        String result = manager.login(username, password,lifetime);
        fail("Authorised on a invalid password.");
    }
    /**
     * Test of logout method, of class UserManager.
     */
    @Test
    public void testLogout() throws SessionException{
        String sessionId = "session";
        UserManager manager = new UserManager(userDB);
            boolean expResult = true;
            boolean result = manager.logout(sessionId);
            assertEquals("Logout Error",expResult, result);
    }

    /**
     * Test of getUserDetails method, of class UserManager.
     */
    @Test
    public void testGetUserDetails() throws Exception {
        String sessionId = "session";
        String user = "test";
        UserManager manager = new UserManager(userDB);
        UserDetails result = manager.getUserDetails(sessionId, user);
        assertEquals("Get userdetails error","test", result.getFederalId());
    }

    /**
     * Test of login method, of class UserManager.
     */
    @Test
    public void testLoginAdminUsernameAndPassword() throws Exception {
        String adminUsername = "admin";
        String AdminPassword = "adminpassword";
        String runAsUser = "test";
        UserManager manager = new UserManager(userDB);
        String expResult = "session";
        String result = manager.login(adminUsername, AdminPassword, runAsUser);
        assertEquals("Invalid session Id ",expResult, result);
    }

    /**
     * Test of login method, of class UserManager.
     */
    @Test
    public void testLoginCredential() throws Exception {
        String credential = "credential";
        UserManager manager = new UserManager(userDB);
        String expResult = "session";
        String result = manager.login(credential);
        assertEquals("Invalid credential",expResult, result);
    }

}