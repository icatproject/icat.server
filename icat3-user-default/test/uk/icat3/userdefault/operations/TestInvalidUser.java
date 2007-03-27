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
public class TestInvalidUser extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvalidUser.class);
    
    @Test(expected=SessionException.class)
    public void testInvalidUsernamePassword() throws SessionException{
        log.trace("testInvalidUsernamePassword()");
        
        UserManager userManager = new UserManager(em);
        try{
            String sessionId = userManager.login("stupid","stupid");
        } catch (SessionException ex) {
            log.debug(ex.getMessage());
            assertTrue("SessionException MyProxy exception", ex.getMessage().contains("MyProxy"));
            throw ex;
        }
    }
    
    @Test(expected=SessionException.class)
    public void testInvalidSessionid() throws SessionException{
        log.trace("testInvalidSessionid()");
        UserManager userManager = new UserManager(em);
        try {
            
            String sessionId = userManager.getUserIdFromSessionId("stupid");
        } catch (SessionException ex) {
            log.debug(ex.getMessage());
            assertTrue("SessionException must be invalid exception", ex.getMessage().contains("Invalid"));
            throw ex;
        }
    }
    
    @Test(expected=SessionException.class)
    public void testExpiredSessionid() throws SessionException{
        log.trace("testExpiredSessionid()");
        
        //get an invalid session (timeout out), creates one if not there
        Session session = UtilOperations.putInvalidSession(em);
        
        UserManager userManager = new UserManager(em);
        try {
            String sessionId = userManager.getUserIdFromSessionId(session.getUserSessionId());
        } catch (SessionException ex) {
            log.debug(ex.getMessage());
            assertTrue("SessionException must be expired exception", ex.getMessage().contains("expired"));
            throw ex;
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvalidUser.class);
    }
    
}
