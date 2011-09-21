package uk.icat3.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.entity.Dataset;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.manager.BeanManager;
import uk.icat3.manager.RuleManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTest;
import uk.icat3.util.TestConstants;

public class TestGateKeeper extends BaseTest {

	private static EntityManagerFactory emf;

	private static EntityManager em;

	private List<Long> rules;

	@Test
	public void testAddMemberTwice() throws Exception {
		String gName = "g1";
		String mName = "m1";
		RuleManager.addUserGroupMember(gName, mName, em);
		try {
			RuleManager.addUserGroupMember(gName, mName, em);
		} catch (ObjectAlreadyExistsException e) {
			return;
		} finally {
			RuleManager.removeUserGroupMember(gName, mName, em);
		}
		fail("Should have thrown ObjectAlreadyExistsException");
	}

	@Test
	public void testAddAndRemoveMember() throws Exception {
		int ngroups = RuleManager.listUserGroups(em).size();
		String gName = "g1";
		String mName = "m1";
		RuleManager.addUserGroupMember(gName, mName, em);
		assertEquals("Count", ngroups + 1, RuleManager.listUserGroups(em).size());
		RuleManager.removeUserGroupMember(gName, mName, em);
		assertEquals("Count", ngroups, RuleManager.listUserGroups(em).size());
	}

	@Test
	public void removeNonExistentMember() throws Exception {
		String gName = "g1";
		String mName = "m1";
		try {
			RuleManager.removeUserGroupMember(gName, mName, em);
		} catch (NoSuchObjectFoundException e) {
			return;
		} finally {

		}
		fail("Should have thrown NoSuchObjectFoundException");
	}

	@Test
	public void testAddRuleTwice() throws Exception {
		int nrules = RuleManager.listRules(em).size();
		long r1 = RuleManager.addRule("g1", "Dataset", "R", null, em);
		long r2 = RuleManager.addRule("g1", "Dataset", "R", null, em);
		assertEquals("Count", nrules + 2, RuleManager.listRules(em).size());
		RuleManager.removeRule(r1, em);
		RuleManager.removeRule(r2, em);
	}

	@Test
	public void removeNonExistentRule() throws Exception {
		try {
			RuleManager.removeRule(42L, em);
		} catch (NoSuchObjectFoundException e) {
			return;
		}
		fail("Should have thrown NoSuchObjectFoundException");
	}

	private void addMembers() throws ObjectAlreadyExistsException {
		addMember("user-office", "uo", em);
		addMember("CIC-user", "CIC", em);
		addMember("expt-A", "A1", em);
		addMember("Group", "P1", em);
	}

	private void addMember(String name, String member, EntityManager em) throws ObjectAlreadyExistsException {
		RuleManager.addUserGroupMember(name, member, em);
	}

	@Test
	public void testRead1() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();
		Dataset dataset = new Dataset();
		GateKeeper.performAuthorisation("CIC", dataset, AccessType.READ, em);
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRead2() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();
		
		Facility f = new Facility();
		f.setFacilityShortName("ISIS");
		f.setDaysUntilRelease(90L);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);
		
		BeanManager.create("P1", createDatasetType("GQ", "wibble"), em);
		
		Investigation inv = new Investigation();
		inv.setInvNumber("B");
		inv.setTitle("Not null");
		inv.setInvType("experiment");
		inv.setInvStartDate(new Date());
		inv.setInvEndDate(new Date());
		inv.setFacility("ISIS");
		inv.setId((Long) BeanManager.create("uo", inv, em));
		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setDatasetType("GQ");
		dataset.setInvestigationId(inv.getId());
		BeanManager.create("CIC", dataset, em);
		GateKeeper.performAuthorisation("A1", dataset, AccessType.READ, em);
	}

	@Test
	public void testRead3() throws Exception {
		em.getTransaction().setRollbackOnly();
		addMembers();
		addRules();
		
		Facility f = new Facility();
		f.setFacilityShortName("ISIS");
		f.setDaysUntilRelease(90L);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);
		
		BeanManager.create("P1", createDatasetType("GQ", "wibble"), em);
		
		Investigation inv = new Investigation();
		inv.setInvNumber("A");
		inv.setTitle("Not null");
		inv.setInvType("experiment");
		inv.setInvStartDate(new Date());
		inv.setInvEndDate(new Date());
		inv.setFacility("ISIS");
		inv.setId((Long) BeanManager.create("uo", inv, em));
		Dataset dataset = new Dataset();
		dataset.setName("Wibble");
		dataset.setDatasetType("GQ");
		dataset.setInvestigationId(inv.getId());
		BeanManager.create("CIC", dataset, em);
		GateKeeper.performAuthorisation("A1", dataset, AccessType.READ, em);
	}

	private void addRules() throws Exception {
		addRule("user-office", "Investigation", "CRUD", "  [id=2]  ", em);
		addRule("user-office", "Investigation", "CRUD", null, em);
		addRule("CIC-user", "Dataset", "CRUD", null, em);
		addRule("CIC-user", "DatasetParameter", "CRUD", null, em);
		addRule(null, "Parameter", "R", null, em);
		addRule("expt-A", "Dataset", "R", "Investigation [invNumber = 'A']", em);
		addRule("expt-A", "DatasetParameter", "R", "Dataset <-> Investigation [invNumber = 'A']", em);
		addRule("expt-A", "Datafile", "R", "Dataset <-> Investigation [invNumber = 'A']", em);
		addRule("expt-A", "DatafileParameter", "R", "<-> Datafile <-> Dataset <-> Investigation [invNumber = 'A']", em);
		addRule(null, "Dataset", "R", "<-> Investigation <-> Investigator [facilityUser.facilityUserId = :user]", em);
		addRule("Group", "InvestigationType", "CRUD", null, em);
		addRule("Group", "Facility", "CRUD", null, em);
		addRule("Group", "DatasetType", "CRUD", null, em);
	}

	private void addRule(String groupName, String what, String crud, String restriction, EntityManager em)
			throws BadParameterException, IcatInternalException {
		rules.add(RuleManager.addRule(groupName, what, crud, restriction, em));
	}

	@Before
	public void beginTX() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
		rules = new ArrayList<Long>();
	}

	@After
	public void closeTX() throws NoSuchObjectFoundException {
		em.getTransaction().rollback();
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
