package uk.icat3.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.BaseTestTransaction;
import uk.icat3.util.RuleManager;

public class TestInvestigationManager extends BaseTestTransaction {

	private Facility f;
	private InvestigationType type;

	@Before
	public void setUpManager() throws Exception {
		RuleManager.oldAddUserGroupMember("Group", "P1", em);
		RuleManager.oldAddUserGroupMember("Group", "P2", em);
		RuleManager.oldAddRule("Group", "Investigator", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "User", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "Investigation", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "InvestigationType", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "Facility", "CRUD", null, em);

		f = new Facility();
		f.setName("ISIS");
		f.setDaysUntilRelease(90);
		BeanManager.create("P1", f, em);

		type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);
	}

	@Test
	public void testCreateInvestigation() throws Exception {
		int nInvestigations = BeanManager.search("P1", "Investigation", em).getList().size();

		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		assertEquals("42", inv.getName());
		assertNotNull(inv.getId());
		assertEquals("createId", "P1", inv.getCreateId());
		assertEquals("modId", "P1", inv.getModId());
	}

	/**
	 * Test of updateInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testUpdateInvestigation() throws Exception {
		int nInvestigations = BeanManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		Date today = new Date();
		inv.setReleaseDate(today);

		BeanManager.update("P2", inv, em);
		inv = (Investigation) BeanManager.get("P2", "Investigation", invId, em).getBean();

		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		assertEquals("42", inv.getName());
		assertEquals(invId, inv.getId());
		assertEquals(today, inv.getReleaseDate());

		assertEquals("createId", "P1", inv.getCreateId());
		assertEquals("modId", "P2", inv.getModId());
	}

	/**
	 * Test of removeInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testRemoveInvestigation() throws Exception {
		int nInvestigations = BeanManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		inv = new Investigation();
		inv.setId(invId);

		BeanManager.delete("P2", inv, em);
		assertEquals("Size", nInvestigations, BeanManager.search("P1", "Investigation", em).getList().size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRemoveInvestigationBadUser() throws Exception {
		int nInvestigations = BeanManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());
		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		Long invId = inv.getId();
		inv = new Investigation();
		inv.setId(invId);

		BeanManager.delete("P3", inv, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateInvestigationNotExists() throws Exception {
		int nInvestigations = BeanManager.search("P1", "Investigation", em).getList().size();
		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());

		inv = new Investigation();

		inv.setId(7890423782437L);
		assertEquals("Size", nInvestigations + 1, BeanManager.search("P1", "Investigation", em).getList().size());

		BeanManager.delete("P2", inv, em);
	}

}