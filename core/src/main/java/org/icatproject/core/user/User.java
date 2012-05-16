package org.icatproject.core.user;

import javax.servlet.http.HttpServletRequest;

import org.icatproject.core.IcatException;

public interface User  {
	public String login(String username, String password, HttpServletRequest req)
			throws IcatException;

	public void logout(String sessionId) throws IcatException;

	public double getRemainingMinutes(String sessionId) throws IcatException;

	public String getUserName(String sessionId) throws IcatException;
}