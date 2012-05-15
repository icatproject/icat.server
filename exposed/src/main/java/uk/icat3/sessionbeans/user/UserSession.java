package uk.icat3.sessionbeans.user;

import javax.ejb.Remote;

import org.icatproject.core.IcatException;

/**
 * This is the business interface for UserSession enterprise bean.
 */
@Remote
public interface UserSession {

	public String getUserIdFromSessionId(String sid) throws IcatException;

}
