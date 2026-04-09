package org.icatproject.core.manager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Session;
import org.icatproject.core.manager.PropertyHandler.CallType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class SessionManager {

	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

	@EJB
	Transmitter transmitter;

	@Resource
	UserTransaction userTransaction;

	// This EntityManager is for a different persistence context from the rest of icat.server that only contains the
	// Session entity.
	@PersistenceContext(unitName = "session")
	EntityManager sessionEntityManager;

	@EJB
	PropertyHandler propertyHandler;

	private boolean log;
	private Set<CallType> logRequests;
	private int lifetimeMinutes;

	@PostConstruct
	void init() {
		logRequests = propertyHandler.getLogSet();
		log = !logRequests.isEmpty();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
	}

	// Run every hour
	@Schedule(hour = "*")
	public void removeExpiredSessions() {
		try {
			userTransaction.begin();
			int n = sessionEntityManager.createNamedQuery(Session.DELETE_EXPIRED).executeUpdate();
			userTransaction.commit();
			logger.debug("{} sessions were removed", n);
		} catch (Exception e) {
			logger.error("Error removing expired sessions", e);
		}
	}

	public double getRemainingMinutes(String sessionId) throws IcatException {
		logger.debug("getRemainingMinutes for sessionId {}", sessionId);
		Session session = getSession(sessionId);
		return session.getRemainingMinutes();
	}

	private Session getSession(String sessionId) throws IcatException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatExceptionType.SESSION, "Session Id cannot be null or empty.");
		}
		session = (Session) sessionEntityManager.find(Session.class, sessionId);
		if (session == null) {
			throw new IcatException(IcatExceptionType.SESSION, "Unable to find user by sessionid: " + sessionId);
		}
		return session;
	}

	public String getUserName(String sessionId) throws IcatException {
		try {
			Session session = getSession(sessionId);
			String userName = session.getUserName();
			logger.debug("user: {} is associated with: {}", userName, sessionId);
			return userName;
		} catch (IcatException e) {
			logger.debug("sessionId {} is not associated with valid session ", sessionId, e);
			throw e;
		}
	}

	public boolean isLoggedIn(String userName) {
		logger.debug("isLoggedIn for user {}", userName);
		return sessionEntityManager.createNamedQuery(Session.ISLOGGEDIN, Long.class).setParameter("userName", userName).getSingleResult() > 0;
	}

	public String login(String userName, String ip) throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		Session session = new Session(userName, lifetimeMinutes);

		try {
			userTransaction.begin();
			try {
				sessionEntityManager.persist(session);
				sessionEntityManager.flush();
				userTransaction.commit();
			} catch (PersistenceException e) {
				logger.error("Database error", e);
				throw new IcatException(IcatExceptionType.INTERNAL, "Database error: " + e.getMessage());
			} finally {
				if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
					userTransaction.rollback();
				}
			}
		} catch (HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException e) {
			logger.error("Transaction error", e);
			throw new IcatException(IcatExceptionType.INTERNAL, "Transaction error: " + e.getMessage());
		}

		String result = session.getId();
		logger.debug("Session {} persisted.", result);
		if (logRequests.contains(CallType.SESSION)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				gen.writeEnd();
			}
			transmitter.processMessage("login", ip, baos.toString(), startMillis);
		}

		return result;
	}

	public void logout(String sessionId, String ip) throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.debug("logout for sessionId {}", sessionId);

		String userName;
		try {
			userTransaction.begin();
			try {
				Session session = getSession(sessionId);
				userName = session.getUserName(true);
				sessionEntityManager.remove(session);
				sessionEntityManager.flush();
				userTransaction.commit();
			} catch (PersistenceException e) {
				logger.error("Database error", e);
				throw new IcatException(IcatExceptionType.INTERNAL, "Database error: " + e.getMessage());
			} finally {
				if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
					userTransaction.rollback();
				}
			}
		} catch (HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException e) {
			logger.error("Transaction error", e);
			throw new IcatException(IcatExceptionType.INTERNAL, "Transaction error: " + e.getMessage());
		}

		logger.debug("Session {} removed.", sessionId);
		if (logRequests.contains(CallType.SESSION)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				gen.writeEnd();
			}
			transmitter.processMessage("logout", ip, baos.toString(), startMillis);
		}
	}

	public void refresh(String sessionId, String ip) throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.debug("logout for sessionId {}", sessionId);

		String userName;
		try {
			userTransaction.begin();
			try {
				Session session = getSession(sessionId);
				userName = session.getUserName(true);
				session.refresh(lifetimeMinutes);
				sessionEntityManager.flush();
				userTransaction.commit();
			} catch (PersistenceException e) {
				logger.error("Database error", e);
				throw new IcatException(IcatExceptionType.INTERNAL, "Database error: " + e.getMessage());
			} finally {
				if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
					userTransaction.rollback();
				}
			}
		} catch (HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException e) {
			logger.error("Transaction error", e);
			throw new IcatException(IcatExceptionType.INTERNAL, "Transaction error: " + e.getMessage());
		}

		logger.debug("Session {} refreshed.", sessionId);
		if (logRequests.contains(CallType.SESSION)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				gen.writeEnd();
			}
			transmitter.processMessage("refresh", ip, baos.toString(), startMillis);
		}
	}
}
