package uk.icat3.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Facility;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.BaseTestTransaction;

public class TestInvestigatorManager extends BaseTestTransaction {

	private Long invId;
	private String fu;

	@Before
	public void setUpManager() throws Exception {
		RuleManager.addUserGroupMember("Group", "P1", em);
		RuleManager.addUserGroupMember("Group", "P2", em);
		RuleManager.addRule("Group", "Investigator", "CRUD", null, em);
		RuleManager.addRule("Group", "FacilityUser", "CRUD", null, em);
		RuleManager.addRule("Group", "Investigation", "CRUD", null, em);
		RuleManager.addRule("Group", "InvestigationType", "CRUD", null, em);
		RuleManager.addRule("Group", "Facility", "CRUD", null, em);

		Facility f = new Facility();
		f.setFacilityShortName("ISIS");
		f.setDaysUntilRelease(90L);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);

		invId = (Long) BeanManager.create("P1", createInvestigation("42", "Fred", "experiment", "ISIS"), em).getPk();
		fu = (String) BeanManager.create("P1", createFacilityUser("freda"), em).getPk();
	}

	/**
	 * Test of createInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testCreateInvestigator() throws Exception {
		int nInvestigators = SearchManager.search("P1", "Investigator", em).getList().size();

		Investigator investigator = createInvestigator(fu, invId);
		investigator.setInvestigatorPK((InvestigatorPK) BeanManager.create("P1", createInvestigator(fu, invId), em)
				.getPk());
		investigator = (Investigator) BeanManager.get("P1", "Investigator", investigator.getPK(), em).getBean();

		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		assertEquals("freda", investigator.getInvestigatorPK().getFacilityUserId());
		assertEquals(invId, investigator.getInvestigatorPK().getInvestigationId());
		assertNull(investigator.getRole());
		assertEquals("createId", "P1", investigator.getCreateId());
		assertEquals("modId", "P1", investigator.getModId());
	}

	/**
	 * Test of updateInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testUpdateInvestigator() throws Exception {
		int nInvestigators = SearchManager.search("P1", "Investigator", em).getList().size();

		Investigator investigator = createInvestigator(fu, invId);
		investigator.setInvestigatorPK((InvestigatorPK) BeanManager.create("P1", createInvestigator(fu, invId), em)
				.getPk());
		investigator = (Investigator) BeanManager.get("P1", "Investigator", investigator.getPK(), em).getBean();
		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		Investigator newP = createInvestigator(fu, invId);
		newP.setRole("wibble");
		BeanManager.update("P2", newP, em);
		investigator = (Investigator) BeanManager.get("P2", "Investigator", newP.getPK(), em).getBean();
		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		assertEquals("freda", investigator.getInvestigatorPK().getFacilityUserId());
		assertEquals(invId, investigator.getInvestigatorPK().getInvestigationId());

		assertEquals("wibble", investigator.getRole());
		assertEquals("createId", "P1", investigator.getCreateId());
		assertEquals("modId", "P2", investigator.getModId());
	}

	/**
	 * Test of removeInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testRemoveInvestigator() throws Exception {
		int nInvestigators = SearchManager.search("P1", "Investigator", em).getList().size();
		Investigator investigator = createInvestigator(fu, invId);
		investigator.setInvestigatorPK((InvestigatorPK) BeanManager.create("P1", createInvestigator(fu, invId), em)
				.getPk());
		investigator = (Investigator) BeanManager.get("P1", "Investigator", investigator.getPK(), em).getBean();
		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		Investigator newP = createInvestigator(fu, invId);
		BeanManager.delete("P2", newP, em);
		assertEquals("Size", nInvestigators, SearchManager.search("P1", "Investigator", em).getList().size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRemoveInvestigatorBadUser() throws Exception {
		int nInvestigators = SearchManager.search("P1", "Investigator", em).getList().size();
		Investigator investigator = createInvestigator(fu, invId);
		investigator.setInvestigatorPK((InvestigatorPK) BeanManager.create("P1", createInvestigator(fu, invId), em)
				.getPk());
		investigator = (Investigator) BeanManager.get("P1", "Investigator", investigator.getPK(), em).getBean();
		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		Investigator newP = createInvestigator(fu, invId);
		BeanManager.delete("P3", newP, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateInvestigatorNotExists() throws Exception {
		int nInvestigators = SearchManager.search("P1", "Investigator", em).getList().size();
		Investigator investigator = createInvestigator(fu, invId);
		investigator.setInvestigatorPK((InvestigatorPK) BeanManager.create("P1", createInvestigator(fu, invId), em)
				.getPk());
		investigator = (Investigator) BeanManager.get("P1", "Investigator", investigator.getPK(), em).getBean();
		assertEquals("Size", nInvestigators + 1, SearchManager.search("P1", "Investigator", em).getList().size());

		BeanManager.create("P1", createFacilityUser("fritz"), em);
		Investigator newP = createInvestigator("fritz", invId);
		BeanManager.delete("P2", newP, em);
	}

}