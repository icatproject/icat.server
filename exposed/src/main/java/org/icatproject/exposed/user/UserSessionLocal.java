package org.icatproject.exposed.user;

import javax.ejb.Local;

import org.icatproject.core.user.User;

/**
 * This is the business interface for UserSession enterprise bean.
 */
@Local
public interface UserSessionLocal  extends User {
}
