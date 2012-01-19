package uk.icat3.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.BaseTestTransaction;

public class TestInvestigationManager extends BaseTestTransaction {

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
	}

	@Test
	public void testCreateInvestigation() throws Exception {
		int nInvestigations = SearchManager.search("P1", "Investigation", em).getList().size();

		Investigation inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		assertEquals("42", inv.getInvNumber());
		assertNotNull(inv.getId());
		assertEquals("createId", "P1", inv.getCreateId());
		assertEquals("modId", "P1", inv.getModId());
	}

	/**
	 * Test of updateInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testUpdateInvestigation() throws Exception {
		int nInvestigations = SearchManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		inv.setGrantId(7000L);

		BeanManager.update("P2", inv, em);
		inv = (Investigation) BeanManager.get("P2", "Investigation", invId, em).getBean();

		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		assertEquals("42", inv.getInvNumber());
		assertEquals(invId, inv.getId());
		assertEquals((Long) 7000L, inv.getGrantId());

		assertEquals("createId", "P1", inv.getCreateId());
		assertEquals("modId", "P2", inv.getModId());
	}

	/**
	 * Test of removeInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testRemoveInvestigation() throws Exception {
		int nInvestigations = SearchManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		inv = new Investigation();
		inv.setId(invId);

		BeanManager.delete("P2", inv, em);
		assertEquals("Size", nInvestigations, SearchManager.search("P1", "Investigation", em).getList().size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRemoveInvestigationBadUser() throws Exception {
		int nInvestigations = SearchManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		inv = new Investigation();
		inv.setId(invId);

		BeanManager.delete("P3", inv, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateInvestigationNotExists() throws Exception {
		int nInvestigations = SearchManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());

		inv = new Investigation();

		inv.setId(7890423782437L);
		assertEquals("Size", nInvestigations + 1, SearchManager.search("P1", "Investigation", em).getList().size());

		BeanManager.delete("P2", inv, em);
	}

}