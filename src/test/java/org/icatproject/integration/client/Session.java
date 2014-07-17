package org.icatproject.integration.client;

import java.nio.file.Path;

public class Session {

	public enum Attributes {
		/** Include createId etc */
		ALL,

		/** Only export attributes which may normally be set by the user */
		USER
	}

	public enum DuplicateAction {
		/** Throw an expection */
		THROW,

		/** Don't check just go to the next row */
		IGNORE,

		/** Check that new data matches the old */
		CHECK,

		/** Replace old data with new */
		OVERWRITE
	}

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

	public void importMetaData(Path path, DuplicateAction duplicateAction, Attributes attributes)
			throws IcatException {
		icat.importMetaData(sessionId, path, duplicateAction, attributes);
	}

}
