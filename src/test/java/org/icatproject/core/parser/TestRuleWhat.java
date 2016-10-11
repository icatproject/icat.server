package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.icatproject.core.entity.Dataset;
import org.junit.Test;

public class TestRuleWhat {

	@Test
	public void testBad1() throws Exception {

		try {
			new RuleWhat("SELECT ds FROM Dataset ds JOIN ds.parameters p WHERE p.type.name = 'TIMESTAMP' "
					+ "INCLUDE ds.datafile.parameters, ds.parameters " + "LIMIT 12, 50");
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("No more input expected at token INCLUDE in = TIMESTAMP < INCLUDE > ds.datafile.parameters , ",
					e.getMessage());
		}
	}

	@Test
	public void testBad2() throws Exception {

		try {
			new RuleWhat("SELECT ds.investigation.name FROM Dataset ds");
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("class org.icatproject.core.entity.Dataset does not have attribute investigation.name",
					e.getMessage());
		}
	}

	@Test
	public void testBad3() throws Exception {

		try {
			new RuleWhat("SELECT count(ds) FROM Dataset ds");
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("Expected token from types [NAME] at token COUNT in SELECT < COUNT > ( ds ", e.getMessage());
		}
	}

	@Test
	public void testBad4() throws Exception {

		try {
			new RuleWhat("SELECT df, ds FROM Datafile, df.dataset ds");
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("Expected token from types [FROM] at token , in SELECT df < , > ds FROM ", e.getMessage());
		}
	}

	@Test
	public void testGood1() throws Exception {
		RuleWhat sq = new RuleWhat("SELECT ds FROM Dataset ds JOIN ds.parameters p WHERE p.type.name = 'TIMESTAMP' ");
		assertEquals(" Dataset ds JOIN ds.parameters p", sq.getFrom());
		assertEquals(" p.type.name = 'TIMESTAMP'", sq.getWhere());
		assertNull(sq.getAttribute());
		assertEquals(Dataset.class, sq.getBean());
	}

	@Test
	public void testGood2() throws Exception {

		RuleWhat sq = new RuleWhat("SELECT ds FROM Dataset ds WHERE ds.name LIKE 'PUBLIC%' ");
		assertEquals(" Dataset ds", sq.getFrom());
		assertNull(sq.getAttribute());
		assertEquals(Dataset.class, sq.getBean());

	}

	@Test
	public void testGoodAttribute() throws Exception {

		RuleWhat sq = new RuleWhat("SELECT ds.doi FROM Dataset ds WHERE ds.name LIKE 'PUBLIC%' ");
		assertEquals(" Dataset ds", sq.getFrom());
		assertEquals(" ds.name LIKE 'PUBLIC%'", sq.getWhere());
		assertEquals("doi", sq.getAttribute());
		assertEquals(Dataset.class, sq.getBean());

	}

}