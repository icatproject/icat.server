package org.icatproject.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.icatproject.core.parser.GetQuery;
import org.icatproject.core.parser.IncludeClause;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.ParserException;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Before;
import org.junit.Test;

public class TestEntityBaseBean {

	private Datafile df3;

	@Before
	/*
	 * df3 -> dfp2, df3 -> ds1, ds1 -> inv4, inv4 > [ds1, ds5], inv4 -> invp6
	 */
	public void before() {
		Investigation inv4 = new Investigation();
		inv4.setId(4L);
		inv4.setName("An Inv");
		Dataset ds1 = new Dataset();
		ds1.setId(1l);
		ds1.setName("ds1");
		ds1.setInvestigation(inv4);
		Dataset ds5 = new Dataset();
		ds5.setId(5l);
		ds5.setName("ds5");
		ds5.setInvestigation(inv4);
		inv4.getDatasets().add(ds1);
		inv4.getDatasets().add(ds5);
		InvestigationParameter invp6 = new InvestigationParameter();
		invp6.setId(6l);
		invp6.setStringValue("invp6");
		inv4.getParameters().add(invp6);
		DatafileParameter dfp2 = new DatafileParameter();
		dfp2.setId(2L);
		dfp2.setNumericValue(42.0);
		df3 = new Datafile();
		df3.setId(3L);
		df3.setName("df3");
		df3.setDataset(ds1);
		df3.getParameters().add(dfp2);
	}

	private void assertBasic(Datafile df, Datafile clone) {
		assertEquals("df3", clone.getName());
		assertNull(clone.getDescription());
		assertEquals(df.getId(), clone.getId());
		assertEquals(df.getCreateId(), clone.getCreateId());
		assertEquals(df.getCreateTime(), clone.getCreateTime());
		assertEquals(df.getModId(), clone.getModId());
		assertEquals(df.getModTime(), clone.getModTime());
	}

	@Test
	public void testNoInclude() throws Exception {
		Datafile clone = (Datafile) df3.pruned(false, -1, null);
		assertBasic(df3, clone);
		assertNull(clone.getDataset());
		assertEquals(0, clone.getParameters().size());
	}

	@Test
	public void testIncludeone() throws Exception {
		Datafile clone = getClone(df3, "Datafile INCLUDE 1");
		assertBasic(df3, clone);
		assertEquals("ds1", clone.getDataset().getName());
		assertEquals(0, clone.getParameters().size());
	}

	private Datafile getClone(Datafile df, String queryString) throws Exception {
		GetQuery query = new GetQuery(new Input(Tokenizer.getTokens(queryString)));
		IncludeClause include = query.getInclude();
		Datafile clone;
		if (include == null) {
			clone = (Datafile) df.pruned(false, -1, null);
		} else {
			clone = (Datafile) df.pruned(include.isOne(), 0, include.getSteps());
		}
		assertBasic(df, clone);
		return clone;
	}

	@Test
	public void testIncludeDataset() throws Exception {
		Datafile clone = getClone(df3, "Datafile df INCLUDE df.dataset");
		assertBasic(df3, clone);
		assertEquals("ds1", clone.getDataset().getName());
		assertEquals(0, clone.getParameters().size());
	}

	@Test
	public void testIncludeDatafileParameters() throws Exception {
		Datafile clone = getClone(df3, "Datafile df INCLUDE df.parameters");
		assertNull(clone.getDataset());
		assertEquals(1, clone.getParameters().size());
	}

	@Test
	public void testIncludeBoth() throws Exception {
		Datafile clone = getClone(df3, "Datafile df INCLUDE df.dataset, df.parameters");
		assertEquals("ds1", clone.getDataset().getName());
		assertEquals(1, clone.getParameters().size());
	}

	@Test
	public void testIncludeAll() throws Exception {
		Datafile clone = getClone(df3,
				"Datafile df3 INCLUDE df3.dataset.investigation inv, df3.parameters, inv.datasets, inv.parameters");
		assertEquals("ds1", clone.getDataset().getName());
		assertEquals(1, clone.getParameters().size());
		Investigation inv4Clone = clone.getDataset().getInvestigation();
		assertEquals((Long) 4L, inv4Clone.getId());
		assertEquals(2, inv4Clone.getDatasets().size());
		assertEquals(1, inv4Clone.getParameters().size());
	}

	@Test
	public void testBad() throws Exception {
		try {
			getClone(df3,
					"Datafile df3 INCLUDE df3.dataset.inv inv, df3.parameters, inv.datasets, inv.parameters");
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("Problem with INCLUDE clause: inv is not a field of Dataset",
					e.getMessage());
		}
	}

}