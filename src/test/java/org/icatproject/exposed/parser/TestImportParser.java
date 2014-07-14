package org.icatproject.exposed.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.icatproject.core.IcatException;
import org.icatproject.exposed.RestfulBeanManager;
import org.icatproject.exposed.importParser.Input;
import org.icatproject.exposed.importParser.ParserException;
import org.icatproject.exposed.importParser.Table;
import org.icatproject.exposed.importParser.TableField;
import org.icatproject.exposed.importParser.Token;
import org.icatproject.exposed.importParser.Token.Type;
import org.icatproject.exposed.importParser.Tokenizer;
import org.junit.Test;

public class TestImportParser {

	@Test()
	public void testDataCollection() throws Exception {
		Table t = new Table(new Input(Tokenizer.getTokens(" DataCollection(?:0)")));
		assertEquals("DataCollection(?:0)", t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertNull(tableField.getField());
		assertEquals((Integer) 0, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());
		assertFalse(tableField.isQmark());

		assertEquals(1, tableFields.size());
	}

	@Test()
	public void testJob() throws Exception {
		Input input = new Input(Tokenizer.getTokens(" Job(application(facility(name:0), name:1, "
				+ "version:2), inputDataCollection(?:3), outputDataCollection(?:4))"));
		Table t = new Table(input);
		assertNull(input.peek(0));
		assertEquals(
				"Job(application(facility(name:0),name:1,version:2),inputDataCollection(?:3),outputDataCollection(?:4))",
				t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"private org.icatproject.core.entity.Application org.icatproject.core.entity.Job.application",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals("SELECT n0 FROM Application n0 JOIN n0.facility n0n0 "
				+ "WHERE n0n0.name = :p0 AND n0.name = :p1 AND n0.version = :p2",
				tableField.getJPQL());
		assertEquals(3, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 1, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());
		assertEquals((Integer) 2, tableField.getAttributes().get(2).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(2).getType());
		assertFalse(tableField.isQmark());

		tableField = tableFields.get(1);

		assertEquals(
				"private org.icatproject.core.entity.DataCollection org.icatproject.core.entity.Job.inputDataCollection",
				tableField.getField().toString());
		assertEquals((Integer) 3, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());
		assertTrue(tableField.isQmark());

		tableField = tableFields.get(2);
		assertEquals(
				"private org.icatproject.core.entity.DataCollection org.icatproject.core.entity.Job.outputDataCollection",
				tableField.getField().toString());
		assertEquals((Integer) 4, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());
		assertTrue(tableField.isQmark());

		assertEquals(3, tableFields.size());
	}

	@Test(expected = ParserException.class)
	public void testBadFacility() throws Exception {
		Input input = new Input(Tokenizer.getTokens(" Facility (createId:0, createTime:1,  "
				+ "modId : 2,  modTime: 3, name :4, daysUntilRelease:5))"));
		new Table(input);
		input.checkEnded();

	}

	@Test()
	public void testFacility() throws Exception {
		Input input = new Input(Tokenizer.getTokens(" Facility (createId:0, createTime:1,  "
				+ "modId : 2,  modTime: 3, name :4, daysUntilRelease:5)"));
		Table t = new Table(input);
		assertNull(input.peek(0));
		assertEquals(
				"Facility(createId:0,createTime:1,modId:2,modTime:3,name:4,daysUntilRelease:5)",
				t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"protected java.lang.String org.icatproject.core.entity.EntityBaseBean.createId",
				tableField.getField().toString());
		assertEquals((Integer) 0, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		assertEquals(6, tableFields.size());
	}

	@Test
	public void testDataCollectionDatafile() throws Exception {
		Table t = new Table(new Input(Tokenizer.getTokens("DataCollectionDatafile(datafile(dataset"
				+ "(investigation(facility(name:0), name:1, visitId:2), name:3), name:4), "
				+ "dataCollection(?:5))")));
		assertEquals(
				"DataCollectionDatafile(datafile(dataset(investigation(facility(name:0),name:1,"
						+ "visitId:2),name:3),name:4),dataCollection(?:5))", t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"private org.icatproject.core.entity.Datafile org.icatproject.core.entity.DataCollectionDatafile.datafile",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals(
				"SELECT n0 FROM Datafile n0 JOIN n0.dataset n0n0 JOIN n0n0.investigation n0n0n0 JOIN n0n0n0.facility n0n0n0n0 "
						+ "WHERE n0n0n0n0.name = :p0 AND n0n0n0.name = :p1 AND n0n0n0.visitId = :p2 AND n0n0.name = :p3 AND n0.name = :p4",
				tableField.getJPQL());
		assertEquals(5, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 1, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());
		assertEquals((Integer) 2, tableField.getAttributes().get(2).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(2).getType());
		assertEquals((Integer) 3, tableField.getAttributes().get(3).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(3).getType());
		assertEquals((Integer) 4, tableField.getAttributes().get(4).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(3).getType());
		assertFalse(tableField.isQmark());

		tableField = tableFields.get(1);
		assertEquals(
				"private org.icatproject.core.entity.DataCollection org.icatproject.core.entity.DataCollectionDatafile.dataCollection",
				tableField.getField().toString());
		assertEquals((Integer) 5, tableField.getOffset());

		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());
		assertTrue(tableField.isQmark());

		assertEquals(2, tableFields.size());
	}

	@Test()
	public void testDatafile() throws Exception {
		Table t = new Table(new Input(
				Tokenizer.getTokens("Datafile(dataset(investigation    (   facility(name:0), "
						+ "name:1, visitId:2), name:3  ) , name:4, fileSize:5) ")));
		assertEquals(
				"Datafile(dataset(investigation(facility(name:0),name:1,visitId:2),name:3),name:4,fileSize:5)",
				t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"private org.icatproject.core.entity.Dataset org.icatproject.core.entity.Datafile.dataset",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals(
				"SELECT n0 FROM Dataset n0 JOIN n0.investigation n0n0 JOIN n0n0.facility n0n0n0 "
						+ "WHERE n0n0n0.name = :p0 AND n0n0.name = :p1 AND n0n0.visitId = :p2 AND n0.name = :p3",
				tableField.getJPQL());
		assertEquals(4, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 1, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());
		assertEquals((Integer) 2, tableField.getAttributes().get(2).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(2).getType());
		assertEquals((Integer) 3, tableField.getAttributes().get(3).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(3).getType());

		tableField = tableFields.get(1);
		assertEquals("private java.lang.String org.icatproject.core.entity.Datafile.name",
				tableField.getField().toString());
		assertEquals((Integer) 4, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(2);
		assertEquals("private java.lang.Long org.icatproject.core.entity.Datafile.fileSize",
				tableField.getField().toString());
		assertEquals((Integer) 5, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		assertEquals(3, tableFields.size());
	}

	@Test()
	public void testDataset() throws Exception {
		Table t = new Table(
				new Input(
						Tokenizer
								.getTokens("Dataset (investigation(facility(name:0), name:1, visitId:2) , name:3, type(facility(name:0), name:4), complete:5, startDate:6)")));
		assertEquals(
				"Dataset(investigation(facility(name:0),name:1,visitId:2),name:3,type(facility(name:0),name:4),complete:5,startDate:6)",
				t.toString());
		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"private org.icatproject.core.entity.Investigation org.icatproject.core.entity.Dataset.investigation",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals(
				"SELECT n0 FROM Investigation n0 JOIN n0.facility n0n0 WHERE n0n0.name = :p0 AND n0.name = :p1 AND n0.visitId = :p2",
				tableField.getJPQL());
		assertEquals(3, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 1, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());
		assertEquals((Integer) 2, tableField.getAttributes().get(2).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(2).getType());

		tableField = tableFields.get(1);
		assertEquals("private java.lang.String org.icatproject.core.entity.Dataset.name",
				tableField.getField().toString());
		assertEquals((Integer) 3, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(2);
		assertEquals(
				"private org.icatproject.core.entity.DatasetType org.icatproject.core.entity.Dataset.type",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals(
				"SELECT n0 FROM DatasetType n0 JOIN n0.facility n0n0 WHERE n0n0.name = :p0 AND n0.name = :p4",
				tableField.getJPQL());
		assertEquals(2, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 4, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());

		tableField = tableFields.get(3);
		assertEquals("private boolean org.icatproject.core.entity.Dataset.complete", tableField
				.getField().toString());
		assertEquals((Integer) 5, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(4);
		assertEquals("private java.util.Date org.icatproject.core.entity.Dataset.startDate",
				tableField.getField().toString());
		assertEquals((Integer) 6, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		assertEquals(5, tableFields.size());
	}

	@Test()
	public void testInvestigation() throws Exception {
		Table t = new Table(new Input(
				Tokenizer.getTokens("Investigation(facility  ( name:0), name  :  1, visitId:2, "
						+ "type(facility(name:0), name:3))")));
		assertEquals(
				"Investigation(facility(name:0),name:1,visitId:2,type(facility(name:0),name:3))",
				t.toString());

		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(0);
		assertEquals(
				"private org.icatproject.core.entity.Facility org.icatproject.core.entity.Investigation.facility",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals("SELECT n0 FROM Facility n0 WHERE n0.name = :p0", tableField.getJPQL());
		assertEquals(1, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());

		tableField = tableFields.get(1);
		assertEquals("private java.lang.String org.icatproject.core.entity.Investigation.name",
				tableField.getField().toString());
		assertEquals((Integer) 1, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(2);
		assertEquals("private java.lang.String org.icatproject.core.entity.Investigation.visitId",
				tableField.getField().toString());
		assertEquals((Integer) 2, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(3);
		assertEquals(
				"private org.icatproject.core.entity.InvestigationType org.icatproject.core.entity.Investigation.type",
				tableField.getField().toString());
		assertNull(tableField.getOffset());
		assertEquals(
				"SELECT n0 FROM InvestigationType n0 JOIN n0.facility n0n0 WHERE n0n0.name = :p0 AND n0.name = :p3",
				tableField.getJPQL());
		assertEquals(2, tableField.getAttributes().size());
		assertEquals((Integer) 0, tableField.getAttributes().get(0).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(0).getType());
		assertEquals((Integer) 3, tableField.getAttributes().get(1).getFieldNum());
		assertEquals("String", tableField.getAttributes().get(1).getType());

		assertEquals(4, tableFields.size());
	}

	@Test()
	public void testParameterType() throws Exception {
		Table t = new Table(
				new Input(
						Tokenizer
								.getTokens("ParameterType(facility  ( name:0),name:1, units:2, minimumNumericValue:3, applicableToInvestigation:4, valueType:5)")));
		assertEquals(
				"ParameterType(facility(name:0),name:1,units:2,minimumNumericValue:3,applicableToInvestigation:4,valueType:5)",
				t.toString());

		List<TableField> tableFields = t.getTableFields();

		TableField tableField = tableFields.get(3);
		assertEquals(
				"private java.lang.Double org.icatproject.core.entity.ParameterType.minimumNumericValue",
				tableField.getField().toString());
		assertEquals((Integer) 3, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(4);
		assertEquals(
				"private boolean org.icatproject.core.entity.ParameterType.applicableToInvestigation",
				tableField.getField().toString());
		assertEquals((Integer) 4, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		tableField = tableFields.get(5);
		assertEquals(
				"private org.icatproject.core.entity.ParameterValueType org.icatproject.core.entity.ParameterType.valueType",
				tableField.getField().toString());
		assertEquals((Integer) 5, tableField.getOffset());
		assertNull(tableField.getJPQL());
		assertEquals(0, tableField.getAttributes().size());

		assertEquals(6, tableFields.size());

	}

	@Test
	public void testTimestamp() throws IcatException {
		Date d1 = RestfulBeanManager.getDate("2014-05-16T16:58:26.12+12:30");
		Date d2 = RestfulBeanManager.getDate("2014-05-16T16:58:26.1234+12:30");
		Date d3 = RestfulBeanManager.getDate("2014-05-16T16:58:26+12:30");

		Date d4 = RestfulBeanManager.getDate("2014-05-16T16:58:26.12");
		Date d5 = RestfulBeanManager.getDate("2014-05-16T16:58:26.1234");
		Date d6 = RestfulBeanManager.getDate("2014-05-16T16:58:26");

		Date d7 = RestfulBeanManager.getDate("2014-05-16T16:58:26.12Z");
		Date d8 = RestfulBeanManager.getDate("2014-05-16T16:58:26.1234Z");
		Date d9 = RestfulBeanManager.getDate("2014-05-16T16:58:26Z");

		assertEquals(3L, d5.getTime() - d4.getTime());
		assertEquals(120L, d4.getTime() - d6.getTime());
		assertEquals(45000000L, d7.getTime() - d1.getTime());
		assertEquals(123L, d2.getTime() - d3.getTime());
		assertEquals(123L, d8.getTime() - d9.getTime());
	}

	@Test
	public void testdata() throws Exception {
		Input i = new Input(
				Tokenizer
						.getTokens("\"db/root\",  2014-05-16T16:58:26.12+12:30    \"db/root\"  "
								+ "2014-05-16T16:58:26Z \"Test Facility\" 90 -5 17.4 -17.3 Null TRue fAlse NUMERIC"));
		assertEquals("db/root", i.consume(Token.Type.STRING).getValue());
		assertEquals(",", i.consume(Token.Type.COMMA).getValue());
		assertEquals("2014-05-16T16:58:26.12+12:30", i.consume(Token.Type.TIMESTAMP).getValue());
		assertEquals("db/root", i.consume(Token.Type.STRING).getValue());
		assertEquals("2014-05-16T16:58:26Z", i.consume(Token.Type.TIMESTAMP).getValue());
		assertEquals("Test Facility", i.consume(Token.Type.STRING).getValue());
		assertEquals("90", i.consume(Token.Type.INTEGER).getValue());
		assertEquals("-5", i.consume(Token.Type.INTEGER).getValue());
		assertEquals("17.4", i.consume(Token.Type.REAL).getValue());
		assertEquals("-17.3", i.consume(Token.Type.REAL).getValue());
		assertEquals("NULL", i.consume(Token.Type.NULL).getValue());
		assertEquals("TRUE", i.consume(Token.Type.BOOLEAN).getValue());
		assertEquals("FALSE", i.consume(Token.Type.BOOLEAN).getValue());
		assertEquals("NUMERIC", i.consume(Token.Type.NAME).getValue());
		assertNull(i.consume());
	}

	@Test
	public void tokens() throws Exception {
		List<Token> ts = Tokenizer.getTokens("\"db/root\",  2014-05-16T16:58:26.12+12:30  "
				+ " DATE_AND_TIME NULL NA trUE false");
		tokenCheck(ts.get(0), Token.Type.STRING, "db/root");
		tokenCheck(ts.get(1), Token.Type.COMMA, ",");

		tokenCheck(ts.get(2), Token.Type.TIMESTAMP, "2014-05-16T16:58:26.12+12:30");

		tokenCheck(ts.get(3), Token.Type.NAME, "DATE_AND_TIME");

		tokenCheck(ts.get(4), Token.Type.NULL, "NULL");
		tokenCheck(ts.get(5), Token.Type.NAME, "NA");
		tokenCheck(ts.get(6), Token.Type.BOOLEAN, "TRUE");
		tokenCheck(ts.get(7), Token.Type.BOOLEAN, "FALSE");

	}

	private void tokenCheck(Token token, Type type, String value) {
		assertEquals(type, token.getType());
		assertEquals(value, token.getValue());
	}

}
