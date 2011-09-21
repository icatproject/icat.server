package uk.icat3.util;

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
	 * If user has options to modify and
	 */
	MANAGE_USERS,
	/**
	 * Set Facility acquired data.
	 */
	SET_FA;
}
