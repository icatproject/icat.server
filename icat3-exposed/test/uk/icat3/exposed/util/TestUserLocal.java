/*
 * TestUserLocal.java
 *
 * Created on 01-Jun-2007, 12:55:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.util;

import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.user.UserDetails;

/**
 *
 * @author gjd37
 */
public class TestUserLocal implements UserSessionLocal{
    
    public TestUserLocal() {
    }
    
    public String getUserIdFromSessionId(String sessionId) throws SessionException{
        if(sessionId.equals(TestConstants.VALID_SESSION)) return  TestConstants.VALID_USER_FOR_INVESTIGATION;
        else if (sessionId.equals(TestConstants.VALID_SESSION_ICAT_ADMIN)) return  TestConstants.VALID_ICAT_ADMIN_FOR_INVESTIGATION;
        else if (sessionId.equals(TestConstants.INVALID_SESSION)) return  TestConstants.INVALID_USER;
        else throw new SessionException("Invalid sessionId: "+sessionId);
    }
    
    
    public String login(String username, String password) throws SessionException{
        if(username.equals("valid")) return "validSession";
        else throw new SessionException("Invalid username");
    }
    
    
    public String login(String username, String password, int lifetime) throws SessionException{
        throw new SessionException("Invalid username");
    }
    
    
    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException{
        throw new SessionException("Invalid adminUsername");
    }
    
    
    public String login(String credential) throws SessionException{
        throw new SessionException("Invalid credential");
    }
    
    
    public boolean logout(String sessionId){
        return true;
    }
    
    
    public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException{
        throw new SessionException("Invalid getUserDetails");
    }
}

