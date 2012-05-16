package org.icatproject.useransto.facility;

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
import org.icatproject.useransto.entity.Session;
import org.icatproject.useransto.entity.UserE;

public class AnstoUser implements org.icatproject.core.user.User {

	private final EntityManager manager;
	private static final Logger log = Logger.getLogger(AnstoUser.class);
	private AddressChecker anstoAddressChecker;
	private IcatException icatInternalException;

	public AnstoUser(EntityManager manager) {
		File f = new File("icat.properties");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(f));
			String authips = props.getProperty("auth.ansto.ip");
			if (authips != null) {
				anstoAddressChecker = new AddressChecker(authips);
			}
		} catch (Exception e) {
			icatInternalException = new IcatException(IcatException.IcatExceptionType.INTERNAL, "Problem with "
					+ f.getAbsolutePath() + "  " + e.getMessage());
			log.fatal("Problem with " + f.getAbsolutePath() + "  " + e.getMessage());
		}
		this.manager = manager;
		log.trace("Created AnstoUser with Entitity Manager" + manager);
	}

	@Override
	public String getUserName(String sessionId) throws IcatException {
		log.trace("getUserIdFromSessionId(" + sessionId + ")");
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session Id cannot be null or empty.");
		}

		try {
			final Session session = (Session) this.manager.createNamedQuery("AnstoSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			session.checkValid();
			log.debug("user: " + session.getRunAs() + " is associated with: " + sessionId);
			return session.getRunAs();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Invalid sessionid: " + sessionId);
		} catch (final IcatException e) {
			throw e;
		} catch (final Exception e) {
			log.warn(e.getMessage());
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Unable to find user by sessionid: " + sessionId);
		}
	}

	@Override
	public String login(String username, String password,  HttpServletRequest req)
			throws IcatException {
		UserE user = null;
		log.trace("login:" +  username);
		if (icatInternalException != null) {
			throw icatInternalException;
		}
		if (anstoAddressChecker != null) {
			if (!anstoAddressChecker.check(req.getRemoteAddr())) {
				throw new IcatException(IcatException.IcatExceptionType.SESSION, "You may not log in by 'ansto' from your IP address " + req.getRemoteAddr());
			}
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
			user = (UserE) this.manager.createNamedQuery("AnstoUser.findByUserId").setParameter("userId", username)
					.getSingleResult();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Username and password do not match");
		} catch (final Exception e) {
			log.trace("Unexpected problem " + e.getMessage());
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Unexpected problem " + e.getMessage());
		}
		if (!user.getPassword().equals(password)) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Username and password do not match");
		}

		final Session session = newSession(username, 2);
		this.manager.persist(session);
		String sid = session.getUserSessionId();
		log.info("Logged in for user: " + username + " with sessionid:" + sid);

		final Timestamp loginTime = new Timestamp(new Date().getTime());
		log.info("About to send login message");
		LoginInterceptor.sendLoginMessage(sid, username, loginTime);

		return sid;
	}

	private Session newSession(String effectiveUser, int lifetime) {
		final String sid = UUID.randomUUID().toString();

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, lifetime);
		final Session session = new Session(sid, effectiveUser, cal.getTime());

		return session;
	}

	@Override
	public void logout(String sessionId) throws IcatException {
		log.trace("logout(" + sessionId + ")");
		try {
			final Session session = (Session) this.manager.createNamedQuery("AnstoSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			this.manager.remove(session);
			session.checkValid();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:" + sessionId + " has expired");
		}
	}

	@Override
	public double getRemainingMinutes(String sessionId) throws IcatException {
		log.trace("getRemainingMinutes");
		try {
			final Session session = (Session) this.manager.createNamedQuery("AnstoSession.findByUserSessionId")
					.setParameter("userSessionId", sessionId).getSingleResult();
			return session.getRemainingTimeMinutes();
		} catch (final NoResultException e) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:" + sessionId + " has expired");
		}
	}

}
