package uk.icat3.user;

/*
 * User.java
 *
 * Created on 20 February 2007, 15:52
 *
 * The icat3 api enables each facility to plug in their own
 * user database to authenticate and provide personal details
 * such as name, title, email address etc. to the api callers
 * as context information e.g. displaying names of investigators
 * for an experiment instead of just their userid/distinguished
 * name as held in the icat3 database.
 *
 * <p>In order to acheive this, the icat2 api provides a contract
 * (java interface) of methods that any user database (to be used
 * by icat3) must implement.  These methods and their signatures
 * are defined below.</p> 
 *
 * @author df01
 * @version 1.0
 */
public interface User {
    
    public String getUserIdFromSessionId (String sessionId);
    
    public String login (String username, String password);
    
    public void logout (String sessionId);
    
    public UserDetails getUserDetails(String sessionId, String user);     
}