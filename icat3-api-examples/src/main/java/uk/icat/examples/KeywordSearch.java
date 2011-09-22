/**
 * Example to perform keyword search on investigations using ICAT API.
 * 
 * @author Richard Tyer
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *
 * $Id: KeywordSearch.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 *  
 */
package uk.icat.examples;

import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;

public class KeywordSearch extends ExampleBase {

	public static void main(String[] args) throws Exception {

		ICAT icat = getIcat();

		String sid = icat.login(username, password);

		Investigation[] investigations = icat.searchByKeywords(sid, keywordsList.toArray(new String[0]));

		for (Investigation inv : investigations) {
			System.out.println("Investigation " + inv.getId() + " title: " + inv.getTitle());
		}
	}
}
