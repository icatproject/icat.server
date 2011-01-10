/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.user;

import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;

/**
 * Test database for User
 * @author Mr. Srikanth Nagella
 */
public class UserDB implements User {

    private String validUsername = "test";
    private String validPassword = "password";
    private String validSessionId = "session";
    private String adminUsername = "admin";
    private String adminPassword = "adminpassword";
    private String validCredential = "credential";
    private UserDetails validUserDetails;

    public UserDB() {
        validUserDetails = new UserDetails();
        validUserDetails.setCredential(validCredential);
        validUserDetails.setDepartment("ESC");
        validUserDetails.setEmail("sri@gmail.com");
        validUserDetails.setFederalId(validUsername);
        validUserDetails.setFirstName("testfirst");
        validUserDetails.setLastName("testlast");
        validUserDetails.setInitial("T");
        validUserDetails.setInstitution("ICAT");
        validUserDetails.setTitle("Mr.");
    }

    @Override
    public String getUserIdFromSessionId(String sessionId) throws SessionException {
        if (validSessionId.compareToIgnoreCase(sessionId) == 0) {
            return validUsername;
        }
        throw new SessionException("Invalid User");
    }

    @Override
    public String login(String username, String password) throws SessionException {
        if (validUsername.compareTo(username) == 0 && validPassword.compareTo(password) == 0) {
            return validSessionId;
        }
        throw new SessionException("Invalid username or password");
    }

    @Override
    public String login(String username, String password, int lifetime) throws SessionException {
        if (validUsername.compareTo(username) == 0 && validPassword.compareTo(password) == 0) {
            return validSessionId;
        }
        throw new SessionException("Invalid username or password");
    }

    @Override
    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException {
        if (adminUsername.compareTo(adminUsername) == 0 && adminPassword.compareTo(AdminPassword) == 0) {
            return validSessionId;
        }
        throw new SessionException("Invalid admin username or password");
    }

    @Override
    public String login(String credential) throws SessionException {
        if (validCredential.compareTo(credential) == 0) {
            return validSessionId;
        }
        throw new SessionException("Invalid credential");
    }

    @Override
    public boolean logout(String sessionId) {
        if (validSessionId.compareTo(sessionId) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException {
        if (validSessionId.compareTo(sessionId) != 0) {
            throw new SessionException("Invalid session Id");
        }
        if (validUsername.compareTo(user) != 0) {
            throw new NoSuchUserException("Invalid user");
        }
        return validUserDetails;

    }
}
