package uk.icat3.manager;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.BaseTestTransaction;
import uk.icat3.util.RuleManager;

public class TestDatafileManager extends BaseTestTransaction {

	private Dataset dataset;

	@Before
	public void setUpManager() throws Exception {
		RuleManager.oldAddUserGroupMember("Group", "P1", em);
		RuleManager.oldAddUserGroupMember("Group", "P2", em);
		RuleManager.oldAddRule("Group", "Datafile", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "Dataset", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "DatasetType", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "Investigation", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "InvestigationType", "CRUD", null, em);
		RuleManager.oldAddRule("Group", "Facility", "CRUD", null, em);

		Facility f = new Facility();
		f.setName("ISIS");
		f.setDaysUntilRelease(90);
		BeanManager.create("P1", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("experiment");
		BeanManager.create("P1", type, em);

		Investigation inv = createInvestigation("42", "Fred", type, f);
		inv.setId((Long) BeanManager.create("P1", inv, em).getPk());

		DatasetType dst = createDatasetType("dstype", "wibble");
		BeanManager.create("P1", dst, em);
		dataset = createDataset(inv, "dsname", dst);
		dataset.getDatafiles().add(createDatafile(null, "dfname1"));
		dataset.getDatafiles().add(createDatafile(null, "dfname2"));
		dataset.setId((Long) BeanManager.create("P1", dataset, em).getPk());

	}

	@Test
	public void testCreate() throws Exception {
		int nDatafiles = BeanManager.search("P1", "Datafile", em).getList().size();

		Datafile datafile = createDatafile(dataset, "dfname3");
		Long id = (Long) BeanManager.create("P1", datafile, em).getPk();
		datafile = (Datafile) BeanManager.get("P1", "Datafile", id, em).getBean();

		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());

		assertEquals("dfname3", datafile.getName());
		assertEquals("createId", "P1", datafile.getCreateId());
		assertEquals("modId", "P1", datafile.getModId());
	}

	@Test
	public void testUpdate() throws Exception {
		int nDatafiles = BeanManager.search("P1", "Datafile", em).getList().size();
		Datafile datafile = createDatafile(dataset, "dfname3");
		Long id = (Long) BeanManager.create("P1", datafile, em).getPk();
		datafile = (Datafile) BeanManager.get("P1", "Datafile", id, em).getBean();

		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.update("P2", newP, em);
		newP = (Datafile) BeanManager.get("P2", "Datafile", datafile.getId(), em).getBean();
		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());
		assertEquals("dfname4", newP.getName());
		assertEquals("createId", "P1", newP.getCreateId());
		assertEquals("modId", "P2", newP.getModId());
	}

	@Test
	public void testDelete() throws Exception {
		int nDatafiles = BeanManager.search("P1", "Datafile", em).getList().size();

		Datafile datafile = createDatafile(dataset, "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em).getPk());
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em).getBean();

		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.delete("P2", newP, em);
		assertEquals("Size", nDatafiles, BeanManager.search("P1", "Datafile", em).getList().size());
	}

	@Test(expected = InsufficientPrivilegesException.class)
	public void testDeleteBadUser() throws Exception {
		int nDatafiles = BeanManager.search("P1", "Datafile", em).getList().size();

		Datafile datafile = createDatafile(dataset, "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em).getPk());
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em).getBean();

		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(datafile.getId());
		BeanManager.delete("P3", newP, em);
	}

	@Test(expected = NoSuchObjectFoundException.class)
	public void testUpdateNotExists() throws Exception {
		int nDatafiles = BeanManager.search("P1", "Datafile", em).getList().size();

		Datafile datafile = createDatafile(dataset, "dfname3");
		datafile.setId((Long) BeanManager.create("P1", datafile, em).getPk());
		datafile = (Datafile) BeanManager.get("P1", "Datafile", datafile.getId(), em).getBean();

		assertEquals("Size", nDatafiles + 1, BeanManager.search("P1", "Datafile", em).getList().size());
		Datafile newP = createDatafile(null, "dfname4");
		newP.setId(9873784293472L);
		BeanManager.delete("P3", newP, em);
	}

}