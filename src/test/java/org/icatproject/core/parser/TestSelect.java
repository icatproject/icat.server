package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

public class TestSelect {

	@Test
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELEC ds FROM Dataset ds");
		try {
			new SelectClause(new Input(tokens));
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("Expected token from types [SELECT] at token SELEC in < SELEC > ds FROM ",
					e.getMessage());
		}
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT ds FROM");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" ds", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(1, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("ds"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT c.name, c.capital.name FROM Country c");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" c.name , c.capital.name", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(2, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("c.name"));
		assertTrue(clause.getIdPaths().contains("c.capital.name"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());
	}

	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT count(e) FROM Employee e");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" COUNT ( e )", clause.toString());
		assertTrue(clause.isCount());
		assertEquals(1, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("e"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());
	}

	@Test
	public void testGood4() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT e, a FROM Employee e, MailingAddress a WHERE e.address = a.address");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" e , a", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(2, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("e"));
		assertTrue(clause.getIdPaths().contains("a"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());
	}

	@Test
	public void testGood5() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT COUNT(df), max(df.id), min(df.id) FROM Datafile df");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" COUNT ( df ) , MAX ( df.id ) , MIN ( df.id )", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(2, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("df"));
		assertTrue(clause.getIdPaths().contains("df.id"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());
	}

	@Test
	public void testList1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("SELECT f.investigations FROM Facility f");
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" f.investigations", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(1, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("f.investigations"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());

		clause.replace("f.investigations", "a0");
		assertEquals(" a0", clause.toString());
	}

	@Test
	public void testList2() throws Exception {
		String query = "SELECT f.investigations, a0, ff.investigations FROM Facility f "
				+ "JOIN f.investigationTypes a0 JOIN f.facilityCycles ff";
		List<Token> tokens = Tokenizer.getTokens(query);
		Input input = new Input(tokens);
		SelectClause clause = new SelectClause(input);
		assertEquals(" f.investigations , a0 , ff.investigations", clause.toString());
		assertFalse(clause.isCount());
		assertEquals(3, clause.getIdPaths().size());
		assertTrue(clause.getIdPaths().contains("f.investigations"));
		assertTrue(clause.getIdPaths().contains("a0"));
		assertTrue(clause.getIdPaths().contains("ff.investigations"));
		assertEquals(Token.Type.FROM, input.peek(0).getType());

		clause.replace("f.investigations", "a1");
		assertEquals(" a1 , a0 , ff.investigations", clause.toString());
	}

}