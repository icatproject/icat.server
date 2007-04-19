package uk.icat3.user;

import uk.icat3.exceptions.SessionException;
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
     * {@link SessionException} will be thrown.
     * 
     * @param sessionId authentication token obtained on successful login
     * @return userid/distinguished name of user
     * @throws SessionException  if user provides an invalid sessionId
     */
    public String getUserIdFromSessionId(String sessionId) throws SessionException;
    
    /**
     * Returns a sessionId (authenitcation token) to a user after verification
     * of correct username and password combination.  If user does not provide
     * the correct login credentials a {@link SessionException} will be thrown.
     * 
     * @param username username/dn of user
     * @param password of user
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide username and
     *                   password each time.
     * @throws SessionException   if user provides an invalid username and password
     *                          combination.
     */
    public String login(String username, String password) throws SessionException;
    
    /**
     * Returns a sessionId (authenitcation token) to a user after verification
     * of correct username and password combination.  If user does not provide
     * the correct login credentials a {@link SessionException} will be thrown.
     * 
     * @param username username/dn of user
     * @param password of user
     * @param lifetime of the sesssion
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide username and
     *                   password each time.
     * @throws SessionException   if user provides an invalid username and password
     *                          combination.
     */
    public String login(String username, String password, int lifetime) throws SessionException;
    
    /**
     * Returns a sessionId (authenitcation token) to a user after verification
     * of correct admin username and password combination.  If user does not provide
     * the correct login credentials a {@link SessionException} will be thrown.
     * This login method allows an admin user, on behalf of another user, to
     * perform their operations.
     * 
     * @param adminUsername admin username of user
     * @param AdminPassword admin password of user
     * @param runAsUser user the admin wants the operations to be performed as
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide username and
     *                   password each time.
     * @throws SessionException   if user provides an invalid admin username and password
     *                          combination.
     */
    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException;
    
    /**
     * Returns a sessionId (authenitcation token) to a user after verification
     * of a string representation of a x509 proxy credential.  If user does not provide
     * the correct login credentials a {@link SessionException} will be thrown, i.e
     * the cerfificate is invalid, not trusted, expired etc.
     * 
     * @param credential of user, string format
     * @return sessionId authentication token that user can use in corresponding
     *                   methods calls without having to provide a credential
     *                   each time.
     * @throws SessionException   if user provides an invalid credentials.
     */
    public String login(String credential) throws SessionException;
    
    /**
     * Removes sessionId authentication token from user database which
     * effectively logs user out of the system.  Any further attempt to
     * use the icat3 api with an 'old' sessionId will fail as it will
     * no longer exist in the user database.  Any potential exceptions
     * that could be raised should be suppressed e.g. logging out a user
     * who is already logged out.
     *
     * @param sessionId authentication token obtained on successful login
     * @return boolean if logged out
     */
    public boolean logout(String sessionId);
    
    /**
     * Returns a serializable object that contains personal details of a requested
     * <code>user</code> as defined in {@link UserDetails}.  A {@link SessionException}
     * should be thrown if an invalid sessionId is supplied and a
     * {@link NoSuchUserException} should be thrown if the requested <code>user</code>
     * cannot be found in the database.  If both <code>sessionId</code> and
     * <code>user</code> are invalid then a {@link SessionException} should be thrown first.
     * 
     * @param sessionId sessionId of user
     * @param user users name
     * @return UserDetails {@Link UserDetails}    
     * @throws SessionException   if user provides an invalid <code>sessionId</code>
     * @throws NoSuchUserException   if <code>user</code> cannot be found in user database
     */
    public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException;
}