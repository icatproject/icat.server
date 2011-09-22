/**
 * Example to show retrieval of datasets and datafiles directly, i.e. 
 * without getting parent investigations first
 * 
 * @author Richard Tyer
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *
 * $Id: DirectDSDF.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 * 
 */
package uk.icat.examples;
 
import uk.icat3.client.Datafile;
import uk.icat3.client.Dataset;
import uk.icat3.client.DatasetInclude;
import uk.icat3.client.ICAT;

public class DirectDSDF extends ExampleBase {

	public static void main(String[] args) throws Exception {

		ICAT icat = getIcat();

		String sid = icat.login(username, password);
		Dataset dataset = icat.getDatasetIncludes(sid, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY);
		System.out.println("Dataset name: " + dataset.getName());

		if (dataset.getDatafileCollection().length > 0) {
			for (Datafile df : dataset.getDatafileCollection()) {
				System.out.println("---> Data file ID " + df.getId());
			}
		} else {
			System.out.println("Dataset has no datafiles?");
		}
	}
}
