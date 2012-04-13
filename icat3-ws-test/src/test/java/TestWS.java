import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.icatproject.Application;
import org.icatproject.BadParameterException_Exception;
import org.icatproject.Constraint;
import org.icatproject.Datafile;
import org.icatproject.DatafileFormat;
import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.DatasetType;
import org.icatproject.DestType;
import org.icatproject.EntityField;
import org.icatproject.EntityInfo;
import org.icatproject.Facility;
import org.icatproject.InputDatafile;
import org.icatproject.InputDataset;
import org.icatproject.Investigation;
import org.icatproject.InvestigationParameter;
import org.icatproject.InvestigationType;
import org.icatproject.Job;
import org.icatproject.KeyType;
import org.icatproject.NoSuchObjectFoundException_Exception;
import org.icatproject.OutputDatafile;
import org.icatproject.OutputDataset;
import org.icatproject.ParameterType;
import org.icatproject.ParameterValueType;
import org.icatproject.PermissibleStringValue;
import org.icatproject.RelType;
import org.icatproject.Sample;
import org.icatproject.SampleParameter;
import org.icatproject.ValidationException_Exception;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestWS {

	private static Session session;
	private static Random random;

	private static void create() throws Exception {

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A",
				"Not null", investigationType);

		ParameterType p = new ParameterType();
		p.setName("TIMESTAMP");
		p.setUnits("TIMESTAMP");
		p.setDescription("F is not a wibble");
		p.setApplicableToDataset(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		p.setFacility(facility);
		p.setId((Long) session.create(p));

		Dataset wibble = session.createDataset("Wibble", dst, inv);

		DatafileFormat dft1 = session.createDatafileFormat(facility, "png",
				"binary");
		DatafileFormat dft2 = session.createDatafileFormat(facility, "bmp",
				"binary");

		session.createDatafile("wib1", dft1, wibble);
		session.createDatafile("wib2", dft2, wibble);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(c);
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

		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A",
				"Not null", investigationType);

		Dataset wibble = session.createDataset("Wibble", dst, inv);
		Dataset wobble = session.createDataset("Wobble", dst, inv);

		DatafileFormat dfmt = session.createDatafileFormat(facility, "png",
				"binary");

		Datafile df = session.createDatafile("fred", dfmt, wibble);
		df = (Datafile) session.get("Datafile INCLUDE Dataset, DatafileFormat",
				df.getId());
		assertEquals("Wibble", df.getDataset().getName());

		df.setDataset(wobble);
		df.setLocation("guess");
		df.setDatafileFormat(session.createDatafileFormat(facility, "notpng",
				"notbinary"));
		df.setDatafileFormat(null);
		df.setFileSize(-1L);
		session.update(df);
		df = (Datafile) session.get("Datafile INCLUDE Dataset,DatafileFormat",
				df.getId());
		assertEquals("Wobble", df.getDataset().getName());

	}

	@Test
	public void testInvestigation() throws Exception {
		EntityInfo ei = session.getEntityInfo("Investigation");
		assertEquals("An investigation or experiment", ei.getClassComment());
		assertEquals("id", ei.getKeyFieldname());
		assertEquals(KeyType.GENERATED, ei.getKeyType());
		for (Constraint constraint : ei.getConstraints()) {
			assertEquals(Arrays.asList("name", "visitId", "facilityCycle",
					"instrument"), constraint.getFieldNames());
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
				assertEquals("Full title of the investigation",
						field.getComment());
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
		session.createNotificationRequest("A", DestType.P_2_P, "Facility", "C",
				"ptp", "notificationName userId entityName entityKey callArgs");
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
		assertEquals("No params", 0, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session
				.search("Dataset INCLUDE Datafile [id = " + dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE DatasetParameter [id = "
				+ dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("No Files", 0, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session
				.search("Dataset INCLUDE Datafile, DatasetParameter [id = "
						+ dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session
				.search("Dataset INCLUDE Datafile, DatasetParameter, Investigation [id = "
						+ dsid + "]");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Value", dsid, ds.getId());
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
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

		results = session
				.search("Job INCLUDE InputDataset, InputDatafile, Dataset, Datafile");
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

	private Dataset addDataset(Investigation inv, String name, DatasetType type) {
		Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		inv.getDatasets().add(dataset);
		return dataset;
	}

	private Datafile addDatafile(Dataset dataset, String name,
			DatafileFormat format) {
		Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		dataset.getDatafiles().add(datafile);
		return datafile;
	}

	private DatasetParameter addDatasetParameter(Dataset dataset, Object o,
			ParameterType p) {
		DatasetParameter dsp = new DatasetParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) o);
		}
		dsp.setType(p);
		dataset.getParameters().add(dsp);
		return dsp;
	}

	private SampleParameter addSampleParameter(Sample sample, Object o,
			ParameterType p) {
		SampleParameter sp = new SampleParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			sp.setDateTimeValue((XMLGregorianCalendar) o);
		}
		sp.setType(p);
		sample.getParameters().add(sp);
		return sp;
	}

	@Test
	public void numericParameterRanges() throws Exception {
		session.clear();
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A",
				"Not null", investigationType);

		ParameterType ptn = new ParameterType();
		ptn.setName("TestNumeric");
		ptn.setApplicableToInvestigation(true);
		ptn.setValueType(ParameterValueType.NUMERIC);
		ptn.setFacility(facility);
		ptn.setEnforced(true);
		ptn.setMinimumNumericValue(50.);
		ptn.setId((Long) session.create(ptn));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(ptn);
		ip.setInvestigation(inv);

		try {
			ip.setNumericValue(40.);
			session.create(ip);
			fail("No throw");
		} catch (ValidationException_Exception e) {
			System.out.println(e.getMessage());
		}

		ip.setNumericValue(60.);
		ip.setId((Long) session.create(ip));

		try {
			ip.setNumericValue(30.);
			session.update(ip);
			fail("No throw");
		} catch (ValidationException_Exception e) {
			System.out.println(e.getMessage());
		}

		ip.setNumericValue(70.);
		session.update(ip);

	}

	@Test
	public void inapplicableParameterType() throws Exception {
		session.clear();
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A",
				"Not null", investigationType);

		ParameterType pts = new ParameterType();
		pts.setName("UselessString");
		pts.setValueType(ParameterValueType.STRING);
		pts.setFacility(facility);
		pts.setId((Long) session.create(pts));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(pts);
		ip.setInvestigation(inv);

		try {
			ip.setStringValue("bad");
			session.create(ip);
			fail("No throw");
		} catch (ValidationException_Exception e) {
			assertEquals(
					"Parameter of type UselessString is not applicable to an Investigation",
					e.getMessage());
		}
	}

	@Test
	public void stringParameterRanges() throws Exception {
		session.clear();
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A",
				"Not null", investigationType);

		ParameterType pts = new ParameterType();
		pts.setName("TestString");
		pts.setApplicableToInvestigation(true);
		pts.setValueType(ParameterValueType.STRING);
		pts.setFacility(facility);
		pts.setEnforced(true);
		PermissibleStringValue psv = new PermissibleStringValue();
		psv.setValue("good1");
		pts.getPermissibleStringValues().add(psv);
		psv = new PermissibleStringValue();
		psv.setValue("good2");
		pts.getPermissibleStringValues().add(psv);
		pts.setId((Long) session.create(pts));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(pts);
		ip.setInvestigation(inv);

		try {
			ip.setStringValue("bad");
			session.create(ip);
			fail("No throw");
		} catch (ValidationException_Exception e) {
			System.out.println(e.getMessage());
		}

		ip.setStringValue("good1");
		ip.setId((Long) session.create(ip));

		try {
			ip.setStringValue("worse");
			session.update(ip);
			fail("No throw");
		} catch (ValidationException_Exception e) {
			System.out.println(e.getMessage());
		}

		ip.setStringValue("good2");
		session.update(ip);
	}

	@Test
	public void bigCreate() throws Exception {
		session.clear();

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session
				.createInvestigationType("TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		ParameterType p = new ParameterType();
		p.setName("TIMESTAMP");
		p.setUnits("TIMESTAMP");
		p.setDescription("F is not a wibble");
		p.setApplicableToSample(true);
		p.setApplicableToDataset(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		p.setFacility(facility);
		p.setId((Long) session.create(p));

		DatafileFormat dft1 = session.createDatafileFormat(facility, "png",
				"binary");
		DatafileFormat dft2 = session.createDatafileFormat(facility, "bmp",
				"binary");

		Investigation inv = new Investigation();
		inv.setId(42L);
		inv.setFacility(facility);
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(investigationType);

		final Dataset wibble = addDataset(inv, "Wibble", dst);

		addDatafile(wibble, "wib1", dft1);

		addDatafile(wibble, "wib2", dft2);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(c);

		addDatasetParameter(wibble, date, p);

		Dataset wobble = addDataset(inv, "Wobble", dst);
		addDatafile(wobble, "wob1", dft1);

		Sample sample = addSample(inv, "S1");
		addSample(inv, "S2");

		addSampleParameter(sample, date, p);

		session.registerInvestigation(inv);
		// TODO try to allow this
		// inv = (Investigation)
		// session.get("Investigation INCLUDE Dataset, Datafile, DatasetParameter, Facility, Sample, SampleParameter",
		// inv.getId());
		inv = (Investigation) session.get(
				"Investigation INCLUDE  Sample, SampleParameter", inv.getId());
		assertEquals(2, inv.getSamples().size());
		for (Sample s : inv.getSamples()) {
			if (s.getName().equals("S1")) {
				assertEquals(1, s.getParameters().size());
			} else if (s.getName().equals("S2")) {
				assertEquals(0, s.getParameters().size());
			} else {
				fail("Neither S1 nor S2");
			}
		}

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

	private Sample addSample(Investigation inv, String sampleName) {
		Sample sample = new Sample();
		sample.setName(sampleName);
		inv.getSamples().add(sample);
		return sample;

	}

	@Test
	public void gets() throws Exception {
		session.clear();
		create();
		Long dsId = (Long) session.search("Dataset.id [name = 'Wibble']")
				.get(0);
		assertEquals("Wibble",
				((Dataset) session.get("Dataset", dsId)).getName());
		try {
			session.get("Dataset", random.nextLong());
			fail("No throw");
		} catch (NoSuchObjectFoundException_Exception e) {
			// Nothing to do
		}
		try {
			session.get("Dataset INCLUDE Investigator", dsId);
			fail("No throw");
		} catch (BadParameterException_Exception e) {
			assertEquals(
					"uk.icat3.entity.Investigator is not known to the class loader",
					e.getMessage());
		}
		try {
			session.get("Dataset INCLUDE User", dsId);
			fail("No throw");
		} catch (BadParameterException_Exception e) {
			assertEquals("Unable to reach User", e.getMessage());
		}

	}

	@Test
	public void searches() throws Exception {
		session.clear();
		create();

		Long invId = (Long) session.search("Investigation.id").get(0);

		List<?> results = session
				.search("Dataset.id "
						+ "<-> DatasetParameter[type.name = 'TIMESTAMP'] "
						+ "<-> Investigation[name <> 12]");
		assertEquals("Count", 1, results.size());

		results = session
				.search("Datafile [name = 'fred'] <-> Dataset[id <> 42]");
		assertEquals("Count", 1, results.size());

		String query = "Dataset.id  ORDER BY id [type.name IN :types] <-> Investigation[id BETWEEN :lower AND :upper]";

		query = query.replace(":lower", Long.toString(invId))
				.replace(":upper", Long.toString(invId))
				.replace(":types", "('GS', 'GQ')");
		results = session.search(query);
		assertEquals("Count", 4, results.size());

		query = "Dataset.id ORDER BY startDate [type.name IN :types AND name >= :lower AND name <= :upper]";
		query = query.replace(":lower", "'Wabble'")
				.replace(":upper", "'Wobble'")
				.replace(":types", "('GS', 'GQ')");
		results = session.search(query);
		assertEquals("Count", 2, results.size());

		query = "ParameterType.name [description LIKE 'F%']";
		results = session.search(query);
		assertEquals("Count", 1, results.size());
		assertEquals("TIMESTAMP", results.get(0));

		results = session.search("Datafile.name ORDER BY id");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "wib1", results.get(0));
		assertEquals("Result", "wib2", results.get(1));

		results = session.search(",1 Datafile.name ORDER BY id");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "wib1", results.get(0));
		
		results = session.search("1, Datafile.name ORDER BY id");
		assertEquals("Count", 5, results.size());
		assertEquals("Result", "wib2", results.get(0));
		
		results = session.search("1,1 Datafile.name ORDER BY id");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "wib2", results.get(0));
		
		results = session.search("100,1 Datafile.name ORDER BY id");
		assertEquals("Count", 0, results.size());
		
		results = session.search("0,100 Datafile.name ORDER BY id");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "wib1", results.get(0));
		assertEquals("Result", "wib2", results.get(1));
	}

	@BeforeClass
	public static void setup() throws Exception {
		random = new Random();
		session = new Session();
		session.setAuthz();
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
