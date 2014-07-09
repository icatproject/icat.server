package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.icatproject.core.entity.Dataset;
import org.junit.Test;

public class TestRuleWhat {

	@Test
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' "
				+ "INCLUDE ds.datafile.parameters, ds.parameters " + "LIMIT 12, 50");
		try {
			new RuleWhat(new Input(tokens));
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals(
					"No more input expected at token INCLUDE in = TIMESTAMP < INCLUDE > ds.datafile.parameters , ",
					e.getMessage());
		}
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' ");
		Input input = new Input(tokens);
		RuleWhat sq = new RuleWhat(input);
		assertEquals(" Dataset $0$ JOIN $0$.parameters $1$", sq.getCrudFrom());
		assertEquals(" LEFT JOIN $0$.parameters $1$", sq.getFrom());
		assertEquals(" $1$.type.name = 'TIMESTAMP'", sq.getWhere());
		assertEquals(Dataset.class, sq.getBean());
		assertEquals(2, sq.getVarCount());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds  "
				+ "WHERE ds.name LIKE 'PUBLIC%' ");
		Input input = new Input(tokens);
		RuleWhat sq = new RuleWhat(input);
		assertEquals(" Dataset $0$", sq.getCrudFrom());
		assertEquals("", sq.getFrom());
		assertEquals(" $0$.name LIKE 'PUBLIC%'", sq.getWhere());
		assertEquals(Dataset.class, sq.getBean());
		assertEquals(1, sq.getVarCount());
		assertNull(input.peek(0));
	}

	@Test
	public void testBad2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT COUNT(ds) FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP' " + "LIMIT 12, 50");
		try {
			new RuleWhat(new Input(tokens));
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals(
					"Expected token from types [NAME, DISTINCT] at token COUNT in SELECT < COUNT > ( ds ",
					e.getMessage());
		}
	}

}