import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.client.Dataset;
import uk.icat3.client.Investigation;
import uk.icat3.client.ObjectAlreadyExistsException_Exception;
import uk.icat3.client.Parameter;
import uk.icat3.client.ParameterValueType;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestWS {

	private static Session session;

	private static void create() throws Exception {

		session.createFacility("Test Facility", 90L);

		session.createInvestigationType("TestExperiment");

		session.createDatasetType("GQ");

		Investigation inv = session.createInvestigation("Test Facility", "A", "Not null", "TestExperiment");

		Parameter p = session.createParameterPK("TIMESTAMP", "TIMESTAMP", "F is not a wibble",
				Session.ParameterType.DATASET, ParameterValueType.DATE_AND_TIME);

		Dataset ds = session.createDataset("Wibble", "GQ", inv.getId());

		session.createDatafile("fred", ds);
		session.createDatafile("bill", ds);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		session.createDatasetParameter(date, p, ds);

	}

	@Test
	public void t1() throws Exception {
		List<?> results = session.search("Dataset.id");

		assertEquals("Count", 1, results.size());
		Long dsid = (Long) results.get(0);

		results = session.search("Dataset [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		Dataset ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("No files", 0, ds.getDatafileCollection().size());
		assertEquals("No params", 0, ds.getDatasetParameterCollection().size());

		results = session.search("Dataset INCLUDE Datafile [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafileCollection().size());
		assertEquals("No params", 0, ds.getDatasetParameterCollection().size());

		results = session.search("Dataset INCLUDE DatasetParameter [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("No Files", 0, ds.getDatafileCollection().size());
		assertEquals("Params", 1, ds.getDatasetParameterCollection().size());

		results = session.search("Dataset INCLUDE Datafile, DatasetParameter [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafileCollection().size());
		assertEquals("Params", 1, ds.getDatasetParameterCollection().size());
	}

	@BeforeClass
	public static void setup() throws Exception {
		session = new Session();
		try {
			session.setAuthz();
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage() + " already exists");
		}
		session.clear();
		create();
	}
	
	@AfterClass
	public static void zap() throws Exception {
		session.clear();
	}

}
