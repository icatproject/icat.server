
package uk.icat3.sessionbeans.user;

import javax.ejb.Local;
import javax.ejb.Remote;
import uk.icat3.exceptions.SessionException;


/**
 * This is the business interface for UserSession enterprise bean.
 */
@Local
@Remote
public interface UserSessionLocal {
    
    public String login(String username, String password) throws SessionException ;
    
    public boolean logout(String sid) throws SessionException ;
    
    public String getUserId(String sid) throws SessionException ;
    
}
