package uk.icat3.user;

import uk.icat3.user.exceptions.LoginException;
import uk.icat3.user.exceptions.NoSuchUserException;
import uk.icat3.user.facility.ISISUser;

/*
 * UserManager.java
 *
 * Created on 20 February 2007, 15:53
 *
 * Added UserManager class that will act as the user database implementation 
 * and will be used directly by icat3.  In fact, this class will delegate all 
 * method calls to a facility specific implementation of the User.java interface 
 * (e.g. to ISISUser.java - which will provide all the necessary work to 
 * retrieve the desired information from the ISIS user database and present it 
 * in a form digestable for icat3).
 * 
 * @author df01
 * @version 1.0
 */
public class UserManager implements User {        
    private User user;
    
    /** Creates a new instance of UserManager */
    public UserManager() throws LoginException {
        user = new ISISUser();          
    }
    
    public String getUserIdFromSessionId (String sessionId) throws LoginException {
        return user.getUserIdFromSessionId(sessionId);
    }
    
    public String login (String username, String password) throws LoginException {
        return user.login(username, password);
    }
    
    public void logout (String sessionId) {
        user.logout(sessionId);
    }
    
    public UserDetails getUserDetails(String sessionId, String user) throws LoginException, NoSuchUserException {
        return this.user.getUserDetails(sessionId, user);
    }
    
}