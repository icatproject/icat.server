package uk.icat3.sessionbeans.user;

import javax.ejb.Local;

import uk.icat3.exceptions.SessionException;
import uk.icat3.user.User;

/**
 * This is the business interface for UserSession enterprise bean.
 */
@Local
public interface UserSessionLocal extends User {

    public abstract boolean isSessionValid(String sessionId);

	public abstract String loginAdmin(String sessionId, String runAsUserFedId) throws SessionException;
    
}
