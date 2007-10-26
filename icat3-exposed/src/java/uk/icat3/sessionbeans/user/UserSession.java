
package uk.icat3.sessionbeans.user;

import javax.ejb.Remote;
import uk.icat3.exceptions.SessionException;


/**
 * This is the business interface for UserSession enterprise bean.
 */
@Remote
public interface UserSession {
    
    public String getUserIdFromSessionId(String sid) throws SessionException;
    
}
