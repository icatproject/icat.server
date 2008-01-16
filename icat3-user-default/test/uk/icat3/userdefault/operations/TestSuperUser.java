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
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.util.UtilOperations;

/**
 *
 * @author gjd37
 */
public class TestSuperUser extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestSuperUser.class);
    
    private String correctUser = IcatRoles.SUPER_USER.toString(); //not checked now
    private String correctSuperUser = IcatRoles.SUPER_USER.toString();
    private String correctUserPassword = "supertestpassword"+Math.random();
    private String correctSid;
    
    @Test
    public void testValidSuperUsernamePassword() throws Exception {
        log.trace("testValidSuperUsernamePassword()");
        
        Session validSuperSession = UtilOperations.putValidAdminSession(correctSuperUser,em);
        
        UserManager userManager = new UserManager(em);
        
        //password not checked now
        String sessionId = userManager.login(correctUser,validSuperSession.getUserId().getPassword(), correctSuperUser);
        sessionId = userManager.login(correctUser+"invalid",validSuperSession.getUserId().getPassword()+"invalid", correctSuperUser);
       
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
        
        assertEquals("User runs as in DB must be "+correctSuperUser, returnedSession.getRunAs(),correctSuperUser);
        
        assertTrue("Session must be a super session", returnedSession.isSuper());
    }
    
    @Test
    public void testValidSessionid() throws Exception {
        log.trace("testValidSessionid()");
        UserManager userManager = new UserManager(em);
        
        Session validSuperSession = UtilOperations.putValidAdminSession(correctSuperUser,em);
        
        String userId = userManager.getUserIdFromSessionId(validSuperSession.getUserSessionId());
        
        assertNotNull("Correct user id, cannot be null", userId);
        assertEquals("user id must be "+correctSuperUser+" but is "+userId, userId, correctSuperUser);
    }
    
    @Test(expected=SessionException.class)
    public void testRemoveValidSessionid() throws Exception {
        log.trace("testRemoveValidSessionid()");
        
        UserManager userManager = new UserManager(em);
        
        Session validSuperSession = UtilOperations.putValidAdminSession(correctSuperUser,em);
        
        boolean loggedOut = userManager.logout(validSuperSession.getUserSessionId());
        
        assertNotNull("Logged out cannot be null", loggedOut);
        assertTrue("logged out must be true", loggedOut);
        
        //check session id not there
        try{
            UtilOperations.getSession(validSuperSession.getUserSessionId(),em);
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
        return new JUnit4TestAdapter(TestSuperUser.class);
    }
    
}
