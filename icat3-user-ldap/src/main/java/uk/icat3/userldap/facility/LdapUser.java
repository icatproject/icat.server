package uk.icat3.userldap.facility;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.UserDetails;
import uk.icat3.userldap.entity.LdapSession;
import uk.icat3.userldap.entity.LdapUserE;
import uk.icat3.userldap.message.LoginLdap;

public class LdapUser implements uk.icat3.user.User {

	private final EntityManager manager;
	private static final Logger log = Logger.getLogger(LdapUser.class);

	public LdapUser(EntityManager manager) {
		this.manager = manager;
		log.trace("Created AnstoUser with Entitity Manager" + manager);
	}

	@Override
	public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException {
		log.trace("getUserDetails(" + sessionId + ")");
		if (sessionId == null || sessionId.equals("")) {
			throw new SessionException("LdapSession Id cannot be null or empty.");
		}

		try {
			final LdapSession session = (LdapSession) this.manager.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			session.checkValid();
			// TODO do we want to get rid of the getUserDetails call?
			UserDetails userDetails = new UserDetails();
			userDetails.setFederalId(session.getRunAs());
			return userDetails;
		} catch (final NoResultException e) {
			throw new SessionException("Invalid sessionid: " + sessionId);
		} catch (final SessionException e) {
			throw e;
		} catch (final Exception e) {
			log.warn(e.getMessage());
			throw new SessionException("Unable to find user by sessionid: " + sessionId);
		}
	}

	@Override
	public String getUserIdFromSessionId(String sessionId) throws SessionException {
		log.trace("getUserIdFromSessionId(" + sessionId + ")");
		if (sessionId == null || sessionId.equals("")) {
			throw new SessionException("LdapSession Id cannot be null or empty.");
		}

		try {
			final LdapSession session = (LdapSession) this.manager.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			session.checkValid();
			log.debug("user: " + session.getRunAs() + " is associated with: " + sessionId);
			return session.getRunAs();
		} catch (final NoResultException e) {
			throw new SessionException("Invalid sessionid: " + sessionId);
		} catch (final SessionException e) {
			throw e;
		} catch (final Exception e) {
			log.warn(e.getMessage());
			throw new SessionException("Unable to find user by sessionid: " + sessionId);
		}
	}

	@Override
	public String login(String credential) throws SessionException {
		throw new UnsupportedOperationException("Method not supported.");
	}

	@Override
	public String login(String username, String password) throws SessionException {
		return this.login(username, password, 2); // 2 hours
	}

	@Override
	public String login(String username, String password, int lifetime) throws SessionException {
		log.trace("login(" + username + ", *********, " + lifetime + ")");
		if (username == null || username.equals("")) {
			throw new IllegalArgumentException("Username cannot be null or empty.");
		}
		if (password == null || password.equals("")) {
			throw new IllegalArgumentException("Password cannot be null or empty.");
		}
		log.trace("Entitity Manager is " + manager);
		log.info("Checking password against database");
		try {
			LdapUserE user = (uk.icat3.userldap.entity.LdapUserE) this.manager
					.createNamedQuery("LdapUserE.findByUserId").setParameter("userId", username).getSingleResult();
			if (!user.getPassword().equals(password)) {
				throw new SessionException("Username and password do not match");
			}
		} catch (final NoResultException e) {
			if (!LoginLdap.ldapAuthenticate(username, password)) {
				throw new SessionException("Username and password do not match");
			}
		} catch (final Exception e) {
			log.trace("Unexpected problem " + e.getMessage());
			throw new SessionException("Unexpected problem " + e.getMessage());
		}

		final LdapSession session = newSession(username, lifetime);
		this.manager.persist(session);
		String sid = session.getUserSessionId();
		log.info("Logged in for user: " + username + " with sessionid:" + sid);

		// final Timestamp loginTime = new Timestamp(new Date().getTime());
		// log.info("About to send login message");
		// LoginInterceptor.sendLoginMessage(sid, username, loginTime);

		return sid;
	}

	@Override
	public String login(String adminUsername, String dummy, String runAsUser) {
		log.trace("login(admin, *********, " + runAsUser + ")");

		final LdapSession session = newSession(runAsUser, 2);
		this.manager.persist(session);
		String sid = session.getUserSessionId();
		log.info("Logged in for user: " + runAsUser + " running as  " + runAsUser + " with sessionid:" + sid);

		return sid;

	}

	private LdapSession newSession(String effectiveUser, int lifetime) {
		final String sid = UUID.randomUUID().toString();

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, lifetime);
		final LdapSession session = new LdapSession(sid, effectiveUser, cal.getTime());

		return session;
	}

	@Override
	public boolean logout(String sessionId) {
		log.trace("logout(" + sessionId + ")");
		try {
			final LdapSession session = (LdapSession) this.manager.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			this.manager.remove(session);
			return true;
		} catch (final NoResultException e) {
			log.warn(sessionId + " not in DB");
			return false;
		}
	}

}
