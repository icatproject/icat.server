package org.icatproject.authentication;

import java.io.Serializable;

/**
 * An authentication object holding both the raw user name and the mechanism
 * 
 * To support multiple authentication mechanism at a site, an authentication object holds both the
 * name of the mechanism and the user name authenticated by that mechanism. The mechanism may be
 * null if there is only one plugin deployed of if the deployer is sure that user names found by
 * different mechanisms will never be the same.
 * 
 */
@SuppressWarnings("serial")
public class Authentication implements Serializable {
	private String userName;
	private String mechanism;

	/**
	 * @param userName
	 *            must identify a user uniquely for the chosen mechanism
	 * @param mechanism
	 *            a way of identifying a user. May be null.
	 */
	public Authentication(String userName, String mechanism) {
		this.mechanism = mechanism;
		this.userName = userName;
	}

	/**
	 * @return a String that uniquely identifies a user for the ICAT installation and that can be
	 *         used in authorization.
	 */
	public String getUserName() {
		if (mechanism == null) {
			return userName;
		} else {
			return mechanism + "/" + userName;
		}
	}

	@Override
	public String toString() {
		return getUserName();
	}
}
