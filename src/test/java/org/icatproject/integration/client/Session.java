package org.icatproject.integration.client;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class Session {

	public enum Attributes {
		/** Include createId etc */
		ALL,

		/** Only export attributes which may normally be set by the user */
		USER
	}

	public enum DuplicateAction {
		/** Check that new data matches the old */
		CHECK,

		/** Don't check just go to the next row */
		IGNORE,

		/** Replace old data with new */
		OVERWRITE,

		/** Throw an expection */
		THROW
	}

	private ICAT icat;
	private String sessionId;

	Session(ICAT icat, String sessionId) {
		this.icat = icat;
		this.sessionId = sessionId;
	}

	public List<Long> create(String entities) throws IcatException {
		return icat.create(sessionId, entities);
	}

	public InputStream exportMetaData(Attributes attributes) throws IcatException {
		return icat.exportMetaData(sessionId, null, attributes);
	}

	public InputStream exportMetaData(String query, Attributes attributes) throws IcatException {
		return icat.exportMetaData(sessionId, query, attributes);
	}

	public String get(String query, long id) throws IcatException {
		return icat.get(sessionId, query, id);
	}

	public double getRemainingMinutes() throws IcatException {
		return icat.getRemainingMinutes(sessionId);
	}

	public String getUserName() throws IcatException {
		return icat.getUserName(sessionId);
	}

	public void importMetaData(Path path, DuplicateAction duplicateAction, Attributes attributes)
			throws IcatException {
		icat.importMetaData(sessionId, path, duplicateAction, attributes);
	}

	public void logout() throws IcatException {
		icat.logout(sessionId);
	}

	public void refresh() throws IcatException {
		icat.refresh(sessionId);
	}

	public String search(String query) throws IcatException {
		return icat.search(sessionId, query);
	}

}
