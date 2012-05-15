/*
 * UserSessionBean.java
 *
 * Created on 20 March 2007, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.user;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.jws.WebMethod;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.user.UserDetails;
import org.icatproject.core.user.UserManager;

import uk.icat3.sessionbeans.EJBObject;

@Stateless(mappedName = "UserSession")
@PermitAll
// requires new transaction for each method call
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class UserSessionBean extends EJBObject implements UserSessionLocal, UserSession {

	public static final String ADMIN = "root";

	private static final Logger log = Logger.getLogger(UserSessionBean.class);

	@PersistenceContext(unitName = "icatuser")
	private EntityManager managerUser;

	private String authClassName;

	@SuppressWarnings("unused")
	@PostConstruct
	private void getAuthClassName() {
		File f = new File("icat.properties");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(f));
			authClassName = props.getProperty("auth.classname");
			log.info("Setting auth.classname as: " + authClassName);
		} catch (Exception e) {
			log.fatal("Problem with " + f.getAbsolutePath() + "  " + e.getMessage());
		}
	}

	@WebMethod()
	@ExcludeClassInterceptors
	public String login(String username, String password, HttpServletRequest req) throws IcatException {
		log.trace("login(" + username + ", *******)");
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.login(username, password, req);
	}

	@WebMethod(operationName = "loginLifetime")
	@ExcludeClassInterceptors
	@RequestWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginLifetime")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginLifetimeResponse")
	public String login(String username, String password, int lifetime, HttpServletRequest req) throws IcatException {
		log.trace("login(" + username + ", *******, " + lifetime + ")");
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.login(username, password, lifetime, req);
	}

	@WebMethod(operationName = "loginCredentials")
	@RequestWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginCredentials")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginCredentialsResponse")
	public String login(String credential) throws IcatException {

		UserManager userManager = new UserManager(authClassName, managerUser);

		return userManager.login(credential);
	}

	@WebMethod(operationName = "loginAdmin")
	@ExcludeClassInterceptors
	@RequestWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginAdmin")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.user.jaxws.loginAdminResponse")
	public String login(String username, String password, String runAs) throws IcatException {
		log.trace("login(" + username + ", *******, " + runAs + ")");
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.login(username, password, runAs);
	}

	@WebMethod()
	public boolean logout(String sid) throws IcatException {
		UserManager userManager;
		try {
			userManager = new UserManager(authClassName, managerUser);
		} catch (IcatException e) {
			if (e.getType() == IcatException.Type.SESSION) {
				return false;
			} else {
				throw e;
			}
		}
		return userManager.logout(sid);
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getUserIdFromSessionId(String sid) throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.getUserIdFromSessionId(sid);
	}

	@WebMethod()
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public UserDetails getUserDetails(String sid, String user) throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.getUserDetails(sid, user);
	}

	public boolean isSessionValid(String sessionId) {
		try {
			this.getUserIdFromSessionId(sessionId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String loginAdmin(String sessionId, String runAs) throws IcatException {
		if (!ADMIN.equals(getUserIdFromSessionId(sessionId))) {
			throw new IcatException(IcatException.Type.SESSION, "You must be logged in as " + ADMIN + " to do this");
		}
		return login(ADMIN, null, runAs);
	}
}
