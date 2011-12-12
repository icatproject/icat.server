import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.client.DatafileFormat;
import uk.icat3.client.DatasetType;
import uk.icat3.client.Facility;
import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationType;
import uk.icat3.client.Parameter;

public class TestWS {

	private static ICAT icatEP;
	private static String sessionId;
	private static Session session;

	@Test
	public void clear() throws Exception {
		List<Object> lo = TestWS.icatEP.search(TestWS.sessionId, "Investigation");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (Investigation) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "DatasetType");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (DatasetType) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "DatafileFormat");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (DatafileFormat) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "Investigation");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (Investigation) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "InvestigationType");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (InvestigationType) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "Parameter");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (Parameter) o);
		}
		lo = TestWS.icatEP.search(TestWS.sessionId, "Facility");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			TestWS.icatEP.delete(TestWS.sessionId, (Facility) o);
		}
	}

	@Test
	public void create() throws Exception {
		String fac = "CLF";
		session.createFacility(fac, 90L);

		session.createInvestigationType("experiment");

		session.createInvestigation(fac, "None", "None", "experiment");
		session.createInvestigation(fac, "Temp", "Temp", "experiment");
		session.createInvestigation(fac, "G4 07-08 P2", "Inv", "experiment");
		session.createInvestigation(fac, "G3 07-08 P2", "Inv", "experiment");
		session.createInvestigation(fac, "81030", "Inv", "experiment");
		session.createInvestigation(fac, "81007", "Inv", "experiment");
		session.createInvestigation(fac, "81025", "Inv", "experiment");
		session.createInvestigation(fac, "91009", "Inv", "experiment");
		session.createInvestigation(fac, "91034", "Inv", "experiment");
		session.createInvestigation(fac, "91018", "Inv", "experiment");
		session.createInvestigation(fac, "91023", "Inv", "experiment");
		session.createInvestigation(fac, "101019", "Inv", "experiment");
		session.createInvestigation(fac, "SequoiaRunA", "Inv", "experiment");
		session.createInvestigation(fac, "91009_91015", "Inv", "experiment");
		session.createInvestigation(fac, "SequoiaRunB", "Inv", "experiment");
		session.createInvestigation(fac, "RadCommissioning2011", "Inv", "experiment");
		session.createInvestigation(fac, "91008", "Inv", "experiment");
		session.createInvestigation(fac, "102026", "Inv", "experiment");
		session.createInvestigation(fac, "102025", "Inv", "experiment");
		session.createInvestigation(fac, "112013", "Inv", "experiment");
		session.createInvestigation(fac, "102030", "Inv", "experiment");
		session.createInvestigation(fac, "112010", "Inv", "experiment");

		session.createDatafileFormat("png", "binary");
		session.createDatafileFormat("bmp", "binary");
		session.createDatafileFormat("pcx", "binary");
		session.createDatafileFormat("tiff", "binary");
		session.createDatafileFormat("jpg", "binary");
		session.createDatafileFormat("gif", "binary");
		session.createDatafileFormat("dat", "text");
		session.createDatafileFormat("xml", "text");

		session.createDatasetType("GD");
		session.createDatasetType("GQ");
		session.createDatasetType("GS");
		session.createDatasetType("PB");
		session.createDatasetType("HB");

	}



	@BeforeClass
	public static void setup() throws Exception {
		session = new Session();
	}

	@Test
	public void setAuthz() throws Exception {
		String impUsername = "IMP";
		String cicUsername = "CIC";
		String guestUsername = "guest";
		TestWS.icatEP.addUserGroupMember(TestWS.sessionId, impUsername, impUsername);
		TestWS.icatEP.addRule(TestWS.sessionId, impUsername, "Investigation", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, impUsername, "FacilityUser", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, impUsername, "Investigator", "CRUD", null);

		TestWS.icatEP.addUserGroupMember(TestWS.sessionId, cicUsername, cicUsername);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "Investigation", "RUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "Dataset", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "Parameter", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "DatasetParameter", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "Datafile", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "DatafileFormat", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, cicUsername, "DatasetType", "CRUD", null);
		TestWS.icatEP.addUserGroupMember(TestWS.sessionId, "root", "root");
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "DatafileFormat", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "DatasetType", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "Facility", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "Investigation", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "InvestigationType", "CRUD", null);
		TestWS.icatEP.addRule(TestWS.sessionId, "root", "Parameter", "CRUD", null);

		TestWS.icatEP.addUserGroupMember(TestWS.sessionId, guestUsername, guestUsername);
		TestWS.icatEP.addRule(TestWS.sessionId, guestUsername, "DatasetType", "R", null);
		TestWS.icatEP.addRule(TestWS.sessionId, guestUsername, "Parameter", "R", null);
	}

}
