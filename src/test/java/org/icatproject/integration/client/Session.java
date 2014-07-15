package org.icatproject.integration.client;

public class Session {

	private String sessionId;
	private ICAT icat;

	Session(ICAT icat, String sessionId) {
		this.icat = icat;
		this.sessionId = sessionId;
	}

	public String getUserName() throws IcatException {
		return icat.getUserName(sessionId);
	}

	public double getRemainingMinutes() throws IcatException {
		return icat.getRemainingMinutes(sessionId);
	}

	public void logout() throws IcatException {
		icat.logout(sessionId);
	}

	public void refresh() throws IcatException {
		icat.refresh(sessionId);
	}

}
