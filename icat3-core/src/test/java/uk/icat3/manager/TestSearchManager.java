package uk.icat3.manager;

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
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.ParameterType;
import uk.icat3.entity.ParameterType.ParameterValueType;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.RuleManager;
import uk.icat3.util.TestConstants;

public class TestSearchManager {

	private static Logger log = Logger.getLogger(TestSearchManager.class);
	private static EntityManagerFactory emf;
	private EntityManager em;
	private Date invStartDate;
	private Date invEndDate;

	private static final DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
	private static final String tsb = "{ts ";
	private static final String tse = "}";
	private Long invId;
	private Facility facility;
	private InvestigationType investigationType;
	private DatasetType datasetType;

	private void addRules() throws Exception {
		RuleManager.oldAddRule("user-office", "Investigation", "CRUD", "  [id=2]  ", em);
		RuleManager.oldAddRule("user-office", "Investigation", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "Datafile", "CRUD", "[name='fred']", em);
		RuleManager.oldAddRule("CIC-user", "Dataset", "CRUD", null, em);
		RuleManager.oldAddRule("CIC-user", "DatasetParameter", "CRUD", null, em);
		RuleManager.oldAddRule("CIC-user", "Datafile", "CRUD", null, em);
		RuleManager.oldAddRule(null, "ParameterType", "R", null, em);
		RuleManager.oldAddRule("expt-A", "Dataset", "R", "Investigation [name = 'A']", em);
		RuleManager.oldAddRule("expt-A", "DatasetParameter", "R", "Dataset <-> Investigation [name = 'A']", em);
		RuleManager.oldAddRule("expt-A", "Datafile", "R", "Dataset <-> Investigation [name = 'A']", em);
		RuleManager.oldAddRule("expt-A", "DatafileParameter", "R",
				"<-> Datafile <-> Dataset <-> Investigation [name = 'A']", em);
		RuleManager.oldAddRule(null, "Dataset", "R", "<-> Investigation <-> Investigator [user.name = :user]", em);

		RuleManager.oldAddRule("user-office", "Facility", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "InvestigationType", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "DatasetType", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "ParameterType", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "DatasetParameter", "CRUD", null, em);

		facility = new Facility();
		facility.setName("TestFacility");
		facility.setDaysUntilRelease(90);
		BeanManager.create("uo", facility, em);

		investigationType = new InvestigationType();
		investigationType.setName("TestExperiment");
		BeanManager.create("uo", investigationType, em);

		datasetType = new DatasetType();
		datasetType.setName("GQ");
		BeanManager.create("uo", datasetType, em);
	}

	private Long addData() throws Exception {
		Investigation inv = new Investigation();
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(investigationType);
		invStartDate = new Date();
		invEndDate = new Date(invStartDate.getTime() + 5000);
		inv.setStartDate(invStartDate);
		inv.setEndDate(invEndDate);
		inv.setFacility(facility);
		invId = (Long) BeanManager.create("uo", inv, em).getPk();

		ParameterType p = new ParameterType();
		p.setName("TIMESTAMP");
		p.setUnits("TIMESTAMP");
		p.setDescription("F is not a wibble");
		p.setApplicableToDataset(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		BeanManager.create("uo", p, em);

		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setType(datasetType);
		dataset.setInvestigation(inv);

		Datafile fred = new Datafile();
		fred.setName("fred");
		dataset.getDatafiles().add(fred);
		Datafile bill = new Datafile();
		bill.setName("bill");
		dataset.getDatafiles().add(bill);
		dataset.setId((Long) BeanManager.create("CIC", dataset, em).getPk());

		DatasetParameter dsp = new DatasetParameter();
		dsp.setDataset(dataset);
		dsp.setDateTimeValue(invStartDate);
		dsp.setParameterType(p);
		BeanManager.create("uo", dsp, em);

		return dataset.getId();
	}

	@Test
	public void t1() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = BeanManager.search("A1", "Dataset.id [id = " + dsid + "]", em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Value", dsid, results.get(0));
	}

	@Test
	public void t2() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = BeanManager.search("B1", "Dataset.id [id = " + dsid + "]", em).getList();
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t3() throws Exception {
		addMembers();
		addRules();
		Long dsid = addData();
		List<?> results = BeanManager.search("A1", "DISTINCT Dataset.id <-> Investigation[name = 'A']", em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Value", dsid, results.get(0));
	}

	@Test
	public void t4() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = BeanManager.search("A1", "DISTINCT Dataset.id <-> Investigation[name = 'B']", em).getList();
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t5() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = BeanManager.search(
				"A1",
				"Dataset.id "
						+ "<-> DatasetParameter[parameterType.name = 'TA3_SHOT_NUM_VALUE' AND numericValue > 4000] "
						+ "<-> Investigation[name > 12]", em).getList();
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t7() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = BeanManager.search(
				"uo",
				"Investigation.name ORDER BY id"
						+ "<-> DatasetParameter[parameterType.name = 'TA3_SHOT_NUM_VALUE' AND numericValue > 4000] "
						+ "<-> Dataset", em).getList();
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t8() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = BeanManager.search("uo", "Datafile [name = 'fred'] <-> Dataset[id = 42]", em).getList();
		assertEquals("Count", 0, results.size());
	}

	@Test
	public void t9() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id ORDER BY id [datasetType.name IN :types] <-> DatasetParameter[parameterType.name = 'TIMESTAMP' AND dateTimeValue BETWEEN :lower AND :upper]";
		query = query.replace(":lower", tsb + dfout.format(invStartDate) + tse)
				.replace(":upper", tsb + dfout.format(invEndDate) + tse).replace(":types", "('GS', 'GQ')");
		List<?> results = BeanManager.search("CIC", query, em).getList();
		assertEquals("Count", 1, results.size());
		Dataset ds = (Dataset) BeanManager.get("CIC", "Dataset", results.get(0), em).getBean();
		assertEquals("Name", "Wibble", ds.getName());
	}

