package uk.icat3.user;

import uk.icat3.exceptions.LoginException;
import uk.icat3.exceptions.NoSuchUserException;

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
    
    /**
     * Returns a userid/distinguished name for a user with a valid
     * sessionId.  If user does not have a valid sessionId then an
     * {@link LoginException} will be thrown.
     *
     * @param sessionId authentication token obtained on successful login
     * @return userid/distinguished name of user
     * @throws LoginException  if user provides an invalid sessionId
     */
    public String getUserIdFromSessionId (String sessionId) throws LoginException;
    
    /**
     * Returns a sessionId (authenitcation token) to a user after verification
     * of correct username and password combination.  If user does not provide 
     * the correct login credentials a {@link LoginException} will be thrown.
     *
     * @param username username/dn of user
     * @param password of user
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide username and
     *                   password each time.
     * @throws LoginException   if user provides an invalid username and password
     *                          combination.
     */
    public String login (String username, String password) throws LoginException;
    
    /**
     * Removes sessionId authentication token from user database which
     * effectively logs user out of the system.  Any further attempt to
     * use the icat3 api with an 'old' sessionId will fail as it will
     * no longer exist in the user database.  Any potential exceptions
     * that could be raised should be suppressed e.g. logging out a user
     * who is already logged out.
     *
     * @param sessionId authentication token obtained on successful login     
     */
    public void logout (String sessionId);
    
    /**
     * Returns a serializable object that contains personal details of a requested
     * <code>user</code> as defined in {@link UserDetails}.  A {@link LoginException}
     * should be thrown if an invalid sessionId is supplied and a 
     * {@link No SuchUserException} should be thrown if the requested <code>user</code>
     * cannot be found in the database.  If both <code>sessionId</code> and 
     * <code>user</code> are invalid then a {@link LoginException} should be thrown first.
     *
     * @param username username/dn of user
     * @param password of user
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide username and
     *                   password each time.
     * @throws LoginException   if user provides an invalid <code>sessionId</code>
     * @throws NoSuchUserException   if <code>user</code> cannot be found in user database
     * @see UserDetails
     */
    public UserDetails getUserDetails(String sessionId, String user) throws LoginException, NoSuchUserException;     
}