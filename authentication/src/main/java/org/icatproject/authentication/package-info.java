/**
 * <p>Provides the classes necessary to create an ICAT authentication plugin.</p>
 * 
 * <p>All plugins must conform to the {@link org.icatproject.authentication.Authenticator} interface. An authenticator must implement one 
 * method, authenticate, which returns an {@link org.icatproject.authentication.Authentication}.</p>
 * 
 * <p>To support multiple authentication mechanism at a site, an authentication object holds both the
 * name of the mechanism and the user name authenticated by that mechanism. The mechanism may be
 * null if there is only one plugin deployed of if the deployer is sure that user names found by
 * different mechanisms will never be the same.</p>
 * 
 * <p>It is recommended that the implementation allows the mechanism part of the authentication object to be configured by a properties file.
 * If the deployer chooses to publish the mechanism, then it is recommended that it is the same 
 * string as that used for the mnemonic configured in the icat.properties file for that plugin and that it is short such as "db" or "ldap".</p>
 * 
 * <p>In the case where a plugin performs some mapping from an external system to a local identity, such as 
 * might occur with <a href="http://umbrella.psi.ch">Umbrella</a> then the mnemonic for the plugin should be something meaningful to the 
 * user - e.g. "umbrella" but the mechanism returned inside the authentication object, if configured, should relate to the local system.</p>
 * 
 */
package org.icatproject.authentication;