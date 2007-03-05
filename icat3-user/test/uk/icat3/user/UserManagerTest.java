package uk.icat3.user;


import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
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
public class UserManagerTest {
    
    String username = null;
    String password = null;
    UserManager instance = null;
    String sessionId = null;
    
    @Before
    public void setUp() throws Exception {
        username = "damian.flannery@rl.ac.uk";
        password = "helloworld";                    
        instance = new UserManager();        
    }

    @After
    public void tearDown() throws Exception {
        instance.logout(sessionId);
        sessionId = null;
        username = null;
        password = null;
        instance = null;
    }

    /**
     * Test of getUserIdFromSessionId method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=LoginException.class)
    public void testGetUserIdFromSessionIdWithInvalidSessionId() throws Exception {                                          
            String result = instance.getUserIdFromSessionId(sessionId);        
    }

    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    @Test
    public void testValidLogin() throws Exception {        
        String result = instance.login(username, password);
        assertNotNull(result);        
    }
    
    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=LoginException.class)
    public void testLoginWithInvalidUsername() throws Exception {           
        String result = instance.login("mickeymouse", password);                    
    }

    /**
     * Test of login method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=LoginException.class)
    public void testLoginWithInvalidPassword() throws Exception {           
        String result = instance.login(username, "pluto");        
    }
    
    /**
     * Test of logout method, of class uk.icat3.user.UserManager.
     */
    @Test
    public void testLogoutWithInvalidSessionId() throws Exception {                             
        instance.logout(sessionId);
        assertTrue(true);        
    }
    
    /**
     * Test of logout method, of class uk.icat3.user.UserManager.
     */
    @Test
    public void testLogoutWithValidSessionId() throws Exception {                               
        sessionId = instance.login(username, password);
        instance.logout(sessionId);
        assertTrue(true);        
    }

    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=LoginException.class)
    public void testGetUserDetailsWithInvalidSessionId() throws Exception {                        
        String user = "9932";                                
        UserDetails result = instance.getUserDetails(sessionId, user);                    
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=NoSuchUserException.class)
    public void testGetUserDetailsWithInvalidUser() throws Exception {                        
        String user = "thedarkoverlord";                                
        sessionId = instance.login(username, password);
        UserDetails result = instance.getUserDetails(sessionId, user);                    
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    @Test(expected=LoginException.class)
    public void testGetUserDetailsWithInvalidSessionIdAndInvalidUser() throws Exception {                        
        String user = "monkeyboy";                                
        UserDetails result = instance.getUserDetails(sessionId, user);                    
    }
    
    /**
     * Test of getUserDetails method, of class uk.icat3.user.UserManager.
     */
    @Test
    public void testValidGetUserDetails() throws Exception {                        
        String user = "9932";                                
        sessionId = instance.login(username, password);
        UserDetails result = instance.getUserDetails(sessionId, user);            
        assertNotNull(result);                        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(UserManagerTest.class);
    }
    
}
