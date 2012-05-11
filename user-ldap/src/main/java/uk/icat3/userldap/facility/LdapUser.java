package uk.icat3.userldap.facility;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.AddressChecker;
import uk.icat3.user.UserDetails;
import uk.icat3.userldap.entity.LdapSession;
import uk.icat3.userldap.entity.LdapUserE;

public class LdapUser implements uk.icat3.user.User {

	private final EntityManager manager;
	private LdapAuthenticator ldapAuthenticator;
	private static final Logger log = Logger.getLogger(LdapUser.class);
	private AddressChecker ldapAddressChecker;
	private AddressChecker anstoAddressChecker;
	private IcatInternalException icatInternalException;

	public LdapUser(EntityManager manager) throws IcatInternalException {
		File f = new File("icat.properties");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(f));
			String providerUrl = props.getProperty("auth.ldap.provider_url");
			if (providerUrl == null) {
				throw new IcatInternalException("auth.ldap.provider_url not defined");
			}
			String securityPrincipal = props.getProperty("auth.ldap.security_principal");
			if (securityPrincipal == null) {
				throw new IcatInternalException("auth.ldap.security_principal not defined");
			}
			if (securityPrincipal.indexOf('%') < 0) {
				throw new IcatInternalException("auth.ldap.security_principal value must include a % to be substituted by the user name");
			}
			ldapAuthenticator = new LdapAuthenticator(providerUrl, securityPrincipal);
			String authips = props.getProperty("auth.ldap.ip");
			if (authips != null) {
				ldapAddressChecker = new AddressChecker(authips);
			}
			 authips = props.getProperty("auth.ansto.ip");
			if (authips != null) {
				anstoAddressChecker = new AddressChecker(authips);
			}
		} catch (Exception e) {
			String msg = "Problem with " + f.getAbsolutePath() + "  " + e.getMessage();
			log.fatal(msg);
			icatInternalException = new IcatInternalException(msg);
			throw icatInternalException;
		}
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
	public String login(String username, String password, HttpServletRequest req) throws SessionException,
			IcatInternalException {
		return this.login(username, password, 2, req); // 2 hours
	}

	@Override
	public String login(String username, String password, int lifetime, HttpServletRequest req) throws SessionException, IcatInternalException {
		log.trace("login(" + username + ", *********, " + lifetime + ")");
		if (icatInternalException != null) {
			throw icatInternalException;
		}
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
			if (anstoAddressChecker != null) {
				if (!anstoAddressChecker.check(req.getRemoteAddr())) {
					throw new SessionException("You may not log in by 'ansto' from your IP address " + req.getRemoteAddr());
				}
			}
		} catch (final NoResultException e) {
			if (!ldapAuthenticator.authenticate(username, password)) {
				throw new SessionException("Username and password do not match");
			}
			if (ldapAddressChecker != null) {
				if (!ldapAddressChecker.check(req.getRemoteAddr())) {
					throw new SessionException("You may not log in by 'ldap' from your IP address " + req.getRemoteAddr());
				}
			}
		} catch (SessionException e) {
			log.trace(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			log.trace(e.getMessage());
			throw e;
		} catch (final Exception e) {
			log.trace("Unexpected problem " + e.getMessage());
			throw new SessionException("Unexpected problem " + e.getMessage());
		}

		final LdapSession session = newSession(username, lifetime);
		this.manager.persist(session);
		String sid = session.getUserSessionId();
		log.info("Logged in for user: " + username + " with sessionid:" + sid);

		final Timestamp loginTime = new Timestamp(new Date().getTime());
		log.info("About to send login message");
		LoginInterceptor.sendLoginMessage(sid, username, loginTime);

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
