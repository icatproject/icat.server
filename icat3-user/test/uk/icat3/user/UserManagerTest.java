package uk.icat3.user;

import junit.framework.*;
import uk.icat3.user.exceptions.LoginException;
import uk.icat3.user.exceptions.NoSuchUserException;
/*
 * UserManagerTest.java
 *
 * Created on 21 February 2007, 13:40
 *
 * JUnit tests for UserManager.java - Please update setUp() method
 * with a valid username and password for testing purposes.
 *
 * @author df01
 * @version 1.0
 */
public class UserManagerTest extends TestCase {
    
    String username = null;
    String password = null;
    UserManager instance = null;
    String sessionId = null;
    
    public UserManagerTest(String testName) {        
        super(testName);        
    }

    protected void setUp() throws Exception {
        username = "damian.flannery@rl.ac.uk";
        password = "helloworld";    
        
        try {
            instance = new UserManager();
        } catch (Exception e) {            
            fail("failed to establish connection with user database");
        }
    }

    protected void tearDown() throws Exception {
        instance.logout(sessionId);
        sessionId = null;
        username = null;
        password = null;
        instance = null;
    }

    /**
     * Test of getUserIdFromSessionId method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserIdFromSessionIdWithInvalidSessionId() throws Exception {                           
        try {        
            String result = instance.getUserIdFromSessionId(sessionId);
            fail("Shouldn't get here because I passed in an invalid sessionId!");
        } catch (LoginException le) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed as expected, but with an unexpected exception type");
        }//end try/catch                
    }

    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    public void testValidLogin() throws Exception {        
        String result = instance.login(username, password);
        assertNotNull(result);        
    }
    
    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    public void testLoginWithInvalidUsername() throws Exception {   
        try {
            String result = instance.login("mickeymouse", password);
            fail("Should have thrown exception by now");
        } catch (LoginException le) {
            assertTrue(true);
        } catch (Exception e) {
            fail("wrong exception type was thrown");
        }//end try/catch        
    }

    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    public void testLoginWithInvalidPassword() throws Exception {   
        try {
            String result = instance.login(username, "pluto");
            fail("Should have thrown exception before now");
        } catch (LoginException le) {
            assertTrue(true);
        } catch (Exception e) {
            fail("incorrect exception was thrown");
        }//end try/catch        
    }
    
    /**
     * Test of logout method, of class uk.icat3.user.UserManager.
     */
    public void testLogoutWithInvalidSessionId() {                     
        try {
            instance.logout(sessionId);
            assertTrue(true);
        } catch(Exception e) {
            fail("Should not throw an exception");
        }//end try/catch        
    }
    
    /**
     * Test of logout method, of class uk.icat3.user.UserManager.
     */
    public void testLogoutWithValidSessionId() {                       
        try {
            sessionId = instance.login(username, password);
            instance.logout(sessionId);
            assertTrue(true);
        } catch(Exception e) {
            fail("Should not throw an exception");
        }//end try/catch        
    }

    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserDetailsWithInvalidSessionId() throws Exception {                        
        String user = "9932";                        
        try {
            UserDetails result = instance.getUserDetails(sessionId, user);            
            fail("Should have thrown an exception before here");
        } catch (LoginException le) {
            assertTrue(true);
        } catch(Exception e) {
            fail("Failed as expected, but with the wrong exception type");
        }//end try/catch                
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserDetailsWithInvalidUser() throws Exception {                        
        String user = "thedarkoverlord";                        
        try {
            sessionId = instance.login(username, password);
            UserDetails result = instance.getUserDetails(sessionId, user);            
            fail("Should have thrown an exception before here");
        } catch (LoginException le) {
            fail("Wrong exception type thrown, should be NoSuchUserException");
        } catch (NoSuchUserException nsu) {
            assertTrue(true);
        } catch(Exception e) {
            fail("Failed as expected, but with the wrong exception type");
        }//end try/catch                
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    public void testGetUserDetailsWithInvalidSessionIdAndInvalidUser() throws Exception {                        
        String user = "monkeyboy";                        
        try {
            UserDetails result = instance.getUserDetails(sessionId, user);            
            fail("Should have thrown an exception before here");
        } catch (NoSuchUserException nsu) {
            fail("wrong exception type thrown");
        } catch (LoginException le) {
            assertTrue(true);
        } catch(Exception e) {
            fail("Failed as expected, but with the wrong exception type");
        }//end try/catch                
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    public void testValidGetUserDetails() throws Exception {                        
        String user = "9932";                        
        try {
            sessionId = instance.login(username, password);
            UserDetails result = instance.getUserDetails(sessionId, user);            
            assertNotNull(result);                
        } catch(Exception e) {
            fail("Should not have failed");
        }//end try/catch                
    }
    
}
