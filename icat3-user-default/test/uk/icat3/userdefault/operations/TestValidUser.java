/*
 * TestInvalidUser.java
 *
 * Created on 21 March 2007, 16:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.operations;

import java.security.cert.CertificateException;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.entity.Session;
import uk.icat3.userdefault.facility.Certificate;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.util.UtilOperations;

/**
 *
 * @author gjd37
 */
public class TestValidUser extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestValidUser.class);
    private String correctUser = System.getProperty("user.name");
    private String correctUserPassword = System.getProperty("usersso.password");
    private String correctSid;
    
     @Test
    public void testValidUsernamePassword() throws SessionException, CertificateException {
        log.trace("testValidUsernamePassword()");
        assertNotNull("Username cannot be null, please change/add the runtime arguments -Duser.name=",correctUser);
        assertNotNull("Password cannot be null, please change/add the runtime arguments -Dusersso.password=",correctUserPassword);
        assertFalse("Username cannot be null, please change/add the runtime arguments -Duser.name=",correctUser.compareTo("")==0);
        assertFalse("Password cannot be null, please change/add the runtime arguments -Dusersso.password=",correctUserPassword.compareTo("")==0);
        UtilOperations.loadMyProxyServer(em);
        
        UserManager userManager = new UserManager(em);
        
        String sessionId = userManager.login(correctUser,correctUserPassword);
        
        assertNotNull("Correct session id, cannot be null", sessionId);
        log.debug("SessionId returned: "+sessionId);
        
        //TODO now check all variables in session and user DB
        Session returnedSession = UtilOperations.getSession(sessionId, em);
        
        //now check session
        assertNotNull("User cannot be null for sid: "+sessionId,returnedSession.getUserId());
        assertNotNull("Session id cannot be null for sid: "+sessionId,returnedSession.getUserSessionId());
        assertNotNull("Expiretime cannot be null for sid: "+sessionId,returnedSession.getExpireDateTime());
        
        //assertEquals("User in DB must be "+correctUser, returnedSession.getUserId().getUserId(),correctUser);
        assertEquals("Sessionid in DB must be "+sessionId+", but was "+returnedSession.getUserSessionId(), returnedSession.getUserSessionId(),sessionId);
        
        Certificate cert = new Certificate(returnedSession.getCredential());
        
        assertEquals("cert in DB must have DN "+cert.getDn()+" but was "+returnedSession.getUserId().getDn(), returnedSession.getUserId().getDn(), cert.getDn());
        
        
    }
    
    //@Test
    public void testValidSessionid() throws SessionException {
        log.trace("testValidSessionid()");
        UserManager userManager = new UserManager(em);
        
        Session validSession = UtilOperations.putValidSession(em);
        
        String userId = userManager.getUserIdFromSessionId(validSession.getUserSessionId());
        
        assertNotNull("Correct user id, cannot be null", userId);
        assertEquals("user id must be "+validSession.getUserId().getUserId()+" but is "+userId, userId, validSession.getUserId().getUserId());
    }
    
    //@Test(expected=SessionException.class)
    public void testRemoveValidSessionid() throws SessionException {
        log.trace("testRemoveValidSessionid()");
        
        UserManager userManager = new UserManager(em);
        
        Session validSession = UtilOperations.putValidSession(em);
        
        boolean loggedOut = userManager.logout(validSession.getUserSessionId());
        
        assertNotNull("Logged out cannot be null", loggedOut);
        assertTrue("logged out must be true", loggedOut);
        
        //check session id not there
        try{
            UtilOperations.getSession(validSession.getUserSessionId(),em);
        } catch (SessionException ex) {
            log.debug(ex.getMessage());
            assertTrue("SessionException must be invalid exception", ex.getMessage().contains("Invalid"));
            throw ex;
        }
    }
    
   // @Test
    public void testRemoveInvalidSessionid() throws SessionException {
        log.trace("testRemoveInvalidSessionid()");
        
        UserManager userManager = new UserManager(em);
                 
        boolean loggedOut = userManager.logout("stupid sessionid "+Math.random());
        
        assertNotNull("Logged out cannot be null", loggedOut);
        assertFalse("logged out must be false", loggedOut);
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestValidUser.class);
    }
    
}
