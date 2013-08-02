package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.Grouping;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.SearchQuery;
import org.icatproject.core.parser.Token;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;

public class TestSearch {

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT ds FROM Dataset ds JOIN ds.parameters p "
						+ "WHERE p.type.name = 'TIMESTAMP' "
						+ "INCLUDE ds.datafile.parameters ps, ps.type, ds.parameters, ds.investigation AS i LIMIT 12, 50");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals(
				"SELECT $0$ FROM Dataset $0$ JOIN $0$.parameters $1$ "
						+ "WHERE $1$.type.name = 'TIMESTAMP' "
						+ "INCLUDE  $0$.datafile.parameters  $2$,  $2$.type,  $0$.parameters,  $0$.investigation  $3$ LIMIT 12,50",
				sq.toString());
		assertEquals((Integer) 12, sq.getOffset());
		assertEquals((Integer) 50, sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT ds.id FROM Dataset AS ds JOIN ds.parameters AS p "
						+ "WHERE p.type.name = 'TIMESTAMP' " + "LIMIT 12, 50");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT $0$.id FROM Dataset $0$ JOIN $0$.parameters $1$ "
				+ "WHERE $1$.type.name = 'TIMESTAMP' LIMIT 12,50", sq.toString());
		assertEquals((Integer) 12, sq.getOffset());
		assertEquals((Integer) 50, sq.getNumber());
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
		assertEquals("SELECT COUNT($0$) FROM Dataset $0$ JOIN $0$.parameters $1$ "
				+ "WHERE $1$.type.name = 'TIMESTAMP'", sq.toString());
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
		assertEquals("SELECT COUNT(DISTINCT $0$) FROM Dataset $0$ JOIN $0$.parameters $1$ "
				+ "WHERE $1$.type.name = 'TIMESTAMP'", sq.toString());
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
		assertEquals("SELECT $0$ FROM Dataset $0$ JOIN $0$.parameters $1$ "
				+ "WHERE $1$.type.name != 'TIMESTAMP' ORDER BY ds.name ASC", sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(Dataset.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood6() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT df.name FROM Datafile df ORDER BY df.id LIMIT 0,1");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT $0$.name FROM Datafile $0$ ORDER BY df.id LIMIT 0,1", sq.toString());
		assertEquals((Integer) 0, sq.getOffset());
		assertEquals((Integer) 1, sq.getNumber());
		assertEquals(Datafile.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood7() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT Grouping$ FROM Grouping AS Grouping$ WHERE (Grouping$.name = 'root')");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("SELECT $0$ FROM Grouping $0$ WHERE ( $0$.name = 'root' )", sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(Grouping.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testGood8() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT pt FROM ParameterType pt JOIN pt.facility f WHERE f.id=42 AND pt.valueType=org.icatproject.ParameterValueType.NUMERIC");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals(
				"SELECT $0$ FROM ParameterType $0$ JOIN $0$.facility $1$ "
						+ "WHERE $1$.id = 42 AND $0$.valueType = org.icatproject.core.entity.ParameterValueType.NUMERIC",
				sq.toString());
		assertNull(sq.getOffset());
		assertNull(sq.getNumber());
		assertEquals(ParameterType.class, sq.getBean());
		assertNull(input.peek(0));
	}

	@Test
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("SELECT pt FROM ParameterType pt JOIN pt.facility f WHERE f.id=42 AND pt.valueType=org.icatproject.Parameter.ValueType.NUMERIC");
		Input input = new Input(tokens);
		try {
			new SearchQuery(input);
			fail("Should have thrown exception)");
		} catch (Exception e) {
			assertEquals(ParserException.class, e.getClass());
			assertEquals(
					"Enum literal org.icatproject.Parameter.ValueType.NUMERIC must contain exactly 4 parts",
					e.getMessage());
		}

	}

}