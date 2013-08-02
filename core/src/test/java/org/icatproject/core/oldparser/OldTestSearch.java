package org.icatproject.core.oldparser;

import static org.junit.Assert.assertEquals;

import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.SearchQuery;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;

public class OldTestSearch {

	@Test
	public void testGood1() throws Exception {
		String query = "COUNT(Dataset)";
		String expect = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$";
		test(query, expect);
	}

	@Test
	public void testGood2() throws Exception {
		String query = "Dataset INCLUDE Datafile, DatasetParameter, DatafileParameter, DatasetType";
		syntest(query);
	}

	@Test
	public void testGood3() throws Exception {
		String query = "MIN(Dataset.id) [id > 0]";
		String expect = "SELECT MIN(Dataset$.id) FROM Dataset AS Dataset$ WHERE (Dataset$.id > 0)";
		test(query, expect);
	}

	@Test
	public void testGood4() throws Exception {
		String query = "Investigation.id";
		String expect = "SELECT Investigation$.id FROM Investigation AS Investigation$";
		test(query, expect);
	}

	@Test
	public void testGood5() throws Exception {
		String query = "Dataset.id " + "<-> DatasetParameter[type.name = 'TIMESTAMP'] "
				+ "<-> Investigation[name <> 12]";
		syntest(query);
	}

	@Test
	public void testGood6() throws Exception {
		String query = "Datafile INCLUDE 1 [name = 'fred'] <-> Dataset[id <> 42]";
		String expect = "SELECT Datafile$ "
				+ "FROM Datafile AS Datafile$ JOIN Datafile$.dataset AS Dataset$ "
				+ "WHERE (Datafile$.name = 'fred') AND (Dataset$.id <> 42) INCLUDE 1";
		test(query, expect);
	}

	@Test
	public void testGood7() throws Exception {
		String query = "Dataset.id  ORDER BY id [type.name IN ('GS', 'GQ')] <-> Investigation[id BETWEEN 3 AND 42]";
		String expect = "SELECT Dataset$.id "
				+ "FROM Dataset AS Dataset$ JOIN Dataset$.investigation AS Investigation$ "
				+ "WHERE (Dataset$.type.name IN ('GS', 'GQ')) AND (Investigation$.id BETWEEN 3 AND 42) ORDER BY Dataset$.id";
		test(query, expect);
	}

	@Test
	public void testGood8() throws Exception {
		String query = "Dataset.id ORDER BY startDate [type.name IN ('GS', 'GQ') AND name >= 'M' AND name <= 'Z']";
		String expect = "SELECT Dataset$.id FROM Dataset AS Dataset$ "
				+ "WHERE (Dataset$.type.name IN ('GS', 'GQ') AND Dataset$.name >= 'M' AND Dataset$.name <= 'Z') "
				+ "ORDER BY Dataset$.startDate";
		test(query, expect);
	}

	@Test
	public void testGood9() throws Exception {
		String query = "ParameterType.name [description LIKE 'F%']";
		String expect = "SELECT ParameterType$.name FROM ParameterType AS ParameterType$ WHERE (ParameterType$.description LIKE 'F%')";
		test(query, expect);
	}

	@Test
	public void testGood10() throws Exception {
		String query = ",1 Datafile.name ORDER BY id";
		String expect = "SELECT Datafile$.name FROM Datafile AS Datafile$  ORDER BY Datafile$.id LIMIT 0,1";
		test(query, expect);
	}

	@Test
	public void testGood11() throws Exception {
		String query = "1, Datafile.name ORDER BY id";
		String expect = "SELECT Datafile$.name FROM Datafile AS Datafile$  ORDER BY Datafile$.id LIMIT 1,*";
		test(query, expect);
	}

	@Test
	public void testGood12() throws Exception {
		String query = "1,1 Datafile.name ORDER BY id";
		String expect = "SELECT Datafile$.name FROM Datafile AS Datafile$  ORDER BY Datafile$.id LIMIT 1,1";
		test(query, expect);
	}

	@Test
	public void testGood13() throws Exception {
		String query = "0,100 Datafile.name ORDER BY id";
		String expect = "SELECT Datafile$.name FROM Datafile AS Datafile$  ORDER BY Datafile$.id LIMIT 0,100";
		test(query, expect);
	}

	@Test
	public void testGood14() throws Exception {
		String query = "DISTINCT ParameterType.valueType";
		String expect = "SELECT DISTINCT ParameterType$.valueType FROM ParameterType AS ParameterType$";
		test(query, expect);
	}

	@Test
	public void testGood15() throws Exception {
		String query = "Dataset [complete = TRUE]";
		String expect = "SELECT Dataset$ FROM Dataset AS Dataset$ WHERE (Dataset$.complete = TRUE)";
		test(query, expect);
	}

	@Test
	public void testGood16() throws Exception {
		String query = "ParameterType [facility.id=42 AND valueType=NUMERIC]";
		String expect = "SELECT ParameterType$ FROM ParameterType AS ParameterType$ "
				+ "WHERE (ParameterType$.facility.id = 42 AND ParameterType$.valueType = org.icatproject.ParameterValueType.NUMERIC)";
		test(query, expect);
	}

	@Test
	public void testGood17() throws Exception {
		String query = "ParameterType [facility.id=42 AND NUMERIC=valueType]";
		String expect = "SELECT ParameterType$ FROM ParameterType AS ParameterType$ "
				+ "WHERE (ParameterType$.facility.id = 42 AND ParameterType$.valueType = org.icatproject.ParameterValueType.NUMERIC)";
		test(query, expect);
	}

	private void test(String query, String expect) throws Exception {
		OldSearchQuery q = new OldSearchQuery(new OldInput(OldTokenizer.getTokens(query)));
		query = q.getNewQuery();
		assertEquals(expect, query);
		new SearchQuery(new Input(Tokenizer.getTokens(query)));
	}

	private void syntest(String query) throws Exception {
		OldSearchQuery q = new OldSearchQuery(new OldInput(OldTokenizer.getTokens(query)));
		System.out.println(q.getNewQuery());
		new SearchQuery(new Input(Tokenizer.getTokens(q.getNewQuery())));
	}

}