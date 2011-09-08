package uk.icat3.manager;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Dataset;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.search.Search;
import uk.icat3.util.BaseTestTransaction;

public class TestDatasetManager extends BaseTestTransaction {

	private Investigation inv;

	@Before
	public void setUpManager() throws Exception {
		RuleManager.addUserGroupMember("Group", "P1", em);
		RuleManager.addUserGroupMember("Group", "P2", em);
		RuleManager.addRule("Group", "Dataset", "CRUD", null, em);
		RuleManager.addRule("Group", "Datafile", "CRUD", null, em);
		RuleManager.addRule("Group", "Investigation", "CRUD", null, em);
		RuleManager.addRule("Group", "DatasetType", "CRUD", null, em);
		RuleManager.addRule("Group", "InvestigationType", "CRUD", null, em);
		RuleManager.addRule("Group", "Facility", "CRUD", null, em);
		
		Facility f = new Facility();
		f.setFacilityShortName("ISIS");
		f.setDaysUntilRelease(90L);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);
		
		inv = createInvestigation("42", "Fred", "experiment", "ISIS");
		inv.setId((Long) BeanManager.create("P1",		inv, em));
		BeanManager.create("P1", createDatasetType("dstype", "wibble"), em);
	}


	@Test
	public void testCreateDataset() throws Exception {
		int nDatasets = Search.search("P1", "Dataset", em).size();
		int nDatafiles = Search.search("P1", "Datafile", em).size();
		Dataset ds = makeDs();
		assertEquals("Size", nDatasets + 1, Search.search("P1", "Dataset", em).size());
		assertEquals("Size", nDatafiles + 2, Search.search("P1", "Datafile", em).size());

		assertEquals("dsname", ds.getName());
		assertEquals("dfname1", ds.getDatafileCollection().iterator().next().getName());
		assertEquals("createId", "P1", ds.getCreateId());
		assertEquals("modId", "P1", ds.getModId());
	}
	
	@Test (expected = ObjectAlreadyExistsException.class)
	public void testCreateDatasetClash() throws Exception {
		makeDs();
		makeDs();
		
	}
	
	private Dataset makeDs() throws Exception {
		Dataset ds = createDataset(inv.getId(), "dsname", "dstype");
		ds.getDatafileCollection().add(createDatafile(null, "dfname1"));
		ds.getDatafileCollection().add(createDatafile(null, "dfname2"));
		ds.setId( (Long) BeanManager.create("P1", ds, em));
		ds = (Dataset) BeanManager.get("P1", "Dataset", ds.getId(), em);
		return ds;
	}

	/**
	 * Test of updateInvestigator method, of class InvestigatorManager.
	 */
	@Test
	public void testUpdateDataset() throws Exception {

		int nDatasets = Search.search("P1", "Dataset", em).size();
		int nDatafiles = Search.search("P1", "Datafile", em).size();
		Dataset ds = makeDs();
		assertEquals("Size", nDatasets + 1, Search.search("P1", "Dataset", em).size());
		assertEquals("Size", nDatafiles + 2, Search.search("P1", "Datafile", em).size());

		assertEquals("dsname", ds.getName());
		assertEquals("dfname1", ds.getDatafileCollection().iterator().next().getName());
		assertEquals("createId", "P1", ds.getCreateId());
		assertEquals("modId", "P1", ds.getModId());

		Long dsid = ds.getId();
		ds = createDataset(inv.getId(), "dsname", "dstype");
		ds.setId(dsid);
		ds.setLocation("guess");

		BeanManager.update("P2", ds, em);
		ds = (Dataset) BeanManager.get("P2", "Dataset", ds.getId(), em);
		
		assertEquals("Size", nDatasets + 1, Search.search("P1", "Dataset", em).size());
		assertEquals("Size", nDatafiles + 2, Search.search("P1", "Datafile", em).size());

		assertEquals("guess", ds.getLocation());

		assertEquals("dsname", ds.getName());
		assertEquals("dfname1", ds.getDatafileCollection().iterator().next().getName());
		assertEquals("createId", "P1", ds.getCreateId());
		assertEquals("modId", "P2", ds.getModId());
	}

	@Test
	public void testDeleteDataset() throws Exception {
		int nDatasets = Search.search("P1", "Dataset", em).size();
		int nDatafiles = Search.search("P1", "Datafile", em).size();
		Dataset ds = makeDs();
		Long dsid = ds.getId();

		ds = createDataset(inv.getId(), "dsname", "dstype");
		ds.setId(dsid);

		BeanManager.delete("P2", ds, em);
		assertEquals("Size", nDatasets, Search.search("P1", "Dataset", em).size());
		assertEquals("Size", nDatafiles, Search.search("P1", "Datafile", em).size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testDeleteBadUser() throws Exception {
		Dataset ds = makeDs();
		Long dsid = ds.getId();

		ds = createDataset(inv.getId(), "dsname", "dstype");
		ds.setId(dsid);

		BeanManager.delete("P3", ds, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateNotExists() throws Exception {
		Dataset ds = makeDs();

		ds = createDataset(inv.getId(), "dsname", "dstype");
		ds.setId(890325842309L);

		BeanManager.delete("P1", ds, em);
	}

}