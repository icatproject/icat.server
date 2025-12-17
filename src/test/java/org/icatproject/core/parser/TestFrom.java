package org.icatproject.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationType;

import org.junit.jupiter.api.Test;

public class TestFrom {

	@Test
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("FRO Dataset ds");
		try {
			new FromClause(new Input(tokens), new HashSet<String>());
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals("Expected token from types [FROM] at token FRO in < FRO > Dataset ds ", e.getMessage());
		}
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("FROM Datafile df WHERE");
		Input input = new Input(tokens);
		Set<String> idPaths = new HashSet<>(Arrays.asList("df", "df.dataset", "df.name", "df.dataset.complete"));
		FromClause clause = new FromClause(input, idPaths);
		assertEquals(" Datafile df", clause.toString());
		assertEquals(2, clause.getAuthzMap().size());
		assertEquals(Datafile.class, clause.getAuthzMap().get("df.id"));
		assertEquals(Dataset.class, clause.getAuthzMap().get("df.dataset.id"));
		assertEquals(0, clause.getReplaceMap().size());
		assertEquals(Token.Type.WHERE, input.peek(0).getType());
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("FROM Datafile df, Dataset ds WHERE df.name = ds.name");
		Input input = new Input(tokens);
		Set<String> idPaths = new HashSet<>(Arrays.asList("df", "ds"));
		FromClause clause = new FromClause(input, idPaths);
		assertEquals(" Datafile df , Dataset ds", clause.toString());
		assertEquals(2, clause.getAuthzMap().size());
		assertEquals(Datafile.class, clause.getAuthzMap().get("df.id"));
		assertEquals(Dataset.class, clause.getAuthzMap().get("ds.id"));
		assertEquals(0, clause.getReplaceMap().size());
		assertEquals(Token.Type.WHERE, input.peek(0).getType());
	}

	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("FROM Dataset ds JOIN ds.datafiles df JOIN df.datafileFormat dff WHERE");
		Input input = new Input(tokens);
		Set<String> idPaths = new HashSet<>(Arrays.asList("ds", "dff"));
		FromClause clause = new FromClause(input, idPaths);
		assertEquals(" Dataset ds JOIN ds.datafiles df JOIN df.datafileFormat dff", clause.toString());
		assertEquals(2, clause.getAuthzMap().size());
		assertEquals(Dataset.class, clause.getAuthzMap().get("ds.id"));
		assertEquals(DatafileFormat.class, clause.getAuthzMap().get("dff.id"));
		assertEquals(0, clause.getReplaceMap().size());
		assertEquals(Token.Type.WHERE, input.peek(0).getType());
	}

	@Test
	public void testList1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("FROM Facility f WHERE");
		Input input = new Input(tokens);
		Set<String> idPaths = new HashSet<>(Arrays.asList("f.investigations"));
		FromClause clause = new FromClause(input, idPaths);
		assertEquals(" Facility f JOIN f.investigations a0", clause.toString());
		assertEquals(1, clause.getAuthzMap().size());
		assertEquals(Investigation.class, clause.getAuthzMap().get("a0.id"));
		assertEquals(1, clause.getReplaceMap().size());
		assertEquals("a0", clause.getReplaceMap().get("f.investigations"));
		assertEquals(Token.Type.WHERE, input.peek(0).getType());
	}

	@Test
	public void testList2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("FROM Facility f JOIN f.investigationTypes a0 WHERE");
		Input input = new Input(tokens);
		Set<String> idPaths = new HashSet<>(Arrays.asList("f.investigations", "a0"));
		FromClause clause = new FromClause(input, idPaths);
		assertEquals(" Facility f JOIN f.investigationTypes a0 JOIN f.investigations a1", clause.toString());
		assertEquals(2, clause.getAuthzMap().size());
		assertEquals(InvestigationType.class, clause.getAuthzMap().get("a0.id"));
		assertEquals(Investigation.class, clause.getAuthzMap().get("a1.id"));
		assertEquals(1, clause.getReplaceMap().size());
		assertEquals("a1", clause.getReplaceMap().get("f.investigations"));
		assertEquals(Token.Type.WHERE, input.peek(0).getType());
	}

}