package org.icatproject.core.manager;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.icatproject.core.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SessionManager {

	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

	@PersistenceContext(unitName = "icat")
	private EntityManager entityManager;

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
}