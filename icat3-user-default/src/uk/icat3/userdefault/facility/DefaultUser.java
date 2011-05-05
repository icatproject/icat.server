/*
 * DefaultUser.java
 *
 * Created on 20 March 2007, 09:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.userdefault.facility;

import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.user.User;
import uk.icat3.user.UserDetails;
import uk.icat3.userdefault.cog.DelegateCredential;
import uk.icat3.userdefault.cog.PortalCredential;
import uk.icat3.userdefault.entity.*;
import uk.icat3.userdefault.exception.LoginError;
import uk.icat3.userdefault.message.LoginInterceptor;
import uk.icat3.util.IcatRoles;

/**
 * This class uses a local DB connection through an entitymanager with three tables for the session
 * information.  A user will present a username and password for a credential in a myproxy server
 * configured in a table.  The code will get the proxy and insert the information in the session
 * table with the associated user in the user table.  An admin user can log onto the system and
 * run commands on behalf of a user, the admin password needs to be set up in the user table first
 *
 * @author gjd37
 */
public class DefaultUser implements User {

    //entity manager for the session database.
    private EntityManager manager;
    // Global class logger
    static Logger log = Logger.getLogger(DefaultUser.class);

    /** Creates a new instance of DefaultUser */
    public DefaultUser(EntityManager manager) {
        this.manager = manager;
    }

    /** Creates a new instance of DefaultUser */
    public DefaultUser() {
    }

    public String getUserIdFromSessionId(String sessionId) throws SessionException {
        log.trace("getUserIdFromSessionId(" + sessionId + ")");
        if (sessionId == null || sessionId.equals("")) {
            throw new SessionException("Session Id cannot be null or empty.");
        }

        try {
            //find the user by session id, throws NoResultException if session not found
            Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", sessionId).getSingleResult();

            //is valid
            if (session.getExpireDateTime().before(new Date())) {
                throw new SessionException("Session " + sessionId + " has expired");
            }

            //check if session id is running as admin, if so, return runAs userId
            if (session.isAdmin()) {
                log.debug("user: " + session.getRunAs() + " is associated with: " + sessionId);
                return session.getRunAs();
            } else {
                log.debug("user: " + session.getUserId().getUserId() + " is associated with: " + sessionId);
                return session.getUserId().getUserId();
            }

        } catch (NoResultException ex) {
            throw new SessionException("Invalid sessionid: " + sessionId);
        } catch (SessionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn(ex.getMessage());
            throw new SessionException("Unable to find user by sessionid: " + sessionId);
        }
    }

    /**
     * Logs on with username and password with default session timeout of 2 hours
     *
     * @param username
     * @param password
     * @throws uk.icat3.exceptions.SessionException
     * @return session id
     */
    public String login(String username, String password) throws SessionException {
        return login(username, password, 2); //2 hours
    }

    /**
     *  Logs on with username, password and lifetime of session
     *
     * @param username
     * @param password
     * @param lifetime lifetime of session before been invalid
     * @throws uk.icat3.exceptions.SessionException
     * @return session id
     */
    public String login(String username, String password, int lifetime) throws SessionException {
        log.trace("login(" + username + ", *********, " + lifetime + ")");
        if (username == null || username.equals("")) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null || password.equals("")) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (IcatRoles.SUPER_USER.toString().equals(username) || IcatRoles.ADMIN_USER.toString().equals(username)) {
            //ICAT keyword IcatRoles.SUPER_USER, cannot be used to log in this way, 
            //try login(String adminUsername, String adminPassword, String runAsUser)
            throw new SessionException("Cannot login using username " + IcatRoles.SUPER_USER);
        }
        if (IcatRoles.ADMIN_USER.toString().equals(username)) {
            //ICAT keyword IcatRoles.SUPER_USER, cannot be used to log in this way, 
            //try login(String adminUsername, String adminPassword, String runAsUser)
            throw new SessionException("Cannot login using username " + IcatRoles.ADMIN_USER);
        }

