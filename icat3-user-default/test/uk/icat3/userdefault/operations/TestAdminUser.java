/*
 * TestInvalidUser.java
 *
 * Created on 21 March 2007, 16:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.operations;

import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.entity.Session;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.util.UtilOperations;

/**
 *
 * @author gjd37
 */
public class TestAdminUser extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestAdminUser.class);
    
    private String correctUser = "ADMIN_USER";
    private String correctAdminUser = "admin";
    private String correctUserPassword = "admintestpassword"+Math.random();
    private String correctSid;
    
    @Test
    public void testValidAdminUsernamePassword() throws Exception {
        log.trace("testValidAdminUsernamePassword()");
        
        Session validAdminSession = UtilOperations.putValidAdminSession(correctAdminUser,em);
        
        UserManager userManager = new UserManager(em);
        
        //password not checked now
        String sessionId = userManager.login(correctUser,validAdminSession.getUserId().getPassword(), correctAdminUser);
        sessionId = userManager.login(correctUser+"invalid",validAdminSession.getUserId().getPassword()+"invalid", correctAdminUser);
       
        assertNotNull("Correct session id, cannot be null", sessionId);
        log.debug("SessionId returned: "+sessionId);
        
        //TODO now check all variables in session and user DB
        Session returnedSession = UtilOperations.getSession(sessionId, em);
        
        //now check session
        assertNotNull("User cannot be null for sid: "+sessionId,returnedSession.getUserId());
        assertNotNull("Session id cannot be null for sid: "+sessionId,returnedSession.getUserSessionId());
        assertNotNull("Expiretime cannot be null for sid: "+sessionId,returnedSession.getExpireDateTime());
        
        assertEquals("User in DB must be "+correctUser, returnedSession.getUserId().getUserId(),correctUser);
        assertEquals("Sessionid in DB must be "+sessionId+", but was "+returnedSession.getUserSessionId(), returnedSession.getUserSessionId(),sessionId);
        
        assertEquals("User runs as in DB must be "+correctAdminUser, returnedSession.getRunAs(),correctAdminUser);
        
        assertTrue("Session must be a admin session", returnedSession.isAdmin());
    }
    
    @Test
    public void testValidSessionid() throws Exception {
        log.trace("testValidSessionid()");
        UserManager userManager = new UserManager(em);
        
        Session validAdminSession = UtilOperations.putValidAdminSession(correctAdminUser,em);
        
        String userId = userManager.getUserIdFromSessionId(validAdminSession.getUserSessionId());
        
        assertNotNull("Correct user id, cannot be null", userId);
        assertEquals("user id must be "+correctAdminUser+" but is "+userId, userId, correctAdminUser);
    }
    
    @Test(expected=SessionException.class)
    public void testRemoveValidSessionid() throws Exception {
        log.trace("testRemoveValidSessionid()");
        
        UserManager userManager = new UserManager(em);
        
        Session validAdminSession = UtilOperations.putValidAdminSession(correctAdminUser,em);
        
        boolean loggedOut = userManager.logout(validAdminSession.getUserSessionId());
        
        assertNotNull("Logged out cannot be null", loggedOut);
        assertTrue("logged out must be true", loggedOut);
        
        //check session id not there
        try{
            UtilOperations.getSession(validAdminSession.getUserSessionId(),em);
        } catch (SessionException ex) {
            log.debug(ex.getMessage());
            assertTrue("SessionException must be invalid exception", ex.getMessage().contains("Invalid"));
            throw ex;
        }
    }
    
    @Test
    public void testRemoveInvalidSessionid() throws SessionException {
        log.trace("testRemoveInvalidSessionid()");
        
        UserManager userManager = new UserManager(em);
        
        boolean loggedOut = userManager.logout("stupid sessionid "+Math.random());
        
        assertNotNull("Logged out cannot be null", loggedOut);
        assertFalse("logged out must be false", loggedOut);
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestAdminUser.class);
    }
    
}
