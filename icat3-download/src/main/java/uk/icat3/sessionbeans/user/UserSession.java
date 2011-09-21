/*
 * UserSession.java
 * 
 * Created on 23-Oct-2007, 11:35:32
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.user;

import uk.icat3.exceptions.SessionException;

/**
 *
 * @author gjd37
 */
public interface UserSession {

    public String getUserIdFromSessionId(String sessionId) throws SessionException;

}
