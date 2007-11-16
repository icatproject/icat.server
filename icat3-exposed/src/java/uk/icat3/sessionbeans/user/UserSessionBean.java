/*
 * UserSessionBean.java
 *
 * Created on 20 March 2007, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.user;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserDetails;
import uk.icat3.user.UserManager;
import static uk.icat3.sessionbeans.util.Constants.*;
/**
 *
 * @author gjd37
 */
@Stateless(mappedName="UserSession")
@PermitAll
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
//requires new transaction for each method call
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class UserSessionBean extends EJBObject implements UserSessionLocal, UserSession {
    
    static Logger log = Logger.getLogger(UserSessionBean.class);
    
    @PersistenceContext(unitName="icat3-exposed-user")
    private EntityManager managerUser;
    
    /**
     * Logs in with a username and password and returns a session id
     *
     * @param username
     * @param password
     * @throws uk.icat3.exceptions.SessionException if username and pasword are invalid
     * @return session id
     */
    @WebMethod()
    @ExcludeClassInterceptors
    public String login(String username, String password) throws SessionException {
        log.trace("login("+username+", *******)");
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username,password);
    }
    
    /**
     * Logs in with a username and password and returns a session id
     *
     * @param username
     * @param password
     * @param lifetime lifetime of the session
     * @throws uk.icat3.exceptions.SessionException if username and pasword are invalid
     * @return session id
     */
    @WebMethod(operationName="loginLifetime")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginLifetime")
    @ResponseWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginLifetimeResponse")
    public String login(String username, String password, int lifetime) throws SessionException {
        log.trace("login("+username+", *******, "+lifetime+")");
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username,password, lifetime);
    }
    
    /**
     * Logs in with a credential and returns a session id
     * 
     * @param credential
     * @throws uk.icat3.exceptions.SessionException if credential is invalid
     * @return session id
     */
    @WebMethod(operationName="loginCredentials")
    @RequestWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginCredentials")
    @ResponseWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginCredentialsResponse")
    public String login(String credential) throws SessionException {
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(credential);
    }
    
     /**
     * Logs in with a username, password and runAs and returns a session id
     *
     * @param username admin username
     * @param password admin password
     * @param runAs the user you wish to log in as
     * @throws uk.icat3.exceptions.SessionException if username and pasword are invalid
     * @return session id
     */
    @WebMethod(operationName="loginAdmin")
    @ExcludeClassInterceptors
    @RequestWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginAdmin")
    @ResponseWrapper(className="uk.icat3.sessionbeans.user.jaxws.loginAdminResponse")
    public String login(String username, String password, String runAs) throws SessionException {
        log.trace("login("+username+", *******, "+runAs+")");
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username, password, runAs);
    }
    
    /**
     * Logs out of the system
     * 
     * @param sid seesion id
     * @return true if sucessfully logged out
     */
    @WebMethod()
    public boolean logout(String sid) {
        
        UserManager userManager;
        try {
            userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        } catch (SessionException ex) {
            return false;
        }
        
        return userManager.logout(sid);
    }
    
    /**
     *
     * @param sid
     * @throws uk.icat3.exceptions.SessionException
     * @return String
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getUserIdFromSessionId(String sid) throws SessionException {
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.getUserIdFromSessionId(sid);
    }
    
    /**
     *
     * @param sid
     * @param user
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchUserException
     * @return userdeatils
     */
    @WebMethod()
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public UserDetails getUserDetails(String sid, String user) throws SessionException, NoSuchUserException {
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.getUserDetails(sid, user);
    }
}
