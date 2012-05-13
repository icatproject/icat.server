package org.icatproject.userldap.facility;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

public class LdapAuthenticator {

	final private static Logger logger = Logger.getLogger(LdapAuthenticator.class);
	private String providerUrl;
	private String securityPrincipal;

	public LdapAuthenticator(String providerUrl, String securityPrincipal) {
		this.providerUrl = providerUrl;
		this.securityPrincipal = securityPrincipal;
	}

	public boolean authenticate(String username, String password) {
		logger.info("In ldapAuthenticate");

		Hashtable<Object, Object> authEnv = new Hashtable<Object, Object>();
		authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		authEnv.put(Context.PROVIDER_URL, providerUrl);
		authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		authEnv.put(Context.SECURITY_PRINCIPAL, securityPrincipal.replace("%", username));
		authEnv.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(authEnv);
			logger.info("Authentication successful");
			return true;
		} catch (AuthenticationException authEx) {
			logger.fatal("Authentication exception thrown:" + authEx.getMessage());
			return false;
		} catch (NamingException nameEx) {
			logger.fatal("Naming exception thrown" + nameEx.getMessage());
			return false;
		}
	}

}
