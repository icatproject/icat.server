/*
 * UserSessionBean.java
 *
 * Created on 20 March 2007, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.user;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserDetails;
import uk.icat3.user.UserManager;
import static uk.icat3.util.Constants.*;
/**
 *
 * @author gjd37
 */
@Stateless()
@WebService()
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UserSessionBean extends EJBObject implements UserSessionLocal {
    
    static Logger log = Logger.getLogger(UserSessionBean.class);
    
    @PersistenceContext(unitName="icat3-exposed-user")
    private EntityManager managerUser;
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String login(String username, String password) throws SessionException {        
        
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username,password);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String login(String username, String password, int lifetime) throws SessionException {
                
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username,password, lifetime);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String login(String credential) throws SessionException {
               
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(credential);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String login(String username, String password, String runAs) throws SessionException {
                
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.login(username, password, runAs);
    }
    
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean logout(String sid) {
       
        UserManager userManager;
        try {
            userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        } catch (SessionException ex) {
            return false;
        }
        
        return userManager.logout(sid);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getUserIdFromSessionId(String sid) throws SessionException {
                
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.getUserIdFromSessionId(sid);
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public UserDetails getUserDetails(String sid, String user) throws SessionException, NoSuchUserException {
                
        UserManager userManager = new UserManager(DEFAULT_USER_IMPLEMENTATION, managerUser);
        
        return userManager.getUserDetails(sid, user);
    }
    
    
}
