/*
 * UserSessionBean.java
 *
 * Created on 20 March 2007, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.icatproject.exposed.user;

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

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.user.UserManager;
import org.icatproject.exposed.EJBObject;

@Stateless(mappedName = "UserSession")
@PermitAll
// requires new transaction for each method call
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class UserSessionBean extends EJBObject implements UserSessionLocal {

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
	public String login(String username, String password, HttpServletRequest req)
			throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.login(username, password, req);
	}

	@WebMethod()
	public void logout(String sid) throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		userManager.logout(sid);
	}

	@WebMethod()
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getUserName(String sessionId) throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.getUserName(sessionId);
	}

	@Override
	@WebMethod()
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public double getRemainingMinutes(String sessionId) throws IcatException {
		UserManager userManager = new UserManager(authClassName, managerUser);
		return userManager.getRemainingMinutes(sessionId);
	}
}
