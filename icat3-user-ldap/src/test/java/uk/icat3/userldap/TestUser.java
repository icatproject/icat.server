/*
 * TestUser.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.userldap;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.entity.User;

/**
 * 
 * @author gjd37
 */
public class TestUser {

	protected static Logger log = Logger.getLogger(TestUser.class);

	// TODO code application logic here
	static EntityManagerFactory emf = null;
	// Create new EntityManager
	static EntityManager em = null;

	/**
	 * Creates a new instance of TestUser
	 */
	public TestUser() {
	}

	protected static void setUp() {

		emf = Persistence.createEntityManagerFactory("icat3-defaultunit-testing-PU");
		em = emf.createEntityManager();

		// Begin transaction
		em.getTransaction().begin();
	}

	protected static void tearDown() {
		// Commit the transaction
		em.getTransaction().commit();

		em.close();
	}

	public void login(String username, String password, int lifetime) throws SessionException {

		setUp();
		long time = System.currentTimeMillis();
		UserManager userManager = new UserManager("uk.icat3.userldap.facility.LdapUser", em);
		userManager.login(username, password, lifetime);
		System.out.println((System.currentTimeMillis() - time) / 1000f);

		tearDown();
	}

	public void loginAdmin(String username, String password, String runAs) throws SessionException {

		setUp();
		long time = System.currentTimeMillis();
		UserManager userManager = new UserManager("uk.icat3.userldap.facility.LdapUser", em);

		userManager.login(username, password, runAs);
		System.out.println((System.currentTimeMillis() - time) / 1000f);

		tearDown();
	}

	public void logout(String sessionId) throws SessionException {

		setUp();
		long time = System.currentTimeMillis();
		UserManager userManager = new UserManager("uk.icat3.userldap.facility.LdapUser", em);

		userManager.logout(sessionId);
		System.out.println((System.currentTimeMillis() - time) / 1000f);

		tearDown();
	}

	public void getUserId(String sessionId) throws SessionException {

		setUp();
		long time = System.currentTimeMillis();
		UserManager userManager = new UserManager("uk.icat3.userldap.facility.LdapUser", em);

		String userId = userManager.getUserIdFromSessionId(sessionId);
		System.out.println(userId);
		System.out.println((System.currentTimeMillis() - time) / 1000f);

		tearDown();
	}

	public static void main(String[] args) throws SessionException {
		String username = "";
		String password = "";
		TestUser tu = new TestUser();
		if (username.compareTo("") == 0 || password.compareTo("") == 0) {
			System.out.println("Please change the source code TestUser.java to add username and password");
			return;
		}
		for (int i = 0; i < 1; i++) {
			tu.login("username", "password", 2);
		}

	}
}
