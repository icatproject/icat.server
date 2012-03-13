package uk.icat3.manager;

public enum AccessType {

	READ, UPDATE, DELETE, CREATE,
	/**
	 * Admin
	 */
	ADMIN,
	/**
	 * Download from SRB
	 */
	DOWNLOAD,

	/**
	 * Set Facility acquired data.
	 */
	SET_FA;
}
