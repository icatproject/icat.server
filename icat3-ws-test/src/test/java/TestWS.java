import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.client.Datafile;
import uk.icat3.client.DatafileFormat;
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

		Dataset ds1 = session.createDataset("Wibble", "GQ", inv);
		
		DatafileFormat dft1 = session.createDatafileFormat("png", "binary");
		DatafileFormat dft2 = session.createDatafileFormat("bmp", "binary");
		
		session.createDatafile("fred", dft1, ds1);
		session.createDatafile("bill", dft2, ds1);
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		session.createDatasetParameter(date, p, ds1);
		
		Dataset ds2 = session.createDataset("Wobble", "GQ", inv);
		session.createDatafile("mog", dft1, ds2);
		
//		Application application = session.createApplication("The one", "1.0");
//		Job job = session.createJob(application, ds1, ds2);

	

	}
	
	@Test
	public void updates() throws Exception {
		session.clear();
		
		session.createFacility("Test Facility", 90L);

		session.createInvestigationType("TestExperiment");

		session.createDatasetType("GQ");

		Investigation inv = session.createInvestigation("Test Facility", "A", "Not null", "TestExperiment");

		Parameter p = session.createParameterPK("TIMESTAMP", "TIMESTAMP", "F is not a wibble",
				Session.ParameterType.DATASET, ParameterValueType.DATE_AND_TIME);

		Dataset ds = session.createDataset("Wibble", "GQ", inv);
		
		DatafileFormat dfmt = session.createDatafileFormat("png", "binary");

		Datafile df = session.createDatafile("fred", dfmt, ds);
		df.setLocation("guess");
		session.update(df);
		df.setDatafileFormat(session.createDatafileFormat("notpng", "notbinary"));
		session.update(df);
		df.setDatafileFormat(null);
		df.setFileSize(-1L);
		session.update(df);
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		session.createDatasetParameter(date, p, ds);
	}

	@Test
	public void includes() throws Exception {
		session.clear();
		create();
		
		List<?> results = session.search("Dataset.id order by id");

		assertEquals("Count", 2, results.size());
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
	}
	
	@AfterClass
	public static void zap() throws Exception {
//		session.clear();
	}

}