        GSSCredential myproxy_proxy;
        try {
            //lookup proxy and contact MyProxy found for users credential
            ProxyServers proxyserver = (ProxyServers) manager.createNamedQuery("ProxyServers.findByActive").setParameter("active", true).getSingleResult();
            myproxy_proxy = DelegateCredential.getProxy(username, password, lifetime, PortalCredential.getPortalProxy(),
                    proxyserver.getProxyServerAddress(), proxyserver.getPortNumber(), proxyserver.getCaRootCertificate());

            //insert proxy into DB
            String sid = insertSessionImpl(username, myproxy_proxy);

            log.info("Logged in for user: " + username + " with sessionid: " + sid);
            return sid;

        } catch (NoResultException ex) {
            log.warn("No MyProxy Server found in database, information set up incorrectly or not set.", ex);
            //no proxy in database or no proxy with active setup
            throw new SessionException("MyProxy server information set up incorrectly.");
        } catch (MyProxyException mex) {
            log.warn("Error from myproxy server: " + mex.getMessage(), mex);
            throw new SessionException(handleMyProxyException(mex));
        } catch (SessionException lex) {
            log.warn("SessionException:", lex);
            throw lex;
        } catch (Exception e) {
            log.warn("Unexpected error from myproxy: " + e.getMessage(), e);
            throw new SessionException("Unexpected exception logging into system.");
        }

    }

    /**
     * Logout of system
     *
     * @param sessionId
     * @return boolean is correctly logged out
     */
    public boolean logout(String sessionId) {
        log.trace("logout(" + sessionId + ")");
        try {
            Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", sessionId).getSingleResult();
            manager.remove(session);
            return true;
        } catch (NoResultException ex) {
            log.warn(sessionId + " not in DB");
            return false;
        }
    }

    /**
     * To support all method in User interface, used to get credential from session DB
     * 
     *
     * @param sessionId
     * @param user
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchUserException
     * @return UserDetails
     */
    public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException {
        log.trace("getUserDetails(" + sessionId + ")");
        if (sessionId == null || sessionId.equals("")) {
            throw new SessionException("Session Id cannot be null or empty.");
        }

        try {
            //find the user by session id, throws NoResultException if session not found
            Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", sessionId).getSingleResult();

            //is valid
            if (session.getExpireDateTime().before(new Date())) {
                throw new SessionException("Session " + sessionId + " has expired");
            }

            UserDetails userDetails = new UserDetails();
            userDetails.setCredential(session.getCredential());

            return userDetails;

        } catch (NoResultException ex) {
            throw new SessionException("Invalid sessionid: " + sessionId);
        } catch (SessionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn(ex.getMessage());
            throw new SessionException("Unable to find user by sessionid: " + sessionId);
        }
    }

    /**
     *
     * @param adminUsername
     * @param adminPassword
     * @param runAsUser
     * @throws uk.icat3.exceptions.SessionException
     * @return
     */
    public String login(String adminUsername, String adminPassword, String runAsUser) throws SessionException {
        log.trace("login(admin, *********, " + runAsUser + ")");

        boolean isSuper = false;

        //find admin user first
        uk.icat3.userdefault.entity.User user = null;

        //check if trying to log on as super, if super in DB then this is enabled
        if (IcatRoles.SUPER_USER.toString().equals(runAsUser)) {
            try {
                user = (uk.icat3.userdefault.entity.User) manager.createNamedQuery("User.findByUserId").setParameter("userId", IcatRoles.SUPER_USER.toString()).getSingleResult();
                isSuper = true;
            } catch (NoResultException ex) {
                log.warn("Super user account not set up in DB");
                throw new SessionException("Super user account not set up");
            }
        } else {
            //check if trying to log on as admin, if admin in DB then this is enabled
            try {
                user = (uk.icat3.userdefault.entity.User) manager.createNamedQuery("User.findByUserId").setParameter("userId", IcatRoles.ADMIN_USER.toString()).getSingleResult();
            } catch (NoResultException ex) {
                log.warn("Admin user account not set up in DB");
                throw new SessionException("Admin user account not set up");
            }
        }

        //check that password is the same, should really have it
        //method protected by Glassfish basic authentication
        /*if(!adminPassword.equals(user.getPassword())){
        log.warn("Invalid admin password: "+adminPassword);
        } else  log.info("Admin password correct");*/

        //create session
        //create UUID for session
        String sid = UUID.randomUUID().toString();

        //create a session to put in DB
        Session session = new Session();
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(GregorianCalendar.HOUR, 2); //add 2 hours
        session.setExpireDateTime(cal.getTime());
        session.setUserSessionId(sid);
        session.setRunAs(runAsUser);
        if (isSuper) {
            session.setCredential("SUPER_CREDENTIAL");
        } else {
            session.setCredential("ADMIN_CREDENTIAL");
        }

        user.addSession(session);
        manager.persist(session);

        log.info("Logged in for user: " + runAsUser + " running as " + ((isSuper) ? "super" : "admin") + " with sessionid:" + sid);

        return sid;

    }

    /**
     * To support all method in User interface, throws Runtime UnsupportedOperationException as this method
     * is not support by the default implementation
     */
    public String login(String credential) throws SessionException {
        throw new UnsupportedOperationException("Method not supported.");
    }

    ///////////////////////////  Private Methods /////////////////////////////////////////////////////////////
    private String insertSessionImpl(String username, GSSCredential credential) throws SessionException {

        log.trace("Starting insertSessionImpl");

        Certificate certificate = null;
        String DN = null;
        boolean lifetimeLeft = false;

        uk.icat3.userdefault.entity.User user = null;

        try {
            certificate = new Certificate(credential);
            DN = certificate.getDn();
            lifetimeLeft = certificate.isLifetimeLeft();

            log.debug("Loaded credential, user " + DN);

        } catch (CertificateExpiredException ce) {
            log.warn("Certificate has expired.", ce);
            throw new SessionException(LoginError.CREDENTIALS_EXPIRED.toString(), ce);
        } catch (CertificateException ex) {
            log.warn("Unable to load certificate", ex);
            throw new SessionException(LoginError.UNKNOWN.toString(), ex);

        }

        if (lifetimeLeft) {
            //set up new session

            //create UUID for session
            String sid = UUID.randomUUID().toString();

            //create a session to put in DB
            Session session = new Session();

            session.setCredential(certificate.getStringRepresentation());

            //set expire time on session
            Calendar cal = GregorianCalendar.getInstance();
            try {
                cal.add(GregorianCalendar.SECOND, (int) certificate.getLifetime() - 60 * 5); //minus 5 mins
                log.trace("Lifetime left is: " + certificate.getLifetime() + " secs and " + certificate.getLifetime() / 3600);
                log.trace("Setting expire time as: " + cal.getTime());
            } catch (CertificateException ex) {
                log.warn("Unable to load certificate", ex);
                throw new SessionException(LoginError.UNKNOWN.toString(), ex);

            }
            session.setExpireDateTime(cal.getTime());
            session.setUserSessionId(sid);

            //get user
            try {
                log.trace("Getting user.");
                //need to get user corresponding to DN
                user = (uk.icat3.userdefault.entity.User) manager.createNamedQuery("User.findByDn").setParameter("dn", DN).getSingleResult();

                //user is owner so set it there
                user.addSession(session);

            } catch (Exception enfe) {
                //no entity found, so create one
                log.info("No user found, creating new user.");
                user = new uk.icat3.userdefault.entity.User();
                user.setDn(DN);
                user.setUserId(username);

                manager.persist(user);
                //add new user to session
                //user is owner so set it there
                user.addSession(session);
            //session.setUserId(user);
            }

            //save session
            log.trace("Persiting session.");
            manager.persist(session);
            Timestamp loginTime = new Timestamp(new Date().getTime());
            LoginInterceptor.sendLoginMessage(sid, username, loginTime);
            log.info("New session created for user: " + DN + " sid: " + sid);

            return sid;

        } else {
            log.warn("No session created. Proxy has expired for " + DN);
            throw new SessionException(LoginError.CREDENTIALS_EXPIRED.toString());
        }
    }

    private String handleMyProxyException(MyProxyException e) {
        String trace = e.getLocalizedMessage().trim();

        String errMsg = null;
        log.trace("MyProxy Trace is : '" + trace + "'");
        if (trace.compareToIgnoreCase("MyProxy get failed. [Root error message: invalid pass phrase]") == 0) {
            errMsg = "Invalid Passphrase Please Try Again";

        } else if (trace.compareToIgnoreCase("MyProxy get failed. [Root error message: Bad password invalid pass phrase]") == 0) {
            errMsg = "Invalid Passphrase Please Try Again";

        } else if (trace.compareToIgnoreCase("MyProxy get failed. [Root error message: requested credentials have expired]") == 0) {
            errMsg = "Credentials have expired on MyProxy server. Upload a new proxy and try again";

        } else if (trace.compareToIgnoreCase("MyProxy get failed. [Root error message: Credentials do not exist Unable to retrieve credential information]") == 0) {
            errMsg = "No credentials on MyProxy server. Upload a proxy and try again";

        } else if (trace.compareToIgnoreCase("MyProxy") == 0) {
            errMsg = "No credentials on MyProxy server. Upload a proxy and try again";

        } else if (trace.contains("Authentication failure invalid pass phrase") || trace.contains("Authentication failureinvalid pass phrase")) {
            errMsg = "Invalid Password. Please Try Again";
        } else {
            errMsg = "Unknown exception - " + trace;
        }
        return errMsg;
    }
}
