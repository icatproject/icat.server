
import static org.junit.Assert.fail;

import java.util.Date;

import org.icatproject.Dataset;
import org.icatproject.DatasetType;
import org.icatproject.Facility;
import org.icatproject.InsufficientPrivilegesException;
import org.icatproject.Investigation;
import org.icatproject.InvestigationType;
import org.icatproject.NoSuchObjectFoundException;
import org.icatproject.ObjectAlreadyExistsException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGateKeeper extends BaseTest {

	private static EntityManagerFactory emf;

	private static EntityManager em;

	@Test
	public void testAddMemberTwice() throws Exception {
		String gName = "g1";
		String mName = "m1";
		RuleManager.addUserGroupMember("root", gName, mName, em);
		try {
			RuleManager.addUserGroupMember("root", gName, mName, em);
		} catch (ObjectAlreadyExistsException e) {
			return;
		}
		fail("Should have thrown ObjectAlreadyExistsException");
	}

	private void addMembers() throws Exception {
		RuleManager.addUserGroupMember("root", "user-office", "uo", em);
		RuleManager.addUserGroupMember("root", "CIC-user", "CIC", em);
		RuleManager.addUserGroupMember("root", "expt-A", "A1", em);
		RuleManager.addUserGroupMember("root", "Group", "P1", em);
	}

	@Test
	public void testRead1() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();

		Facility f = new Facility();
		f.setName("None");
		f.setDaysUntilRelease(90);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);

		DatasetType dst = createDatasetType("GQ", "wibble");
		BeanManager.create("P1", dst, em);

		Investigation inv = new Investigation();
		inv.setName("B");
		inv.setTitle("Not null");
		inv.setType(type);
		inv.setStartDate(new Date());
		inv.setEndDate(new Date());
		inv.setFacility(f);
		inv.setId((Long) BeanManager.create("uo", inv, em).getPk());
		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setType(dst);
		dataset.setInvestigation(inv);
		dataset.setId((Long) BeanManager.create("CIC", dataset, em).getPk());
		GateKeeper.performAuthorisation("CIC", dataset, AccessType.READ, em);
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRead2() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();
		Dataset dataset = new Dataset();
		GateKeeper.performAuthorisation("A1", dataset, AccessType.READ, em);
	}

	@Test
	public void testRead3() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();

		Facility f = new Facility();
		f.setName("ISIS");
		f.setDaysUntilRelease(90);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);

		DatasetType dst = createDatasetType("GQ", "wibble");
		BeanManager.create("P1", dst, em);

		Investigation inv = new Investigation();
		inv.setName("A");
		inv.setTitle("Not null");
		inv.setType(type);
		inv.setStartDate(new Date());
		inv.setEndDate(new Date());
		inv.setFacility(f);
		inv.setId((Long) BeanManager.create("uo", inv, em).getPk());
		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setType(dst);
		dataset.setInvestigation(inv);
		BeanManager.create("CIC", dataset, em);
		GateKeeper.performAuthorisation("A1", dataset, AccessType.READ, em);
	}

	private void addRules() throws Exception {
		addRule("user-office", "Investigation  [id=2]", "CRUD");
		addRule("user-office", "Investigation", "CRUD");
		addRule("CIC-user", "Dataset", "CRUD");
		addRule("CIC-user", "DatasetParameter", "CRUD");
		addRule(null, "ParameterType", "R");
		addRule("expt-A", "Dataset <-> Investigation [name = 'A']", "R");
		addRule("expt-A",
				"DatasetParameter <-> Dataset <-> Investigation [name = 'A']",
				"R");
		addRule("expt-A", "Datafile Dataset <-> Investigation [name = 'A']",
				"R");
		addRule("expt-A",
				"DatafileParameter <-> Datafile <-> Dataset <-> Investigation [name = 'A']",
				"R");
		addRule(null,
				"Dataset <-> Investigation <-> Investigator [user.name = :user]",
				"R");
		addRule("Group", "InvestigationType", "CRUD");
		addRule("Group", "Facility", "CRUD");
		addRule("Group", "DatasetType", "CRUD");
	}

	private void addRule(String groupName, String what, String crudFlags)
			throws Exception {
		RuleManager.addRule("root", groupName, what, crudFlags, em);
	}

	@Before
	public void beginTX() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}

	@After
	public void closeTX() throws NoSuchObjectFoundException {
		em.getTransaction().rollback();
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
