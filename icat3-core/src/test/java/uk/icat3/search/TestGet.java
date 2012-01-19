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
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.manager.BeanManager;
import uk.icat3.manager.RuleManager;
import uk.icat3.util.ParameterValueType;
import uk.icat3.util.TestConstants;

public class TestGet {

	private static Logger log = Logger.getLogger(TestGet.class);
	private static EntityManagerFactory emf;
	private EntityManager em;
	private Date invStartDate;
	private Date invEndDate;

	private Long invId;
	private Long dsId;

	private void addRules() throws Exception {
		RuleManager.addRule("user-office", "Investigation", "CRUD", "  [id=2]  ", em);
		RuleManager.addRule("user-office", "Investigation", "CRUD", null, em);
		RuleManager.addRule("user-office", "Datafile", "CRUD", "[name='fred']", em);
		RuleManager.addRule("CIC-user", "Dataset", "CRUD", null, em);
		RuleManager.addRule("CIC-user", "DatasetParameter", "CRUD", null, em);
		RuleManager.addRule(null, "Parameter", "R", null, em);
		RuleManager.addRule("expt-A", "Dataset", "R", "Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "DatasetParameter", "R", "Dataset <-> Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "Datafile", "R", "Dataset <-> Investigation [invNumber = 'A']", em);
		RuleManager.addRule("expt-A", "DatafileParameter", "R",
				"<-> Datafile <-> Dataset <-> Investigation [invNumber = 'A']", em);
		RuleManager.addRule(null, "Dataset", "R",
				"<-> Investigation <-> Investigator [facilityUser.facilityUserId = :user]", em);

		RuleManager.addRule("user-office", "Facility", "CRUD", null, em);
		RuleManager.addRule("user-office", "InvestigationType", "CRUD", null, em);
		RuleManager.addRule("user-office", "DatasetType", "CRUD", null, em);
		RuleManager.addRule("user-office", "Parameter", "CRUD", null, em);
		RuleManager.addRule("user-office", "DatasetParameter", "CRUD", null, em);

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

	private void addData() throws Exception {
		Investigation inv = new Investigation();
		inv.setInvNumber("A");
		inv.setTitle("Not null");
		inv.setInvType("TestExperiment");
		invStartDate = new Date();
		invEndDate = new Date(invStartDate.getTime() + 5000);
		inv.setInvStartDate(invStartDate);
		inv.setInvEndDate(invEndDate);
		inv.setFacility("TestFacility");
		invId = (Long) BeanManager.create("uo", inv, em).getPk();

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
		dataset.setId((Long) BeanManager.create("CIC", dataset, em).getPk());

		DatasetParameterPK datasetParameterPK = new DatasetParameterPK();
		datasetParameterPK.setName("TIMESTAMP");
		datasetParameterPK.setUnits("TIMESTAMP");
		datasetParameterPK.setDatasetId(dataset.getId());
		DatasetParameter dsp = new DatasetParameter();
		dsp.setDatasetParameterPK(datasetParameterPK);
		dsp.setDateTimeValue(invStartDate);
		dsp.setParameter(p);
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

	private void addMembers() throws ObjectAlreadyExistsException {
		RuleManager.addUserGroupMember("user-office", "uo", em);
		RuleManager.addUserGroupMember("CIC-user", "CIC", em);
		RuleManager.addUserGroupMember("expt-A", "A1", em);
		RuleManager.addUserGroupMember("expt-B", "B1", em);
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
