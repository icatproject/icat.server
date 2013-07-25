package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.SearchQuery;
import org.icatproject.core.parser.Token;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;

public class TestSearch {

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' "
				+ "INCLUDE ds.datafile.parameters, ds.parameters " + "LIMIT 12, 50");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' "
				+ "INCLUDE ds.datafile.parameters, ds.parameters " + "LIMIT 12,50", sq.toString());
		assertEquals((Integer)12, sq.getOffset());
		assertEquals((Integer)50, sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT ds.id FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP' " + "LIMIT 12, 50");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT ds.id FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP' " + "LIMIT 12,50", sq.toString());
		assertEquals((Integer)12, sq.getOffset());
		assertEquals((Integer)50, sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT COUNT(ds) FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP'");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT COUNT(ds) FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP'", sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood4() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT COUNT(DISTINCT ds) FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP' ");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT COUNT(DISTINCT ds) FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name = 'TIMESTAMP'", sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood5() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name != 'TIMESTAMP' ORDER BY ds.name ASC");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT ds FROM Dataset ds JOIN ds.parameters p "
				+ "WHERE p.type.name != 'TIMESTAMP' ORDER BY ds.name ASC", sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}
	
	@Test
	public void testGood6() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT df.name FROM Datafile df ORDER BY df.id LIMIT 0,1");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT df.name FROM Datafile df ORDER BY df.id LIMIT 0,1", sq.toString());
		assertEquals((Integer)0, sq.getOffset());
		assertEquals((Integer)1, sq.getNumber());
		assertEquals(Datafile.class, sq.getBean());
		assertNull(input.peek(0));
	}

}