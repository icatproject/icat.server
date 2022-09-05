package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.icatproject.AccessType;
import org.icatproject.Application;
import org.icatproject.AuthenticatorCredentialKey;
import org.icatproject.AuthenticatorInfo;
import org.icatproject.Constraint;
import org.icatproject.DataCollection;
import org.icatproject.Datafile;
import org.icatproject.DatafileFormat;
import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.DatasetType;
import org.icatproject.EntityBaseBean;
import org.icatproject.EntityField;
import org.icatproject.EntityInfo;
import org.icatproject.Facility;
import org.icatproject.FieldSet;
import org.icatproject.Grouping;
import org.icatproject.IcatException;
import org.icatproject.IcatExceptionType;
import org.icatproject.IcatException_Exception;
import org.icatproject.Instrument;
import org.icatproject.Investigation;
import org.icatproject.InvestigationParameter;
import org.icatproject.InvestigationType;
import org.icatproject.InvestigationUser;
import org.icatproject.Job;
import org.icatproject.ParameterType;
import org.icatproject.ParameterValueType;
import org.icatproject.PermissibleStringValue;
import org.icatproject.PublicStep;
import org.icatproject.RelType;
import org.icatproject.Rule;
import org.icatproject.Sample;
import org.icatproject.SampleParameter;
import org.icatproject.SampleType;
import org.icatproject.Study;
import org.icatproject.User;
import org.icatproject.UserGroup;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.utils.ContainerGetter.ContainerType;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestWS {

	private static final String version = "5.0.";
	private static Random random;
	private static WSession session;

	@BeforeClass
	public static void beforeClass() throws Exception {
		try {
			random = new Random();
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
		session.clear();
		session.clearAuthz();
	}

	private static void create() throws Exception {

		Facility facility = session.createFacility("Test Facility", 90);

		SampleType sampleType = new SampleType();
		sampleType.setFacility(facility);
		sampleType.setName("somename");
		sampleType.setMolecularFormula("Someformula");
		session.create(sampleType);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation invA = session.createInvestigation(facility, "A", "Not null", investigationType);

		Investigation invB = session.createInvestigation(facility, "B", "Not null", investigationType);

		Investigation invC = session.createInvestigation(facility, "C", "Not null", investigationType);

		Instrument wish = session.createInstrument(facility, "WISH");
		Instrument bone = session.createInstrument(facility, "BONE");

		Study study = session.createStudy("long");
		session.createStudyInvestigation(study, invA);

		session.createInvestigationInstrument(invA, wish);
		session.createInvestigationInstrument(invA, bone);
		session.createInvestigationInstrument(invB, wish);

		User notroot = (User) session.search("User[name='db/notroot']").get(0);
		session.createInstrumentScientist(wish, notroot);

		session.createInvestigationUser(invA, notroot, "A role");
		session.createInvestigationUser(invC, notroot, "Another role");

		ParameterType p = new ParameterType();
		p.setName("TIMESTAMP");
		p.setUnits("TIMESTAMP");
		p.setDescription("F is not a wibble");
		p.setApplicableToDataset(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		p.setFacility(facility);
		p.setId((Long) session.create(p));

		Dataset wibble = session.createDataset("Wibble", dst, invA);

		DatafileFormat dft1 = session.createDatafileFormat(facility, "png", "binary");
		DatafileFormat dft2 = session.createDatafileFormat(facility, "bmp", "binary");

		session.createDatafile("wib1", dft1, wibble, 5L);
		session.createDatafile("wib2", dft2, wibble, 7L);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		session.createDatasetParameter(date, p, wibble);

		Dataset wobble = session.createDataset("Wobble", dst, invA);
		session.createDatafile("wob1", dft1, wobble, 0L);

		Dataset dfsin = session.createDataset("dfsin", dst, invA);
		Datafile fred = session.createDatafile("fred", dft1, dfsin, 11L);
		Datafile bill = session.createDatafile("bill", dft1, dfsin, 17L);

		Dataset dfsout = session.createDataset("dfsout", dst, invA);
		Datafile mog = session.createDatafile("mog", dft1, dfsout, 11L);

		Application application = session.createApplication(facility, "The one", "1.0");

		DataCollection input = session.createDataCollection(wibble, fred, bill);
		DataCollection output = session.createDataCollection(wobble, mog);

		session.createJob(application, input, output);
	}

	private Datafile addDatafile(Dataset dataset, String name, DatafileFormat format) {
		Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		dataset.getDatafiles().add(datafile);
		return datafile;
	}

	private Dataset addDataset(Investigation inv, String name, DatasetType type) {
		Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		inv.getDatasets().add(dataset);
		return dataset;
	}

	private DatasetParameter addDatasetParameter(Dataset dataset, Object o, ParameterType p) {
		DatasetParameter dsp = new DatasetParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) o);
		}
		dsp.setType(p);
		dataset.getParameters().add(dsp);
		return dsp;
	}

	private Sample addSample(Investigation inv, String sampleName) {
		Sample sample = new Sample();
		sample.setName(sampleName);
		inv.getSamples().add(sample);
		return sample;
	}

	private SampleParameter addSampleParameter(Sample sample, Object o, ParameterType p) {
		SampleParameter sp = new SampleParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			sp.setDateTimeValue((XMLGregorianCalendar) o);
		}
		sp.setType(p);
		sample.getParameters().add(sp);
		return sp;
	}

	@Test
	public void publicTable() throws Exception {
		session.addRule(null, "DatafileFormat", "R");
		session.addRule(null, "DatasetType", "R");
		session.delRule(null, "DatafileFormat", "R");
		session.delRule(null, "DatasetType", "R");
	}

	@Test
	public void publicStep() throws Exception {
		PublicStep ps = new PublicStep();
		ps.setOrigin("Application");
		ps.setField("jobs");
		session.create(ps);

		ps = new PublicStep();
		ps.setOrigin("Applicatio");
		ps.setField("jobs");
		try {
			session.create(ps);
			fail("Should have thrown exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Applicatio is not an EntityBaseBean", e.getMessage());
		}

		ps = new PublicStep();
		ps.setOrigin("Application");
		ps.setField("Jobs");
		try {
			session.create(ps);
			fail("Should have thrown exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Field value Jobs does not implement a relationship from Application", e.getMessage());
		}
	}

	@Test
	public void entities() throws Exception {
		List<String> entities = session.getEntityNames();
		assertEquals(52, entities.size());
		assertTrue(entities.contains("Application"));
	}

	@Test
	public void authz() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dstPB = session.createDatasetType(facility, "PB");
		DatasetType dstQQ = session.createDatasetType(facility, "QQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		Dataset dsPB = session.createDataset("PB", dstPB, inv);
		Dataset dsQQ = session.createDataset("QQ", dstQQ, inv);

		DatafileFormat dfmt = session.createDatafileFormat(facility, "png", "binary");

		session.createDatafile("PB", dfmt, dsPB, 3L);
		session.createDatafile("QQ", dfmt, dsQQ, 7L);

		String q1 = "COUNT(Dataset.name) [type.name = 'PB'] <-> Datafile[name = 'PB']";
		String q2 = "COUNT(Dataset.name)";
		String q3 = "COUNT(Dataset)";

		assertEquals(1L, session.search(q1).get(0));
		assertEquals(2L, session.search(q2).get(0));
		assertEquals(2L, session.search(q3).get(0));

		try {
			session.delRule("notroot", "Dataset", "CRUD");
			// The space between the single quotes is necessary - I suspect a
			// bug in eclipselink

			session.addRule("notroot", "Dataset <-> DatasetType [name = ' ']", "R");

			assertEquals(0L, session.search(q1).get(0));
			assertEquals(0L, session.search(q2).get(0));
			assertEquals(0L, session.search(q3).get(0));

			session.delRule("notroot", "Dataset <-> DatasetType [name = ' ']", "R");
			session.addRule("notroot", "Dataset <-> DatasetType[name = 'PB']", "R");

			assertEquals(1L, session.search(q1).get(0));
			assertEquals(1L, session.search(q2).get(0));
			assertEquals(1L, session.search(q3).get(0));

			session.delRule("notroot", "Dataset <-> DatasetType[name = 'PB']", "R");
		} finally {
			session.addRule("notroot", "Dataset", "CRUD");
		}
	}

	@Test
	public void authz2() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		session.addRule(null, "Investigation <-> InvestigationUser <-> User [name = :user]", "R");

		User piOne = new User();
		piOne.setName("db/piOne");
		piOne.setId(session.create(piOne));

		User piTwo = new User();
		piTwo.setName("db/piTwo");
		piTwo.setId(session.create(piTwo));

		Investigation invOne = session.createInvestigation(facility, "InvestigationOne", "Investigation one",
				investigationType);

		Investigation invTwo = session.createInvestigation(facility, "InvestigationTwo", "Investigation two",
				investigationType);

		InvestigationUser iuOne = new InvestigationUser();
		iuOne.setInvestigation(invOne);
		iuOne.setUser(piOne);
		iuOne.setRole("Principal Investigator");
		iuOne.setId(session.create(iuOne));

		InvestigationUser iuTwo = new InvestigationUser();
		iuTwo.setInvestigation(invTwo);
		iuTwo.setUser(piTwo);
		iuTwo.setRole("Principal Investigator");
		iuTwo.setId(session.create(iuTwo));

		WSession piOneSession = session.getSession("db", "username", "piOne", "password", "piOne");
		WSession piTwoSession = session.getSession("db", "username", "piTwo", "password", "piTwo");

		List<Object> invsOne = piOneSession.search("Investigation");
		assertEquals(1, invsOne.size());
		assertEquals("InvestigationOne", ((Investigation) invsOne.get(0)).getName());

		List<Object> invsTwo = piTwoSession.search("Investigation");
		assertEquals(1, invsTwo.size());
		assertEquals("InvestigationTwo", ((Investigation) invsTwo.get(0)).getName());

		// Create new user for PIs to relate to his investigation
		User aone = new User();
		aone.setName("aone");
		aone.setId(session.create(aone));

		Grouping oneControllers = new Grouping();
		oneControllers.setName("OneControllers");
		oneControllers.setId(session.create(oneControllers));

		UserGroup userGroup = new UserGroup();
		userGroup.setUser(piOne);
		userGroup.setGrouping(oneControllers);
		session.create(userGroup);

		session.addRule(oneControllers.getName(), "InvestigationUser <-> Investigation [name = 'InvestigationOne']",
				"CRUD");

		try {
			InvestigationUser iuaone = new InvestigationUser();
			iuaone.setInvestigation(invTwo);
			iuaone.setUser(aone);
			iuaone.setId(piOneSession.create(iuaone));
			fail("Should not get here as can't grant access to inv two");
		} catch (Exception e) {
		}

		{
			InvestigationUser iuaone = new InvestigationUser();
			iuaone.setInvestigation(invOne);
			iuaone.setUser(aone);
			iuaone.setRole("PI");
			iuaone.setId(piOneSession.create(iuaone));
		}

		assertEquals(2, (piOneSession.search("InvestigationUser")).size());
		assertEquals(0, (piTwoSession.search("InvestigationUser")).size());

		// Create a simple rule allowing oneControllers full access to
		// InvestigationUser and ensure
		// that reading works.
		session.addRule(oneControllers.getName(), "InvestigationUser", "CRUD");

		assertEquals(3, (piOneSession.search("InvestigationUser")).size());

		try {
			piTwoSession.search("InvestigationUser");
		} catch (Exception e) {
		}

		// User has no read perms on objects of this type at all, some objects
		// exist
		List<Object> results = piTwoSession.search("SELECT COUNT(i) FROM Rule i");
		assertEquals(1, results.size());
		assertEquals(0L, results.get(0));

		results = piTwoSession.search("SELECT SUM(i.id) FROM Rule i");
		assertEquals(0, results.size());

		// User has no read perms on objects of this type at all, no objects
		// exist
		results = piTwoSession.search("SELECT COUNT(i) FROM DataCollection i");
		assertEquals(1, results.size());
		assertEquals(0L, results.get(0));
	}

	/**
	 * To test with the case when an operation is allowed by a general rule but
	 * an unrestricted rule also exists. This used to give a problem if the
	 * unrestricted rule was not processed first.
	 */
	@Test
	public void authz3() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		List<Object> objects = session.search("Rule [bean='Investigation']<-> Grouping[name='root']");
		for (Object o : objects) {
			Rule r = (Rule) o;
			Grouping g = r.getGrouping();
			String gname = (g == null) ? null : g.getName();
			session.delRule(gname, r.getWhat(), r.getCrudFlags());
		}

		Grouping rootG = (Grouping) session.search("Grouping [name='root']").get(0);

		session.addRule(rootG.getName(), "Investigation", "C");
		session.addRule(null, "Investigation <-> InvestigationUser <-> User [name = :user]", "R");

		session.createInvestigation(facility, "InvestigationOne", "Investigation one", investigationType);

		session.createInvestigation(facility, "InvestigationTwo", "Investigation two", investigationType);

		session.addRule(rootG.getName(), "Investigation", "CRUD");

		assertEquals(2, (session.search("Investigation")).size());
	}

	@Test
	public void badRule() throws Exception {
		try {
			session.addRule(null, "SELECT i FROM Investigation i where i.investigationInstrument IS NULL", "C");
			fail("Should have thrown exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().startsWith("An exception occurred while creating a query in EntityManager:")
					| e.getMessage().startsWith("org.hibernate.QueryException"));
		}
	}

	@Test
	public void goodRuleWithTime() throws Exception {
		session.addRule(null, "SELECT i FROM Investigation i where i.modTime = {ts 1950-01-21 06:00:00}", "C");
		session.delRule(null, "SELECT i FROM Investigation i where i.modTime = {ts 1950-01-21 06:00:00}", "C");
	}

	@Test
	public void authz4() throws Exception {
		// Samples - via investigation

		session.addRule(null,
				"SELECT s FROM Sample s JOIN s.investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
				"CRU");

		// Samples - via dataset
		session.addRule(null,
				"SELECT s FROM Sample AS s JOIN s.datasets AS ds JOIN ds.investigation AS i JOIN i.investigationInstruments AS ii JOIN ii.instrument AS inst JOIN inst.instrumentScientists AS instSci JOIN instSci.user user WHERE user.name = :user",
				"CRU");

		// Test
		session.search(
				"SELECT COUNT(s) FROM Sample AS s JOIN s.datasets as ds JOIN ds.investigation AS i JOIN i.investigationInstruments AS ii JOIN ii.instrument AS inst WHERE (inst.name = 'WISH')");

		// Clean up
		session.delRule(null,
				"SELECT s FROM Sample s JOIN s.investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
				"CRU");
		session.delRule(null,
				"SELECT s FROM Sample AS s JOIN s.datasets AS ds JOIN ds.investigation AS i JOIN i.investigationInstruments AS ii JOIN ii.instrument AS inst JOIN inst.instrumentScientists AS instSci JOIN instSci.user user WHERE user.name = :user",
				"CRU");
	}

	@Test
	public void authz5() throws Exception {
		session.delRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		try {
			session.addRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");

			session.search(
					"SELECT COUNT(i) FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst WHERE inst.name='WISH'");
			session.delRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");
		} finally {
			session.addRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		}
	}

	@Test
	public void authz6() throws Exception {
		create();
		session.delRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		try {
			// Create a rule for me as an Instrument Scientist on WISH
			System.out.println("Adding InstrumentScientist Rule");
			session.addRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");

			assertEquals(2L, session.search("SELECT COUNT(i) FROM Investigation i").get(0));
			checkInvestigationNames("Investigation ORDER BY name", "A", "B");

			assertEquals(2L, session
					.search("COUNT(Investigation) <-> InvestigationInstrument <-> Instrument[name='WISH']").get(0));
			checkInvestigationNames(
					"Investigation ORDER BY name <-> InvestigationInstrument <-> Instrument[name='WISH']", "A", "B");

			// Delete the rule
			session.delRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");

			// Create a rule so I can see 'My' Investigations (those where I am
			// an investigator)
			session.addRule(null, "Investigation <-> InvestigationUser <-> User [name = :user]", "R");

			assertEquals(2L, session.search("SELECT COUNT(i) FROM Investigation i").get(0));
			checkInvestigationNames("Investigation ORDER BY name", "A", "C");

			assertEquals(2L, session.search("COUNT(Investigation) <-> InvestigationUser <-> User[name=:user]").get(0));
			checkInvestigationNames("Investigation ORDER BY name <-> InvestigationUser <-> User[name=:user]", "A", "C");

			assertEquals(1L, session
					.search("COUNT(Investigation) <-> InvestigationInstrument <-> Instrument[name='WISH']").get(0));
			checkInvestigationNames("Investigation <-> InvestigationInstrument <-> Instrument[name='WISH']", "A");

			// Add the InstrumentScientist rule back in.
			session.addRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");

			assertEquals(3L, session.search("SELECT COUNT(i) FROM Investigation i").get(0));
			checkInvestigationNames("Investigation ORDER BY name", "A", "B", "C");

			assertEquals(2L, session
					.search("COUNT(Investigation) <-> InvestigationInstrument <-> Instrument[name='WISH']").get(0));
			checkInvestigationNames(
					"Investigation  ORDER BY name<-> InvestigationInstrument <-> Instrument[name='WISH']", "A", "B");

			assertEquals(2L, session.search("COUNT(Investigation) <-> InvestigationUser <-> User[name=:user]").get(0));
			checkInvestigationNames("Investigation ORDER BY name <-> InvestigationUser <-> User[name=:user]", "A", "C");

			session.delRule(null,
					"SELECT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst JOIN inst.instrumentScientists instSci JOIN instSci.user user WHERE user.name = :user",
					"CRU");
			session.delRule(null, "Investigation <-> InvestigationUser <-> User [name = :user]", "R");
		} finally {
			session.addRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		}
	}

	/*
	 * Create two rules allowing access to the same investigation and ensure
	 * that only one instance is returned - i.e, no DISTINCT problem remains
	 */
	@Test
	public void authz7() throws Exception {
		try {
			create();

			session.addRule(null,
					"Datafile <-> Dataset <-> Investigation <-> InvestigationUser <-> User [name = :user]", "R");
			session.addRule(null, "Datafile <-> Dataset  [name = 'dfsin']", "R");

			User piOne = new User();
			piOne.setName("db/piOne");
			piOne.setId(session.create(piOne));

			List<Object> invAs = session.search("Investigation [name = 'A']");
			assertEquals(1, invAs.size());
			Investigation invOne = (Investigation) invAs.get(0);

			InvestigationUser iuOne = new InvestigationUser();
			iuOne.setInvestigation(invOne);
			iuOne.setUser(piOne);
			iuOne.setRole("Principal Investigator");
			iuOne.setId(session.create(iuOne));

			WSession piOneSession = session.getSession("db", "username", "piOne", "password", "piOne");

			List<Object> freds = piOneSession.search("SELECT DISTINCT df FROM Datafile df");

			assertEquals(6, freds.size());

			assertEquals(51L, piOneSession.search("SELECT SUM(df.fileSize) from Datafile df").get(0));
			assertEquals(40L, piOneSession.search("SELECT SUM(DISTINCT df.fileSize) from Datafile df").get(0));
			assertEquals(6L, piOneSession.search("SELECT COUNT(df.fileSize) from Datafile df").get(0));
			assertEquals(5L, piOneSession.search("SELECT COUNT(DISTINCT df.fileSize) from Datafile df").get(0));
			assertEquals(6L, piOneSession.search("SELECT COUNT(df) from Datafile df").get(0));
			assertEquals(6L, piOneSession.search("SELECT COUNT(DISTINCT df) from Datafile df").get(0));
			assertEquals(17L, piOneSession.search("SELECT MAX(df.fileSize) from Datafile df").get(0));
			assertEquals(17L, piOneSession.search("SELECT MAX(DISTINCT df.fileSize) from Datafile df").get(0));
			assertEquals(0L, piOneSession.search("SELECT MIN(df.fileSize) from Datafile df").get(0));
			assertEquals(0L, piOneSession.search("SELECT MIN(DISTINCT df.fileSize) from Datafile df").get(0));
		} finally {
			session.setAuthz();
		}
	}

	@Test
	public void authzForUpdate() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dstX = session.createDatasetType(facility, "X");
		DatasetType dstY = session.createDatasetType(facility, "Y");

		Investigation invA = session.createInvestigation(facility, "A", "Not null", investigationType);
		Investigation invB = session.createInvestigation(facility, "B", "Not null", investigationType);

		Dataset ds = session.createDataset("A1", dstX, invA);

		/* Initially can read datasets from investigations A and B */
		session.delRule("notroot", "Dataset", "CRUD");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "R");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'B']", "R");
		ds = (Dataset) session.search("SELECT ds FROM Dataset ds WHERE ds.investigation.name = 'A' INCLUDE 1")
				.get(0);
		ds.setName("A2");
		try {
			session.update(ds);
			fail();
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getFaultInfo().getType());
			assertEquals("UPDATE access to this Dataset is not allowed.", e.getMessage());
		}

		/*
		 * Permissions were insufficient to change an attribute value so
		 * change to allow update for data sets from investigation A
		 */
		session.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "R");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "U");
		session.update(ds);

		/*
		 * Check that non-defining relationship fields can also be updated
		 */
		ds.setType(dstY);
		session.update(ds);

		/* Changing a defining relationship field will fail however */

		ds.setInvestigation(invB);
		try {
			session.update(ds);
			fail();
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getFaultInfo().getType());
			assertEquals("DELETE access to this Dataset is not allowed.", e.getMessage());
		}

		/*
		 * This effectively does a delete from investigation A so permit
		 * this
		 */
		session.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "U");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
		try {
			session.update(ds);
			fail();
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getFaultInfo().getType());
			assertEquals("CREATE access to this Dataset is not allowed.", e.getMessage());
		}

		/*
		 * But it also is effectively a create in investigation B. So add
		 * create to A - which won't work of course
		 */
		session.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "CUD");
		try {
			session.update(ds);
			fail();
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getFaultInfo().getType());
			assertEquals("CREATE access to this Dataset is not allowed.", e.getMessage());
		}

		/* So now add create to B - and it works */
		session.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "CUD");
		session.delRule("notroot", "Dataset <-> Investigation [name = 'B']", "R");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
		session.addRule("notroot", "Dataset <-> Investigation [name = 'B']", "C");
		session.update(ds);
	}

	private void checkInvestigationNames(String query, String... names) throws IcatException_Exception {
		List<Object> objects = session.search(query);
		assertEquals(names.length, objects.size());
		int i = 0;
		for (Object o : objects) {
			assertEquals(names[i++], ((Investigation) o).getName());
		}
	}

	@Ignore
	@Test
	public void manyBigGets() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		Dataset wibble = session.createDataset("Wibble", dst, inv);

		DatafileFormat dfmt = session.createDatafileFormat(facility, "png", "binary");

		int n = 7500;

		List<EntityBaseBean> dfs = new ArrayList<EntityBaseBean>();
		for (int i = 0; i < n; i++) {
			final Datafile datafile = new Datafile();
			datafile.setDatafileFormat(dfmt);
			datafile.setName("bill" + i);
			datafile.setDataset(wibble);
			dfs.add(datafile);
		}
		long start = System.currentTimeMillis();
		session.createMany(dfs);
		System.out.println(
				"Time per datafile using createMany: " + (System.currentTimeMillis() - start) / (n + 0.) + "ms");

		start = System.currentTimeMillis();

		Long wibid = wibble.getId();
		Dataset ds = null;
		int m = 5;
		for (int i = 0; i < m; i++) {
			ds = (Dataset) session.get("Dataset INCLUDE Datafile, DatafileFormat", wibid);
			System.out.print(i);
			System.out.flush();
		}

		System.out.println("Time per datafile to retrieve: " + ds.getDatafiles().size() + " datafiles "
				+ (System.currentTimeMillis() - start) / (ds.getDatafiles().size() * m) + "ms");

	}

	@Test
	public void bigCreate() throws Exception {
		session.delRule("notroot", "SampleType", "CRUD");
		session.delRule("notroot", "Sample", "CRUD");
		session.delRule("notroot", "PublicStep", "CRUD");

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

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

		DatafileFormat dft1 = session.createDatafileFormat(facility, "png", "binary");
		DatafileFormat dft2 = session.createDatafileFormat(facility, "bmp", "binary");

		Investigation inv = new Investigation();
		inv.setId(42L);
		inv.setFacility(facility);
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(investigationType);
		inv.setVisitId("57");

		final Dataset wibble = addDataset(inv, "Wibble", dst);

		addDatafile(wibble, "wib1", dft1);

		addDatafile(wibble, "wib2", dft2);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		addDatasetParameter(wibble, date, p);

		Dataset wobble = addDataset(inv, "Wobble", dst);
		addDatafile(wobble, "wob1", dft1);

		Sample sample = addSample(inv, "S1");
		addSample(inv, "S2");

		addSampleParameter(sample, date, p);

		session.registerInvestigation(inv);

		inv = (Investigation) session.get("Investigation INCLUDE  Sample, SampleParameter", inv.getId());
		assertEquals(0, inv.getSamples().size());

		session.addRule("notroot", "Sample", "R");

		inv = (Investigation) session.get("Investigation INCLUDE  Sample, SampleParameter", inv.getId());
		assertEquals(2, inv.getSamples().size());
		for (Sample s : inv.getSamples()) {
			assertEquals(0, s.getParameters().size());
		}

		session.addRule("notroot", "SampleParameter", "R");

		inv = (Investigation) session.get("Investigation INCLUDE  Sample, SampleParameter", inv.getId());
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
	}

	@Test
	public void createCascade() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		List<Object> objects = session.search("User [name = 'db/root']");
		User u = (User) objects.get(0);
		InvestigationUser iu = new InvestigationUser();
		iu.setUser(u);
		iu.setRole("PI");
		Investigation i = new Investigation();
		i.setFacility(facility);
		i.setName("Frederick");
		i.setTitle("the Great");
		i.setVisitId("42");
		i.setType(investigationType);
		i.getInvestigationUsers().add(iu);
		session.create(i);
		objects = session.search("Investigation INCLUDE InvestigationUser, User [name='Frederick']");
		assertEquals(1, objects.size());
		i = (Investigation) objects.get(0);
		assertEquals(1, i.getInvestigationUsers().size());
		assertEquals("db/root", i.getInvestigationUsers().get(0).getUser().getName());
	}

	@Test
	public void duplicates() throws Exception {
		Facility f = new Facility();
		f.setName("TestDuplicates");
		session.create(f);
		try {
			session.create(f);
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			assertEquals("Facility exists with name = 'TestDuplicates'", e.getMessage());
			IcatException ue = e.getFaultInfo();
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, ue.getType());
			assertEquals(-1, ue.getOffset());
		}
	}

	@Test
	public void duplicatesMany() throws Exception {
		List<EntityBaseBean> beans = new ArrayList<EntityBaseBean>();
		Facility f = new Facility();
		f.setName("One");
		beans.add(f);
		f = new Facility();
		f.setName("Two");
		beans.add(f);
		f = new Facility();
		f.setName("Three");
		beans.add(f);
		f = new Facility();
		f.setName("Two");
		beans.add(f);
		f = new Facility();
		f.setName("One");
		beans.add(f);
		try {
			session.createMany(beans);
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			assertEquals("Facility exists with name = 'Two'", e.getMessage());
			IcatException ue = e.getFaultInfo();
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, ue.getType());
			assertEquals(3, ue.getOffset());
		}
	}

	@Test
	public void duplicatesMany2() throws Exception {
		List<EntityBaseBean> beans = new ArrayList<EntityBaseBean>();
		Facility f = new Facility();
		f.setName("One");
		beans.add(f);
		f = new Facility();
		f.setName("Two");
		beans.add(f);
		f = new Facility();
		f.setName("Three");
		beans.add(f);
		f = new Facility();
		f.setName("Two");
		beans.add(f);
		f = new Facility();
		f.setName("One");
		beans.add(f);
		f = new Facility();
		f.setName("Two");
		session.create(f);
		try {
			session.createMany(beans);
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			assertEquals("Facility exists with name = 'Two'", e.getMessage());
			IcatException ue = e.getFaultInfo();
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, ue.getType());
			assertEquals(1, ue.getOffset());
		}
	}

	@Test
	public void duplicatesMany3() throws Exception {
		Facility f = new Facility();
		f.setName("Two");
		f.setId(session.create(f));
		List<EntityBaseBean> beans = new ArrayList<EntityBaseBean>();
		InvestigationType i = new InvestigationType();
		i.setName("One");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Two");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Three");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Two");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("One");
		i.setFacility(f);
		beans.add(i);
		try {
			session.createMany(beans);
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			assertTrue(e.getMessage().startsWith("InvestigationType exists with name = 'Two', facility = 'id:"));
			IcatException ue = e.getFaultInfo();
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, ue.getType());
			assertEquals(3, ue.getOffset());
		}
	}

	@Ignore("Needs Oracle for this test to be useful")
	@Test
	public void getInvestigationWithVeryManyDatasets() throws Exception {
		WSession piOneSession = session.getSession("db", "username", "piOne", "password", "piOne");

		Facility facility = session.createFacility("Test Facility", 90);

		SampleType sampleType = new SampleType();
		sampleType.setFacility(facility);
		sampleType.setName("somename");
		sampleType.setMolecularFormula("Someformula");
		session.create(sampleType);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		session.addRule(null, "Investigation <-> InvestigationUser <-> User [name = :user]", "R");
		session.addRule(null, "Dataset <-> Investigation <-> InvestigationUser <-> User [name = :user]", "R");

		User piOne = new User();
		piOne.setName("db/piOne");
		piOne.setId(session.create(piOne));

		InvestigationUser iuOne = new InvestigationUser();
		iuOne.setInvestigation(inv);
		iuOne.setUser(piOne);
		iuOne.setRole("Principal Investigator");
		iuOne.setId(session.create(iuOne));

		int ndataset = 1500;
		for (int i = 0; i < ndataset; i++) {
			session.createDataset("Wibble" + i, dst, inv);
		}

		for (Object o : piOneSession.search("Investigation INCLUDE Dataset")) {
			inv = (Investigation) o;
			assertEquals("A", inv.getName());
			assertEquals(ndataset, inv.getDatasets().size());
		}
	}

	@Test
	public void groupAuthz() throws Exception {
		session.clearAuthz();
		session.addUserGroupMember("notroot", "db/notroot");

		session.addUserGroupMember("investigation_test_owner", "db/piOne");
		session.addUserGroupMember("investigation_test_reader", null);
		session.addUserGroupMember("investigation_test_writer", "db/CIC");
		session.addUserGroupMember("investigation_test_writer", "db/piOne");

		session.addUserGroupMember("root", "root");
		session.addUserGroupMember("root", "useroffice");

		for (String t : EntityInfoHandler.getAlphabeticEntityNames()) {
			session.addRule("root", "SELECT x FROM " + t + " x", "CRUD");
			session.addRule("notroot", "SELECT x FROM " + t + " x", "CRUD");
		}

		for (String t : Arrays.asList("DatafileFormat", "DatasetType", "Facility")) {
			session.addRule(null, "SELECT x FROM " + t + " x", "R");
		}

		session.addRule(null, "Dataset <-> Investigation <-> InvestigationGroup [role='writer'] "
				+ "<-> Grouping <-> UserGroup <-> User [name=:user]", "CRUD");

		session.addRule(null, "Dataset <-> Investigation <-> InvestigationGroup <-> Grouping "
				+ "<-> UserGroup <-> User [name=:user]", "R");

		session.addRule(null,
				" Investigation <-> InvestigationGroup <-> Grouping <-> UserGroup " + "<-> User [name=:user]", "R");

		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		Investigation invA = session.createInvestigation(facility, "A", "Not null", investigationType);

		DatasetType dst = session.createDatasetType(facility, "GQ");

		session.createDataset("DS1", dst, invA);

		session.createInvestigationGroup(invA, "investigation_test_owner", "owner");
		session.createInvestigationGroup(invA, "investigation_test_reader", "reader");
		session.createInvestigationGroup(invA, "investigation_test_writer", "writer");

		WSession aSession = session.getSession("db", "username", "piOne", "password", "piOne");

		Long invId = ((Investigation) aSession.search("Investigation [name='A']").get(0)).getId();
		System.out.println(invId);

		List<Object> dss = aSession
				.search("Dataset INCLUDE Investigation [name='DS1' AND investigation.id = " + invId + "]");
		assertEquals(1, dss.size());
		Investigation inv = ((Dataset) dss.get(0)).getInvestigation();
		assertNotNull(inv);
		assertEquals(invId, inv.getId());

		aSession = session.getSession("db", "username", "CIC", "password", "password");
		dss = aSession.search("Dataset INCLUDE Investigation [name='DS1' AND investigation.id = " + invId + "]");
		assertEquals(1, dss.size());
		inv = ((Dataset) dss.get(0)).getInvestigation();
		assertNotNull(inv);
		assertEquals(invId, inv.getId());
	}

	@Test
	public void gets() throws Exception {
		create();
		Long dsId = (Long) session.search("Dataset.id [name = 'Wibble']").get(0);

		assertEquals("Wibble", ((Dataset) session.get("Dataset", dsId)).getName());

		Dataset ds = (Dataset) session.get("Dataset ds INCLUDE ds.datafiles df, df.datafileFormat", dsId);
		assertEquals("Wibble", ds.getName());
		assertEquals(2, ds.getDatafiles().size());
		for (Datafile df : ds.getDatafiles()) {
			assertTrue(df.getName().equals("wib1") || df.getName().equals("wib2"));
			String tn = df.getDatafileFormat().getName();
			if (df.getName().equals("wib1")) {
				assertEquals(tn, "png");
			} else {
				assertEquals(tn, "bmp");
			}
		}

		try {
			session.get("Dataset", random.nextLong());
			fail("No throw");
		} catch (IcatException_Exception e) {
			if (e.getFaultInfo().getType() != IcatExceptionType.NO_SUCH_OBJECT_FOUND) {
				throw e;
			}
		}

		try {
			session.get("Dataset INCLUDE Investigator", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Investigator is not an EntityBaseBean", e.getMessage());
		}
		try {
			session.get("Dataset INCLUDE Investigation, Facility, Instrument", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
		}
		try {
			session.get("Dataset INCLUDE User", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Unable to reach User in list of INCLUDES.", e.getMessage());

		}
	}

	@Test
	public void inapplicableParameterType() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		ParameterType pts = new ParameterType();
		pts.setName("UselessString");
		pts.setValueType(ParameterValueType.STRING);
		pts.setFacility(facility);
		pts.setUnits("peck");
		pts.setId((Long) session.create(pts));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(pts);
		ip.setInvestigation(inv);

		try {
			ip.setStringValue("bad");
			session.create(ip);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
			assertEquals("Parameter of type UselessString is not applicable to an Investigation", e.getMessage());
		}
	}

	@Test
	public void includes() throws Exception {
		create();

		List<?> results = session.search("Dataset.id order by id");

		assertEquals("Count", 4, results.size());

		results = session.search("Dataset [name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		Dataset ds = (Dataset) results.get(0);
		assertEquals("No files", 0, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile[name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		try {
			results = session.search("Dataset INCLUDE 1, Datafile [name = 'Wibble']");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
			assertEquals("Expected token from types [ENTSEP] at token , in INCLUDE 1 < , > Datafile [ ",
					e.getMessage());
		}

		try {
			results = session.search("Dataset INCLUDE Datafile, 1 [name = 'Wibble']");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
			assertEquals("Expected token from types [NAME] at token 1 in Datafile , < 1 > [ name ", e.getMessage());
		}

		results = session.search("Dataset INCLUDE 1 [name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("No files", 0, ds.getDatafiles().size());
		assertEquals("No params", 0, ds.getParameters().size());
		assertNotNull("Inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE DatasetParameter [name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("No Files", 0, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile, DatasetParameter [name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
		assertNull("No inv", ds.getInvestigation());

		results = session.search("Dataset INCLUDE Datafile, DatasetParameter, Investigation [name = 'Wibble']");
		assertEquals("Count", 1, results.size());
		ds = (Dataset) results.get(0);
		assertEquals("Files", 2, ds.getDatafiles().size());
		assertEquals("Params", 1, ds.getParameters().size());
		assertNotNull("Inv", ds.getInvestigation());

		results = session.search("Job");
		assertEquals("Count", 1, results.size());
		Job job = (Job) results.get(0);
		assertNull("InputDataset", job.getInputDataCollection());
		assertNull("OutputDataset", job.getOutputDataCollection());

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

		try {
			results = session.search("Job INCLUDE InputDataset, InputDatafile, Dataset, Datafile");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
		}

		try {
			results = session.search(
					"Job INCLUDE InputDataset, InputDatafile, OutputDataset, OutputDatafile, Dataset, Datafile");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
		}

		try {
			session.search(
					"SELECT DISTINCT i FROM Investigation i JOIN i.investigationInstruments ii JOIN ii.instrument inst "
							+ "WHERE inst.name='WISH' ORDER BY i.id ASC INCLUDE ii.instruments, i.parameters LIMIT 100, 100");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
			assertTrue(e.getMessage().contains("II"));
		}

		try {
			results = session.search("SELECT inv FROM Investigation inv INCLUDE 1, inv.investigationInstruments");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
			assertTrue(e.getMessage().contains("INCLUDE 1"));
		}

		try {
			results = session.search(
					"SELECT inv FROM Investigation inv INCLUDE inv.investigationInstruments, inv.investigationInstruments.instrument");
			fail("Exception not thrown");
		} catch (IcatException_Exception e) {
			IcatException ue = e.getFaultInfo();
			assertEquals(-1, ue.getOffset());
			assertEquals(IcatExceptionType.BAD_PARAMETER, ue.getType());
			assertTrue(e.getMessage().contains("investigationInstruments"));
		}

		for (Object o : session
				.search("SELECT inv FROM Investigation inv INCLUDE inv.investigationInstruments.instrument")) {
			Investigation inv = (Investigation) o;
			String invName = inv.getName();
			assertTrue(Arrays.asList("A", "B", "C").contains(invName));
			int nii = inv.getInvestigationInstruments().size();
			if (invName.equals("A")) {
				assertEquals(2, nii);
			} else if (invName.equals("B")) {
				assertEquals(1, nii);
			} else if (invName.equals("C")) {
				assertEquals(0, nii);
			}
		}

	}

	@Test
	public void login() throws Exception {
		double rm = session.getRemainingMinutes();
		assertTrue(rm > 0);
		assertTrue("API version", session.getApiVersion().startsWith(version));
		assertEquals("db/notroot", session.getUserName());
		Thread.sleep(10);
		rm = session.getRemainingMinutes();
		session.refresh();
		assertTrue(session.getRemainingMinutes() > rm);

		WSession piOneSession = session.getSession("db", "username", "piOne", "password", "piOne");
		piOneSession.logout();
	}

	@Test
	public void badPlugin() throws Exception {
		try {
			session.getSession("typo", "username", "notroot", "password", "password");
			fail("Should throw an exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.SESSION, e.getFaultInfo().getType());
			assertEquals("Authenticator mnemonic typo not recognised", e.getMessage());
		}
	}

	@Test
	public void authenticatorInfo() throws IcatException_Exception {
		for (AuthenticatorInfo info : session.getAuthenticatorInfo()) {
			if (info.getMnemonic().equals("db")) {
				assertNull(info.getFriendly());
				assertFalse(info.isAdmin());
				assertEquals(2, info.getKeys().size());
				for (AuthenticatorCredentialKey key : info.getKeys()) {
					if (key.getName().equals("username")) {
						assertNull(key.getPattern());
						assertFalse(key.isHide());
					} else if (key.getName().equals("password")) {
						assertNull(key.getPattern());
						assertTrue(key.isHide());
					} else {
						fail("Unexpected key name " + key.getName());
					}
				}
			}
		}
	}

	@Test
	public void refresh() throws Exception {
		try {
			session.refresh("wibble");
			fail("Should throw an exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.SESSION, e.getFaultInfo().getType());
		}
	}

	@Test
	public void noDuplicates() throws Exception {
		Facility f = new Facility();
		f.setName("Two");
		f.setId(session.create(f));
		Facility f2 = new Facility();
		f2.setName("Three");
		f2.setId(session.create(f2));
		List<EntityBaseBean> beans = new ArrayList<EntityBaseBean>();
		InvestigationType i = new InvestigationType();
		i.setName("One");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Two");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Three");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("Four");
		i.setFacility(f);
		beans.add(i);
		i = new InvestigationType();
		i.setName("One");
		i.setFacility(f2);
		beans.add(i);

		session.createMany(beans);

	}

	@Test
	public void numericParameterRanges() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		ParameterType ptn = new ParameterType();
		ptn.setName("TestNumeric");
		ptn.setApplicableToInvestigation(true);
		ptn.setValueType(ParameterValueType.NUMERIC);
		ptn.setFacility(facility);
		ptn.setEnforced(true);
		ptn.setMinimumNumericValue(50.);
		ptn.setUnits("bushel");
		ptn.setId((Long) session.create(ptn));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(ptn);
		ip.setInvestigation(inv);

		try {
			ip.setNumericValue(40.);
			session.create(ip);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
		}

		ip.setNumericValue(60.);
		ip.setId((Long) session.create(ip));

		try {
			ip.setNumericValue(30.);
			session.update(ip);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
		}

		ip.setNumericValue(70.);
		session.update(ip);

	}

	@Test
	public void oldGets() throws Exception {
		create();
		Long dsId = (Long) session.search("Dataset.id [name = 'Wibble']").get(0);

		assertEquals("Wibble", ((Dataset) session.get("Dataset", dsId)).getName());

		try {
			session.get("Dataset", random.nextLong());
			fail("No throw");
		} catch (IcatException_Exception e) {
			if (e.getFaultInfo().getType() != IcatExceptionType.NO_SUCH_OBJECT_FOUND) {
				throw e;
			}
		}

		try {
			session.get("Dataset INCLUDE Investigator", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Investigator is not an EntityBaseBean", e.getMessage());
		}
		try {
			session.get("Dataset INCLUDE Investigation, Facility, Instrument", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
		}
		try {
			session.get("Dataset INCLUDE User", dsId);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
			assertEquals("Unable to reach User in list of INCLUDES.", e.getMessage());

		}
	}

	@Test
	public void oldSearches() throws Exception {
		create();

		assertEquals(4L, session.search("COUNT(Dataset)").get(0));

		long max = -999999999999999L;
		long min = 999999999999999L;
		Dataset ds = null;
		for (Object result : session.search("Dataset INCLUDE Investigation")) {
			ds = (Dataset) result;
			max = Math.max(ds.getId(), max);
			min = Math.min(ds.getId(), min);
		}
		Long invId = ds.getInvestigation().getId();

		assertEquals(min, session.search("MIN(Dataset.id) [id > 0]").get(0));
		assertEquals(max, session.search("MAX(Dataset.id) [id > 0]").get(0));

		List<?> results = session.search(
				"Dataset.id " + "<-> DatasetParameter[type.name = 'TIMESTAMP'] " + "<-> Investigation[name <> 12]");
		assertEquals("Count", 1, results.size());

		results = session.search("Datafile [name = 'fred'] <-> Dataset[id <> 42]");
		assertEquals("Count", 1, results.size());

		String query = "Dataset.id  ORDER BY id [type.name IN :types] <-> Investigation[id BETWEEN :lower AND :upper]";

		query = query.replace(":lower", Long.toString(invId)).replace(":upper", Long.toString(invId)).replace(":types",
				"('GS', 'GQ')");
		results = session.search(query);
		assertEquals("Count", 4, results.size());

		query = "Dataset.id ORDER BY startDate [type.name IN :types AND name >= :lower AND name <= :upper]";
		query = query.replace(":lower", "'Wabble'").replace(":upper", "'Wobble'").replace(":types", "('GS', 'GQ')");
		results = session.search(query);
		assertEquals("Count", 2, results.size());

		query = "ParameterType.name [description LIKE 'F%']";
		results = session.search(query);
		assertEquals("Count", 1, results.size());
		assertEquals("TIMESTAMP", results.get(0));

		results = session.search("Datafile.name ORDER BY name");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "bill", results.get(0));
		assertEquals("Result", "fred", results.get(1));

		results = session.search(",1 Datafile.name ORDER BY name");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "bill", results.get(0));

		results = session.search("1, Datafile.name ORDER BY name");
		assertEquals("Count", 5, results.size());
		assertEquals("Result", "fred", results.get(0));

		results = session.search("1,1 Datafile.name ORDER BY name");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "fred", results.get(0));

		results = session.search("100,1 Datafile.name ORDER BY name");
		assertEquals("Count", 0, results.size());

		results = session.search("0,100 Datafile.name ORDER BY name");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "bill", results.get(0));
		assertEquals("Result", "fred", results.get(1));

		results = session.search("Facility");
		Facility facility = (Facility) results.get(0);

		results = session.search("DISTINCT ParameterType.valueType");
		ParameterValueType pvt = (ParameterValueType) results.get(0);

		results = session.search("ParameterType [facility.id=" + facility.getId() + " AND valueType=" + pvt + "]");
		assertEquals(1, results.size());

		results = session.search("ParameterType [facility.id=" + facility.getId() + " AND " + pvt + "= valueType]");
		assertEquals(1, results.size());

		results = session.search("Dataset [complete = TRUE]");
		assertEquals(0, results.size());
		results = session.search("Dataset [complete = FALSE]");
		assertEquals(4, results.size());
	}

	@Test
	public void performance() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		Dataset wibble = session.createDataset("Wibble", dst, inv);

		DatafileFormat dfmt = session.createDatafileFormat(facility, "png", "binary");

		long start = System.currentTimeMillis();
		int n = 100;
		for (int i = 0; i < n; i++) {
			session.createDatafile("fred" + i, dfmt, wibble, 0L);
		}
		System.out.println("Time per datafile to write: " + (System.currentTimeMillis() - start) / (n + 0.) + "ms");
		List<EntityBaseBean> dfs = new ArrayList<EntityBaseBean>();
		for (int i = 0; i < n; i++) {
			final Datafile datafile = new Datafile();
			datafile.setDatafileFormat(dfmt);
			datafile.setName("bill" + i);
			datafile.setDataset(wibble);
			dfs.add(datafile);
		}
		start = System.currentTimeMillis();
		session.createMany(dfs);
		System.out.println(
				"Time per datafile using createMany: " + (System.currentTimeMillis() - start) / (n + 0.) + "ms");

		start = System.currentTimeMillis();
		List<Object> results = null;
		;
		// int m = 10000000;
		int m = 1;
		for (int i = 0; i < m; i++) {
			results = session.search("SELECT df FROM Datafile df INCLUDE df.datafileFormat, df.dataset");
		}

		System.out.println("Time per datafile to retrieve: " + results.size() + " datafiles "
				+ (System.currentTimeMillis() - start) / (results.size() + 0.) + "ms");

		start = System.currentTimeMillis();
		results = session.search("SELECT df FROM Datafile df");
		System.out.println("Time per datafile to retrieve no includes: " + results.size() + " datafiles "
				+ (System.currentTimeMillis() - start) / (results.size() + 0.) + "ms");

		start = System.currentTimeMillis();
		@SuppressWarnings("unused")
		List<Object> ids = session.search("SELECT df.id FROM Datafile df");
		System.out.println("Time per datafile to retrieve ids: " + results.size() + " datafiles "
				+ (System.currentTimeMillis() - start) / (results.size() + 0.) + "ms");

		dfs.clear();
		for (Object odf : results) {
			Datafile df = (Datafile) odf;
			df.setDataset(null);
			df.setDatafileFormat(null);
			dfs.add(df);
		}

		start = System.currentTimeMillis();
		session.deleteMany(dfs);
		System.out.println("Time per datafile to delete: " + results.size() + " datafiles with deleteMany: "
				+ (System.currentTimeMillis() - start) / (results.size() + 0.) + "ms");
	}

	@Test
	public void searches() throws Exception {
		create();

		List<?> results = session.search("select investigation from Investigation investigation, "
				+ "investigation.investigationUsers as investigationUser, investigationUser.user as user "
				+ "where user.name = :user ORDER BY investigation.startDate desc limit 0, 50 "
				+ "include investigation.investigationInstruments.instrument");
		assertEquals("Count", 2, results.size());

		Date now = new Date(new Date().getTime() + 1001); // Move on a second
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		String nowString = "{ts " + df.format(now) + "}";
		String baseq = "SELECT p FROM DatasetParameter p WHERE p.dateTimeValue";

		assertEquals(0, session.search(baseq + " > " + nowString).size());
		assertEquals(1, session.search(baseq + " <= " + nowString).size());

		assertEquals(4L, session.search("SELECT COUNT(ds) FROM Dataset ds").get(0));

		long max = -999999999999999L;
		long min = 999999999999999L;
		for (Object result : session.search("SELECT ds FROM Dataset ds")) {
			Dataset ds = (Dataset) result;
			max = Math.max(ds.getId(), max);
			min = Math.min(ds.getId(), min);
		}

		assertEquals(0, session.search("SELECT ds FROM Dataset ds WHERE ds.name = 'dfsin' LIMIT 1,10").size());
		// TODO this next test should return 0 rather than 1
		assertEquals(1, session.search("SELECT ds FROM Dataset ds WHERE ds.id = " + max + " LIMIT 1,10").size());
		assertEquals(0, session.search("SELECT ds FROM Dataset ds WHERE ds.id IN ( " + max + ") LIMIT 1,10").size());
		assertEquals(min, session.search("SELECT MIN(ds.id) FROM Dataset ds WHERE ds.id > 0").get(0));
		assertEquals(max, session.search("SELECT MAX(ds.id) FROM Dataset ds WHERE ds.id > 0").get(0));

		Long invId = (Long) session.search("SELECT inv.id FROM Investigation inv WHERE inv.datasets IS NOT EMPTY")
				.get(0);

		results = session.search("SELECT ds.id FROM Dataset ds JOIN ds.parameters dsp JOIN ds.investigation inv"
				+ " WHERE dsp.type.name = 'TIMESTAMP' AND inv.name <> 12");
		assertEquals("Count", 1, results.size());

		results = session
				.search("SELECT df FROM Datafile df JOIN df.dataset ds WHERE df.name = 'fred' AND ds.id <> 42");
		assertEquals("Count", 1, results.size());

		String query = "SELECT ds.id FROM Dataset ds JOIN ds.investigation inv "
				+ "WHERE ds.type.name IN :types AND inv.id BETWEEN :lower AND :upper " + "ORDER BY ds.id";
		query = query.replace(":lower", Long.toString(invId)).replace(":upper", Long.toString(invId)).replace(":types",
				"('GS', 'GQ')");

		results = session.search(query);
		assertEquals("Count", 4, results.size());

		query = "SELECT ds.id FROM Dataset ds WHERE ds.type.name IN :types AND ds.name >= :lower AND ds.name <= :upper ORDER BY ds.startDate";
		query = query.replace(":lower", "'Wabble'").replace(":upper", "'Wobble'").replace(":types", "('GS', 'GQ')");
		results = session.search(query);
		assertEquals("Count", 2, results.size());

		query = "SELECT pt.name FROM ParameterType pt WHERE pt.description LIKE 'F%'";
		results = session.search(query);
		assertEquals("Count", 1, results.size());
		assertEquals("TIMESTAMP", results.get(0));

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "bill", results.get(0));
		assertEquals("Result", "fred", results.get(1));

		results = session.search("SELECT df.name, df.fileSize FROM Datafile df ORDER BY df.name");
		assertEquals("Count", 6, results.size());

		assertEquals("Result", "bill", ((FieldSet) results.get(0)).getFields().get(0));
		assertEquals("Result", 17L, ((FieldSet) results.get(0)).getFields().get(1));
		assertEquals("Result", "fred", ((FieldSet) results.get(1)).getFields().get(0));
		assertEquals("Result", 11L, ((FieldSet) results.get(1)).getFields().get(1));

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name LIMIT 0,1");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "bill", results.get(0));

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name LIMIT 1,100");
		assertEquals("Count", 5, results.size());
		assertEquals("Result", "fred", results.get(0));

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name LIMIT 1,1");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "fred", results.get(0));

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name LIMIT 100,1");
		assertEquals("Count", 0, results.size());

		results = session.search("SELECT df.name FROM Datafile df ORDER BY df.name LIMIT 0,100");
		assertEquals("Count", 6, results.size());
		assertEquals("Result", "bill", results.get(0));
		assertEquals("Result", "fred", results.get(1));

		results = session.search("SELECT ds.name from Dataset ds JOIN ds.datafiles df1 JOIN ds.datafiles df2 "
				+ "WHERE df1.name = 'fred' AND df2.name = 'bill'");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "dfsin", results.get(0));

		results = session.search("SELECT ds.name from Dataset ds JOIN ds.datafiles df1 JOIN ds.datafiles df2 "
				+ "WHERE LOWER(df1.name) = 'fred' AND df2.name = LOWER('bill')");
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "dfsin", results.get(0));

		results = session.search("SELECT f FROM Facility f");
		Facility facility = (Facility) results.get(0);

		results = session.search("SELECT DISTINCT pt.valueType FROM ParameterType pt");
		ParameterValueType pvt = (ParameterValueType) results.get(0);

		results = session.search("SELECT pt FROM ParameterType pt WHERE pt.facility.id=" + facility.getId()
				+ " AND pt.valueType=" + pvt.getClass().getName() + "." + pvt);
		assertEquals(1, results.size());

		results = session.search("SELECT pt FROM ParameterType pt WHERE pt.facility.id=" + facility.getId() + " AND "
				+ pvt.getClass().getName() + "." + pvt + "= pt.valueType");
		assertEquals(1, results.size());

		results = session.search("SELECT ds FROM Dataset ds WHERE ds.complete = TRUE");
		assertEquals(0, results.size());
		results = session.search("SELECT ds FROM Dataset ds WHERE ds.complete = FALSE");
		assertEquals(4, results.size());

		if (session.getContainerType() != ContainerType.Glassfish
				&& session.getContainerType() != ContainerType.JBoss) {
			// This should throw an exception as datafile is not an attribute of
			// Dataset.
			try {
				results = session.search("SELECT ds from Dataset ds WHERE (SELECT COUNT(df) FROM ds.datafile df) = 2");
				fail("Should have thrown an exception");
			} catch (IcatException_Exception e) {
				assertEquals(IcatExceptionType.BAD_PARAMETER, e.getFaultInfo().getType());
				assertTrue(e.getMessage().indexOf("EntityManager") > 0
						|| e.getMessage().indexOf("could not resolve property") > 0);
			}
		}

		// Nested select
		results = session.search("SELECT ds from Dataset ds WHERE (SELECT COUNT(df) FROM ds.datafiles df) = 2");
		assertEquals(2, results.size());

		results = session.search(
				"SELECT i.id FROM Investigation i JOIN i.samples s JOIN s.type st " + "WHERE i.type.name = 'Data' "
						+ "AND st.id = (SELECT st2.id FROM SampleType st2 JOIN st2.samples s2 JOIN s2.investigation i2 WHERE i2.id=i.id) "
						+ "ORDER BY st.name");

		assertEquals(3, session.search("SELECT i FROM Investigation i").size());
		assertEquals(3, session.search("SELECT i.facility FROM Investigation i").size());
		assertEquals(1, session.search("SELECT DISTINCT i.facility FROM Investigation i").size());

		/* Should be 3 but WS handles nulls badly */
		assertEquals(0, session.search("SELECT i.facility.url FROM Investigation i").size());

		assertEquals(3L, session.search("SELECT COUNT(i) FROM Investigation i").get(0));
		assertEquals(3L, session.search("SELECT COUNT(i.facility) FROM Investigation i").get(0));
		assertEquals(1L, session.search("SELECT COUNT(DISTINCT i.facility) FROM Investigation i").get(0));
		assertEquals(0L, session.search("SELECT COUNT(i.facility.url) FROM Investigation i").get(0));

		// Check on wild card handling
		session.createFacility("Test__Facility", 90);
		session.createFacility("TestXYFacility", 90);
		session.createFacility("TestX_Facility", 90);

		query = "SELECT f.name FROM Facility f WHERE f.name LIKE 'Test__Facility'";
		assertEquals("Count", 3, session.search(query).size());

		query = "SELECT f.name FROM Facility f WHERE f.name LIKE 'Test$_$_Facility'";
		assertEquals("Count", 0, session.search(query).size());

		query = "SELECT f.name FROM Facility f WHERE f.name LIKE 'Test$_$_Facility' ESCAPE '$'";
		assertEquals("Count", 1, session.search(query).size());
	}

	@Test
	public void duplicateIds() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");
		Investigation inv1 = session.createInvestigation(facility, "inv1", "Not null", investigationType);
		Investigation inv2 = session.createInvestigation(facility, "inv2", "Not null", investigationType);
		DatasetType dst1 = session.createDatasetType(facility, "type1");
		DatasetType dst2 = session.createDatasetType(facility, "type2");
		session.createDataset("ds", dst1, inv1);
		session.createDataset("ds", dst2, inv2);
		assertEquals(1, session.search("DISTINCT Dataset.name").size());
		assertEquals(2, session.search("Dataset.name").size());
		assertEquals(2, session.search("DISTINCT Dataset.id").size());
		assertEquals(2, session.search("Dataset.id").size());
	}

	@Test
	public void stringParameterRanges() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);
		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");
		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

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
		pts.setUnits("chain");
		pts.setId((Long) session.create(pts));

		InvestigationParameter ip = new InvestigationParameter();

		ip.setType(pts);
		ip.setInvestigation(inv);

		try {
			ip.setStringValue("bad");
			session.create(ip);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
		}

		ip.setStringValue("good1");
		ip.setId(session.create(ip));

		try {
			ip.setStringValue("worse");
			session.update(ip);
			fail("No throw");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
		}

		ip.setStringValue("good2");
		session.update(ip);
	}

	@Test
	public void testInvestigation() throws Exception {
		EntityInfo ei = session.getEntityInfo("Investigation");
		assertEquals("An investigation or experiment", ei.getClassComment());
		for (Constraint constraint : ei.getConstraints()) {
			assertEquals(Arrays.asList("facility", "name", "visitId"), constraint.getFieldNames());
		}
		assertEquals(30, ei.getFields().size());
		int n = 0;
		for (EntityField field : ei.getFields()) {
			if (field.getName().equals("id")) {
				assertEquals("Long", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.ATTRIBUTE, field.getRelType());
				assertEquals(null, field.getStringLength());
			} else if (field.getName().equals("facility")) {
				assertEquals("Facility", field.getType());
				assertEquals(true, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.ONE, field.getRelType());
				assertEquals(null, field.getStringLength());
			} else if (field.getName().equals("title")) {
				assertEquals("String", field.getType());
				assertEquals(true, field.isNotNullable());
				assertEquals("Full title of the investigation", field.getComment());
				assertEquals(RelType.ATTRIBUTE, field.getRelType());
				assertEquals((Integer) 255, field.getStringLength());
			} else if (field.getName().equals("investigationUsers")) {
				assertEquals("InvestigationUser", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(RelType.MANY, field.getRelType());
				assertEquals(null, field.getStringLength());
			} else {
				n++;
			}
		}
		assertEquals(26, n);
	}

	@Test
	public void testTestCalls() throws Exception {
		/* Create user and rules */
		User piOne = new User();
		piOne.setName("db/piOne");
		piOne.setId(session.create(piOne));

		User piTwo = new User();
		piTwo.setName("db/piTwo");
		piTwo.setId(session.create(piTwo));

		Grouping ones = new Grouping();
		ones.setName("Ones");
		ones.setId(session.create(ones));

		UserGroup userGroup = new UserGroup();
		userGroup.setUser(piOne);
		userGroup.setGrouping(ones);
		session.create(userGroup);

		session.addRule(ones.getName(), "Facility", "CRUD");

		WSession piOneSession = session.getSession("db", "username", "piOne", "password", "piOne");
		WSession piTwoSession = session.getSession("db", "username", "piTwo", "password", "piTwo");

		Facility facility = new Facility();
		facility.setName("A Facility");

		/* testCreate */
		assertTrue(piOneSession.isAccessAllowed(facility, AccessType.CREATE));

		assertFalse(piTwoSession.isAccessAllowed(facility, AccessType.CREATE));

		facility.setId(piOneSession.create(facility));

		/* testUpdate */
		facility.setName("Banana");
		assertTrue(piOneSession.isAccessAllowed(facility, AccessType.UPDATE));

		assertFalse(piTwoSession.isAccessAllowed(facility, AccessType.UPDATE));

		piOneSession.update(facility);

		assertTrue(piOneSession.isAccessAllowed(facility, AccessType.UPDATE));

		assertFalse(piTwoSession.isAccessAllowed(facility, AccessType.UPDATE));

		/* testDelete */
		piOneSession.isAccessAllowed(facility, AccessType.DELETE);

		assertFalse(piTwoSession.isAccessAllowed(facility, AccessType.DELETE));

		piOneSession.delete(facility);

		// Object doesn't exist
		assertFalse(piOneSession.isAccessAllowed(facility, AccessType.DELETE));

		// Object doesn't exist
		assertFalse(piTwoSession.isAccessAllowed(facility, AccessType.DELETE));

		// Bad call
		Investigation investigation = new Investigation();
		investigation.setId(42L);
		try {
			piOneSession.isAccessAllowed(investigation, AccessType.CREATE);
			fail("Should have thrown exception");
		} catch (IcatException_Exception e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getFaultInfo().getType());
			assertEquals("Investigation: facility cannot be null.", e.getMessage());
		}
	}

	@Test
	public void updates() throws Exception {
		Facility facility = session.createFacility("Test Facility", 90);

		InvestigationType investigationType = session.createInvestigationType(facility, "TestExperiment");

		DatasetType dst = session.createDatasetType(facility, "GQ");

		Investigation inv = session.createInvestigation(facility, "A", "Not null", investigationType);

		Dataset wibble = session.createDataset("Wibble", dst, inv);
		Dataset wobble = session.createDataset("Wobble", dst, inv);

		DatafileFormat dfmt = session.createDatafileFormat(facility, "png", "binary");

		Datafile df = session.createDatafile("fred", dfmt, wibble, 0L);
		df = (Datafile) session.get("Datafile INCLUDE Dataset, DatafileFormat", df.getId());
		assertEquals("Wibble", df.getDataset().getName());

		df.setModTime(null); // To provoke a bug
		df.setDataset(wobble);
		df.setLocation("guess");
		df.setDatafileFormat(session.createDatafileFormat(facility, "notpng", "notbinary"));
		df.setDatafileFormat(null);
		df.setFileSize(-1L);
		session.update(df);
		df = (Datafile) session.get("Datafile INCLUDE Dataset,DatafileFormat", df.getId());
		assertEquals("Wobble", df.getDataset().getName());
		assertNull(df.getDatafileFormat());
	}

}
