package org.icatproject.authentication;

import java.util.Map;

import javax.ejb.Remote;

import org.icatproject.core.IcatException;

/**
 * An interface to be implemented by an ICAT authenticator plugin. An implementation might make use
 * of the {@link AddressChecker}.
 */
@Remote
public interface Authenticator {

	/**
	 * @param credentials
	 *            a map with keys such as username and password. The names and meanings of the keys
	 *            is the responsibility of the implementor of this interface.
	 * 
	 * @param remoteAddr
	 *            a string representation of the numeric form of an IP4 or IP6 address. This may be
	 *            used by the authenticator making use of the {@link AddressChecker} to determine
	 *            the outcome.
	 * 
	 * @return an Authentication object
	 * 
	 * @throws IcatException
	 *             of type {@link org.icatproject.core.IcatException.IcatExceptionType#SESSION} if
	 *             unable to authenticate.
	 */
	Authentication authenticate(Map<String, String> credentials, String remoteAddr)
			throws IcatException;

}