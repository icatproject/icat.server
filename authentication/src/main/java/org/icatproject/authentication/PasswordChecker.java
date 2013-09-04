package org.icatproject.authentication;

import static org.apache.commons.codec.digest.Crypt.crypt;

/**
 * A password checker able to deal with plain text and encoded passwords (as indicated by the
 * leading '$')
 */
public class PasswordChecker {

	/**
	 * @param password
	 *            the password as entered by the user
	 * @param encodedPassword
	 *            the password to compare against. It is not necessarily encoded
	 * @return true if pass matches the encodePassword
	 */
	public static boolean verify(String password, String encodedPassword) {
		if (encodedPassword == null || encodedPassword.isEmpty())
			return false;

		if (encodedPassword.charAt(0) == '$') {
			return encodedPassword.equals(crypt(password, encodedPassword));
		} else {
			return encodedPassword.equals(password);
		}
	}

}
