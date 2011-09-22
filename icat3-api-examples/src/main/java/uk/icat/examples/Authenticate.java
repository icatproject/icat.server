/**
 * Example to show user authentication with ICAT API to get SID
 * 
 * @author Richard Tyer
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *
 * $Id: Authenticate.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 * 
 */
package uk.icat.examples;
 
import uk.icat3.client.ICAT;

public class Authenticate extends ExampleBase {

	public static void main(String[] args) throws Exception {
		ICAT icat = getIcat();
		String sid = icat.login(username, password);
		System.out.println("Session ID = " + sid);
		icat.logout(sid);
	}
}
