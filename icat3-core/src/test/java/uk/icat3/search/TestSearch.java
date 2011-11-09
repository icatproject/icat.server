package uk.icat3.search;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.manager.BeanManager;
import uk.icat3.manager.RuleManager;
import uk.icat3.util.ParameterValueType;
import uk.icat3.util.TestConstants;

public class TestSearch {

	private static Logger log = Logger.getLogger(TestSearch.class);
	private static EntityManagerFactory emf;
	private EntityManager em;
	private Date invStartDate;
	private Date invEndDate;

	private static final DateFormat dfout = new SimpleDateFormat(
			"yyyy-MM-dd' 'HH:mm:ss");
	private static final String tsb = "{ts ";
	private static final String tse = "}";
	private Long invId;

	private void addRules() throws Exception {
		RuleManager.addRule("user-office", "Investigation", "CRUD",
				"  [id=2]  ", em);
		RuleManager.addRule("user-office", "Investigation", "CRUD", null, em);
		RuleManager.addRule("user-office", "Datafile", "CRUD", "[name='fred']",
				em);
		RuleManager.addRule("CIC-user", "Dataset", "CRUD", null, em);
		RuleManager.addRule("CIC-user", "DatasetParameter", "CRUD", null, em);
		RuleManager.addRule(null, "Parameter", "R", null, em);
		RuleManager.addRule("expt-A", "Dataset", "R",
				"Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "DatasetParameter", "R",
				"Dataset <-> Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "Datafile", "R",
				"Dataset <-> Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "DatafileParameter", "R",
				"<-> Datafile <-> Dataset <-> Investigation [invNumber = 'A']",
				em);
		RuleManager
				.addRule(
						null,
						"Dataset",
						"R",
						"<-> Investigation <-> Investigator [facilityUser.facilityUserId = :user]",
						em);

		RuleManager.addRule("user-office", "Facility", "CRUD", null, em);
		RuleManager.addRule("user-office", "InvestigationType", "CRUD", null,
				em);
		RuleManager.addRule("user-office", "DatasetType", "CRUD", null, em);
		RuleManager.addRule("user-office", "Parameter", "CRUD", null, em);
		RuleManager
				.addRule("user-office", "DatasetParameter", "CRUD", null, em);

		Facility f = new Facility();
		f.setFacilityShortName("TestFacility");
		f.setDaysUntilRelease(90L);
		BeanManager.create("uo", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("TestExperiment");
		BeanManager.create("uo", type, em);

		DatasetType dst = new DatasetType();
		dst.setName("GQ");
		BeanManager.create("uo", dst, em);
	}

	private Long addData() throws Exception {
		Investigation inv = new Investigation();
		inv.setInvNumber("A");
		inv.setTitle("Not null");
		inv.setInvType("TestExperiment");
		invStartDate = new Date();
		invEndDate = new Date(invStartDate.getTime() + 5000);
		inv.setInvStartDate(invStartDate);
		inv.setInvEndDate(invEndDate);
		inv.setFacility("TestFacility");
		invId = (Long) BeanManager.create("uo", inv, em);

		ParameterPK ppk = new ParameterPK();
		ppk.setName("TIMESTAMP");
		ppk.setUnits("TIMESTAMP");
		Parameter p = new Parameter();
		p.setParameterPK(ppk);
		p.setDescription("F is not a wibble");
		p.setDatasetParameter(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		BeanManager.create("uo", p, em);

		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setDatasetType("GQ");
		dataset.setInvestigationId(invId);

		Datafile fred = new Datafile();
		fred.setName("fred");
		dataset.getDatafileCollection().add(fred);
		Datafile bill = new Datafile();
		bill.setName("bill");
		dataset.getDatafileCollection().add(bill);
		dataset.setId((Long) BeanManager.create("CIC", dataset, em));

		DatasetParameterPK datasetParameterPK = new DatasetParameterPK();
		datasetParameterPK.setName("TIMESTAMP");
		datasetParameterPK.setUnits("TIMESTAMP");
		datasetParameterPK.setDatasetId(dataset.getId());
		DatasetParameter dsp = new DatasetParameter();
		dsp.setDatasetParameterPK(datasetParameterPK);
		dsp.setDateTimeValue(invStartDate);
		dsp.setParameter(p);
		BeanManager.create("uo", dsp, em);

		return dataset.getId();
	}

	@Test
	public void t1() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = Search.search("A1", "Dataset.id [id = " + dsid + "]",
				em);
		assertEquals("Count", 1, results.size());
		assertEquals("Value", dsid, results.get(0));
	}

	@Test
	public void t2() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = Search.search("B1", "Dataset.id [id = " + dsid + "]",
				em);
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t3() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = Search.search("A1",
				"DISTINCT Dataset.id <-> Investigation[invNumber = 'A']", em);
		assertEquals("Count", 1, results.size());
		assertEquals("Value", dsid, results.get(0));
	}

