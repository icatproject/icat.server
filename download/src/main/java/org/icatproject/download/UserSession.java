package org.icatproject.download;

import org.icatproject.core.IcatException;

public interface UserSession {

    public String getUserIdFromSessionId(String sessionId) throws IcatException;

}