	@Test
	public void t10() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id  ORDER BY id [datasetType.name IN :types] <-> Investigation[id BETWEEN :lower AND :upper]";
		query = query.replace(":lower", Long.toString(invId)).replace(":upper", Long.toString(invId))
				.replace(":types", "('GS', 'GQ')");
		List<?> results = BeanManager.search("CIC", query, em).getList();
		assertEquals("Count", 1, results.size());
	}

	@Test
	public void t11() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "Dataset.id ORDER BY startDate [datasetType.name IN :types AND name >= :lower AND name <= :upper]";
		query = query.replace(":lower", "'Wabble'").replace(":upper", "'Wobble'").replace(":types", "('GS', 'GQ')");
		List<?> results = BeanManager.search("CIC", query, em).getList();
		assertEquals("Count", 1, results.size());
	}

	@Test
	public void t12() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "ParameterType.name [description LIKE 'F%']";
		List<?> results = BeanManager.search("CIC", query, em).getList();
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
			String query = "Dataset.id [datasetType.name IN :types] <-> DatasetParameter [modTime >= :lower] ";
			query = query.replace(":lower", tsb + dfout.format(date) + tse).replace(":types", "('GS', 'GQ')");
			List<?> results = BeanManager.search("CIC", query, em).getList();
			assertEquals("Count", 0, results.size());
		}

		{
			Date date = new Date(System.currentTimeMillis() - 5000);
			String query = "Dataset.id [datasetType.name IN :types] <-> DatasetParameter [modTime >= :lower] ";
			query = query.replace(":lower", tsb + dfout.format(date) + tse).replace(":types", "('GS', 'GQ')");
			List<?> results = BeanManager.search("CIC", query, em).getList();
			assertEquals("Count", 1, results.size());
		}

	}

	@Test
	public void t14() throws Exception {
		addMembers();
		addRules();
		addData();
		String query = "COUNT (Dataset.id)  [datasetType.name IN :types] <-> DatasetParameter[parameterType.name = 'TIMESTAMP' AND dateTimeValue BETWEEN :lower AND :upper]";
		query = query.replace(":lower", tsb + dfout.format(invStartDate) + tse)
				.replace(":upper", tsb + dfout.format(invEndDate) + tse).replace(":types", "('GS', 'GQ')");
		List<?> results = BeanManager.search("CIC", query, em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Result", 1L, results.get(0));
	}

	@Test
	public void t15() throws Exception {
		addMembers();
		addRules();
		addData();
		List<?> results = BeanManager.search("CIC", "Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 2, results.size());
		assertEquals("Result", "fred", results.get(0));
		assertEquals("Result", "bill", results.get(1));
		results = BeanManager.search("CIC", ",1 Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "fred", results.get(0));
		results = BeanManager.search("CIC", "1, Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "bill", results.get(0));
		results = BeanManager.search("CIC", "1,1 Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 1, results.size());
		assertEquals("Result", "bill", results.get(0));
		results = BeanManager.search("CIC", "100,1 Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 0, results.size());
		results = BeanManager.search("CIC", "0,100 Datafile.name ORDER BY id", em).getList();
		assertEquals("Count", 2, results.size());
		assertEquals("Result", "fred", results.get(0));
		assertEquals("Result", "bill", results.get(1));
	}

	private void addMembers() throws Exception {
		RuleManager.oldAddUserGroupMember("user-office", "uo", em);
		RuleManager.oldAddUserGroupMember("CIC-user", "CIC", em);
		RuleManager.oldAddUserGroupMember("expt-A", "A1", em);
		RuleManager.oldAddUserGroupMember("expt-B", "B1", em);
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
		emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
	}

	@AfterClass
	public static void AfterClassTearDown() {
		emf.close();
	}

}
