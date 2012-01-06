package uk.icat3.user;

import java.lang.reflect.Constructor;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;

public class UserManager implements User {

	static Logger log = Logger.getLogger(UserManager.class);

	private User user;

	/** Creates a new instance of UserManager with entity manager */
	public UserManager(String className, EntityManager manager) throws SessionException {
		user = (uk.icat3.user.User) createObject(className, manager);
	}

	public String getUserIdFromSessionId(String sessionId) throws SessionException {
		return user.getUserIdFromSessionId(sessionId);
	}

	public String login(String username, String password, HttpServletRequest req) throws SessionException, IcatInternalException {
		return user.login(username, password, req);
	}

	public String login(String username, String password, int lifetime, HttpServletRequest req) throws SessionException, IcatInternalException {
		return user.login(username, password, lifetime, req);
	}

	public boolean logout(String sessionId) {
		return user.logout(sessionId);
	}

	public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException {
		return this.user.getUserDetails(sessionId, user);
	}

	private static Object createObject(String className, EntityManager manager) {
		Constructor<?> entityManagerConstructor = null;
		Object object = null;

		try {
			Class<?>[] entityManagerArgaClass = new Class[] { EntityManager.class };
			Object[] inArgs = new Object[] { manager };

			Class<?> classDefinition = Class.forName(className);

			entityManagerConstructor = classDefinition.getConstructor(entityManagerArgaClass);

			log.trace("Constructor: " + entityManagerConstructor.toString());

			object = entityManagerConstructor.newInstance(inArgs);
			log.trace("Object: " + object.toString());

		} catch (Exception e) {
			log.error(e);
		}
		return object;
	}

	public String login(String adminUsername, String adminPassword, String runAsUser) throws SessionException {
		return this.user.login(adminUsername, adminPassword, runAsUser);
	}

	public String login(String credential) throws SessionException {
		return this.user.login(credential);
	}

}
