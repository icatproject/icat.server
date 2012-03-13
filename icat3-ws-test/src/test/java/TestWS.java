import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.client.Application;
import uk.icat3.client.Constraint;
import uk.icat3.client.Datafile;
import uk.icat3.client.DatafileFormat;
import uk.icat3.client.Dataset;
import uk.icat3.client.DatasetParameter;
import uk.icat3.client.DatasetType;
import uk.icat3.client.DestType;
import uk.icat3.client.EntityField;
import uk.icat3.client.EntityInfo;
import uk.icat3.client.Facility;
import uk.icat3.client.InputDatafile;
import uk.icat3.client.InputDataset;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationType;
import uk.icat3.client.Job;
import uk.icat3.client.KeyType;
import uk.icat3.client.OutputDatafile;
import uk.icat3.client.OutputDataset;
import uk.icat3.client.ParameterType;
import uk.icat3.client.ParameterValueType;
import uk.icat3.client.RelType;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestWS {

	private static Session session;

	private static void create() throws Exception {

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType("GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		ParameterType p = session.createParameterType("TIMESTAMP", "TIMESTAMP", "F is not a wibble",
				Session.ParameterApplicability.DATASET, ParameterValueType.DATE_AND_TIME);

		Dataset wibble = session.createDataset("Wibble", dst, inv);

		DatafileFormat dft1 = session.createDatafileFormat("png", "binary");
		DatafileFormat dft2 = session.createDatafileFormat("bmp", "binary");

		session.createDatafile("wib1", dft1, wibble);
		session.createDatafile("wib2", dft2, wibble);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		session.createDatasetParameter(date, p, wibble);

		Dataset wobble = session.createDataset("Wobble", dst, inv);
		session.createDatafile("wob1", dft1, wobble);

		Dataset dfsin = session.createDataset("dfsin", dst, inv);
		Datafile fred = session.createDatafile("fred", dft1, dfsin);
		Datafile bill = session.createDatafile("bill", dft1, dfsin);

		Dataset dfsout = session.createDataset("dfsout", dst, inv);
		Datafile mog = session.createDatafile("mog", dft1, dfsout);

		Application application = session.createApplication("The one", "1.0");
		Job job = session.createJob(application);
		session.addInputDataset(job, wibble);
		session.addOutputDataset(job, wobble);
		session.addInputDatafile(job, fred);
		session.addInputDatafile(job, bill);
		session.addOutputDatafile(job, mog);
	}

	@Test
	public void updates() throws Exception {
		session.clear();

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType("GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		ParameterType p = session.createParameterType("TIMESTAMP", "TIMESTAMP", "F is not a wibble",
				Session.ParameterApplicability.DATASET, ParameterValueType.DATE_AND_TIME);

		Dataset wibble = session.createDataset("Wibble", dst, inv);
		Dataset wobble = session.createDataset("Wobble", dst, inv);

		DatafileFormat dfmt = session.createDatafileFormat("png", "binary");

		Datafile df = session.createDatafile("fred", dfmt, wibble);
		String orig = session.dump(df, 1);
		df = (Datafile) session.get("Datafile INCLUDE Dataset, DatafileFormat", df.getId());
		// assertEquals(orig, session.dump(df, 1));
		assertEquals("Wibble", df.getDataset().getName());

		df.setDataset(wobble);
		df.setLocation("guess");
		df.setDatafileFormat(session.createDatafileFormat("notpng", "notbinary"));
		df.setDatafileFormat(null);
		df.setFileSize(-1L);
		session.update(df);
		df = (Datafile) session.get("Datafile INCLUDE Dataset,DatafileFormat", df.getId());
		assertEquals("Wobble", df.getDataset().getName());

	}

	@Test
	public void testInvestigation() throws Exception {
		EntityInfo ei = session.getEntityInfo("Investigation");
		assertEquals("An investigation or experiment", ei.getClassComment());
		assertEquals("id", ei.getKeyFieldname());
		assertEquals(KeyType.GENERATED, ei.getKeyType());
		for (Constraint constraint : ei.getConstraints()) {
			assertEquals(Arrays.asList("name", "visitId", "facilityCycle", "instrument"), constraint.getFieldNames());
		}
		assertEquals(21, ei.getFields().size());
		int n = 0;
		for (EntityField field : ei.getFields()) {
			if (field.getName().equals("id")) {
				assertEquals("Long", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.ATTRIBUTE, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(null, field.isCascaded());
			} else if (field.getName().equals("facilityCycle")) {
				assertEquals("FacilityCycle", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.ONE, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(false, field.isCascaded());
			} else if (field.getName().equals("title")) {
				assertEquals("String", field.getType());
				assertEquals(true, field.isNotNullable());
				assertEquals("Full title of the investigation", field.getComment());
				assertEquals(RelType.ATTRIBUTE, field.getRelType());
				assertEquals((Integer) 255, field.getStringLength());
				assertEquals(null, field.isCascaded());
			} else if (field.getName().equals("investigationUsers")) {
				assertEquals("InvestigationUser", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.MANY, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(true, field.isCascaded());
			} else {
				n++;
			}
		}
		assertEquals(17, n);
	}

	@Test
	public void notificationRequests() throws Exception {
		session.clear();
		session.createNotificationRequest("A", DestType.P_2_P, "Facility", "C", "ptp",
				"notificationName userId entityName entityKey callArgs");
		session.createFacility("Test Facility", 90);
	}

	@Test
	public void includes() throws Exception {
		session.clear();
		create();

		List<?> results = session.search("Dataset.id order by id");

		assertEquals("Count", 4, results.size());
		Long dsid = (Long) results.get(0);

		results = session.search("Dataset [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		Dataset ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("No files", 0, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getDatasetParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getDatasetParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE DatasetParameter [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("No Files", 0, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getDatasetParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile, DatasetParameter [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getDatasetParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile, DatasetParameter, Investigation [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getDatasetParameters().size());
		assertNotNull("Inv", ds.getInvestigation());

		results = session.search("Job");
		assertEquals("Count", 1, results.size());
		Job job = (Job) results.get(0);
		assertEquals("InputDataset", 0, job.getInputDatasets().size());
		assertEquals("OutputDataset", 0, job.getOutputDatasets().size());
		assertEquals("InputDatafile", 0, job.getInputDatafiles().size());
		assertEquals("OutputDatafile", 0, job.getOutputDatafiles().size());

		results = session.search("Job");
		assertEquals("Count", 1, results.size());
		job = (Job) results.get(0);
		assertNull("Application", job.getApplication());

		results = session.search("Job INCLUDE Application");
		assertEquals("Count", 1, results.size());
		job = (Job) results.get(0);
		assertNotNull("Application", job.getApplication());

		results = session.search("Application");
		assertEquals("Count", 1, results.size());
		Application application = (Application) results.get(0);
		assertEquals("InputDataset", 0, application.getJobs().size());

		results = session.search("Application INCLUDE Job");
		assertEquals("Count", 1, results.size());
		application = (Application) results.get(0);
		assertEquals("InputDataset", 1, application.getJobs().size());

		results = session.search("Job INCLUDE InputDataset, InputDatafile, Dataset, Datafile");
		assertEquals("Count", 1, results.size());
		job = (Job) results.get(0);
		assertEquals("InputDataset", 1, job.getInputDatasets().size());
		assertEquals("OutputDataset", 0, job.getOutputDatasets().size());
		assertEquals("InputDatafile", 2, job.getInputDatafiles().size());
		assertEquals("OutputDatafile", 0, job.getOutputDatafiles().size());

		int ndsin = 0;
		for (InputDataset ids : job.getInputDatasets()) {
			if (ids.getDataset() != null) {
				ndsin++;
			}
		}
		int ndfin = 0;
		for (InputDatafile idf : job.getInputDatafiles()) {
			if (idf.getDatafile() != null) {
				ndfin++;
			}
		}
		assertEquals("Dataset", 1, ndsin);
		assertEquals("Datafile", 2, ndfin);

		results = session
				.search("Job INCLUDE InputDataset, InputDatafile, OutputDataset, OutputDatafile, Dataset, Datafile");
		assertEquals("Count", 1, results.size());
		job = (Job) results.get(0);
		assertEquals("InputDataset", 1, job.getInputDatasets().size());
		assertEquals("OutputDataset", 1, job.getOutputDatasets().size());
		assertEquals("InputDatafile", 2, job.getInputDatafiles().size());
		assertEquals("OutputDatafile", 1, job.getOutputDatafiles().size());

		ndsin = 0;
		for (InputDataset ids : job.getInputDatasets()) {
			if (ids.getDataset() != null) {
				ndsin++;
			}
		}
		ndfin = 0;
		for (InputDatafile idf : job.getInputDatafiles()) {
			if (idf.getDatafile() != null) {
				ndfin++;
			}
		}
		int ndsout = 0;
		for (OutputDataset ods : job.getOutputDatasets()) {
			if (ods.getDataset() != null) {
				ndsout++;
			}
		}
		int ndfout = 0;
		for (OutputDatafile odf : job.getOutputDatafiles()) {
			if (odf.getDatafile() != null) {
				ndfout++;
			}
		}
		assertEquals("Dataset", 1, ndsin);
		assertEquals("Datafile", 2, ndfin);
		assertEquals("Dataset", 1, ndsout);
		assertEquals("Datafile", 1, ndfout);
	}

	@Test
	public void bigCreate() throws Exception {
		session.clear();

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType("GQ");

		ParameterType p = session.createParameterType("TIMESTAMP", "TIMESTAMP", "F is not a wibble",
				Session.ParameterApplicability.DATASET, ParameterValueType.DATE_AND_TIME);

		DatafileFormat dft1 = session.createDatafileFormat("png", "binary");
		DatafileFormat dft2 = session.createDatafileFormat("bmp", "binary");

		Investigation inv = new Investigation();
		inv.setId(42L);
		inv.setFacility(facility);
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(investigationType);

		final Dataset wibble = session.addDataset(inv, "Wibble", dst);

		Datafile datafile = session.addDatafile(wibble, "wib1", dft1);

		datafile = session.addDatafile(wibble, "wib2", dft2);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		DatasetParameter dsp = session.addDatasetParameter(wibble, date, p);

		Dataset wobble = session.addDataset(inv, "Wobble", dst);
		datafile = session.addDatafile(wobble, "wob1", dft1);

		session.registerInvestigation(inv);
		session.get("Investigation INCLUDE Dataset, Datafile, DatasetParameter, Facility", inv.getId());

		// Dataset dfsin = session.createDataset("dfsin", "GQ", inv);
		// Datafile fred = session.createDatafile("fred", dft1, dfsin);
		// Datafile bill = session.createDatafile("bill", dft1, dfsin);
		//
		// Dataset dfsout = session.createDataset("dfsout", "GQ", inv);
		// Datafile mog = session.createDatafile("mog", dft1, dfsout);
		//
		// Application application = session.createApplication("The one",
		// "1.0");
		// Job job = session.createJob(application);
		// session.addInputDataset(job, wibble);
		// session.addOutputDataset(job, wobble);
		// session.addInputDatafile(job, fred);
		// session.addInputDatafile(job, bill);
		// session.addOutputDatafile(job, mog);

	}

	@BeforeClass
	public static void setup() throws Exception {
		session = new Session();
		try {
			session.setAuthz();
		} catch (Exception e) {
			System.out.println(e);
		}
		session.clearAuthz();
		session.setAuthz();
		session.clear();
	}

	@AfterClass
	public static void zap() throws Exception {
		// session.clear();
		// session.clearAuthz();
	}

}
