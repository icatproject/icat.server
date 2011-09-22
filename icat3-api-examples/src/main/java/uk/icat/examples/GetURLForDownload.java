/**
 * Example to get URLs from ICAT API in order to download data files
 * 
 * @author Richard Tyer
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *
 * $Id: GetURLForDownload.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 *  
 */
package uk.icat.examples;

import uk.icat3.client.ICAT;

public class GetURLForDownload extends ExampleBase {

	public static void main(String[] args) throws Exception {

		ICAT icat = getIcat();

		String sid = icat.login(username, password);

		String dfURL = icat.downloadDatafile(sid, datafileId);
		System.out.println("URL for datafile " + dfURL);

		String dsURL = icat.downloadDataset(sid, datasetId);
		System.out.println("URL for dataset " + dsURL);
	}
}
