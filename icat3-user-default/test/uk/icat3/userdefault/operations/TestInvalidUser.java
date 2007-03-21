/*
 * TestInvalidUser.java
 *
 * Created on 21 March 2007, 16:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.operations;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.persistence.NoResultException;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.entity.Session;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestInvalidUser extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvalidUser.class);
    
    // @Test(expected=SessionException.class)
    public void testInvalidUsernamePassword() throws SessionException{
        log.trace("testInvalidUsernamePassword()");
        
        UserManager userManager = new UserManager(em);
        String sessionId = userManager.login("stupid","stupid");
        
    }
    
    @Test(expected=SessionException.class)
    public void testInvalidSessionid() throws SessionException{
        log.trace("testInvalidSessionid()");
        UserManager userManager = new UserManager(em);
        String sessionId = userManager.getUserIdFromSessionId("stupid");
    }
    
    @Test(expected=SessionException.class)
    public void testExpiredSessionid() throws SessionException{
        log.trace("testExpiredSessionid()");
        Session session;
        try {
            session = (Session) em.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", "expiredsession").getSingleResult();
            log.trace("Already got expired session");
        } catch(NoResultException ex) {
            //create one
            log.trace("Creating expired session");
            //input invalid one
            session = new Session();
            Calendar cal =  GregorianCalendar.getInstance();
            cal.add(GregorianCalendar.HOUR,-5); //minus 5 hours, expired
            session.setExpireDateTime(cal.getTime());
            session.setUserSessionId("expiredsession");
            
            session.setCredential("expiredsession_CREDENTIAL");
            em.persist(session);
        }
                     
        UserManager userManager = new UserManager(em);
        String sessionId = userManager.getUserIdFromSessionId("expiredsession");
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvalidUser.class);
    }
    
}
