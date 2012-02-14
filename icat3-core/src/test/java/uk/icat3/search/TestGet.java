package uk.icat3.search;

import static org.junit.Assert.assertEquals;

import java.util.Date;

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
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.BeanManager;
import uk.icat3.util.RuleManager;
import uk.icat3.util.TestConstants;

public class TestGet {

	private static Logger log = Logger.getLogger(TestGet.class);
	private static EntityManagerFactory emf;
	private EntityManager em;
	private Date invStartDate;
	private Date invEndDate;

	private Long dsId;
	private Facility facility;
	private InvestigationType invType;
	private DatasetType dst;

	private void addRules() throws Exception {
		RuleManager.oldAddRule("user-office", "Investigation", "CRUD", "  [id=2]  ", em);
		RuleManager.oldAddRule("user-office", "Investigation", "CRUD", null, em);
		RuleManager.oldAddRule("user-office", "Datafile", "CRUD", "[name='fred']", em);
		RuleManager.oldAddRule("CIC-user", "Dataset", "CRUD", null, em);
		RuleManager.oldAddRule("CIC-user", "DatasetParameter", "CRUD", null, em);
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

		invType = new InvestigationType();
		invType.setName("TestExperiment");
		BeanManager.create("uo", invType, em);

		dst = new DatasetType();
		dst.setName("GQ");
		BeanManager.create("uo", dst, em);
	}

	private void addData() throws Exception {
		Investigation inv = new Investigation();
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(invType);
		invStartDate = new Date();
		invEndDate = new Date(invStartDate.getTime() + 5000);
		inv.setStartDate(invStartDate);
		inv.setEndDate(invEndDate);
		inv.setFacility(facility);
		inv.setId((Long) BeanManager.create("uo", inv, em).getPk());

		ParameterType p = new ParameterType();
		p.setDescription("F is not a wibble");
		p.setName("TIMESTAMP");
		p.setUnits("TIMESTAMP");
		p.setApplicableToDataset(true);
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		BeanManager.create("uo", p, em);

		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setType(dst);
		dataset.setInvestigation(inv);

		Datafile fred = new Datafile();
		fred.setName("fred");
		dataset.getDatafiles().add(fred);
		Datafile bill = new Datafile();
		bill.setName("bill");
		dataset.getDatafiles().add(bill);
		dataset.setId((Long) BeanManager.create("CIC", dataset, em).getPk());

		DatasetParameter dsp = new DatasetParameter();
		dsp.setParameterType(p);
		dsp.setDataset(dataset);
		dsp.setDateTimeValue(invStartDate);
		BeanManager.create("uo", dsp, em);

		dsId = dataset.getId();
	}

	@Test
	public void t1() throws Exception {
		assertEquals("Wibble", ((Dataset) BeanManager.get("CIC", "Dataset", dsId, em).getBean()).getName());
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void t2() throws Exception {
		BeanManager.get("CIC", "Dataset", dsId + 1, em);
	}

	@Test
	public void t3() throws Exception {
		assertEquals("Wibble", ((Dataset) BeanManager
				.get("CIC", "Dataset INCLUDE Datafile, DatasetParameter", dsId, em).getBean()).getName());
	}

	@Test(expected = BadParameterException.class)
	public void t4() throws Exception {
		Dataset ds = (Dataset) BeanManager.get("CIC", "Dataset INCLUDE Investigator", dsId, em).getBean();
		System.out.println(ds.getName());
	}

	@Test(expected = BadParameterException.class)
	public void t5() throws Exception {
		Dataset ds = (Dataset) BeanManager.get("CIC", "Dataset INCLUDE Wible", dsId, em).getBean();
		System.out.println(ds.getName());
	}

	private void addMembers() throws Exception {
		RuleManager.oldAddUserGroupMember("user-office", "uo", em);
		RuleManager.oldAddUserGroupMember("CIC-user", "CIC", em);
		RuleManager.oldAddUserGroupMember("expt-A", "A1", em);
		RuleManager.oldAddUserGroupMember("expt-B", "B1", em);
	}

	@Before
	public void beginTX() throws Exception {
		em = emf.createEntityManager();
		em.getTransaction().begin();
		addMembers();
		addRules();
		addData();
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
