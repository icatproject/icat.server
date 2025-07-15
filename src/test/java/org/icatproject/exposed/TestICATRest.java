package org.icatproject.exposed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.Throwable;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

import org.icatproject.core.entity.ParameterValueType;
import org.icatproject.core.entity.StudyStatus;
import org.icatproject.core.IcatException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestICATRest {

	private static ICATRest icatRest = new ICATRest();
	private static Method jsoniseArray;

	private final static DateFormat df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	@BeforeAll
	public static void setup() throws Exception {

		jsoniseArray = icatRest.getClass().getDeclaredMethod("jsonise", Object.class, JsonGenerator.class);
		jsoniseArray.setAccessible(true);

	}

	private static String getJsonArray(Object data) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			jsoniseArray.invoke(icatRest, data, gen);
			gen.writeEnd();
		}
		return baos.toString();
	}

	@Test
	public void TestJsoniseArray() throws Exception {
		assertEquals("[" + null + "]", getJsonArray(null));

		Long longNum = 2147483648L;   //Max int value + 1
		assertEquals("[" + longNum.toString() + "]", getJsonArray(longNum));

		Double doubleNum = 1.79769313486231570E+308; // Max double value
		assertEquals("[" + doubleNum.toString() + "]", getJsonArray(doubleNum));

		Double doubleNaN = (Double) 0.0 / 0.0; //NaN
		assertEquals("[" + null + "]", getJsonArray(doubleNaN));

		Integer integerNum = -2147483648;  // Min integer value
		assertEquals("[" + integerNum.toString() + "]", getJsonArray(integerNum));

		BigInteger bigintegerNum = new BigInteger("9223372036854775808");  // Max long + 1
		assertEquals("[" + bigintegerNum.toString() + "]", getJsonArray(bigintegerNum));

		BigDecimal bigdecimalNum = new BigDecimal(1.79769313486231570E+308 + 1);  // Max double + 1
		assertEquals("[" + bigdecimalNum.toString() + "]", getJsonArray(bigdecimalNum));

		String testString = "A nasty $tring {to} convert [to] json;";
		assertEquals("[\"" + testString.toString() + "\"]", getJsonArray(testString));

		Boolean testBoolean = false;
		assertEquals("[" + testBoolean.toString() + "]", getJsonArray(testBoolean));

		ParameterValueType testPVT = ParameterValueType.DATE_AND_TIME;
		assertEquals("[\"" + testPVT.toString() + "\"]", getJsonArray(testPVT));

		StudyStatus testSS = StudyStatus.CANCELLED;
		assertEquals("[\"" + testSS.toString() + "\"]", getJsonArray(testSS));

		Date testDate = new Date();
		assertEquals("[\"" + df8601.format(testDate) + "\"]", getJsonArray(testDate));
	}

	@Test
	public void TestJsoniseArrayWithUnimplementedType() throws Exception {
		Object obj = new Object();
		try {
			String json = getJsonArray(obj);
			fail("Didn't throw exception");
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			assertEquals(IcatException.class, cause.getClass());
			assertEquals("Don't know how to jsonise " + obj.getClass(), cause.getMessage());
		}
	}
}
