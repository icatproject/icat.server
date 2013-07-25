package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.icatproject.core.entity.Dataset;
import org.junit.Test;

public class TestRuleWhat {

	@Test(expected = ParserException.class)
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' "
				+ "INCLUDE ds.datafile.parameters, ds.parameters " + "LIMIT 12, 50");
		new RuleWhat(new Input(tokens));
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' ");
		Input input = new Input(tokens);
		RuleWhat sq = new RuleWhat(input);
		assertEquals("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP'", sq.toString());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test(expected = ParserException.class)
	public void testBad2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT COUNT(ds) FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP' " + "LIMIT 12, 50");
		new RuleWhat(new Input(tokens));
	}

}