	@Test
	public void t4() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = Search.search("A1",
				"DISTINCT Dataset.id <-> Investigation[invNumber = 'B']", em);
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t5() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = Search
				.search("A1",
						"Dataset.id "
								+ "<-> DatasetParameter[datasetParameterPK.name = 'TA3_SHOT_NUM_VALUE' AND numericValue > 4000] "
								+ "<-> Investigation[invNumber > 12]", em);
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t7() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = Search
				.search("uo",
						"Investigation.invNumber ORDER BY id"
								+ "<-> DatasetParameter[datasetParameterPK.name = 'TA3_SHOT_NUM_VALUE' AND numericValue > 4000] "
								+ "<-> Dataset", em);
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t8() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = Search.search("uo",
				"Datafile [name = 'fred'] <-> Dataset[id = 42]", em);
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t9() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id ORDER BY id [datasetType IN :types] <-> DatasetParameter[datasetParameterPK.name = 'TIMESTAMP' AND dateTimeValue BETWEEN :lower AND :upper]";
		query = query.replace(":lower", tsb + dfout.format(invStartDate) + tse)
				.replace(":upper", tsb + dfout.format(invEndDate) + tse)
				.replace(":types", "('GS', 'GQ')");
		List<?> results = Search.search("CIC", query, em);
		assertEquals("Count", 1, results.size());
		Dataset ds = (Dataset) BeanManager.get("CIC", "Dataset",
				results.get(0), em);
		System.out.println(ds.getName());
	}

	@Test
	public void t10() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id  ORDER BY id [datasetType IN :types] <-> Investigation[id BETWEEN :lower AND :upper]";
		query = query.replace(":lower", Long.toString(invId))
				.replace(":upper", Long.toString(invId))
				.replace(":types", "('GS', 'GQ')");
		List<?> results = Search.search("CIC", query, em);
		assertEquals("Count", 1, results.size());
	}

	@Test
	public void t11() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id ORDER BY startDate [datasetType IN :types AND name >= :lower AND name <= :upper]";
		query = query.replace(":lower", "'Wabble'")
				.replace(":upper", "'Wobble'")
				.replace(":types", "('GS', 'GQ')");
		List<?> results = Search.search("CIC", query, em);
		assertEquals("Count", 1, results.size());
	}

	@Test
	public void t12() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Parameter.parameterPK.name [description LIKE 'F%']";
		List<?> results = Search.search("CIC", query, em);
		for (Object result : results) {
			System.out.println(result);
		}
		assertEquals("Count", 1, results.size());
		assertEquals("TIMESTAMP", results.get(0));
	}

	@Test
	public void t13() throws Exception {
		addMembers();
		addRules();
		addData();
		{
			Date date = new Date(System.currentTimeMillis() + 5000);
			String query = "Dataset.id [datasetType IN :types] <-> DatasetParameter [modTime >= :lower] ";
			query = query.replace(":lower", tsb + dfout.format(date) + tse)
					.replace(":types", "('GS', 'GQ')");
			List<?> results = Search.search("CIC", query, em);
			for (Object result : results) {
				System.out.println(result);
			}
			assertEquals("Count", 0, results.size());
		}

		{
			Date date = new Date(System.currentTimeMillis() - 5000);
			String query = "Dataset.id [datasetType IN :types] <-> DatasetParameter [modTime >= :lower] ";
			query = query.replace(":lower", tsb + dfout.format(date) + tse)
					.replace(":types", "('GS', 'GQ')");
			List<?> results = Search.search("CIC", query, em);
			for (Object result : results) {
				System.out.println(result);
			}
			assertEquals("Count", 1, results.size());
		}

	}

	private void addMembers() throws ObjectAlreadyExistsException {
		RuleManager.addUserGroupMember("user-office", "uo", em);
		RuleManager.addUserGroupMember("CIC-user", "CIC", em);
		RuleManager.addUserGroupMember("expt-A", "A1", em);
		RuleManager.addUserGroupMember("expt-B", "B1", em);
	}

	@Before
	public void beginTX() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}

	@After
	public void closeTX() throws NoSuchObjectFoundException {
		try {
			em.getTransaction().rollback();
		} catch (RuntimeException t) {
			log.error(t);
			throw t;
		} finally {
			em.close();
		}
	}

	@BeforeClass
	public static void BeforeClassSetUp() {
		emf = Persistence
				.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
	}

	@AfterClass
	public static void AfterClassTearDown() {
		emf.close();
	}

}
