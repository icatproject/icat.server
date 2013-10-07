package org.icatproject.utils;

import static org.junit.Assert.assertEquals;

import org.icatproject.utils.CheckedProperties.CheckedPropertyException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCheckedProperties {

	private static CheckedProperties s_properties;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		s_properties = new CheckedProperties();
		s_properties.loadFromFile("src/test/resources/PropertiesFromResources.properties");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testPropertiesFromResources() throws Exception {
		s_properties.loadFromFile("Doesn't exist");
	}

	@Test
	public final void testGetString1() throws Exception {
		assertEquals("One", s_properties.getString("one"));
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testGetString2() throws Exception {
		s_properties.getString("noone");
	}

	@Test
	public final void testDbl1() throws Exception {
		assertEquals(1E23, s_properties.getDouble("dbl1"), 1.);
	}

	@Test
	public final void testDbl2() throws Exception {
		assertEquals(2.0, s_properties.getDouble("dbl2"), 1E-12);
	}

	@Test
	public final void testDbl3() throws Exception {
		assertEquals(3.0, s_properties.getDouble("dbl3"), 1E-12);
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testBadDbl() throws Exception {
		s_properties.getDouble("badDbl");
	}

	@Test
	public final void testGetNonNegativeInt1() throws Exception {
		assertEquals(2, s_properties.getNonNegativeInt("two"));
	}

	@Test
	public final void testGetNonNegativeInt2() throws Exception {
		assertEquals(0, s_properties.getNonNegativeInt("zero"));
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testGetNonNegativeInt3() throws Exception {
		s_properties.getNonNegativeInt("minustwo");
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testGetPositiveInt1() throws Exception {
		s_properties.getPositiveInt("badtwo");
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testGetPositiveInt2() throws Exception {
		s_properties.getPositiveInt("minustwo");
	}

	@Test(expected = CheckedPropertyException.class)
	public final void testGetPositiveInt3() throws Exception {
		s_properties.getPositiveInt("zero");
	}

	@Test
	public final void testGetPositiveInt4() throws Exception {
		assertEquals(2, s_properties.getPositiveInt("two"));
	}
}