package uk.icat3.manager;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.BaseTestTransaction;
import uk.icat3.util.ParameterValueType;

public class TestParameterManager extends BaseTestTransaction {

	@Before
	public void setUpManager() throws Exception {
		RuleManager.addUserGroupMember("PHacker", "P1", em);
		RuleManager.addUserGroupMember("PHacker", "P2", em);
		RuleManager.addRule("PHacker", "Parameter", "CRUD", null, em);
	}

	private Parameter createKnownParameter() throws Exception {
		return createParameter("test-length", "test-units", true, ParameterValueType.NUMERIC, false, true, true, true,
				true);
	}

	/**
	 * Test of createParameter method, of class ParameterManager.
	 */
	@Test
	public void testCreateParameter() throws Exception {
		int nParms = InvestigationSearch.listAllParameters(em).size();
		Parameter pm = createKnownParameter();
		pm.setParameterPK((ParameterPK) BeanManager.create("P1", pm, em).getPk());
		pm = (Parameter) BeanManager.get("P1", "Parameter", pm.getPK(), em).getBean();
		Parameter pmref = createKnownParameter();
		assertEquals(pmref.getParameterPK().getName(), pm.getParameterPK().getName());
		assertEquals(pmref.getParameterPK().getUnits(), pm.getParameterPK().getUnits());
		assertEquals(pmref.isSampleParameter(), pm.isSampleParameter());
		assertEquals(pmref.isDatasetParameter(), pm.isDatasetParameter());
		assertEquals(pmref.isDatafileParameter(), pm.isDatafileParameter());
		assertEquals(pmref.isDatafileParameter(), pm.isDatafileParameter());
		assertEquals("createId", "P1", pm.getCreateId());
		assertEquals("modId", "P1", pm.getModId());
		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
	}

	/**
	 * Test of updateParameter method, of class ParameterManager.
	 */
	@Test
	public void testUpdateParameter() throws Exception {
		int nParms = InvestigationSearch.listAllParameters(em).size();

		Parameter orig = createKnownParameter();
		orig.setParameterPK((ParameterPK) BeanManager.create("P1", orig, em).getPk());
		orig = (Parameter) BeanManager.get("P1", "Parameter", orig.getPK(), em).getBean();

		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
		Parameter newP = createKnownParameter();
		newP.setDescription("wibble");
		BeanManager.update("P2", newP, em);
		Parameter result = (Parameter) BeanManager.get("P1", "Parameter", orig.getPK(), em).getBean();

		assertEquals(orig.getParameterPK().getName(), result.getParameterPK().getName());
		assertEquals(orig.getParameterPK().getUnits(), result.getParameterPK().getUnits());
		assertEquals(orig.isSampleParameter(), result.isSampleParameter());
		assertEquals(orig.isDatasetParameter(), result.isDatasetParameter());
		assertEquals(orig.isDatafileParameter(), result.isDatafileParameter());
		assertEquals(orig.isDatafileParameter(), result.isDatafileParameter());
		assertEquals("wibble", result.getDescription());
		assertEquals("createId", "P1", result.getCreateId());
		assertEquals("modId", "P2", result.getModId());
		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
	}

	/**
	 * Test of removeParameter method, of class ParameterManager.
	 */
	@Test
	public void testRemoveParameter() throws Exception {
		int nParms = InvestigationSearch.listAllParameters(em).size();
		BeanManager.create("P1", createKnownParameter(), em);
		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
		Parameter newP = createKnownParameter();
		BeanManager.delete("P2", newP, em);
		assertEquals("Size", nParms, InvestigationSearch.listAllParameters(em).size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testRemoveParameterBadUser() throws Exception {
		int nParms = InvestigationSearch.listAllParameters(em).size();
		BeanManager.create("P1", createKnownParameter(), em);
		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
		Parameter newP = createKnownParameter();
		BeanManager.delete("P3", newP, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateParameterNotExists() throws Exception {
		int nParms = InvestigationSearch.listAllParameters(em).size();
		BeanManager.create("P1", createKnownParameter(), em);
		assertEquals("Size", nParms + 1, InvestigationSearch.listAllParameters(em).size());
		Parameter newP = createKnownParameter();
		newP.getParameterPK().setName("Bad");
		BeanManager.delete("P3", newP, em);
	}

}