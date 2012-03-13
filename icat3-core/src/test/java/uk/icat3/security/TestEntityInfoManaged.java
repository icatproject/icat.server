package uk.icat3.security;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.manager.Constraint;
import uk.icat3.manager.EntityField;
import uk.icat3.manager.EntityInfo;
import uk.icat3.manager.EntityInfoHandler;
import uk.icat3.manager.EntityInfoHandler.KeyType;

public class TestEntityInfoManaged {

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@Test(expected = BadParameterException.class)
	public void testBadname() throws Exception {
		eiHandler.getEntityInfo("Fred");
	}

	@Test
	public void testInvestigation() throws Exception {
		EntityInfo ei = eiHandler.getEntityInfo("Investigation");
		assertEquals("An investigation or experiment", ei.getClassComment());
		assertEquals("id", ei.getKeyFieldname());
		assertEquals(KeyType.GENERATED, ei.getKeyType());
		for (Constraint constraint : ei.getConstraints()) {
			assertEquals(Arrays.asList("name", "visitId", "facilityCycle", "instrument"), constraint.getFieldNames());
		}
		assertEquals(21, ei.getFields().size());
		int n = 0;
		for (EntityField field : ei.getFields()) {
			if (field.getName().equals("id")) {
				assertEquals("Long", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(EntityField.RelType.ATTRIBUTE, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(null, field.isCascaded());
			} else if (field.getName().equals("facilityCycle")) {
				assertEquals("FacilityCycle", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(EntityField.RelType.ONE, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(false, field.isCascaded());
			} else if (field.getName().equals("title")) {
				assertEquals("String", field.getType());
				assertEquals(true, field.isNotNullable());
				assertEquals("Full title of the investigation", field.getComment());
				assertEquals(EntityField.RelType.ATTRIBUTE, field.getRelType());
				assertEquals((Integer) 255, field.getStringLength());
				assertEquals(null, field.isCascaded());
			} else if (field.getName().equals("investigationUsers")) {
				assertEquals("InvestigationUser", field.getType());
				assertEquals(false, field.isNotNullable());
				assertEquals(null, field.getComment());
				assertEquals(EntityField.RelType.MANY, field.getRelType());
				assertEquals(null, field.getStringLength());
				assertEquals(true, field.isCascaded());
			} else {
				n++;
			}
		}
		assertEquals(17, n);
	}
}