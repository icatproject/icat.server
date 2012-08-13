package org.icatproject.core.authentication;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Authentication implements Serializable {
	private String userName;
	private String mechanism;

	public Authentication(String userName, String mechanism) {
		this.userName = userName;
		this.mechanism = mechanism;
	}

	public String getUserName() {
		return userName;
	}

	public String getMechanism() {
		return mechanism;
	}
}
