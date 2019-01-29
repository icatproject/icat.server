package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
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
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.icatproject.EntityBaseBean;
import org.icatproject.Facility;
import org.icatproject.core.manager.LuceneApi;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.IcatException.IcatExceptionType;
import org.icatproject.icat.client.ParameterForLucene;
import org.icatproject.icat.client.Session;
import org.icatproject.icat.client.Session.Attributes;
import org.icatproject.icat.client.Session.DuplicateAction;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In
 * particular does the INCLUDE mechanism work properly.
 */
public class TestRS {

	private static WSession wSession;
	private static long end;
	private static long start;

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

	@Ignore("Test fails because of bug in eclipselink")
	@Test
	public void testDistinctBehaviour() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		// See what happens to query with bad order by
		System.out.println(search(session, "SELECT inv FROM Investigation inv", 3));

		System.out.println(
				search(session, "SELECT inv FROM Investigation inv, inv.investigationUsers u ORDER BY u.user.name", 3));

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
	}

	@Test
	public void testLuceneDatasets() throws Exception {

		Session session = setupLuceneTest();

		DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

		// All datasets
		searchDatasets(session, null, null, null, null, null, 20, 5);

		// Use the user
		Set<String> names = new HashSet<>();
		JsonArray array = searchDatasets(session, "db/tr", null, null, null, null, 20, 3);

		for (int i = 0; i < 3; i++) {

			long n = array.getJsonObject(i).getJsonNumber("id").longValueExact();
			JsonObject result = Json.createReader(new ByteArrayInputStream(session.get("Dataset", n).getBytes()))
					.readObject();
			names.add(result.getJsonObject("Dataset").getString("name"));
		}
		assertTrue(names.contains("ds1"));
		assertTrue(names.contains("ds2"));
		assertTrue(names.contains("ds3"));

		// Try a bad user
		searchDatasets(session, "db/fred", null, null, null, null, 20, 0);

		// Try text
		array = searchDatasets(session, null, "gamma AND ds3", null, null, null, 20, 1);

		// Try parameters
		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));
		parameters.add(new ParameterForLucene("birthday", "date", dft.parse("2014-05-16T16:58:26+0000"),
				dft.parse("2014-05-16T16:58:26+0000")));
		parameters.add(new ParameterForLucene("current", "amps", 140, 165));

		array = searchDatasets(session, null, null, null, null, parameters, 20, 1);

		array = searchDatasets(session, null, "gamma AND ds3", dft.parse("2014-05-16T05:09:03+0000"),
				dft.parse("2014-05-16T05:15:26+0000"), parameters, 20, 1);
		checkResultFromLuceneSearch(session, "gamma", array, "Dataset", "description");
	}

	@Test
	public void testLuceneInvestigations() throws Exception {
		Session session = setupLuceneTest();

		DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

		searchInvestigations(session, null, null, null, null, null, null, null, 20, 3);

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));

		JsonArray array = searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), parameters, Arrays.asList("ford AND rust", "koh* AND diamond"),
				"Professor", 20, 1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		// change user
		searchInvestigations(session, "db/fred", "title AND one", null, null, parameters, null, null, 20, 0);

		// change text
		searchInvestigations(session, "db/tr", "title AND two", null, null, parameters, null, null, 20, 0);

		// Only working to a minute
		array = searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:01+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), parameters, null, null, 20, 1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		array = searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:59:58+0000"), parameters, null, null, 20, 1);
		checkResultFromLuceneSearch(session, "one", array, "Investigation", "visitId");

		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:01:00+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), parameters, null, null, 20, 0);

		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:58:00+0000"), parameters, null, null, 20, 0);

		// Change parameters
		List<ParameterForLucene> badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", "name", "green"));
		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), badParameters, Arrays.asList("ford + rust", "koh + diamond"),
				null, 20, 0);

		// Change samples
		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), parameters, Arrays.asList("ford AND rust", "kog* AND diamond"),
				null, 20, 0);

		// Change userFullName
		searchInvestigations(session, "db/tr", "title + one", dft.parse("2011-01-01T00:00:00+0000"),
				dft.parse("2011-12-31T23:59:59+0000"), parameters, Arrays.asList("ford AND rust", "koh* AND diamond"),
				"Doctor", 20, 0);

		// Try provoking an error
		badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", null, "green"));
		try {
			searchInvestigations(session, "db/tr", null, null, null, badParameters, null, null, 10, 0);
			fail("BAD_PARAMETER exception not caught");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
		}
	}

	private void checkResultFromLuceneSearch(Session session, String val, JsonArray array, String ename, String field)
			throws IcatException {
		long n = array.getJsonObject(0).getJsonNumber("id").longValueExact();
		JsonObject result = Json.createReader(new ByteArrayInputStream(session.get(ename, n).getBytes())).readObject();
		assertEquals(val, result.getJsonObject(ename).getString(field));
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

		String urlString = System.getProperty("luceneUrl");
		URI uribase = new URI(urlString);
		LuceneApi luceneApi = new LuceneApi(uribase);
		luceneApi.clear(); // Really empty the db

		List<String> props = wSession.getProperties();
		System.out.println(props);

		// Get known configuration
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);

		rootSession.luceneCommit();
		return session;
	}

	private JsonArray searchDatasets(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, int maxResults, int n) throws IcatException {
		JsonArray result = Json
				.createReader(new ByteArrayInputStream(
						session.searchDatasets(user, text, lower, upper, parameters, maxResults).getBytes()))
				.readArray();
		assertEquals(n, result.size());
		return result;
	}

	private JsonArray searchDatafiles(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, int maxResults, int n) throws IcatException {
		JsonArray result = Json
				.createReader(new ByteArrayInputStream(
						session.searchDatafiles(user, text, lower, upper, parameters, maxResults).getBytes()))
				.readArray();
		assertEquals(n, result.size());
		return result;
	}

	@Test
	public void testVersion() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		assertTrue(icat.getVersion().startsWith("4."));
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

		JsonObject fac = Json
				.createReader(
						new ByteArrayInputStream(session.get("Facility INCLUDE InvestigationType", fid).getBytes()))
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
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session rootSession = icat.login("db", credentials);
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

		JsonObject user = search(session, "SELECT u FROM User u WHERE u.name = 'db/lib'", 1).getJsonObject(0).getJsonObject("User");
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

		// TODO this is wrong - there should be 4 false and 1 true
		array = search(session, "SELECT ds.complete FROM Dataset ds", 4);
		int trues = 0;
		int falses = 0;
		for (int i = 0; i < array.size(); i++) {
			if (array.getBoolean(i)) {
				trues++;
			} else {
				falses++;
			}
		}
		assertEquals(0, trues);
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

	private JsonArray searchInvestigations(Session session, String user, String text, Date lower, Date upper,
			List<ParameterForLucene> parameters, List<String> samples, String userFullName, int maxResults, int n)
			throws IcatException {
		JsonArray result = Json.createReader(new ByteArrayInputStream(
				session.searchInvestigations(user, text, lower, upper, parameters, samples, userFullName, maxResults)
						.getBytes()))
				.readArray();
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

	@Ignore("Test fails - appears brittle to differences in timezone")
	@Test
	public void exportMetaDataQuery() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
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
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "piOne");
		credentials.put("password", "piOne");
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		try {
			session.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);
			fail();
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.INSUFFICIENT_PRIVILEGES, e.getType());
		}
	}

	private void importMetaData(Attributes attributes, String userName) throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
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

		String urlString = System.getProperty("luceneUrl");
		URI uribase = new URI(urlString);
		LuceneApi luceneApi = new LuceneApi(uribase);
		luceneApi.clear(); // Really empty the db

		assertTrue(session.luceneGetPopulating().isEmpty());

		session.lucenePopulate("Dataset", -1);
		session.lucenePopulate("Datafile", -1);
		session.lucenePopulate("Investigation", -1);

		do {
			Thread.sleep(1000);
		} while (!session.luceneGetPopulating().isEmpty());

	}

}
