package org.icatproject.core.manager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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

	@PersistenceContext(unitName = "session")
	EntityManager entityManager;

	@EJB
	PropertyHandler propertyHandler;

	private boolean log;
	private Set<CallType> logRequests;

	@PostConstruct
	void init() {
		logRequests = propertyHandler.getLogSet();
		log = !logRequests.isEmpty();
	}

	// Run every hour
	@Schedule(hour = "*")
	public void removeExpiredSessions() {
		try {
			int n = entityManager.createNamedQuery(Session.DELETE_EXPIRED).executeUpdate();
			logger.debug(n + " sessions were removed");
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}

	public double getRemainingMinutes(String sessionId) throws IcatException {
		logger.debug("getRemainingMinutes for sessionId " + sessionId);
		Session session = getSession(sessionId);
		return session.getRemainingMinutes();
	}


	private Session getSession(String sessionId) throws IcatException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session Id cannot be null or empty.");
		}
		session = (Session) entityManager.find(Session.class, sessionId);
		if (session == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Unable to find user by sessionid: " + sessionId);
		}
		return session;
	}

	public String getUserName(String sessionId) throws IcatException {
		try {
			Session session = getSession(sessionId);
			String userName = session.getUserName();
			logger.debug("user: " + userName + " is associated with: " + sessionId);
			return userName;
		} catch (IcatException e) {
			logger.debug("sessionId " + sessionId + " is not associated with valid session " + e.getMessage());
			throw e;
		}
	}

	public boolean isLoggedIn(String userName) {
		logger.debug("isLoggedIn for user " + userName);
		return entityManager.createNamedQuery(Session.ISLOGGEDIN, Long.class).setParameter("userName", userName).getSingleResult() > 0;
	}

	public String login(String userName, int lifetimeMinutes, String ip) throws IcatException {
		Session session = new Session(userName, lifetimeMinutes);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				entityManager.persist(session);
				entityManager.flush();
				userTransaction.commit();
				String result = session.getId();
				logger.debug("Session " + result + " persisted.");
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userName);
						gen.writeEnd();
					}
					transmitter.processMessage("login", ip, baos.toString(), startMillis);
				}
				return result;
			} catch (Throwable e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for login because of " + e.getClass() + " " + e.getMessage());
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"IllegalStateException " + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException " + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException " + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"NotSupportedException " + e.getMessage());
		}
	}

	public void logout(String sessionId, String ip) throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId);
				entityManager.remove(session);
				entityManager.flush();
				userTransaction.commit();
				logger.debug("Session {} removed.", session.getId());
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", session.getUserName());
						gen.writeEnd();
					}
					transmitter.processMessage("logout", ip, baos.toString(), startMillis);
				}
			} catch (IcatException e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for logout because of " + e.getClass() + " " + e.getMessage());
				if (e.getType() == IcatExceptionType.SESSION) {
					throw e;
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage());
		} catch (RuntimeException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void refresh(String sessionId, int lifetimeMinutes, String ip) throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId);
				session.refresh(lifetimeMinutes);
				entityManager.flush();
				userTransaction.commit();
				logger.debug("Session {} refreshed.", session.getId());
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", session.getUserName());
						gen.writeEnd();
					}
					transmitter.processMessage("refresh", ip, baos.toString(), startMillis);
				}
			} catch (IcatException e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for logout because of " + e.getClass() + " " + e.getMessage());
				if (e.getType() == IcatExceptionType.SESSION) {
					throw e;
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage());
		}
	}
}
