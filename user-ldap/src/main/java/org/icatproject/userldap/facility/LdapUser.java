package org.icatproject.userldap.facility;

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
import org.icatproject.core.IcatException;
import org.icatproject.core.user.AddressChecker;

import org.icatproject.userldap.entity.LdapSession;
import org.icatproject.userldap.entity.LdapUserE;

public class LdapUser implements org.icatproject.core.user.User {

	private final EntityManager manager;
	private LdapAuthenticator ldapAuthenticator;
	private static final Logger log = Logger.getLogger(LdapUser.class);
	private AddressChecker ldapAddressChecker;
	private AddressChecker anstoAddressChecker;
	private IcatException icatException;

	public LdapUser(EntityManager manager) throws IcatException {
		File f = new File("icat.properties");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(f));
			String providerUrl = props.getProperty("auth.ldap.provider_url");
			if (providerUrl == null) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"auth.ldap.provider_url not defined");
			}
			String securityPrincipal = props.getProperty("auth.ldap.security_principal");
			if (securityPrincipal == null) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"auth.ldap.security_principal not defined");
			}
			if (securityPrincipal.indexOf('%') < 0) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"auth.ldap.security_principal value must include a % to be substituted by the user name");
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
			icatException = new IcatException(IcatException.IcatExceptionType.INTERNAL, msg);
			throw icatException;
		}
		this.manager = manager;
		log.trace("Created AnstoUser with Entitity Manager" + manager);
	}

	@Override
	public String getUserName(String sessionId) throws IcatException {
		log.trace("getUserName(" + sessionId + ")");
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"LdapSession Id cannot be null or empty.");
		}

		try {
			final LdapSession session = (LdapSession) this.manager
					.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			session.checkValid();
			log.debug("user: " + session.getRunAs() + " is associated with: " + sessionId);
			return session.getRunAs();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Invalid sessionid: "
					+ sessionId);
		} catch (IcatException e) {
			throw e;
		} catch (final Exception e) {
			log.warn(e.getMessage());
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Unable to find user by sessionid: " + sessionId);
		}
	}

	@Override
	public String login(String username, String password, HttpServletRequest req)
			throws IcatException {
		log.trace("login:" + username);
		if (icatException != null) {
			throw icatException;
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
			LdapUserE user = (LdapUserE) this.manager.createNamedQuery("LdapUserE.findByUserId")
					.setParameter("userId", username).getSingleResult();
			if (!user.getPassword().equals(password)) {
				throw new IcatException(IcatException.IcatExceptionType.SESSION,
						"Username and password do not match");
			}
			if (anstoAddressChecker != null) {
				if (!anstoAddressChecker.check(req.getRemoteAddr())) {
					throw new IcatException(IcatException.IcatExceptionType.SESSION,
							"You may not log in by 'ansto' from your IP address "
									+ req.getRemoteAddr());
				}
			}
		} catch (final NoResultException e) {
			if (!ldapAuthenticator.authenticate(username, password)) {
				throw new IcatException(IcatException.IcatExceptionType.SESSION,
						"Username and password do not match");
			}
			if (ldapAddressChecker != null) {
				if (!ldapAddressChecker.check(req.getRemoteAddr())) {
					throw new IcatException(IcatException.IcatExceptionType.SESSION,
							"You may not log in by 'ldap' from your IP address "
									+ req.getRemoteAddr());
				}
			}
		} catch (IcatException e) {
			log.trace(e.getMessage());
			throw e;
		} catch (final Exception e) {
			log.trace("Unexpected problem " + e.getMessage());
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Unexpected problem "
					+ e.getMessage());
		}

		final LdapSession session = newSession(username, 2);
		this.manager.persist(session);
		String sid = session.getUserSessionId();
		log.info("Logged in for user: " + username + " with sessionid:" + sid);

		final Timestamp loginTime = new Timestamp(new Date().getTime());
		log.info("About to send login message");
		LoginInterceptor.sendLoginMessage(sid, username, loginTime);

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
	public void logout(String sessionId) throws IcatException {
		log.trace("logout(" + sessionId + ")");
		try {
			final LdapSession session = (LdapSession) this.manager
					.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			this.manager.remove(session);
			session.checkValid();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:"
					+ sessionId + " has expired");

		}
	}

	@Override
	public double getRemainingMinutes(String sessionId) throws IcatException {
		log.trace("getRemainingMinutes");
		try {
			final LdapSession session = (LdapSession) this.manager
					.createNamedQuery("LdapSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			return session.getRemainingTimeMinutes();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:"
					+ sessionId + " has expired");
		}
	}

}
