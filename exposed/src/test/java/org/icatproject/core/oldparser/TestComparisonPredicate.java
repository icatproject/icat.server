package org.icatproject.core.oldparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.oldparser.ComparisonPredicate;
import org.icatproject.core.oldparser.OldInput;
import org.icatproject.core.oldparser.OldToken;
import org.icatproject.core.oldparser.OldTokenizer;
import org.junit.Test;

public class TestComparisonPredicate {

	@Test
	public void testString() throws Exception {
		test(Dataset.class, "name = 'fred'", "Dataset$.name = 'fred'");
		test(Dataset.class, "'fred' = name", "Dataset$.name = 'fred'");
	}

	@Test
	public void testStringNav() throws Exception {
		test(Datafile.class, "dataset.investigation.name = 'fred'",
				"Datafile$.dataset.investigation.name = 'fred'");
		test(Datafile.class, "'fred' = dataset.investigation.name",
				"Datafile$.dataset.investigation.name = 'fred'");
	}

	@Test
	public void testStringNavBad() throws Exception {
		testBad(Datafile.class, "datase.investigation.name = 'fred'",
				"Attribute comparisons require one attribute name and one value: datase.investigation.name = fred");

	}

	@Test
	public void testEnum() throws Exception {
		test(ParameterType.class, "valueType = DATE_AND_TIME",
				"ParameterType$.valueType = org.icatproject.ParameterValueType.DATE_AND_TIME");
		test(ParameterType.class, "DATE_AND_TIME= valueType",
				"ParameterType$.valueType = org.icatproject.ParameterValueType.DATE_AND_TIME");
	}

	@Test
	public void testEnumNav() throws Exception {
		test(DatasetParameter.class, "type.valueType = DATE_AND_TIME",
				"DatasetParameter$.type.valueType = org.icatproject.ParameterValueType.DATE_AND_TIME");
		test(DatasetParameter.class, "DATE_AND_TIME= type.valueType",
				"DatasetParameter$.type.valueType = org.icatproject.ParameterValueType.DATE_AND_TIME");
	}

	@Test
	public void testNum() throws Exception {
		test(ParameterType.class, "maximumNumericValue = 12.43",
				"ParameterType$.maximumNumericValue = 12.43");
		test(ParameterType.class, "12.43 = maximumNumericValue",
				"ParameterType$.maximumNumericValue = 12.43");
	}

	@Test
	public void testNumNav() throws Exception {
		test(DatasetParameter.class, "type.maximumNumericValue = 12.43",
				"DatasetParameter$.type.maximumNumericValue = 12.43");
		test(DatasetParameter.class, "12.43 = type.maximumNumericValue",
				"DatasetParameter$.type.maximumNumericValue = 12.43");
	}

	@Test
	public void testBool() throws Exception {
		test(ParameterType.class, "enforced = TRUE", "ParameterType$.enforced = TRUE");
		test(ParameterType.class, "TRUE = enforced", "ParameterType$.enforced = TRUE");
	}

	@Test
	public void testBoolNav() throws Exception {
		test(DatasetParameter.class, "type.enforced = TRUE",
				"DatasetParameter$.type.enforced = TRUE");
		test(DatasetParameter.class, "TRUE = type.enforced",
				"DatasetParameter$.type.enforced = TRUE");
	}

	private void test(Class<? extends EntityBaseBean> klass, String input, String output)
			throws Exception {
		List<OldToken> tokens = OldTokenizer.getTokens(input);
		ComparisonPredicate cp = new ComparisonPredicate(new OldInput(tokens));
		assertEquals(output, cp.getWhere(klass).toString());
	}

	private void testBad(Class<? extends EntityBaseBean> klass, String input, String msg)
			throws Exception {
		List<OldToken> tokens = OldTokenizer.getTokens(input);
		ComparisonPredicate cp = new ComparisonPredicate(new OldInput(tokens));

		try {
			cp.getWhere(klass).toString();
			fail("Should have thrown " + msg);
		} catch (Exception e) {
			assertEquals(msg, e.getMessage());
		}

	}

}