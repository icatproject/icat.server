package org.icatproject.core.authentication;

import java.util.Map;

import javax.ejb.Remote;

import org.icatproject.core.IcatException;

@Remote
public interface Authenticator {

	Authentication authenticate(Map<String, String> credentials, String remoteAddr)
			throws IcatException;

}