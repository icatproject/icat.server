package uk.icat3.manager;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Facility;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.search.Search;
import uk.icat3.util.BaseTestTransaction;

public class TestDatafileManager extends BaseTestTransaction {

	private Dataset dataset;

	@Before
	public void setUpManager() throws Exception {
		RuleManager.addUserGroupMember("Group", "P1", em);
		RuleManager.addUserGroupMember("Group", "P2", em);
		RuleManager.addRule("Group", "Datafile", "CRUD", null, em);
		RuleManager.addRule("Group", "Dataset", "CRUD", null, em);
		RuleManager.addRule("Group", "DatasetType", "CRUD", null, em);
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

		Long invId = (Long) BeanManager.create("P1", createInvestigation("42", "Fred", "experiment", "ISIS"), em);
		BeanManager.create("P1", createDatasetType("dstype", "wibble"), em);
		dataset = createDataset(invId, "dsname", "dstype");
		dataset.getDatafileCollection().add(createDatafile(null, "dfname1"));
		dataset.getDatafileCollection().add(createDatafile(null, "dfname2"));
		dataset.setId((Long) BeanManager.create("P1", dataset, em));

	}

	@Test
	public void testCreate() throws Exception {
		int nDatafiles = Search.search("P1", "Datafile", em).size();

		Datafile datafile = createDatafile(dataset.getId(), "dfname3");
		Long id = (Long) BeanManager.create("P1", datafile, em);
		datafile = (Datafile) BeanManager.get("P1", "Datafile", id, em);

		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());

		assertEquals("dfname3", datafile.getName());
		assertEquals("createId", "P1", datafile.getCreateId());
		assertEquals("modId", "P1", datafile.getModId());
	}

	@Test
	public void testUpdate() throws Exception {
		int nDatafiles = Search.search("P1", "Datafile", em).size();
		Datafile datafile = createDatafile(dataset.getId(), "dfname3");
		Long id = (Long) BeanManager.create("P1", datafile, em);
		datafile = (Datafile) BeanManager.get("P1", "Datafile", id, em);

		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.update("P2", newP, em);
		newP = (Datafile) BeanManager.get("P2", "Datafile", datafile.getId(), em);
		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());
		assertEquals("dfname4", newP.getName());
		assertEquals("createId", "P1", newP.getCreateId());
		assertEquals("modId", "P2", newP.getModId());
	}

	@Test
	public void testDelete() throws Exception {
		int nDatafiles = Search.search("P1", "Datafile", em).size();

		Datafile datafile = createDatafile(dataset.getId(), "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em));
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em);

		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.delete("P2", newP, em);
		assertEquals("Size", nDatafiles, Search.search("P1", "Datafile", em).size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testDeleteBadUser() throws Exception {
		int nDatafiles = Search.search("P1", "Datafile", em).size();

		Datafile datafile = createDatafile(dataset.getId(), "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em));
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em);

		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.delete("P3", newP, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateNotExists() throws Exception {
		int nDatafiles = Search.search("P1", "Datafile", em).size();

		Datafile datafile = createDatafile(dataset.getId(), "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em));
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em);

		assertEquals("Size", nDatafiles + 1, Search.search("P1", "Datafile", em).size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(9873784293472L);
		BeanManager.delete("P3", newP, em);
	}

}