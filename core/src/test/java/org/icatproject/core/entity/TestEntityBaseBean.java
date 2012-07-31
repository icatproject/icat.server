package org.icatproject.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class TestEntityBaseBean {

	private Datafile df;

	@Before
	public void before() {
		Dataset ds = new Dataset();
		ds.setId(1l);
		ds.setName("Fred");
		DatafileParameter dfp = new DatafileParameter();
		dfp.setId(2L);
		dfp.setNumericValue(42.0);
		df = new Datafile();
		df.setId(3L);
		df.setName("Bill");
		df.setDataset(ds);
		df.getParameters().add(dfp);
	}

	private void assertBasic(Datafile df, Datafile clone) {
		assertEquals("Bill", clone.getName());
		assertNull(clone.getDescription());
		assertEquals(df.getId(), clone.getId());
		assertEquals(df.getCreateId(), clone.getCreateId());
		assertEquals(df.getCreateTime(), clone.getCreateTime());
		assertEquals(df.getModId(), clone.getModId());
		assertEquals(df.getModTime(), clone.getModTime());
	}

	@Test
	public void testNoInclude() throws Exception {
		Datafile clone = (Datafile) df.pruned();
		assertBasic(df, clone);
		assertNull(clone.getDataset());
		assertEquals(0, clone.getParameters().size());
	}

	@Test
	public void testIncludeDataset() throws Exception {
		df.getIncludes().add(Dataset.class);
		Datafile clone = (Datafile) df.pruned();
		assertBasic(df, clone);
		assertEquals("Fred", clone.getDataset().getName());
		assertEquals(0, clone.getParameters().size());
	}

	@Test
	public void testIncludeDatafileParameters() throws Exception {
		df.getIncludes().add(DatafileParameter.class);
		Datafile clone = (Datafile) df.pruned();
		assertBasic(df, clone);
		assertNull(clone.getDataset());
		assertEquals((Double) 42.0, clone.getParameters().get(0).getNumericValue());
	}

	@Test
	public void testIncludeBoth() throws Exception {
		df.getIncludes().add(Dataset.class);
		df.getIncludes().add(DatafileParameter.class);
		Datafile clone = (Datafile) df.pruned();
		assertBasic(df, clone);
		assertEquals("Fred", clone.getDataset().getName());
		assertEquals((Double) 42.0, clone.getParameters().get(0).getNumericValue());
	}

}