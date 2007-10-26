/*
 * SessionDelegate.java
 * 
 * Created on 22-Oct-2007, 14:48:11
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.download;

import javax.naming.NamingException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.user.UserSession;
/**
 *
 * @author gjd37
 */
public class SessionDelegate {
    
    private static SessionDelegate sd;
    private static UserSession usr ;
   
    public static SessionDelegate getInstance(){
            synchronized(SessionDelegate.class){
                if(sd == null){
                    try {
                        sd = new SessionDelegate();
                    } catch(Exception se) {
                        throw new RuntimeException(se);
                    }
                }
                return sd;
            }       
    }    
    
    /** Creates a new instance of SessionDelegate */
    private  SessionDelegate() throws NamingException {
        
        CachingServiceLocator csl =  CachingServiceLocator.getInstance();
        usr = (UserSession)csl.lookup("UserSessionEJB");
    }
    
    /*All SessionDelegate methods here*/
    public String getUserFromSessionId(String sessionId) throws SessionException {
        return usr.getUserIdFromSessionId(sessionId);
    }    
}
