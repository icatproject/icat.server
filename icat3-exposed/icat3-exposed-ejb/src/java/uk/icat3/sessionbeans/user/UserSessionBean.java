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
import org.apache.log4j.Logger;
import uk.icat3.exceptions.LoginException;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserManager;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService()
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UserSessionBean extends EJBObject implements UserSessionLocal {
    
    static Logger log = Logger.getLogger(UserSessionBean.class);
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String login(String username, String password) throws LoginException {
        log.trace("login("+username+", "+password+")");
        
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        String sessionId = userManager.login(username,password);
        
        return sessionId;
    }
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean logout(String sid) throws LoginException {
        log.trace("login("+sid+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        return userManager.logout(sid);
    }
    
  //  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String getUserId(String sid) throws LoginException {
        log.trace("getUserId("+sid+")");
        
        //TODO: should user UserManager and User interface here to get the userId from the sessionId
        UserManager userManager = new UserManager(managerUser);
        return userManager.getUserIdFromSessionId(sid);
    }
}
