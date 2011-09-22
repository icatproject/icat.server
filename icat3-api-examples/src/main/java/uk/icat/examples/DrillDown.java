/**
 * Example to demonstrate retrieving an investigation and drilling 
 * down into its constituent datasets and datafiles
 * 
 * @author Richard Tyer
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *
 * $Id: DrillDown.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 *  
 */
package uk.icat.examples;

import uk.icat3.client.Datafile;
import uk.icat3.client.Dataset;
import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationInclude;

public class DrillDown extends ExampleBase {

	public static void main(String[] args) throws Exception {

		ICAT icat = getIcat();

		String sid = icat.login(username, password);

		Investigation inv = icat.getInvestigationIncludes(sid, investigationId, InvestigationInclude.DATASETS_AND_DATAFILES);

		for (Dataset dataset : inv.getDatasetCollection()) {
			Long dsID = dataset.getId();
			String dsName = dataset.getName();

			System.out.println("Dataset: " + dsID + " has name " + dsName);

			for (Datafile datafile : dataset.getDatafileCollection()) {
				long dfID = datafile.getId();
				String dfName = datafile.getName();

				System.out.println("Datafile: " + dfName + " has name " + dfID);
			}
		}
	}
}
