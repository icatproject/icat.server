package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.rules.ErrorCollector;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.icatproject.core.manager.search.LuceneApi;
import org.icatproject.core.manager.search.OpensearchApi;
import org.icatproject.core.manager.search.SearchApi;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.IcatException.IcatExceptionType;
import org.icatproject.icat.client.ParameterForLucene;
import org.icatproject.icat.client.Session;
import org.icatproject.icat.client.Session.Attributes;
import org.icatproject.icat.client.Session.DuplicateAction;
import org.icatproject.EntityBaseBean;
import org.icatproject.Facility;
import org.icatproject.PublicStep;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestRS {

	private static final String NO_DIMENSIONS = "Did not expect responseObject to contain 'dimensions', but it did";
	private static final DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static WSession wSession;
	private static long end;
	private static long start;
	private static SearchApi searchApi;

	/**
	 * Utility function for manually clearing the search engine indices based on the
	 * System properties
	 * 
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws org.icatproject.core.IcatException
	 */
	private static void clearSearch()
			throws URISyntaxException, MalformedURLException, org.icatproject.core.IcatException {
		if (searchApi == null) {
			String searchEngine = System.getProperty("searchEngine");
			String urlString = System.getProperty("searchUrls");
			URI uribase = new URI(urlString);
			if (searchEngine.equals("LUCENE")) {
				searchApi = new LuceneApi(uribase);
			} else if (searchEngine.equals("OPENSEARCH") || searchEngine.equals("ELASTICSEARCH")) {
				searchApi = new OpensearchApi(uribase);
			} else {
				throw new RuntimeException(
						"searchEngine must be one of LUCENE, OPENSEARCH, ELASTICSEARCH, but it was " + searchEngine);
			}
		}
		searchApi.clear();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		try {
			wSession = new WSession();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Before
	public void initializeSession() throws Exception {
		wSession.setAuthz();
	}

	@After
	public void clearSession() throws Exception {
		wSession.clear();
		wSession.clearAuthz();
	}

	private Session rootSession() throws URISyntaxException, IcatException {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		return icat.login("db", credentials);
	}

	private Session piOneSession() throws URISyntaxException, IcatException {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "piOne");
		credentials.put("password", "piOne");
		return icat.login("db", credentials);
	}

	@Ignore("Test fails because of bug in eclipselink")
	@Test
	public void testDistinctBehaviour() throws Exception {
		Session session = rootSession();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		// See what happens to query with bad order by
		System.out.println(search(session, "SELECT inv FROM Investigation inv", 3));

		System.out.println(
				search(session, "SELECT inv FROM Investigation inv, inv.investigationUsers u ORDER BY u.user.name", 3));

	}

	@org.junit.Rule
	public ErrorCollector collector = new ErrorCollector();

	@Test
	public void TestJsoniseBean() throws Exception {
		DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		Session session = createAndPopulate();

		/*
		 * Expected: <[{"User":{"id":8148,"createId":"db/notroot","createTime":
		 * "2019-03-11T14:14:47.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T14:14:47.000Z","affiliation":"Unseen University","familyName":
		 * "Worblehat","fullName":"Dr. Horace Worblehat","givenName":"Horace",
		 * "instrumentScientists":[],"investigationUsers":[],"name":"db/lib","studies":[
		 * ],"userGroups":[]}}]>
		 */
		JsonArray user_response = search(session, "SELECT u from User u WHERE u.name = 'db/lib'", 1);
		collector.checkThat(user_response.getJsonObject(0).containsKey("User"), is(true));

		JsonObject user = user_response.getJsonObject(0).getJsonObject("User");
		collector.checkThat(user.getJsonNumber("id").isIntegral(), is(true)); // Check Integer conversion
		collector.checkThat(user.getString("createId"), is("db/notroot")); // Check String conversion

		/*
		 * Expected: <[{"Facility":{"id":2852,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","applications":[],"datafileFormats":[],
		 * "datasetTypes":[],"daysUntilRelease":90,"facilityCycles":[],"instruments":[],
		 * "investigationTypes":[],"investigations":[],"name":"Test port facility"
		 * ,"parameterTypes":[],"sampleTypes":[]}}]>
		 */
		JsonArray fac_response = search(session, "SELECT f from Facility f WHERE f.name = 'Test port facility'", 1);
		collector.checkThat(fac_response.getJsonObject(0).containsKey("Facility"), is(true));

		/*
		 * Expected: <[{"Instrument":{"id":1449,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","fullName":"EDDI - Energy Dispersive Diffraction"
		 * ,"instrumentScientists":[],"investigationInstruments":[],"name":"EDDI","pid":
		 * "ig:0815","shifts":[]}}]>
		 */
		JsonArray inst_response = search(session, "SELECT i from Instrument i WHERE i.name = 'EDDI'", 1);
		collector.checkThat(inst_response.getJsonObject(0).containsKey("Instrument"), is(true));

		/*
		 * Expected:
		 * <[{"InvestigationType":{"id":3401,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","investigations":[],"name":"atype"}}]>
		 */
		JsonArray it_response = search(session, "SELECT it from InvestigationType it WHERE it.name = 'atype'", 1);
		collector.checkThat(it_response.getJsonObject(0).containsKey("InvestigationType"), is(true));

		/*
		 * Expected: <[{"ParameterType":{"id":5373,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","applicableToDataCollection":false,
		 * "applicableToDatafile":true,"applicableToDataset":true,
		 * "applicableToInvestigation":true,"applicableToSample":false,
		 * "dataCollectionParameters":[],"datafileParameters":[],"datasetParameters":[],
		 * "enforced":false,"investigationParameters":[],"minimumNumericValue":73.4,
		 * "name":"temp","permissibleStringValues":[],"pid":"pt:25c","sampleParameters":
		 * [],"units":"degrees Kelvin","valueType":"NUMERIC","verified":false}}]>
		 */
		JsonArray pt_response = search(session, "SELECT pt from ParameterType pt WHERE pt.name = 'temp'", 1);
		collector.checkThat(pt_response.getJsonObject(0).containsKey("ParameterType"), is(true));
		collector.checkThat((Double) pt_response.getJsonObject(0).getJsonObject("ParameterType")
				.getJsonNumber("minimumNumericValue").doubleValue(), is(73.4)); // Check Double conversion
		collector.checkThat(
				(Boolean) pt_response.getJsonObject(0).getJsonObject("ParameterType").getBoolean("enforced"),
				is(Boolean.FALSE)); // Check boolean conversion
		collector.checkThat(
				pt_response.getJsonObject(0).getJsonObject("ParameterType").getJsonString("valueType").getString(),
				is("NUMERIC")); // Check ParameterValueType conversion

		/*
		 * Expected: <[{"Investigation":{"id":4814,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","datasets":[],"endDate":"2010-12-31T23:59:59.000Z"
		 * ,"investigationGroups":[],"investigationInstruments":[],"investigationUsers":
		 * [],"keywords":[],"name":"expt1","parameters":[],"publications":[],"samples":[
		 * ],"shifts":[],"startDate":"2010-01-01T00:00:00.000Z","studyInvestigations":[]
		 * ,"title":"a title at the beginning","visitId":"zero"}},{"Investigation":{"id"
		 * :4815,"createId":"db/notroot","createTime":"2019-03-11T15:58:33.000Z","modId"
		 * :"db/notroot","modTime":"2019-03-11T15:58:33.000Z","datasets":[],"endDate":
		 * "2011-12-31T23:59:59.000Z","investigationGroups":[],
		 * "investigationInstruments":[],"investigationUsers":[],"keywords":[],"name":
		 * "expt1","parameters":[],"publications":[],"samples":[],"shifts":[],
		 * "startDate":"2011-01-01T00:00:00.000Z","studyInvestigations":[],
		 * "title":"a title in the middle","visitId":"one"}},{"Investigation":{"id":4816
		 * ,"createId":"db/notroot","createTime":"2019-03-11T15:58:33.000Z","modId":
		 * "db/notroot","modTime":"2019-03-11T15:58:33.000Z","datasets":[],"endDate":
		 * "2012-12-31T23:59:59.000Z","investigationGroups":[],
		 * "investigationInstruments":[],"investigationUsers":[],"keywords":[],"name":
		 * "expt1","parameters":[],"publications":[],"samples":[],"shifts":[],
		 * "startDate":"2012-01-01T00:00:00.000Z","studyInvestigations":[],
		 * "title":"a title at the end","visitId":"two"}}]>
		 */
		JsonArray inv_response = search(session, "SELECT inv from Investigation inv WHERE inv.name = 'expt1'", 3);
		collector.checkThat(inv_response.getJsonObject(0).containsKey("Investigation"), is(true));

		/*
		 * Expected:
		 * <[{"InvestigationUser":{"id":4723,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","role":"troublemaker"}}]>
		 */
		JsonArray invu_response = search(session,
				"SELECT invu from InvestigationUser invu WHERE invu.role = 'troublemaker'", 1);
		collector.checkThat(invu_response.getJsonObject(0).containsKey("InvestigationUser"), is(true));

		/*
		 * Expected: <[{"Shift":{"id":2995,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","comment":"waiting","endDate":
		 * "2013-12-31T22:59:59.000Z","startDate":"2013-12-31T11:00:00.000Z"}}]>
		 */
		JsonArray shift_response = search(session, "SELECT shift from Shift shift WHERE shift.comment = 'waiting'", 1);
		collector.checkThat(shift_response.getJsonObject(0).containsKey("Shift"), is(true));

		/*
		 * Expected: <[{"SampleType":{"id":3220,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","molecularFormula":"C","name":"diamond",
		 * "safetyInformation":"fairly harmless","samples":[]}}]>
		 */
		JsonArray st_response = search(session, "SELECT st from SampleType st WHERE st.name = 'diamond'", 1);
		collector.checkThat(st_response.getJsonObject(0).containsKey("SampleType"), is(true));

		/*
		 * Expected: <[{"Sample":{"id":2181,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","datasets":[],"name":"Koh-I-Noor","parameters":[],
		 * "pid":"sdb:374717"}}]>
		 */
		JsonArray s_response = search(session, "SELECT s from Sample s WHERE s.name = 'Koh-I-Noor'", 1);
		collector.checkThat(s_response.getJsonObject(0).containsKey("Sample"), is(true));

		/*
		 * Expected:
		 * <[{"InvestigationParameter":{"id":1123,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","stringValue":"green"}}]>
		 */
		JsonArray invp_response = search(session,
				"SELECT invp from InvestigationParameter invp WHERE invp.stringValue = 'green'", 1);
		collector.checkThat(invp_response.size(), equalTo(1));
		collector.checkThat(invp_response.getJsonObject(0).containsKey("InvestigationParameter"), is(true));

		/*
		 * Expected: <[{"DatasetType":{"id":1754,"createId":"db/notroot","createTime":
		 * "2019-03-11T15:58:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-11T15:58:33.000Z","datasets":[],"name":"calibration"}}]>
		 */
		JsonArray dst_response = search(session, "SELECT dst from DatasetType dst WHERE dst.name = 'calibration'", 1);
		collector.checkThat(dst_response.getJsonObject(0).containsKey("DatasetType"), is(true));

		/*
		 * Expected: <[{"Dataset":{"id":8128,"createId":"db/notroot","createTime":
		 * "2019-03-12T11:40:26.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T11:40:26.000Z","complete":true,"dataCollectionDatasets":[],
		 * "datafiles":[],"description":"alpha","endDate":"2014-05-16T04:28:26.000Z",
		 * "name":"ds1","parameters":[],"startDate":"2014-05-16T04:28:26.000Z"}}]>
		 */
		JsonArray ds_response = search(session, "SELECT ds from Dataset ds WHERE ds.name = 'ds1'", 1);
		collector.checkThat(ds_response.getJsonObject(0).containsKey("Dataset"), is(true));
		collector.checkThat(dft.parse(ds_response.getJsonObject(0).getJsonObject("Dataset").getString("startDate")),
				isA(Date.class)); // Check Date conversion

		/*
		 * Expected:
		 * <[{"DatasetParameter":{"id":4632,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z","stringValue":"green"}}]>
		 */
		JsonArray dsp_response = search(session, "SELECT dsp from DatasetParameter dsp WHERE dsp.stringValue = 'green'",
				1);
		collector.checkThat(dsp_response.size(), equalTo(1));
		collector.checkThat(dsp_response.getJsonObject(0).containsKey("DatasetParameter"), is(true));

		/*
		 * Expected: <[{"Datafile":{"id":15643,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z","dataCollectionDatafiles":[],"destDatafiles":[],
		 * "fileSize":17,"name":"df2","parameters":[],"sourceDatafiles":[]}}]>
		 */
		JsonArray df_response = search(session, "SELECT df from Datafile df WHERE df.name = 'df2'", 1);
		collector.checkThat(df_response.size(), equalTo(1));
		collector.checkThat(df_response.getJsonObject(0).containsKey("Datafile"), is(true));

		/*
		 * Expected:
		 * <[{"DatafileParameter":{"id":1938,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z","stringValue":"green"}}]>
		 */
		JsonArray dfp_response = search(session,
				"SELECT dfp from DatafileParameter dfp WHERE dfp.stringValue = 'green'", 1);
		collector.checkThat(dfp_response.size(), equalTo(1));
		collector.checkThat(dfp_response.getJsonObject(0).containsKey("DatafileParameter"), is(true));

		/*
		 * Expected: <[{"Application":{"id":2972,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z","jobs":[],"name":"aprog","version":"1.2.3"}},{
		 * "Application":{"id":2973,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z","jobs":[],"name":"aprog","version":"1.2.6"}}]>
		 */
		JsonArray a_response = search(session, "SELECT a from Application a WHERE a.name = 'aprog'", 2);
		collector.checkThat(a_response.size(), equalTo(2));
		collector.checkThat(a_response.getJsonObject(0).containsKey("Application"), is(true));

		/*
		 * Expected:
		 * <[{DataCollection":{"id":4485,"createId":"db/notroot","createTime":"2019-03-
		 * 12T13:30:33.000Z","modId":"db/notroot","modTime":"2019-03-12T13:30:33.
		 * 000Z","dataCollectionDatafiles":[],"dataCollectionDatasets":[],"jobsAsInput":[],"jobsAsOutput":[],"parameters":[]}},{"DataCollection":{"id":4486,"createId":"db
		 * /notroot","createTime":"2019-03-12T13:30:33.000Z","modId":"db/
		 * notroot","modTime":"2019-03-12T13:30:33.
		 * 000Z","dataCollectionDatafiles":[],"dataCollectionDatasets":[],"jobsAsInput":[],"jobsAsOutput":[],"parameters":[]}},{"DataCollection":{"id":4487,"createId":"db
		 * /notroot","createTime":"2019-03-12T13:30:33.000Z","modId":"db/
		 * notroot","modTime":"2019-03-12T13:30:33.
		 * 000Z","dataCollectionDatafiles":[],"dataCollectionDatasets":[],"jobsAsInput":[],"jobsAsOutput":[],"parameters
		 * ":[]}}]>
		 */
		JsonArray dc_response = search(session, "SELECT dc from DataCollection dc", 3);
		collector.checkThat(dc_response.size(), equalTo(3));
		collector.checkThat(dc_response.getJsonObject(0).containsKey("DataCollection"), is(true));

		/*
		 * Expected:
		 * <[{"DataCollectionDatafile":{"id":4362,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z"}},{"DataCollectionDatafile":{"id":4363,"createId":
		 * "db/notroot","createTime":"2019-03-12T13:30:33.000Z","modId":"db/notroot",
		 * "modTime":"2019-03-12T13:30:33.000Z"}}]>
		 */
		JsonArray dcdf_response = search(session, "SELECT dcdf from DataCollectionDatafile dcdf", 2);
		collector.checkThat(dcdf_response.getJsonObject(0).containsKey("DataCollectionDatafile"), is(true));

		/*
		 * Expected: <[{"Job":{"id":1634,"createId":"db/notroot","createTime":
		 * "2019-03-12T13:30:33.000Z","modId":"db/notroot","modTime":
		 * "2019-03-12T13:30:33.000Z"}}]>
		 */
		JsonArray j_response = search(session, "SELECT j from Job j", 1);
		collector.checkThat(j_response.getJsonObject(0).containsKey("Job"), is(true));
	}

	@Test
	public void testClone() throws Exception {
		Session session = createAndPopulate();
		long id;
		long idClone;
		Map<String, String> keys;
		JsonArray result;

		id = search(session, "SELECT df.id FROM Datafile df WHERE df.name = 'df2'", 1).getJsonNumber(0)
				.longValueExact();
		keys = new HashMap<>();
		keys.put("name", "NewDf");
		idClone = session.cloneEntity("Datafile", id, keys);
		result = search(session,
				"SELECT df.id, df.name, p.stringValue FROM Datafile df JOIN df.parameters p WHERE df.name IN  ('df2', 'NewDf')",
				2);
		for (JsonValue r : result) {
			JsonArray ra = (JsonArray) r;
			assertEquals("green", ra.getString(2));
			if (ra.getJsonNumber(0).longValueExact() == id) {
				assertEquals("df2", ra.getString(1));
			} else if (ra.getJsonNumber(0).longValueExact() == idClone) {
				assertEquals("NewDf", ra.getString(1));
			} else {
				fail();
			}
		}

		id = search(session, "SELECT ds.id FROM Dataset ds WHERE ds.name = 'ds2'", 1).getJsonNumber(0).longValueExact();
		keys = new HashMap<>();
		keys.put("name", "NewDs");
		idClone = session.cloneEntity("Dataset", id, keys);
		result = search(session,
				"SELECT ds.id, ds.name, df.name FROM Dataset ds JOIN ds.datafiles df WHERE ds.name IN  ('ds2', 'NewDs')",
				8);
		for (JsonValue r : result) {
			JsonArray ra = (JsonArray) r;
			if (ra.getJsonNumber(0).longValueExact() == id) {
				assertEquals("ds2", ra.getString(1));
			} else if (ra.getJsonNumber(0).longValueExact() == idClone) {
				assertEquals("NewDs", ra.getString(1));
			} else {
				fail();
			}
		}

		id = search(session, "SELECT i.id FROM Investigation i WHERE i.visitId = 'one'", 1).getJsonNumber(0)
				.longValueExact();
		keys = new HashMap<>();
		keys.put("name", "NewInv");
		keys.put("visitId", "42");
		idClone = session.cloneEntity("Investigation", id, keys);
		assertEquals(4, search(session, "SELECT COUNT(x) FROM Investigation x", 1).getInt(0));
		assertEquals(8, search(session, "SELECT COUNT(x) FROM DatafileParameter x", 1).getInt(0));
		assertEquals(1, search(session, "SELECT COUNT(x) FROM Facility x", 1).getInt(0));
		assertEquals(16, search(session, "SELECT COUNT(x) FROM Datafile x", 1).getInt(0));

		id = search(session, "SELECT f.id FROM Facility f WHERE f.name = 'Test port facility'", 1).getJsonNumber(0)
				.longValueExact();
		keys = new HashMap<>();
		keys.put("name", "NewFac");
		idClone = session.cloneEntity("Facility", id, keys);
		assertEquals(8, search(session, "SELECT COUNT(x) FROM Investigation x", 1).getInt(0));
		assertEquals(16, search(session, "SELECT COUNT(x) FROM DatafileParameter x", 1).getInt(0));
		assertEquals(2, search(session, "SELECT COUNT(x) FROM Facility x", 1).getInt(0));
		assertEquals(32, search(session, "SELECT COUNT(x) FROM Datafile x", 1).getInt(0));

		try {
			keys = new HashMap<>();
			idClone = session.cloneEntity("Facility", id, keys);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
		}

		try {
			keys = new HashMap<>();
			idClone = session.cloneEntity("Investigation", 0, keys);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.NO_SUCH_OBJECT_FOUND, e.getType());
		}

		long dsid = search(session, "SELECT ds.id FROM Dataset ds LIMIT 0,1", 1).getJsonNumber(0).longValueExact();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Datafile").writeStartObject("dataset").write("id", dsid).writeEnd()
					.write("name", "secure").write("location", "here pseudokey").writeEnd().writeEnd();
		}
		List<Long> dfids = session.write(baos.toString());
		assertEquals(1, dfids.size());
		id = dfids.get(0);
		keys = new HashMap<>();
		keys.put("name", "newSecure");
		idClone = session.cloneEntity("Datafile", id, keys);

	}

	/**
	 * Tests the old lucene/data endpoint
	 */
	@Test
	public void testLuceneDatafiles() throws Exception {
		Session session = setupLuceneTest();
		JsonArray array;

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));

		// All data files
		searchDatafiles(session, null, null, null, null, null, 20, 3);

		// Use the user
		array = searchDatafiles(session, "db/tr", null, null, null, null, 20, 3);
		System.out.println(array);

		// Try a bad user
		searchDatafiles(session, "db/fred", null, null, null, null, 20, 0);

		// Set text and parameters
		array = searchDatafiles(session, null, "df2", null, null, parameters, 20, 1);
		checkResultFromLuceneSearch(session, "df2", array, "Datafile", "name");

		// Search with a user who should not see any results
		Session piOneSession = piOneSession();
		searchDatafiles(piOneSession, null, null, null, null, null, 20, 0);
	}

	/**
	 * Tests the old lucene/data endpoint
	 */
	@Test
	public void testLuceneDatasets() throws Exception {
		Session session = setupLuceneTest();

		// All datasets
		searchDatasets(session, null, null, null, null, null, 20, 5);

		// Use the user
		JsonArray array = searchDatasets(session, "db/tr", null, null, null, null, 20, 3);
		for (int i = 0; i < 3; i++) {
			long n = array.getJsonObject(i).getJsonNumber("id").longValueExact();
			JsonObject result = Json.createReader(new StringReader(session.get("Dataset", n))).readObject();
			assertEquals("ds" + (i + 1), result.getJsonObject("Dataset").getString("name"));
		}

		// Try a bad user
		searchDatasets(session, "db/fred", null, null, null, null, 20, 0);

		// Try text
		array = searchDatasets(session, null, "gamma AND ds3", null, null, null, 20, 1);

		// Try parameters
		List<ParameterForLucene> parameters = new ArrayList<>();
		ParameterForLucene stringParameter = new ParameterForLucene("colour", "name", "green");
		Date birthday = dft.parse("2014-05-16T16:58:26+0000");
		ParameterForLucene dateParameter = new ParameterForLucene("birthday", "date", birthday, birthday);
		ParameterForLucene numericParameter = new ParameterForLucene("current", "amps", 140, 165);
		array = searchDatasets(session, null, null, null, null, Arrays.asList(stringParameter), 20, 1);
		array = searchDatasets(session, null, null, null, null, Arrays.asList(dateParameter), 20, 1);
		array = searchDatasets(session, null, null, null, null, Arrays.asList(numericParameter), 20, 1);
		parameters.add(stringParameter);
		parameters.add(dateParameter);
		parameters.add(numericParameter);
		array = searchDatasets(session, null, null, null, null, parameters, 20, 1);

		Date lower = dft.parse("2014-05-16T05:09:03+0000");
		Date upper = dft.parse("2014-05-16T05:15:26+0000");
		array = searchDatasets(session, null, "gamma AND ds3", lower, upper, parameters, 20, 1);
		checkResultFromLuceneSearch(session, "gamma", array, "Dataset", "description");

		// Search with a user who should not see any results
		Session piOneSession = piOneSession();
		searchDatasets(piOneSession, null, null, null, null, null, 20, 0);
	}

	/**
	 * Tests the old lucene/data endpoint
	 */
	@Test
	public void testLuceneInvestigations() throws Exception {
		Session session = setupLuceneTest();

		Date lowerOrigin = dft.parse("2011-01-01T00:00:00+0000");
		Date lowerSecond = dft.parse("2011-01-01T00:00:01+0000");
		Date lowerMinute = dft.parse("2011-01-01T00:01:00+0000");
		Date upperOrigin = dft.parse("2011-12-31T23:59:59+0000");
		Date upperSecond = dft.parse("2011-12-31T23:59:58+0000");
		Date upperMinute = dft.parse("2011-12-31T23:58:00+0000");
		String textAnd = "title AND one";
		String textTwo = "title AND two";
		String textPlus = "title + one";

		searchInvestigations(session, null, null, null, null, null, null, null, 20, 3);

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));

		JsonArray array = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, parameters,
				null, "Professor", 20, 1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		// change user
		searchInvestigations(session, "db/fred", textAnd, null, null, parameters, null, null, 20, 0);

		// change text
		searchInvestigations(session, "db/tr", textTwo, null, null, parameters, null, null, 20, 0);

		// Only working to a minute
		array = searchInvestigations(session, "db/tr", textAnd, lowerSecond, upperOrigin, parameters, null, null, 20,
				1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		array = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperSecond, parameters, null, null, 20,
				1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		searchInvestigations(session, "db/tr", textAnd, lowerMinute, upperOrigin, parameters, null, null, 20, 0);

		searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperMinute, parameters, null, null, 20, 0);

		// Change parameters
		List<ParameterForLucene> badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", "name", "green"));
		searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, badParameters, null, null, 20, 0);

		// Change userFullName
		searchInvestigations(session, "db/tr", textPlus, lowerOrigin, upperOrigin, parameters, null, "Doctor", 20, 0);

		// Try provoking an error
		badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", null, "green"));
		try {
			searchInvestigations(session, "db/tr", null, null, null, badParameters, null, null, 10, 0);
			fail("BAD_PARAMETER exception not caught");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
		}

		// Search with a user who should not see any results
		Session piOneSession = piOneSession();
		searchInvestigations(piOneSession, null, null, null, null, null, null, null, 20, 0);
	}

	/**
	 * Tests the new search/documents endpoint
	 */
	@Test
	public void testSearchDatafiles() throws Exception {
		Session session = setupLuceneTest();
		JsonObject responseObject;
		JsonValue searchAfter;
		Map<String, String> expectation = new HashMap<>();
		expectation.put("investigation.id", null);
		expectation.put("date", "notNull");

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));

		// All data files
		searchDatafiles(session, null, null, null, null, null, null, 10, null, null, 3);

		// Use the user
		searchDatafiles(session, "db/tr", null, null, null, null, null, 10, null, null, 3);

		// Try a bad user
		searchDatafiles(session, "db/fred", null, null, null, null, null, 10, null, null, 0);

		// Set text and parameters
		responseObject = searchDatafiles(session, null, "df2", null, null, parameters, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("name", "df2");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Try sorting and searchAfter
		String sort = Json.createObjectBuilder().add("name", "desc").add("date", "asc").add("fileSize", "asc").build()
				.toString();
		responseObject = searchDatafiles(session, null, null, null, null, null, null, 1, sort, null, 1);
		searchAfter = responseObject.get("search_after");
		assertNotNull(searchAfter);
		expectation.put("name", "df3");
		checkResultsSource(responseObject, Arrays.asList(expectation), false);

		responseObject = searchDatafiles(session, null, null, null, null, null, searchAfter.toString(), 1, sort, null,
				1);
		searchAfter = responseObject.get("search_after");
		assertNotNull(searchAfter);
		expectation.put("name", "df2");
		checkResultsSource(responseObject, Arrays.asList(expectation), false);

		// Test that changes to the public steps/tables are reflected in returned fields
		PublicStep ps = new PublicStep();
		ps.setOrigin("Datafile");
		ps.setField("dataset");

		ps.setId(wSession.create(ps));
		responseObject = searchDatafiles(session, null, "df2", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("investigation.id", "notNull");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delete(ps);
		responseObject = searchDatafiles(session, null, "df2", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("investigation.id", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.addRule(null, "Dataset", "R");
		responseObject = searchDatafiles(session, null, "df2", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("investigation.id", "notNull");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delRule(null, "Dataset", "R");
		responseObject = searchDatafiles(session, null, "df2", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("investigation.id", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Test searching with someone without authz for the Datafile(s)
		searchDatafiles(piSession(), null, null, null, null, null, null, 10, null, null, 0);

		// Test no facets match on Datafiles
		JsonArray facets = buildFacetRequest("Datafile");
		responseObject = searchDatafiles(session, null, null, null, null, null, null, 10, null, facets, 3);
		assertFalse(responseObject.containsKey("search_after"));
		assertFalse(NO_DIMENSIONS, responseObject.containsKey("dimensions"));

		// Test no facets match on DatafileParameters due to lack of READ access
		facets = buildFacetRequest("DatafileParameter");
		responseObject = searchDatafiles(session, null, null, null, null, null, null, 10, null, facets, 3);
		assertFalse(responseObject.containsKey("search_after"));
		assertFalse(NO_DIMENSIONS, responseObject.containsKey("dimensions"));

		// Test facets match on DatafileParameters
		wSession.addRule(null, "DatafileParameter", "R");
		responseObject = searchDatafiles(session, null, null, null, null, null, null, 10, null, facets, 3);
		assertFalse(responseObject.containsKey("search_after"));
		checkFacets(responseObject, "DatafileParameter.type.name", Arrays.asList("colour"), Arrays.asList(1L));
	}

	/**
	 * Tests the new search/documents endpoint
	 */
	@Test
	public void testSearchDatasets() throws Exception {
		Session session = setupLuceneTest();
		JsonObject responseObject;
		JsonValue searchAfter;
		Map<String, String> expectation = new HashMap<>();
		expectation.put("startDate", "notNull");
		expectation.put("endDate", "notNull");
		expectation.put("investigation.id", "notNull");
		expectation.put("sample.name", null);
		expectation.put("sample.type.name", null);
		expectation.put("type.name", null);

		// All datasets
		searchDatasets(session, null, null, null, null, null, null, 10, null, null, 5);

		// Use the user
		responseObject = searchDatasets(session, "db/tr", null, null, null, null, null, 10, null, null, 3);
		List<Map<String, String>> expectations = new ArrayList<>();
		expectation.put("name", "ds1");
		expectations.add(new HashMap<>(expectation));
		expectation.put("name", "ds2");
		expectations.add(new HashMap<>(expectation));
		expectation.put("name", "ds3");
		expectations.add(new HashMap<>(expectation));
		checkResultsSource(responseObject, expectations, true);

		// Try a bad user
		searchDatasets(session, "db/fred", null, null, null, null, null, 10, null, null, 0);

		// Try text
		responseObject = searchDatasets(session, null, "gamma AND ds3", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Try parameters
		Date lower = dft.parse("2014-05-16T05:09:03+0000");
		Date upper = dft.parse("2014-05-16T05:15:26+0000");
		List<ParameterForLucene> parameters = new ArrayList<>();
		Date parameterDate = dft.parse("2014-05-16T16:58:26+0000");
		parameters.add(new ParameterForLucene("colour", "name", "green"));
		responseObject = searchDatasets(session, null, null, null, null, parameters, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		parameters.add(new ParameterForLucene("birthday", "date", parameterDate, parameterDate));
		responseObject = searchDatasets(session, null, null, null, null, parameters, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		parameters.add(new ParameterForLucene("current", "amps", 140, 165));
		responseObject = searchDatasets(session, null, null, null, null, parameters, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		responseObject = searchDatasets(session, null, "gamma AND ds3", lower, upper, parameters, null, 10, null, null,
				1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Try sorting and searchAfter
		String sort = Json.createObjectBuilder().add("name", "desc").add("date", "asc").add("fileSize", "asc").build()
				.toString();
		responseObject = searchDatasets(session, null, null, null, null, null, null, 1, sort, null, 1);
		searchAfter = responseObject.get("search_after");
		assertNotNull(searchAfter);
		expectation.put("name", "ds4");
		checkResultsSource(responseObject, Arrays.asList(expectation), false);

		responseObject = searchDatasets(session, null, null, null, null, null, searchAfter.toString(), 1, sort, null,
				1);
		searchAfter = responseObject.get("search_after");
		assertNotNull(searchAfter);
		expectation.put("name", "ds3");
		checkResultsSource(responseObject, Arrays.asList(expectation), false);

		// Test that changes to the public steps/tables are reflected in returned fields
		PublicStep ps = new PublicStep();
		ps.setOrigin("Dataset");
		ps.setField("type");

		ps.setId(wSession.create(ps));
		responseObject = searchDatasets(session, null, "ds1", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("name", "ds1");
		expectation.put("type.name", "calibration");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.addRule(null, "Sample", "R");
		responseObject = searchDatasets(session, null, "ds1", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("sample.name", "Koh-I-Noor");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delete(ps);
		responseObject = searchDatasets(session, null, "ds1", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("type.name", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delRule(null, "Sample", "R");
		responseObject = searchDatasets(session, null, "ds1", null, null, null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("sample.name", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Test searching with someone without authz for the Dataset(s)
		searchDatasets(piSession(), null, null, null, null, null, null, 10, null, null, 0);

		// Test facets match on Datasets
		JsonArray facets = buildFacetRequest("Dataset");
		responseObject = searchDatasets(session, null, null, null, null, null, null, 10, null, facets, 5);
		assertFalse(responseObject.containsKey("search_after"));
		checkFacets(responseObject, "Dataset.type.name", Arrays.asList("calibration"), Arrays.asList(5L));

		// Test no facets match on DatasetParameters due to lack of READ access
		facets = buildFacetRequest("DatasetParameter");
		responseObject = searchDatasets(session, null, null, null, null, null, null, 10, null, facets, 5);
		assertFalse(responseObject.containsKey("search_after"));
		assertFalse(NO_DIMENSIONS,
				responseObject.containsKey("dimensions"));

		// Test facets match on DatasetParameters
		wSession.addRule(null, "DatasetParameter", "R");
		responseObject = searchDatasets(session, null, null, null, null, null, null, 10, null, facets, 5);
		assertFalse(responseObject.containsKey("search_after"));
		checkFacets(responseObject, "DatasetParameter.type.name",
				Arrays.asList("colour", "birthday", "current"),
				Arrays.asList(1L, 1L, 1L));
	}

	/**
	 * Tests the new search/documents endpoint
	 */
	@Test
	public void testSearchInvestigations() throws Exception {
		Session session = setupLuceneTest();
		JsonObject responseObject;
		JsonValue searchAfter;
		Map<String, String> expectation = new HashMap<>();
		expectation.put("name", "expt1");
		expectation.put("startDate", "notNull");
		expectation.put("endDate", "notNull");
		expectation.put("type.name", null);
		expectation.put("facility.name", null);

		Date lowerOrigin = dft.parse("2011-01-01T00:00:00+0000");
		Date lowerSecond = dft.parse("2011-01-01T00:00:01+0000");
		Date lowerMinute = dft.parse("2011-01-01T00:01:00+0000");
		Date upperOrigin = dft.parse("2011-12-31T23:59:59+0000");
		Date upperSecond = dft.parse("2011-12-31T23:59:58+0000");
		Date upperMinute = dft.parse("2011-12-31T23:58:00+0000");
		String samplesSingular = "sample.name:ford AND sample.type.name:rust";
		String samplesMultiple = "sample.name:ford sample.type.name:rust sample.name:koh sample.type.name:diamond";
		String samplesBad = "sample.name:kog* AND sample.type.name:diamond";
		String textAnd = "title AND one";
		String textTwo = "title AND two";
		String textPlus = "title + one";

		searchInvestigations(session, null, null, null, null, null, null, null, 10, null, null, 3);

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));
		responseObject = searchInvestigations(session, "db/tr", null, null, null, null,
				null, null, 10, null, null, 2);
		responseObject = searchInvestigations(session, "db/tr", null, lowerOrigin, upperOrigin, null,
				null, null, 10, null, null, 1);
		responseObject = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, null,
				null, null, 10, null, null, 1);
		responseObject = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, parameters,
				null, null, 10, null, null, 1);
		responseObject = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, parameters,
				"Professor", null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// change user
		searchInvestigations(session, "db/fred", textAnd, null, null, parameters, null, null, 10, null, null, 0);

		// change text
		searchInvestigations(session, "db/tr", textTwo, null, null, parameters, null, null, 10, null, null, 0);

		// Only working to a minute
		responseObject = searchInvestigations(session, "db/tr", textAnd, lowerSecond, upperOrigin, parameters,
				null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		responseObject = searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperSecond, parameters,
				null, null, 10, null, null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		searchInvestigations(session, "db/tr", textAnd, lowerMinute, upperOrigin, parameters, null, null,
				10, null, null, 0);

		searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperMinute, parameters, null, null,
				10, null, null, 0);

		// Change parameters
		List<ParameterForLucene> badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", "name", "green"));
		searchInvestigations(session, "db/tr", textAnd, lowerOrigin, upperOrigin, badParameters, null,
				null, 10, null, null, 0);

		// Change samples
		searchInvestigations(session, "db/tr", samplesSingular, lowerOrigin, upperOrigin, parameters, null, null,
				10, null, null, 1);
		searchInvestigations(session, "db/tr", samplesMultiple, lowerOrigin, upperOrigin, parameters, null, null,
				10, null, null, 1);
		searchInvestigations(session, "db/tr", samplesBad, lowerOrigin, upperOrigin, parameters, null, null,
				10, null, null, 0);

		// Change userFullName
		searchInvestigations(session, "db/tr", textPlus, lowerOrigin, upperOrigin, parameters, "Doctor",
				null, 10, null, null, 0);

		// Try sorting and searchAfter
		// Note as all the investigations have the same name/date, we cannot
		// meaningfully sort them, however still check that the search succeeds in
		// returning a non-null searchAfter object
		String sort = Json.createObjectBuilder().add("name", "desc").add("date", "asc").add("fileSize", "asc").build()
				.toString();
		responseObject = searchInvestigations(session, null, null, null, null, null, null, null, 1, sort, null, 1);
		searchAfter = responseObject.get("search_after");
		assertNotNull(searchAfter);
		checkResultsSource(responseObject, Arrays.asList(expectation), false);

		// Test that changes to the public steps/tables are reflected in returned fields
		PublicStep ps = new PublicStep();
		ps.setOrigin("Investigation");
		ps.setField("type");

		ps.setId(wSession.create(ps));
		responseObject = searchInvestigations(session, null, textAnd, null, null, null, null, null, 10, null,
				null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("type.name", "atype");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.addRule(null, "Facility", "R");
		responseObject = searchInvestigations(session, null, textAnd, null, null, null, null, null, 10, null,
				null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("facility.name", "Test port facility");
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delete(ps);
		responseObject = searchInvestigations(session, null, textAnd, null, null, null, null, null, 10, null,
				null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("type.name", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		wSession.delRule(null, "Facility", "R");
		responseObject = searchInvestigations(session, null, textAnd, null, null, null, null, null, 10, null,
				null, 1);
		assertFalse(responseObject.containsKey("search_after"));
		expectation.put("facility.name", null);
		checkResultsSource(responseObject, Arrays.asList(expectation), true);

		// Test searching with someone without authz for the Investigation(s)
		searchInvestigations(piSession(), null, null, null, null, null, null, null, 10, null, null, 0);

		// Test facets match on Investigations
		JsonArray facets = buildFacetRequest("Investigation");
		responseObject = searchInvestigations(session, null, null, null, null, null, null, null, 10, null, facets,
				3);
		assertFalse(responseObject.containsKey("search_after"));
		checkFacets(responseObject, "Investigation.type.name", Arrays.asList("atype"), Arrays.asList(3L));

		// Test no facets match on InvestigationParameters due to lack of READ access
		facets = buildFacetRequest("InvestigationParameter");
		responseObject = searchInvestigations(session, null, null, null, null, null, null, null, 10, null, facets,
				3);
		assertFalse(responseObject.containsKey("search_after"));
		assertFalse(NO_DIMENSIONS, responseObject.containsKey("dimensions"));

		// Test facets match on InvestigationParameters
		wSession.addRule(null, "InvestigationParameter", "R");
		responseObject = searchInvestigations(session, null, null, null, null, null, null, null, 10, null, facets,
				3);
		assertFalse(responseObject.containsKey("search_after"));
		checkFacets(responseObject, "InvestigationParameter.type.name", Arrays.asList("colour"),
				Arrays.asList(1L));
	}

	@Test
	public void testSearchParameterValidation() throws Exception {
		Session session = setupLuceneTest();
		List<ParameterForLucene> badParameters = new ArrayList<>();

		badParameters = Arrays.asList(new ParameterForLucene(null, null, null));
		try {
			searchInvestigations(session, null, null, null, null, badParameters, null, null, 10, null, null, 0);
			fail("BAD_PARAMETER exception not caught");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
			assertEquals("name not set in one of parameters", e.getMessage());
		}

		badParameters = Arrays.asList(new ParameterForLucene("color", null, null));
		try {
			searchInvestigations(session, null, null, null, null, badParameters, null, null,10, null, null, 0);
			fail("BAD_PARAMETER exception not caught");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
			assertEquals("units not set in parameter 'color'", e.getMessage());
		}

		badParameters = Arrays.asList(new ParameterForLucene("color", "string", null));
		try {
			searchInvestigations(session, null, null, null, null, badParameters, null, null, 10, null, null, 0);
			fail("BAD_PARAMETER exception not caught");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
			assertEquals("value not set in parameter 'color'", e.getMessage());
		}
	}

	private JsonArray buildFacetRequest(String target) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonObjectBuilder dimension = Json.createObjectBuilder().add("dimension", "type.name");
		JsonArrayBuilder dimensions = Json.createArrayBuilder().add(dimension);
		builder.add("target", target).add("dimensions", dimensions);
		return Json.createArrayBuilder().add(builder).build();
	}

	private void checkFacets(JsonObject responseObject, String dimension, List<String> expectedLabels,
			List<Long> expectedCounts) {
		String dimensionsMessage = "Expected responseObject to contain 'dimensions', but it had keys "
				+ responseObject.keySet();
		assertTrue(dimensionsMessage, responseObject.containsKey("dimensions"));
		JsonObject dimensions = responseObject.getJsonObject("dimensions");
		String dimensionMessage = "Expected 'dimensions' to contain " + dimension + " but keys were "
				+ dimensions.keySet();
		assertTrue(dimensionMessage, dimensions.containsKey(dimension));
		JsonObject labelsObject = dimensions.getJsonObject(dimension);
		assertEquals(expectedLabels.size(), labelsObject.size());
		for (int i = 0; i < expectedLabels.size(); i++) {
			String expectedLabel = expectedLabels.get(i);
			assertTrue(labelsObject.containsKey(expectedLabel));
			assertEquals(expectedCounts.get(i), new Long(labelsObject.getJsonNumber(expectedLabel).longValueExact()));
		}
	}

	private void checkResultFromLuceneSearch(Session session, String val, JsonArray array, String ename, String field)
			throws IcatException {
		long n = array.getJsonObject(0).getJsonNumber("id").longValueExact();
		JsonObject result = Json.createReader(new ByteArrayInputStream(session.get(ename, n).getBytes())).readObject();
		assertEquals(val, result.getJsonObject(ename).getString(field));
	}

	private JsonArray checkResultsSize(int n, String responseString) {
		JsonArray result = Json.createReader(new ByteArrayInputStream(responseString.getBytes())).readArray();
		assertEquals(n, result.size());
		return result;
	}

	private JsonObject checkResultsArraySize(int n, String responseString) {
		JsonObject responseObject = Json.createReader(new ByteArrayInputStream(responseString.getBytes())).readObject();
		JsonArray results = responseObject.getJsonArray("results");
		assertEquals(n, results.size());
		return responseObject;
	}

	private void checkResultsSource(JsonObject responseObject, List<Map<String, String>> expectations, Boolean scored) {
		JsonArray results = responseObject.getJsonArray("results");
		assertEquals(expectations.size(), results.size());
		for (int i = 0; i < expectations.size(); i++) {
			JsonObject result = results.getJsonObject(i);
			assertTrue("id not present in " + result.toString(), result.containsKey("id"));
			String message = "score " + (scored ? "not " : "") + "present in " + result.toString();
			assertEquals(message, scored, result.containsKey("score"));

			assertTrue(result.containsKey("source"));
			JsonObject source = result.getJsonObject("source");
			assertTrue(source.containsKey("id"));
			Map<String, String> expectation = expectations.get(i);
			for (Entry<String, String> entry : expectation.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value == null) {
					assertFalse("Source " + source.toString() + " should NOT contain " + key,
							source.containsKey(key));
				} else if (value.equals("notNull")) {
					assertTrue("Source " + source.toString() + " should contain " + key, source.containsKey(key));
				} else {
					assertTrue("Source " + source.toString() + " should contain " + key, source.containsKey(key));
					assertEquals(value, source.getString(key));
				}
			}
		}
	}

	private Session piSession() throws URISyntaxException, IcatException {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "piOne");
		credentials.put("password", "piOne");
		Session piSession = icat.login("db", credentials);
		return piSession;
	}

	private Session setupLuceneTest() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		credentials.put("username", "root");
		credentials.put("password", "password");
		Session rootSession = icat.login("db", credentials);

		rootSession.luceneClear(); // Stop populating
		clearSearch(); // Really empty the db

		List<String> props = wSession.getProperties();
		System.out.println(props);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		rootSession.luceneCommit();
		return session;
	}

	/**
	 * For use with the old lucene/data endpoint
	 */
	private JsonArray searchDatafiles(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, int maxResults, int n) throws IcatException {
		String responseString = session.searchDatafiles(user, text, lower, upper, parameters, maxResults);
		return checkResultsSize(n, responseString);
	}

	/**
	 * For use with the old lucene/data endpoint
	 */
	private JsonArray searchDatasets(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, int maxResults, int n) throws IcatException {
		String responseString = session.searchDatasets(user, text, lower, upper, parameters, maxResults);
		return checkResultsSize(n, responseString);
	}

	/**
	 * For use with the old lucene/data endpoint
	 */
	private JsonArray searchInvestigations(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, List<String> samples, String userFullName, int maxResults, int n)
			throws IcatException {
		String responseString = session.searchInvestigations(user, text, lower, upper, parameters, samples,
				userFullName, maxResults);
		return checkResultsSize(n, responseString);
	}

	/**
	 * For use with the new search/documents endpoint
	 */
	private JsonObject searchDatafiles(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, String searchAfter, int maxCount, String sort, JsonArray facets, int n)
			throws IcatException {
		String responseString = session.searchDatafiles(user, text, lower, upper, parameters, searchAfter, maxCount,
				sort,
				facets);
		return checkResultsArraySize(n, responseString);
	}

	/**
	 * For use with the new search/documents endpoint
	 */
	private JsonObject searchDatasets(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, String searchAfter, int maxCount, String sort, JsonArray facets, int n)
			throws IcatException {
		String responseString = session.searchDatasets(user, text, lower, upper, parameters, searchAfter, maxCount,
				sort,
				facets);
		return checkResultsArraySize(n, responseString);
	}

	/**
	 * For use with the new search/documents endpoint
	 */
	private JsonObject searchInvestigations(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, String userFullName, String searchAfter, int maxCount, String sort,
			JsonArray facets, int n) throws IcatException {
		String responseString = session.searchInvestigations(user, text, lower, upper, parameters, userFullName,
				searchAfter, maxCount, sort, facets);
		return checkResultsArraySize(n, responseString);
	}

	@Test
	public void testVersion() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		assertTrue(icat.getVersion().startsWith("5."));
	}

	@Test
	public void testGet() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		long fid = search(session, "Facility.id", 1).getJsonNumber(0).longValueExact();

		String query = "Facility INCLUDE InvestigationType";
		JsonObject fac = Json
				.createReader(
						new ByteArrayInputStream(session.get(query, fid).getBytes()))
				.readObject().getJsonObject("Facility");

		assertEquals("Test port facility", fac.getString("name"));
		assertEquals(90, fac.getInt("daysUntilRelease"));
		JsonArray its = fac.getJsonArray("investigationTypes");
		assertEquals(2, its.size());
		List<String> names = new ArrayList<>();
		for (int j = 0; j < its.size(); j++) {
			JsonObject it = its.getJsonObject(j);
			names.add(it.getString("name"));
		}
		Collections.sort(names);
		assertEquals(Arrays.asList("atype", "btype"), names);

		// Search with a user who should not see any results
		Session piOneSession = piOneSession();
		wSession.addRule(null, "Facility", "R");
		fac = Json.createReader(new StringReader(piOneSession.get(query, fid))).readObject().getJsonObject("Facility");
		its = fac.getJsonArray("investigationTypes");
		assertEquals(0, its.size());
	}

	@Test
	public void testSearchWithNew() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		JsonArray array;

		array = search(session, "SELECT ds.name, inv.name FROM Dataset ds, ds.investigation inv LIMIT 0,20", 5);
		Set<String> dsn = new HashSet<>();
		for (JsonValue r : array) {
			JsonArray array2 = (JsonArray) r;
			assertEquals("expt1", array2.getString(1));
			dsn.add(array2.getString(0));
		}
		assertEquals(new HashSet<String>(Arrays.asList("ds1", "ds2", "ds3", "ds4")), dsn);

		array = search(session, "SELECT DISTINCT ds.name, inv.name FROM Dataset ds, ds.investigation inv LIMIT 0,20",
				4);
		dsn = new HashSet<>();
		for (JsonValue r : array) {
			JsonArray array2 = (JsonArray) r;
			assertEquals("expt1", array2.getString(1));
			dsn.add(array2.getString(0));
		}
		assertEquals(new HashSet<String>(Arrays.asList("ds1", "ds2", "ds3", "ds4")), dsn);

		array = search(session,
				"SELECT df.dataset.name, AVG(df.fileSize) FROM Datafile df GROUP BY df.dataset ORDER BY AVG(df.fileSize)",
				1);
		assertEquals("ds2", array.getJsonArray(0).getString(0));
		assertEquals(17.3333, array.getJsonArray(0).getJsonNumber(1).doubleValue(), 0.0001);

		array = search(session,
				"SELECT df.dataset.name, AVG(df.fileSize) FROM Datafile df GROUP BY df.dataset HAVING AVG(df.fileSize) > 4 ORDER BY AVG(df.fileSize)",
				1);
		assertEquals("ds2", array.getJsonArray(0).getString(0));
		assertEquals(17.3333, array.getJsonArray(0).getJsonNumber(1).doubleValue(), 0.0001);
	}

	@Test
	public void testWait() throws Exception {
		Session rootSession = rootSession();
		long t = System.currentTimeMillis();
		rootSession.waitMillis(1000L);
		System.out.println(System.currentTimeMillis() - t);
	}

	@Test
	public void testSearch() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		JsonArray array;

		JsonObject user = search(session, "SELECT u FROM User u WHERE u.name = 'db/lib'", 1).getJsonObject(0)
				.getJsonObject("User");
		assertEquals("Horace", user.getString("givenName"));
		assertEquals("Worblehat", user.getString("familyName"));
		assertEquals("Unseen University", user.getString("affiliation"));

		String query = "SELECT inv FROM Investigation inv JOIN inv.shifts AS s "
				+ "WHERE s.instrument.pid = 'ig:0815' AND s.comment = 'beamtime' "
				+ "AND s.startDate <= '2014-01-01 12:00:00' AND s.endDate >= '2014-01-01 12:00:00'";
		JsonObject inv = search(session, query, 1).getJsonObject(0).getJsonObject("Investigation");
		assertEquals("expt1", inv.getString("name"));
		assertEquals("zero", inv.getString("visitId"));

		// Make sure that fetching a non-id Double gives no problems
		assertEquals(73.0, search(session, "SELECT MIN(pt.minimumNumericValue) FROM ParameterType pt", 1)
				.getJsonNumber(0).doubleValue(), 0.001);
		assertEquals(73.4,
				((JsonNumber) search(session, "SELECT MAX(pt.minimumNumericValue) FROM ParameterType pt", 1).get(0))
						.doubleValue(),
				0.001);
		assertEquals(73.2,
				((JsonNumber) search(session, "SELECT AVG(pt.minimumNumericValue) FROM ParameterType pt", 1).get(0))
						.doubleValue(),
				0.001);

		inv = search(session, "SELECT inv FROM Investigation inv WHERE inv.visitId = 'zero'", 1)
				.getJsonObject(0).getJsonObject("Investigation");
		Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}");
		for (String name : Arrays.asList("createTime", "modTime", "startDate", "endDate")) {
			assertTrue(name + ": " + inv.getString(name), p.matcher(inv.getString(name)).find());
		}

		// Make sure all types are handled properly
		JsonObject pt = search(session, "SELECT pt FROM ParameterType pt WHERE pt.name = 'colour' LIMIT 0,1", 1)
				.getJsonObject(0).getJsonObject("ParameterType");
		assertEquals("STRING", pt.getString("valueType"));
		assertFalse(pt.getBoolean("enforced"));
		assertNull(pt.get("minimumNumericValue"));

		array = search(session, "SELECT it FROM InvestigationType it INCLUDE 1", 2);
		List<String> names = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			JsonObject it = array.getJsonObject(i).getJsonObject("InvestigationType");
			names.add(it.getString("name"));
		}
		Collections.sort(names);
		assertEquals(Arrays.asList("atype", "btype"), names);

		int n = 5;
		array = search(session, "SELECT ds.id FROM Dataset ds", n);
		List<Long> ids = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			ids.add(array.getJsonNumber(i).longValueExact());
		}
		Collections.sort(ids);

		array = search(session, "SELECT MIN(ds.id) FROM Dataset ds", 1);
		assertEquals((Long) ids.get(0), (Long) array.getJsonNumber(0).longValueExact());

		array = search(session, "SELECT COUNT(ds.id) FROM Dataset ds", 1);
		assertEquals((Long) 5L, (Long) array.getJsonNumber(0).longValueExact());

		Long sum = 0L;
		for (Long id : ids) {
			sum += id;
		}
		double avg = sum.doubleValue() / n;
		array = search(session, "SELECT AVG(ds.id) FROM Dataset ds", 1);
		assertEquals(avg, array.getJsonNumber(0).doubleValue(), 0.001);

		array = search(session, "SELECT ds.id FROM Dataset ds WHERE ds.id = 0", 0);

		array = search(session, "SELECT MIN(ds.id) FROM Dataset ds WHERE ds.id = 0", 1);
		assertTrue(array.isNull(0));

		array = search(session, "SELECT COUNT(ds.id) FROM Dataset ds WHERE ds.id = 0", 1);
		assertEquals((Long) 0L, (Long) array.getJsonNumber(0).longValueExact());

		array = search(session, "SELECT AVG(ds.id) FROM Dataset ds WHERE ds.id = 0", 1);
		assertTrue(array.isNull(0));

		array = search(session, "SELECT ds.complete FROM Dataset ds", 5);
		int trues = 0;
		int falses = 0;
		for (int i = 0; i < array.size(); i++) {
			if (array.getBoolean(i)) {
				trues++;
			} else {
				falses++;
			}
		}
		assertEquals(1, trues);
		assertEquals(4, falses);

		array = search(session, "Facility INCLUDE InvestigationType", 1);
		System.out.println(array);
		for (int i = 0; i < array.size(); i++) {
			JsonObject fac = array.getJsonObject(i).getJsonObject("Facility");
			assertEquals("Test port facility", fac.getString("name"));
			JsonArray its = fac.getJsonArray("investigationTypes");
			assertEquals(2, its.size());
			names.clear();
			for (int j = 0; j < its.size(); j++) {
				JsonObject it = its.getJsonObject(j);
				names.add(it.getString("name"));
			}
			Collections.sort(names);
			assertEquals(Arrays.asList("atype", "btype"), names);
		}

		// Search with a user who should not see any results
		Session piOneSession = piOneSession();
		wSession.addRule(null, "Facility", "R");
		JsonObject searchResult = search(piOneSession, "Facility INCLUDE InvestigationType", 1).getJsonObject(0);
		JsonArray investigationTypes = searchResult.getJsonObject("Facility").getJsonArray("investigationTypes");
		System.out.println(investigationTypes);
		assertEquals(0, investigationTypes.size());
	}

	@Test
	public void authzForUpdateAttribute() throws Exception {
		Session session = createAndPopulate();

		// Just start with Facility, two InvestigationTypes and Investigation
		wSession.clear();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Facility").write("name", "Test Facility")
					.write("daysUntilRelease", 90).writeEnd().writeEnd();
		}
		List<Long> ids = session.write(baos.toString());
		Long fid = ids.get(0);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();
			jw.writeStartObject().writeStartObject("InvestigationType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "TestExperiment").writeEnd().writeEnd();
			jw.writeStartObject().writeStartObject("InvestigationType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "TestExperiment2").writeEnd().writeEnd();
			jw.writeEnd();
		}
		ids = session.write(baos.toString());
		Long itid1 = ids.get(0);
		Long itid2 = ids.get(1);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {

			jw.writeStartObject().writeStartObject("Investigation").writeStartObject("facility").write("id", fid)
					.writeEnd().writeStartObject("type").write("id", itid1).writeEnd().write("name", "A")
					.write("title", "Not null").write("visitId", "42").writeEnd().writeEnd();

		}
		ids = session.write(baos.toString());
		Long invid = ids.get(0);

		// Should be able to change the inv.doi and inv.releaseDate
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "huedhjkwqdh")
					.write("releaseDate", "2001-01-01T17:59:00.000Z").writeEnd().writeEnd();
		}
		ids = session.write(baos.toString());

		// Now delete the the entity rule and try again -it should fail
		wSession.delRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "huedhjkwqdh")
					.write("releaseDate", "2001-01-01T17:59:00.000Z").writeEnd().writeEnd();
		}
		try {
			ids = session.write(baos.toString());
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
			assertEquals("UPDATE access to this Investigation is not allowed.", e.getMessage());
		}

		// Now add the rule to just allow update of doi
		wSession.addRule("notroot", "SELECT x FROM Investigation x", "CRD");
		wSession.addRule("notroot", "SELECT x.doi FROM Investigation x", "U");
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "D O I").writeEnd()
					.writeEnd();
		}
		ids = session.write(baos.toString());
		assertEquals(0, ids.size());

		// Now try again with two attributes
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "new DOI")
					.write("releaseDate", "2015-01-01T17:59:00.000Z").writeEnd().writeEnd();
		}
		try {
			ids = session.write(baos.toString());
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
			assertEquals("UPDATE access to this Investigation is not allowed.", e.getMessage());
		}

		// Now add the rule for second attribute so now it should work
		wSession.addRule("notroot", "SELECT x.releaseDate FROM Investigation x", "U");
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "new DOI")
					.write("releaseDate", "2015-01-01T17:59:00.000Z").writeEnd().writeEnd();
		}
		ids = session.write(baos.toString());
		assertEquals(0, ids.size());

		// Now try changing the type - a relationship which should not be
		// allowed
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "very new DOI")
					.write("releaseDate", "1984-01-01T17:59:00.000Z").writeStartObject("type").write("id", itid2)
					.writeEnd().writeEnd().writeEnd();
		}
		try {
			ids = session.write(baos.toString());
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
			assertEquals("UPDATE access to this Investigation is not allowed.", e.getMessage());
		}

		// Now add a rule allowing the relationship change
		wSession.addRule("notroot", "SELECT x.type FROM Investigation x", "U");
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "very new DOI")
					.write("releaseDate", "1984-01-01T17:59:00.000Z").writeStartObject("type").write("id", itid2)
					.writeEnd().writeEnd().writeEnd();
		}
		ids = session.write(baos.toString());
		assertEquals(0, ids.size());

		// Now try a writeNull which should produce a decent error message
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Investigation").write("id", invid).write("doi", "very new DOI")
					.write("releaseDate", "1984-01-01T17:59:00.000Z").writeNull("type").writeEnd().writeEnd();
		}
		try {
			ids = session.write(baos.toString());
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getType());
			assertEquals("Investigation: type cannot be null.", e.getMessage());
		}

		try {
		} finally {
			wSession.setAuthz();
		}
	}

	@Test
	public void authzForUpdate() throws Exception {
		Session session = createAndPopulate();

		// Make sure that fetching a non-id Double gives no problems
		assertEquals(73.0, search(session, "SELECT MIN(pt.minimumNumericValue) FROM ParameterType pt", 1)
				.getJsonNumber(0).doubleValue(), 0.001);

		wSession.clear();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Facility").write("name", "Test Facility")
					.write("daysUntilRelease", 90).writeEnd().writeEnd();
		}
		List<Long> ids = session.write(baos.toString());
		Long fid = ids.get(0);

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("InvestigationType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "TestExperiment").writeEnd().writeEnd();
		}
		ids = session.write(baos.toString());
		Long itid = ids.get(0);

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("DatasetType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "X").writeEnd().writeEnd();

			jw.writeStartObject().writeStartObject("DatasetType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "Y").writeEnd().writeEnd();

			jw.writeEnd();
		}
		ids = session.write(baos.toString());
		Long dstX = ids.get(0);
		Long dstY = ids.get(1);

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Investigation").writeStartObject("facility").write("id", fid)
					.writeEnd().writeStartObject("type").write("id", itid).writeEnd().write("name", "A")
					.write("title", "Not null").write("visitId", "42").writeEnd().writeEnd();

			jw.writeStartObject().writeStartObject("Investigation").writeStartObject("facility").write("id", fid)
					.writeEnd().writeStartObject("type").write("id", itid).writeEnd().write("name", "B")
					.write("title", "Not null").write("visitId", "42").writeEnd().writeEnd();

			jw.writeEnd();
		}
		ids = session.write(baos.toString());
		Long invA = ids.get(0);
		Long invB = ids.get(1);

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Dataset").writeStartObject("type").write("id", dstX).writeEnd()
					.writeStartObject("investigation").write("id", invA).writeEnd().write("name", "A1").writeEnd()
					.writeEnd();
		}
		ids = session.write(baos.toString());
		Long dsid = ids.get(0);

		try {
			/* Initially can read datasets from investigations A and B */
			wSession.delRule("notroot", "Dataset", "CRUD");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "R");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'B']", "R");

			assertEquals(dsid,
					(Long) search(session, "SELECT ds.id FROM Dataset ds WHERE ds.investigation.name = 'A' INCLUDE 1",
							1).getJsonNumber(0).longValueExact());

			try {
				baos = new ByteArrayOutputStream();
				try (JsonGenerator jw = Json.createGenerator(baos)) {
					jw.writeStartObject().writeStartObject("Dataset").write("id", dsid).write("name", "A2").writeEnd()
							.writeEnd();
				}
				ids = session.write(baos.toString());
				fail();
			} catch (IcatException e) {
				assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
				assertEquals("UPDATE access to this Dataset is not allowed.", e.getMessage());
			}

			/*
			 * Permissions were insufficient to change an attribute value so
			 * change to allow update for data sets from investigation A
			 */
			wSession.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "R");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "U");

			baos = new ByteArrayOutputStream();
			try (JsonGenerator jw = Json.createGenerator(baos)) {
				jw.writeStartObject().writeStartObject("Dataset").write("id", dsid).write("name", "A2").writeEnd()
						.writeEnd();
			}
			ids = session.write(baos.toString());

			/*
			 * Check that non-defining relationship fields can also be updated
			 */
			baos = new ByteArrayOutputStream();
			try (JsonGenerator jw = Json.createGenerator(baos)) {
				jw.writeStartObject().writeStartObject("Dataset").write("id", dsid).writeStartObject("type")
						.write("id", dstY).writeEnd().writeEnd().writeEnd();
			}
			ids = session.write(baos.toString());

			/* Changing a defining relationship field will fail however */
			try {
				baos = new ByteArrayOutputStream();
				try (JsonGenerator jw = Json.createGenerator(baos)) {
					jw.writeStartObject().writeStartObject("Dataset").write("id", dsid)
							.writeStartObject("investigation").write("id", invB).writeEnd().writeEnd().writeEnd();
				}
				ids = session.write(baos.toString());
				fail();
			} catch (IcatException e) {
				assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
				assertEquals("DELETE access implied by UPDATE to this Dataset is not allowed.", e.getMessage());
			}

			/*
			 * This effectively does a delete from investigation A so permit
			 * this
			 */
			wSession.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "U");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
			try {
				session.write(baos.toString());
				fail();
			} catch (IcatException e) {
				assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
				assertEquals("CREATE access to this Dataset is not allowed.", e.getMessage());
			}

			/*
			 * But it also is effectively a create in investigation B. So add
			 * create to A - which won't work of course
			 */
			wSession.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "CUD");
			try {
				session.write(baos.toString());
				fail();
			} catch (IcatException e) {
				assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
				assertEquals("CREATE access to this Dataset is not allowed.", e.getMessage());
			}

			/* So now add create to B - and it works */
			wSession.delRule("notroot", "Dataset <-> Investigation [name = 'A']", "CUD");
			wSession.delRule("notroot", "Dataset <-> Investigation [name = 'B']", "R");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'A']", "UD");
			wSession.addRule("notroot", "Dataset <-> Investigation [name = 'B']", "C");
			session.write(baos.toString());
		} finally {
			// wSession.setAuthz();
		}

	}

	private JsonArray search(Session session, String query, int n) throws IcatException {
		JsonArray result = Json.createReader(new ByteArrayInputStream(session.search(query).getBytes())).readArray();
		assertEquals(n, result.size());
		return result;
	}

	@Test
	public void testWriteGood() throws Exception {

		Session session = createAndPopulate();

		// One
		Long fid = ((EntityBaseBean) wSession.search("Facility INCLUDE InvestigationType").get(0)).getId();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("InvestigationType").writeStartObject("facility").write("id", fid)
					.writeEnd().write("name", "ztype").writeEnd().writeEnd();

			jw.writeStartObject().writeStartObject("Facility").write("name", "another fred").writeEnd().writeEnd();

			jw.writeEnd();
		}
		List<Long> ids = session.write(baos.toString());
		assertEquals(2, ids.size());
		long newInvTypeId = ids.get(0);
		long newFid = ids.get(1);

		JsonArray array = search(session,
				"SELECT it.name, it.facility.name FROM InvestigationType it WHERE it.id = " + newInvTypeId, 1)
				.getJsonArray(0);
		assertEquals("ztype", array.getString(0));
		assertEquals("Test port facility", array.getString(1));

		array = search(session, "SELECT f.name FROM Facility f WHERE f.id = " + newFid, 1);
		assertEquals("another fred", array.getString(0));

		// Two
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("InvestigationType").writeStartObject("facility").write("id", newFid)
					.write("fullName", "Frederick").write("description", "some new words").writeEnd()
					.write("name", "ztype").writeEnd().writeEnd();

			jw.writeStartObject().writeStartObject("Facility").write("name", "yet another fred").writeEnd().writeEnd();

			jw.writeEnd();
		}

		ids = session.write(baos.toString());
		assertEquals(2, ids.size());
		long newestFid = ids.get(1);

		array = search(session,
				"SELECT it.name, it.facility.name, it.facility.description FROM InvestigationType it WHERE it.id = "
						+ ids.get(0),
				1).getJsonArray(0);
		assertEquals("ztype", array.getString(0));
		assertEquals("another fred", array.getString(1));
		assertEquals("some new words", array.getString(2));

		array = search(session, "SELECT f.id, f.name, f.description FROM Facility f WHERE f.id = " + newFid, 1)
				.getJsonArray(0);

		assertEquals(newFid, array.getJsonNumber(0).longValueExact());
		assertEquals("another fred", array.getString(1));
		assertEquals("some new words", array.getString(2));

		array = search(session, "SELECT f.id, f.name, f.description FROM Facility f WHERE f.id = " + newestFid, 1)
				.getJsonArray(0);
		assertEquals(newestFid, array.getJsonNumber(0).longValueExact());
		assertEquals("yet another fred", array.getString(1));
		assertTrue(array.isNull(2));

		// Three
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {

			jw.writeStartObject().writeStartObject("Facility").write("id", newestFid).write("name", "Not Fred")
					.writeEnd().writeEnd();

		}
		ids = session.write(baos.toString());
		assertEquals(0, ids.size());

		array = search(session, "SELECT f.id, f.name, f.description FROM Facility f WHERE f.id = " + newestFid, 1)
				.getJsonArray(0);
		assertEquals(newestFid, array.getJsonNumber(0).longValueExact());
		assertEquals("Not Fred", array.getString(1));
		assertTrue(array.isNull(2));

		// Four
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Facility").write("name", "Pinot Grigio")
					.writeStartArray("investigationTypes");

			jw.writeStartObject().write("name", "a").writeEnd();
			jw.writeStartObject().write("name", "b").writeEnd();

			jw.writeEnd().writeEnd().writeEnd();

			jw.writeEnd();
		}
		ids = session.write(baos.toString());
		assertEquals(1, ids.size());
		array = search(session, "SELECT f.name, it.name FROM Facility f, f.investigationTypes it WHERE f.id = "
				+ ids.get(0) + " ORDER BY it.name", 2);
		assertEquals("Pinot Grigio", array.getJsonArray(0).getString(0));
		assertEquals("Pinot Grigio", array.getJsonArray(1).getString(0));
		assertEquals("a", array.getJsonArray(0).getString(1));
		assertEquals("b", array.getJsonArray(1).getString(1));

		// Five
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {

			jw.writeStartObject().writeStartObject("ParameterType").write("name", "test").write("units", "seconds")
					.writeStartObject("facility").write("id", fid).writeEnd().write("valueType", "NUMERIC")
					.write("applicableToDatafile", true).writeEnd().writeEnd();

		}
		ids = session.write(baos.toString());
		assertEquals(1, ids.size());
		Long ptId = ids.get(0);
		array = search(session,
				"SELECT p.name, p.units, p.valueType, p.createTime FROM ParameterType p where p.id = " + ptId, 1);
		assertEquals("test", array.getJsonArray(0).getString(0));
		assertEquals("seconds", array.getJsonArray(0).getString(1));
		assertEquals("NUMERIC", array.getJsonArray(0).getString(2));

		// Six
		long dfId = search(session, "SELECT df.id FROM Datafile df LIMIT 0, 1", 1).getJsonNumber(0).longValueExact();

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {

			jw.writeStartObject().writeStartObject("DatafileParameter").write("numericValue", 20)
					.writeStartObject("datafile").write("id", dfId).writeEnd().writeStartObject("type")
					.write("id", ptId).writeEnd().writeEnd().writeEnd();

		}
		System.out.println(baos.toString());
		ids = session.write(baos.toString());
		assertEquals(1, ids.size());
		Long dfpId = ids.get(0);
		array = search(session, "SELECT p.numericValue FROM DatafileParameter p where p.id = " + dfpId, 1);
		assertEquals(20.0, array.getJsonNumber(0).doubleValue(), .001);
	}

	@Test
	public void testBug() throws Exception {
		Session session = createAndPopulate();
		ByteArrayOutputStream baos;
		JsonArray ds = search(session, "SELECT ds.id, ds.modTime FROM Dataset ds WHERE ds.name = 'ds1' LIMIT 0, 1", 1)
				.getJsonArray(0);
		Long dsid = ds.getJsonNumber(0).longValueExact();
		LocalDateTime modTime = LocalDateTime.parse(ds.getJsonString(1).getString(),
				DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		Long typid = search(session, "SELECT t.id FROM ParameterType t WHERE t.applicableToDataset = True LIMIT 0, 1",
				1).getJsonNumber(0).longValueExact();

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Dataset").write("id", dsid)
					.write("description", "how can I compare...").writeEnd().writeEnd();
		}
		session.write(baos.toString());
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("DatasetParameter").writeStartObject("dataset").write("id", dsid)
					.writeEnd().writeStartObject("type").write("id", typid).writeEnd().write("numericValue", 42)
					.writeEnd().writeEnd();
		}

		session.write(baos.toString());
		Thread.sleep(2000);

		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Dataset").write("id", dsid)
					.write("description", "Attempt to update").writeEnd().writeEnd();
		}
		session.write(baos.toString());

		ds = search(session, "SELECT ds.description, ds.modTime FROM Dataset ds  WHERE ds.name = 'ds1' LIMIT 0, 1", 1)
				.getJsonArray(0);
		assertEquals("Attempt to update", ds.getJsonString(0).getString());
		LocalDateTime newModTime = LocalDateTime.parse(ds.getJsonString(1).getString(),
				DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		assertEquals(1, newModTime.compareTo(modTime));
	}

	@Test
	public void testBug2() throws Exception {
		Session session = createAndPopulate();
		ByteArrayOutputStream baos;
		baos = new ByteArrayOutputStream();
		Long dsid = search(session, "SELECT ds.id FROM Dataset ds LIMIT 0, 1", 1).getJsonNumber(0).longValueExact();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Dataset").write("id", dsid).writeStartArray("datafiles")
					.writeStartObject().write("name", "df33").write("location", "everywhere").writeEnd().writeEnd()
					.writeEnd().writeEnd();
		}
		session.write(baos.toString());
	}

	@Test
	public void testWriteBad() throws Exception {

		Session session = createAndPopulate();
		ByteArrayOutputStream baos;

		// One
		try {
			session.write("rubbish");
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
			assertEquals("Unexpected char 114 at (line no=1, column no=1, offset=0) in json rubbish", e.getMessage());
			assertEquals(0, e.getOffset());
		}

		// Two
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Facility").write("name", "Woderwick").writeEnd().writeEnd();
			jw.writeStartObject().writeStartObject("Facility").writeEnd().writeEnd();

			jw.writeEnd();
		}
		try {
			session.write(baos.toString());
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getType());
			assertEquals("Facility: name cannot be null.", e.getMessage());
			assertEquals(1, e.getOffset());
		}

		// Three
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Facility").write("name", "Woderwick").writeEnd().writeEnd();

			jw.writeEnd();
		}
		session.write(baos.toString());
		try {
			session.write(baos.toString());
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
			assertEquals("Facility exists with name = 'Woderwick'", e.getMessage());
			assertEquals(0, e.getOffset());
		}

		// Four
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Facility").write("name", "Gargantua").writeEnd().writeEnd();
			jw.writeStartObject().writeStartObject("Facility").write("name", "Gargantua").writeEnd().writeEnd();

			jw.writeEnd();
		}
		try {
			session.write(baos.toString());
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
			assertEquals("Facility exists with name = 'Gargantua'", e.getMessage());
			assertEquals(1, e.getOffset());
		}

		// Five
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject().writeStartObject("Facility").write("name", "Pinot Grigio")
					.writeStartArray("investigationTypes");

			jw.writeStartObject().write("name", "a").writeEnd();
			jw.writeStartObject().write("name", "b").writeEnd();
			jw.writeStartObject().write("name", "c").writeEnd();
			jw.writeStartObject().write("name", "c").writeEnd();
			jw.writeStartObject().write("name", "d").writeEnd();

			jw.writeEnd().writeEnd().writeEnd();

			jw.writeEnd();
		}
		try {
			session.write(baos.toString());
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
			assertEquals("InvestigationType exists with name = 'c', facility = 'id:", e.getMessage().substring(0, 57));
			assertEquals(0, e.getOffset());
		}

		// six
		Long dsid = search(session, "SELECT ds.id FROM Dataset ds LIMIT 0, 1", 1).getJsonNumber(0).longValueExact();
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartArray(("Datafile")).writeStartObject().write("name", "df3")
					.write("location", "loc3").writeStartObject("dataset").write("id", dsid).writeEnd().writeEnd()
					.writeEnd().writeEnd();
		}
		try {
			session.write(baos.toString());
			fail("Should have thrown an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
			assertTrue(e.getMessage().startsWith("Unexpected array found in JSON [{"));
			assertEquals(0, e.getOffset());
		}
	}

	private Session createAndPopulate() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		return session;
	}

	@Test
	public void testDelete() throws Exception {
		Session session = createAndPopulate();
		search(session, "Dataset", 5);
		Long did = search(session, "SELECT ds.id FROM Dataset ds LIMIT 0, 1", 1).getJsonNumber(0).longValueExact();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartObject().writeStartObject("Dataset").write("id", did).writeEnd().writeEnd();
		}

		session.delete(baos.toString());
		search(session, "Dataset", 4);

		JsonArray dids = search(session, "SELECT ds.id FROM Dataset ds LIMIT 0, 2", 2);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();
			jw.writeStartObject().writeStartObject("Dataset").write("id", dids.getJsonNumber(0).longValueExact())
					.writeEnd().writeEnd();
			jw.writeStartObject().writeStartObject("Dataset").write("id", dids.getJsonNumber(1).longValueExact())
					.writeEnd().writeEnd();
			jw.writeEnd();
		}

		session.delete(baos.toString());
		search(session, "Dataset", 2);

		try {
			baos = new ByteArrayOutputStream();
			try (JsonGenerator jw = Json.createGenerator(baos)) {
				jw.writeStartObject().writeStartObject("Dataset").write("id", -1).writeEnd().writeEnd();
			}
			session.delete(baos.toString());
			fail("Should have thrown an error");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.NO_SUCH_OBJECT_FOUND, e.getType());
			assertEquals(0, e.getOffset());
		}
	}

	@Test
	public void testSession() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		assertFalse(icat.isLoggedIn("mnemonic/rubbish"));
		assertFalse(icat.isLoggedIn("rubbish"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		assertEquals("db/notroot", session.getUserName());
		double remainingMinutes = session.getRemainingMinutes();
		assertTrue(remainingMinutes > 119 && remainingMinutes < 120);
		assertTrue(icat.isLoggedIn("db/notroot"));
		session.logout();
		try {
			session.getRemainingMinutes();
			fail();
		} catch (IcatException e) {
			// No action
		}
		session = icat.login("db", credentials);
		Thread.sleep(1000);
		remainingMinutes = session.getRemainingMinutes();
		session.refresh();
		assertTrue(session.getRemainingMinutes() > remainingMinutes);
	}

	@Test
	public void badPlugin() throws Exception {
		try {
			ICAT icat = new ICAT(System.getProperty("serverUrl"));
			Map<String, String> credentials = new HashMap<>();
			credentials.put("username", "notroot");
			credentials.put("password", "password");
			icat.login("typo", credentials);
			fail("Should throw an exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.SESSION, e.getType());
			assertEquals("Authenticator mnemonic typo not recognised", e.getMessage());
		}
	}

	private static void ts(String msg) {
		end = System.currentTimeMillis();
		System.out.println("Time to " + msg + ": " + (end - start) + "ms.");
		start = end;
	}

	@Test
	public void importMetaDataUser() throws Exception {
		importMetaData(Attributes.USER, "db/root");
	}

	@Test
	public void importMetaDataAll() throws Exception {
		importMetaData(Attributes.ALL, "Zorro");
	}

	@Test
	public void exportMetaDataDumpUser() throws Exception {
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "piOne");
		credentials.put("password", "piOne");
		exportMetaDataDump(credentials);
	}

	@Test
	public void exportMetaDataDumpRoot() throws Exception {
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		exportMetaDataDump(credentials);
	}

	private void exportMetaDataDump(Map<String, String> credentials) throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		Map<String, String> rootCredentials = new HashMap<>();
		rootCredentials.put("username", "root");
		rootCredentials.put("password", "password");
		Session rootSession = icat.login("db", rootCredentials);

		// Get known configuration
		rootSession.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);

		Path dump1 = Files.createTempFile("dump1", ".tmp");
		Path dump2 = Files.createTempFile("dump2", ".tmp");
		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData(Attributes.ALL)) {
			Files.copy(stream, dump1, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump1 for " + credentials.get("username"));
		wSession.clear();
		wSession.clearAuthz();
		rootSession.importMetaData(dump1, DuplicateAction.THROW, Attributes.ALL);

		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData(Attributes.ALL)) {
			Files.copy(stream, dump2, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump2 for " + credentials.get("username"));
		assertEquals(dump1.toFile().length(), dump2.toFile().length());
		Files.delete(dump1);
		Files.delete(dump2);
	}

	@Test
	public void exportMetaDataQueryUser() throws Exception {
		Session rootSession = rootSession();
		Session piOneSession = piOneSession();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		// Get known configuration
		rootSession.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);
		String query = "Investigation INCLUDE Facility, Dataset";
		Path dump1 = Files.createTempFile("dump1", ".tmp");
		Path dump2 = Files.createTempFile("dump2", ".tmp");

		// piOne should only be able to dump the Investigation, but not have R access to
		// Dataset, Facility
		wSession.addRule(null, "Investigation", "R");
		try (InputStream stream = piOneSession.exportMetaData(query, Attributes.USER)) {
			Files.copy(stream, dump1, StandardCopyOption.REPLACE_EXISTING);
		}
		// piOne should now be able to dump all due to rules giving R access
		wSession.addRule(null, "Facility", "R");
		wSession.addRule(null, "Dataset", "R");
		try (InputStream stream = piOneSession.exportMetaData(query, Attributes.USER)) {
			Files.copy(stream, dump2, StandardCopyOption.REPLACE_EXISTING);
		}
		List<String> restrictedLines = Files.readAllLines(dump1);
		List<String> permissiveLines = Files.readAllLines(dump2);
		String restrictiveMessage = " appeared in export, but piOne use should not have access";
		String permissiveMessage = " did not appear in export, but piOne should have access";

		boolean containsInvestigations = false;
		for (String line : restrictedLines) {
			System.out.println(line);
			containsInvestigations = containsInvestigations || line.startsWith("Investigation");
			assertFalse("Dataset" + restrictiveMessage, line.startsWith("Dataset"));
			assertFalse("Facility" + restrictiveMessage, line.startsWith("Facility"));
		}
		assertTrue("Investigation" + permissiveMessage, containsInvestigations);

		containsInvestigations = false;
		boolean containsDatasets = false;
		boolean containsFacilities = false;
		for (String line : permissiveLines) {
			System.out.println(line);
			containsInvestigations = containsInvestigations || line.startsWith("Investigation");
			containsDatasets = containsDatasets || line.startsWith("Dataset");
			containsFacilities = containsFacilities || line.startsWith("Facility");
		}
		assertTrue("Investigation" + permissiveMessage, containsInvestigations);
		assertTrue("Dataset" + permissiveMessage, containsDatasets);
		assertTrue("Facility" + permissiveMessage, containsFacilities);

		Files.delete(dump1);
		Files.delete(dump2);
	}

	@Ignore("Test fails - appears brittle to differences in timezone")
	@Test
	public void exportMetaDataQuery() throws Exception {
		Session session = rootSession();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		// Get known configuration
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);

		Path dump = Files.createTempFile("dump1", ".tmp");
		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset", Attributes.USER)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump USER");
		assertEquals(1924, dump.toFile().length());
		session.importMetaData(dump, DuplicateAction.CHECK, Attributes.USER);

		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset", Attributes.ALL)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump ALL");
		long n = dump.toFile().length();

		assertTrue("Size is dependent upon time zone in which test is run " + n,
				n == 2686 || n == 1518 || n == 2771 || n == 2776 || n == 2691);

		session.importMetaData(dump, DuplicateAction.CHECK, Attributes.ALL);
		Files.delete(dump);
	}

	@Test
	public void importMetaDataAllNotRoot() throws Exception {
		Session session = piOneSession();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		try {
			session.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
		}
	}

	private void importMetaData(Attributes attributes, String userName) throws Exception {
		Session session = rootSession();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		start = System.currentTimeMillis();
		session.importMetaData(path, DuplicateAction.CHECK, attributes);
		ts("Simple import");

		long facilityId = (Long) wSession.search("Facility.id [name = 'Test port facility']").get(0);

		Facility facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());

		session.importMetaData(path, DuplicateAction.IGNORE, attributes);
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with ignore");

		session.importMetaData(path, DuplicateAction.CHECK, attributes);
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with check");

		try {
			session.importMetaData(path, DuplicateAction.THROW, attributes);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
		}
		ts("Import with throw");

		session.importMetaData(path, DuplicateAction.OVERWRITE, attributes);
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with overwrite");

		facility = (Facility) wSession.get("Facility INCLUDE 1", facilityId);
		facility.setDaysUntilRelease(365);
		wSession.update(facility);

		session.importMetaData(path, DuplicateAction.IGNORE, attributes);
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 365, facility.getDaysUntilRelease());
		ts("Import with ignore after edit");

		try {
			session.importMetaData(path, DuplicateAction.CHECK, attributes);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.VALIDATION, e.getType());
		}
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 365, facility.getDaysUntilRelease());
		ts("Import with check after edit");

		try {
			session.importMetaData(path, DuplicateAction.THROW, attributes);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
		}
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 365, facility.getDaysUntilRelease());
		ts("Import with throw after edit");

		session.importMetaData(path, DuplicateAction.OVERWRITE, attributes);
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with overwrite after edit");

		try {
			session.importMetaData(path, DuplicateAction.THROW, attributes);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getType());
		}
		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with throw after edit and overwrite");

		session.importMetaData(path, DuplicateAction.CHECK, attributes);

		facility = (Facility) wSession.get("Facility", facilityId);
		assertEquals(userName, facility.getCreateId());
		assertEquals((Integer) 90, facility.getDaysUntilRelease());
		ts("Import with check after edit and overwrite");

	}

	@Test
	public void testLucenePopulate() throws Exception {
		createAndPopulate();

		Map<String, String> credentials = new HashMap<>();

		credentials.put("username", "root");
		credentials.put("password", "password");
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Session session = icat.login("db", credentials);

		session.luceneClear(); // Stop populating
		clearSearch(); // Really empty the db

		assertTrue(session.luceneGetPopulating().isEmpty());

		session.lucenePopulate("Dataset", 0);
		session.lucenePopulate("Datafile", 0);
		session.lucenePopulate("Investigation", 0);

		do {
			Thread.sleep(1000);
		} while (!session.luceneGetPopulating().isEmpty());

	}

}
