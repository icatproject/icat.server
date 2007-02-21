/*
 * UserManager.java
 *
 * Created on 20 February 2007, 15:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user;

import uk.icat3.user.facility.ISISUser;

/**
 *
 * @author df01
 */
public class UserManager implements User {        
    private User user;
    
    /** Creates a new instance of UserManager */
    public UserManager()  {
        user = new ISISUser();  
        
    }
    
    public String getUserIdFromSessionId (String sessionId) {
        return user.getUserIdFromSessionId(sessionId);
    }
    
    public String login (String username, String password) {
        return user.login(username, password);
    }
    
    public void logout (String sessionId) {
        user.logout(sessionId);
    }
    
    public UserDetails getUserDetails(String sessionId, String user) {
        return this.user.getUserDetails(sessionId, user);
    }
    
}

