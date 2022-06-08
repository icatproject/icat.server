package org.icatproject.integration;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.icatproject.DataCollection;
import org.icatproject.DataPublication;
import org.icatproject.DataPublicationDate;
import org.icatproject.DataPublicationType;
import org.icatproject.Datafile;
import org.icatproject.DatafileFormat;
import org.icatproject.Dataset;
import org.icatproject.DatasetType;
import org.icatproject.Facility;
import org.icatproject.Investigation;
import org.icatproject.InvestigationType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the DataPublication functionality added in ICAT 5.0
 */
public class TestDataPublication {

	private static WSession session;

	@BeforeClass
	public static void beforeClass() throws Exception {
		try {
			session = new WSession();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Before
	public void initializeSession() throws Exception {
		session.setAuthz();
	}

	@After
	public void clearSession() throws Exception {
        // delete the Facility objects (including children)
        // and also DataCollection and Study objects (can span Facilities)
		session.clear();
        // delete all Rule, UserGroup, User, Grouping, PublicStep objects
		session.clearAuthz();
	}

	@Test
	public void testCreation() throws Exception {
		// create DataPublications of different types
		Facility facility = session.createFacility("Test DataPublication Facility", 90);
		InvestigationType invType = session.createInvestigationType(facility, "DataPubTest");
		Investigation inv1 = session.createInvestigation(facility, "DataPubTestInv1", 
			"First Investigation to be included in a Data Publication", 
			invType);
		Investigation inv2 = session.createInvestigation(facility, "DataPubTestInv2", 
			"Second Investigation to be included in a Data Publication", 
			invType);
		DatasetType datasetType = session.createDatasetType(facility, "Test dataset type");
		Dataset ds1 = session.createDataset("Dataset1", datasetType, inv2);
		Dataset ds2 = session.createDataset("Dataset2", datasetType, inv2);
		DatafileFormat datafileFormat = session.createDatafileFormat(facility, "Format1", "binary");
		// put Datafile1 in ds1. put Datafile2 and Datafile3 in ds2.
		Datafile df1 = session.createDatafile("Datafile1", datafileFormat, ds1, 10L);
		session.createDatafile("Datafile2", datafileFormat, ds2, 10L);
		session.createDatafile("Datafile3", datafileFormat, ds2, 10L);
		DataCollection dataCollectionInv1 = session.createDataCollection(inv1);
		DataCollection dataCollectionInv2 = session.createDataCollection(inv2);
		DataCollection dataCollectionDsAndDf = session.createDataCollection(df1, ds2);
		DataPublicationType dataPubInvType = session.createDataPublicationType(
				facility,
				"investigation", 
				"Auto-generated data publications created for each Investigation"
		);
		DataPublicationType dataPubUserType = session.createDataPublicationType(
				facility,
				"user-defined", 
				"User-defined data publications created for hand picked selections by users"
		);
		DataPublication dataPublicationNoType = session.createDataPublication(facility, dataCollectionInv1, null, 
				"A Data Publication with no type", "PID:dataPublicationNoType");
		DataPublication dataPublicationInvType = session.createDataPublication(facility, dataCollectionInv2, dataPubInvType, 
				"A Data Publication for an Investigation", "PID:dataPublicationInvType");
		DataPublication dataPublicationUserType = session.createDataPublication(facility, dataCollectionDsAndDf, dataPubUserType, 
				"A Data Publication for for a user-defined selection", "PID:dataPublicationUserType");

		// add a date to one of the DataPublications
		String testDateType = "Test date type";
		String testDateString = "20220131T171926Z";
		DataPublicationDate dataPubDate = session.createDataPublicationDate(dataPublicationNoType, testDateType, testDateString);

		System.out.println("Created dataPublicationNoType with ID: " + dataPublicationNoType.getId());
		System.out.println("Created dataPublicationInvType with ID: " + dataPublicationInvType.getId());
		System.out.println("Created dataPublicationUserType with ID: " + dataPublicationUserType.getId());
		System.out.println("Created dataPubDate with ID: " + dataPubDate.getId());

		// now find and check them
		List<Object> results1 = session.search("SELECT dp FROM DataPublication dp WHERE dp.type IS NULL INCLUDE dp.dates");
		assertEquals(results1.size(), 1);
		DataPublication dp1 = (DataPublication)results1.get(0);
		System.out.println("Found dp1 with ID: " + dp1.getId());
		assertEquals(dataPublicationNoType.getId(), dp1.getId());
		// check the date that was added
		assertEquals(testDateType, ((DataPublicationDate)dp1.getDates().get(0)).getDateType());
		assertEquals(testDateString, ((DataPublicationDate)dp1.getDates().get(0)).getDate());

		List<Object> results2 = session.search("SELECT dp FROM DataPublication dp WHERE dp.type.name = 'investigation'");
		assertEquals(results2.size(), 1);
		DataPublication dp2 = (DataPublication)results2.get(0);
		System.out.println("Found dp2 with ID: " + dp2.getId());
		assertEquals(dataPublicationInvType.getId(), dp2.getId());

		List<Object> results3 = session.search("SELECT dp FROM DataPublication dp WHERE dp.type.name = 'user-defined'");
		assertEquals(results3.size(), 1);
		DataPublication dp3 = (DataPublication)results3.get(0);
		System.out.println("Found dp3 with ID: " + dp3.getId());
		assertEquals(dataPublicationUserType.getId(), dp3.getId());
	}
    

}
