/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.userdefault.message;

/**
 *
 * @author scb24683
 */

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.apache.log4j.Logger;

public class LoginLdap {

    static Logger log;

    public static boolean ldapAuthenticate(String username, String password) {
        log = Logger.getLogger(LoginLdap.class);
        log.info("In ldapAuthenticate");

        Hashtable authEnv = new Hashtable();
        String ldapUrl = "ldap://logon05.fed.cclrc.ac.uk";
        String samAccount = username + "@fed.cclrc.ac.uk";
        boolean success = false;

        authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        authEnv.put(Context.PROVIDER_URL, ldapUrl);
        authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        authEnv.put(Context.SECURITY_PRINCIPAL, samAccount);
        authEnv.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext authCtx = new InitialDirContext(authEnv);
            log.info("Authentication successful");
            success = true;
        } catch (AuthenticationException authEx) {
            log.fatal("Authentication exception thrown", authEx);
            success = false;
        } catch (NamingException nameEx) {
            log.fatal("Naming exception thrown", nameEx);
            success = false;
        }
        return success;
    }


}
