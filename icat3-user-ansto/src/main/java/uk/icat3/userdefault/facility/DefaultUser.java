package uk.icat3.userdefault.facility;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.User;
import uk.icat3.user.UserDetails;
import uk.icat3.userdefault.entity.Session;
import uk.icat3.userdefault.message.LoginInterceptor;

/**
 * This class uses a local DB connection through an entitymanager with two tables for the session
 * information. A user will present a username and password for a credential stored in a table. The
 * code will get the proxy and insert the information in the session table with the associated user
 * in the user table. An admin user can log onto the system and run commands on behalf of a user,
 * the admin password needs to be set up in the user table first
 * 
 * @author gjd37
 */
public class DefaultUser implements User {

	// entity manager for the session database.
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
			// find the user by session id, throws NoResultException if session not found
			Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();

			// is valid
			if (session.getExpireDateTime().before(new Date())) {
				throw new SessionException("Session " + sessionId + " has expired");
			}

			log.debug("user: " + session.getRunAs() + " is associated with: " + sessionId);
			return session.getRunAs();

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
		return login(username, password, 2); // 2 hours
	}

	@Override
	public String login(String username, String password, int lifetime) throws SessionException {

		uk.icat3.userdefault.entity.User user = null;

		log.trace("login(" + username + ", *********, " + lifetime + ")");
		if (username == null || username.equals("")) {
			throw new IllegalArgumentException("Username cannot be null or empty.");
		}
		if (password == null || password.equals("")) {
			throw new IllegalArgumentException("Password cannot be null or empty.");
		}

		log.info("checking password against database ");

		try {
			user = (uk.icat3.userdefault.entity.User) manager.createNamedQuery("User.findByUserId")
					.setParameter("userId", username).getSingleResult();
		} catch (NoResultException e) {
			throw new SessionException("Username and password do not match");
		} catch (Exception e) {
			throw new SessionException("Unexpected problem " + e);
		}
		if (!user.getPassword().equals(password)) {
			throw new SessionException("Username and password do not match");
		}

		String sid = UUID.randomUUID().toString();

		// create a session to put in DB
		Session session = new Session();
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(GregorianCalendar.HOUR, lifetime);
		session.setExpireDateTime(cal.getTime());
		session.setUserSessionId(sid);
		session.setRunAs(username);
		session.setCredential("abc");

		user.addSession(session);
		manager.persist(session);
		Timestamp loginTime = new Timestamp(new Date().getTime());
		log.info("About to send login message");
		LoginInterceptor.sendLoginMessage(sid, username, loginTime);
		log.info("Logged in for user: " + username + " with sessionid:" + sid);

		return sid;
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
			Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
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
			// find the user by session id, throws NoResultException if session not found
			Session session = (Session) manager.createNamedQuery("Session.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();

			// is valid
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
	public String login(String adminUsername, String dummy, String runAsUser) throws SessionException {
		log.trace("login(admin, *********, " + runAsUser + ")");

		// find admin user first
		uk.icat3.userdefault.entity.User user = null;

		// check if trying to log on as admin, if admin in DB then this is enabled
		try {
			user = (uk.icat3.userdefault.entity.User) manager.createNamedQuery("User.findByUserId")
					.setParameter("userId", adminUsername).getSingleResult();
		} catch (NoResultException ex) {
			log.warn("Admin user '" + adminUsername + "' account not set up in DB");
			throw new SessionException("Admin user account not set up");
		}

		// create session
		// create UUID for session
		String sid = UUID.randomUUID().toString();

		// create a session to put in DB
		Session session = new Session();
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(GregorianCalendar.HOUR, 2); // add 2 hours
		session.setExpireDateTime(cal.getTime());
		session.setUserSessionId(sid);
		session.setRunAs(runAsUser);

		session.setCredential("ADMIN_CREDENTIAL");

		user.addSession(session);
		manager.persist(session);

		log.info("Logged in for user: " + runAsUser + " running as  " + runAsUser + " with sessionid:" + sid);

		return sid;

	}

	/**
	 * To support all method in User interface, throws Runtime UnsupportedOperationException as this
	 * method is not support by the default implementation
	 */
	public String login(String credential) throws SessionException {
		throw new UnsupportedOperationException("Method not supported.");
	}

}
