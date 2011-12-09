import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.client.DatafileFormat;
import uk.icat3.client.DatafileFormatPK;
import uk.icat3.client.DatasetType;
import uk.icat3.client.Facility;
import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationType;
import uk.icat3.client.ObjectAlreadyExistsException_Exception;
import uk.icat3.client.Parameter;

public class Setup {

	@Test
	public void t1() {
		assertTrue(false);
	};


	private static ICAT icatEP;
	private static String sessionId;

	@Test
	public  void clear() throws Exception {
		List<Object> lo = Setup.icatEP.search(Setup.sessionId, "Investigation");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (Investigation) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "DatasetType");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (DatasetType) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "DatafileFormat");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (DatafileFormat) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "Investigation");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (Investigation) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "InvestigationType");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (InvestigationType) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "Parameter");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (Parameter) o);
		}
		lo = Setup.icatEP.search(Setup.sessionId, "Facility");
		for (Object o : lo) {
			System.out.println("Deleting " + o);
			Setup.icatEP.delete(Setup.sessionId, (Facility) o);
		}
	}

	@Test
	public void create() throws Exception {
		String fac = "CLF";
		Setup.createFacility(fac, 90L);

		Setup.createInvestigationType("experiment");

		Setup.createInvestigation(fac, "None", "None", "experiment");
		Setup.createInvestigation(fac, "Temp", "Temp", "experiment");
		Setup.createInvestigation(fac, "G4 07-08 P2", "Inv", "experiment");
		Setup.createInvestigation(fac, "G3 07-08 P2", "Inv", "experiment");
		Setup.createInvestigation(fac, "81030", "Inv", "experiment");
		Setup.createInvestigation(fac, "81007", "Inv", "experiment");
		Setup.createInvestigation(fac, "81025", "Inv", "experiment");
		Setup.createInvestigation(fac, "91009", "Inv", "experiment");
		Setup.createInvestigation(fac, "91034", "Inv", "experiment");
		Setup.createInvestigation(fac, "91018", "Inv", "experiment");
		Setup.createInvestigation(fac, "91023", "Inv", "experiment");
		Setup.createInvestigation(fac, "101019", "Inv", "experiment");
		Setup.createInvestigation(fac, "SequoiaRunA", "Inv", "experiment");
		Setup.createInvestigation(fac, "91009_91015", "Inv", "experiment");
		Setup.createInvestigation(fac, "SequoiaRunB", "Inv", "experiment");
		Setup.createInvestigation(fac, "RadCommissioning2011", "Inv", "experiment");
		Setup.createInvestigation(fac, "91008", "Inv", "experiment");
		Setup.createInvestigation(fac, "102026", "Inv", "experiment");
		Setup.createInvestigation(fac, "102025", "Inv", "experiment");
		Setup.createInvestigation(fac, "112013", "Inv", "experiment");
		Setup.createInvestigation(fac, "102030", "Inv", "experiment");
		Setup.createInvestigation(fac, "112010", "Inv", "experiment");

		Setup.createDatafileFormat("png", "binary");
		Setup.createDatafileFormat("bmp", "binary");
		Setup.createDatafileFormat("pcx", "binary");
		Setup.createDatafileFormat("tiff", "binary");
		Setup.createDatafileFormat("jpg", "binary");
		Setup.createDatafileFormat("gif", "binary");
		Setup.createDatafileFormat("dat", "text");
		Setup.createDatafileFormat("xml", "text");

		Setup.createDatasetType("GD");
		Setup.createDatasetType("GQ");
		Setup.createDatasetType("GS");
		Setup.createDatasetType("PB");
		Setup.createDatasetType("HB");

	}


	private static void createDatasetType(String name) throws Exception {
		DatasetType dst = new DatasetType();
		dst.setName(name);
		try {
			Setup.icatEP.create(Setup.sessionId, dst);
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createFacility(String shortName, long daysUntilRelease) throws Exception {
		Facility f = new Facility();
		f.setFacilityShortName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		try {
			Setup.icatEP.create(Setup.sessionId, f);
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createInvestigation(String facility, String invNumber, String title, String invType)
			throws Exception {
		Investigation i = new Investigation();
		i.setFacility(facility);
		i.setInvNumber(invNumber);
		i.setTitle(title);
		i.setInvType(invType);
		try {
			Setup.icatEP.create(Setup.sessionId, i);
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createInvestigationType(String name) throws Exception {
		InvestigationType type = new InvestigationType();
		type.setName(name);
		try {
			Setup.icatEP.create(Setup.sessionId, type);
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@BeforeClass
	public static void setup() throws Exception {

		String urlString = ("http://localhost:8080");

		URL icatUrl = new URL(urlString + "/ICATService/ICAT?wsdl");

		ICATService icatService = new ICATService(icatUrl, new QName("client.icat3.uk", "ICATService"));
		Setup.icatEP = icatService.getICATPort();

		Setup.sessionId = Setup.icatEP.login("root", "password");

	}



	@Test
	public void setAuthz() throws Exception {
		String impUsername = "IMP";
		String cicUsername = "CIC";
		String guestUsername = "guest";
		Setup.icatEP.addUserGroupMember(Setup.sessionId, impUsername, impUsername);
		Setup.icatEP.addRule(Setup.sessionId, impUsername, "Investigation", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, impUsername, "FacilityUser", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, impUsername, "Investigator", "CRUD", null);

		Setup.icatEP.addUserGroupMember(Setup.sessionId, cicUsername, cicUsername);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "Investigation", "RUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "Dataset", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "Parameter", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "DatasetParameter", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "Datafile", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "DatafileFormat", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, cicUsername, "DatasetType", "CRUD", null);
		Setup.icatEP.addUserGroupMember(Setup.sessionId, "root", "root");
		Setup.icatEP.addRule(Setup.sessionId, "root", "DatafileFormat", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, "root", "DatasetType", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, "root", "Facility", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, "root", "Investigation", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, "root", "InvestigationType", "CRUD", null);
		Setup.icatEP.addRule(Setup.sessionId, "root", "Parameter", "CRUD", null);

		Setup.icatEP.addUserGroupMember(Setup.sessionId, guestUsername, guestUsername);
		Setup.icatEP.addRule(Setup.sessionId, guestUsername, "DatasetType", "R", null);
		Setup.icatEP.addRule(Setup.sessionId, guestUsername, "Parameter", "R", null);
	}

}
