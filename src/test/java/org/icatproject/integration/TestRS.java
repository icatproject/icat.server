package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.icatproject.EntityBaseBean;
import org.icatproject.Facility;
import org.icatproject.IcatException_Exception;
import org.icatproject.integration.client.ICAT;
import org.icatproject.integration.client.IcatException;
import org.icatproject.integration.client.IcatException.IcatExceptionType;
import org.icatproject.integration.client.ParameterForLucene;
import org.icatproject.integration.client.Session;
import org.icatproject.integration.client.Session.Attributes;
import org.icatproject.integration.client.Session.DuplicateAction;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * These tests are for those aspects that cannot be tested by the core tests. In particular does the
 * INCLUDE mechanism work properly.
 */
public class TestRS {

	private static WSession wSession;
	private static long end;
	private static long start;

	@BeforeClass
	public static void beforeClass() throws Exception {
		try {
			wSession = new WSession();
			wSession.clearAuthz();
			wSession.setAuthz();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testLuceneDatasets() throws Exception {

		Session session = setupLuceneTest();

		DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));
		// parameters.add(new ParameterForLucene("birthday", "date", dft
		// .parse("2014-05-16T16:58:26.12Z"), dft.parse("2014-05-16T16:58:26.12Z")));
		parameters.add(new ParameterForLucene("current", "amps", 140, 165));

		// All datasets
		searchDatasets(session, null, null, null, null, null, 20, 5);

		JsonArray array = searchDatasets(session, null, "gamma AND ds3",
				dft.parse("2014-05-16T06:09:03"), dft.parse("2014-05-16T06:15:26"), parameters, 20,
				1);
		assertEquals("gamma",
				array.getJsonObject(0).getJsonObject("Dataset").getString("description"));

		//
		// JsonArray array = searchInvestigations(session, "db/tr", "title AND one",
		// dft.parse("2011-01-01T00:00:00"), dft.parse("2011-12-31T23:59:59"), parameters,
		// Arrays.asList("ford AND rust", "koh* AND diamond"), "Professor", 20, 1);
		// assertEquals("one",
		// array.getJsonObject(0).getJsonObject("Investigation").getString("visitId"));
		//
		// // change user
		// searchInvestigations(session, "db/fred", "title AND one", null, null, parameters, null,
		// null, 20, 0);
		//
		// // change text
		// searchInvestigations(session, "db/tr", "title AND two", null, null, parameters, null,
		// null,
		// 20, 0);
		//
		// // Only working to a minute
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:01"),
		// dft.parse("2011-12-31T23:59:59"), parameters, null, null, 20, 1);
		//
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
		// dft.parse("2011-12-31T23:59:58"), parameters, null, null, 20, 1);
		//
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:01:00"),
		// dft.parse("2011-12-31T23:59:59"), parameters, null, null, 20, 0);
		//
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
		// dft.parse("2011-12-31T23:58:00"), parameters, null, null, 20, 0);
		//
		// // Change parameters
		// List<ParameterForLucene> badParameters = new ArrayList<>();
		// badParameters.add(new ParameterForLucene("color", "name", "green"));
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
		// dft.parse("2011-12-31T23:59:59"), badParameters,
		// Arrays.asList("ford + rust", "koh + diamond"), null, 20, 0);
		//
		// // Change samples
		// searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
		// dft.parse("2011-12-31T23:59:59"), parameters,
		// Arrays.asList("ford AND rust", "kog* AND diamond"), null, 20, 0);
		//
		// // Change userFullName
		// searchInvestigations(session, "db/tr", "title + one", dft.parse("2011-01-01T00:00:00"),
		// dft.parse("2011-12-31T23:59:59"), parameters,
		// Arrays.asList("ford AND rust", "koh* AND diamond"), "Doctor", 20, 0);

	}

	@Test
	public void testLuceneInvestigations() throws Exception {

		Session session = setupLuceneTest();

		DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		searchInvestigations(session, null, null, null, null, null, null, null, 20, 3);

		List<ParameterForLucene> parameters = new ArrayList<>();
		parameters.add(new ParameterForLucene("colour", "name", "green"));

		JsonArray array = searchInvestigations(session, "db/tr", "title AND one",
				dft.parse("2011-01-01T00:00:00"), dft.parse("2011-12-31T23:59:59"), parameters,
				Arrays.asList("ford AND rust", "koh* AND diamond"), "Professor", 20, 1);
		assertEquals("one",
				array.getJsonObject(0).getJsonObject("Investigation").getString("visitId"));

		// change user
		searchInvestigations(session, "db/fred", "title AND one", null, null, parameters, null,
				null, 20, 0);

		// change text
		searchInvestigations(session, "db/tr", "title AND two", null, null, parameters, null, null,
				20, 0);

		// Only working to a minute
		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:01"),
				dft.parse("2011-12-31T23:59:59"), parameters, null, null, 20, 1);

		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
				dft.parse("2011-12-31T23:59:58"), parameters, null, null, 20, 1);

		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:01:00"),
				dft.parse("2011-12-31T23:59:59"), parameters, null, null, 20, 0);

		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
				dft.parse("2011-12-31T23:58:00"), parameters, null, null, 20, 0);

		// Change parameters
		List<ParameterForLucene> badParameters = new ArrayList<>();
		badParameters.add(new ParameterForLucene("color", "name", "green"));
		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
				dft.parse("2011-12-31T23:59:59"), badParameters,
				Arrays.asList("ford + rust", "koh + diamond"), null, 20, 0);

		// Change samples
		searchInvestigations(session, "db/tr", "title AND one", dft.parse("2011-01-01T00:00:00"),
				dft.parse("2011-12-31T23:59:59"), parameters,
				Arrays.asList("ford AND rust", "kog* AND diamond"), null, 20, 0);

		// Change userFullName
		searchInvestigations(session, "db/tr", "title + one", dft.parse("2011-01-01T00:00:00"),
				dft.parse("2011-12-31T23:59:59"), parameters,
				Arrays.asList("ford AND rust", "koh* AND diamond"), "Doctor", 20, 0);

		// All datasets
		searchDatasets(session, null, null, null, null, null, 20, 5);

		array = searchDatasets(session, null, "gamma AND ds3", null, null, null, 20, 1);
		assertEquals("gamma",
				array.getJsonObject(0).getJsonObject("Dataset").getString("description"));

		// wSession.luceneClear();
		// wSession.luceneCommit();
		// assertEquals(0, session.luceneSearch("*f*", 100, null).size());
		// assertEquals(0, session.luceneSearch("*f*", 100, "Dataset").size());
		//
		// session.lucenePopulate("Facility");
		// session.lucenePopulate("Investigation");
		// session.lucenePopulate("InvestigationParameter");
		// session.lucenePopulate("Dataset");
		// session.lucenePopulate("DatasetParameter");
		// session.lucenePopulate("Datafile");
		// session.lucenePopulate("DatafileParameter");
		// session.lucenePopulate("Sample");
		// session.lucenePopulate("SampleParameter");
		// List<String> left;
		// while (!(left = session.luceneGetPopulating()).isEmpty()) {
		// System.out.println("Process " + left);
		// Thread.sleep(10);
		// }
		//
		// session.luceneCommit();
		//
		// assertEquals(4, session.luceneSearch("*f*", 100, null).size());
		// assertEquals(2, session.luceneSearch("*f*", 100, "Dataset").size());
	}

	private Session setupLuceneTest() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Clear lucene - just in case
		wSession.luceneClear();
		wSession.luceneCommit();
		List<String> props = wSession.getProperties();
		assertTrue(props.contains("lucene.commitSeconds 1"));

		// Get known configuration
		wSession.clear();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);
		wSession.setAuthz();

		wSession.luceneCommit();
		return session;
	}

	private JsonArray searchDatasets(Session session, String user, String text, Date lower,
			Date upper, List<ParameterForLucene> parameters, int maxResults, int n)
			throws IcatException {
		JsonArray result = Json.createReader(
				new ByteArrayInputStream(session.searchDatasets(user, text, lower, upper,
						parameters, maxResults).getBytes())).readArray();
		assertEquals(n, result.size());
		return result;
	}

	@Test
	public void testVersion() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		assertTrue(icat.getApiVersion().startsWith("4."));
	}

	@Test
	public void testGet() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		wSession.clear();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);
		wSession.setAuthz();

		wSession.setAuthz();

		long fid = search(session, "Facility.id", 1).getJsonNumber(0).longValueExact();

		JsonObject fac = Json
				.createReader(
						new ByteArrayInputStream(session.get("Facility INCLUDE InvestigationType",
								fid).getBytes())).readObject().getJsonObject("Facility");

		assertEquals("Test port facility", fac.getString("name"));
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
	public void testSearch() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		wSession.clear();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);
		wSession.setAuthz();

		wSession.setAuthz();

		JsonArray array;
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

	private JsonArray search(Session session, String query, int n) throws IcatException {
		JsonArray result = Json.createReader(
				new ByteArrayInputStream(session.search(query).getBytes())).readArray();
		assertEquals(n, result.size());
		return result;
	}

	private JsonArray searchInvestigations(Session session, String user, String text, Date lower,
			Date upper, List<ParameterForLucene> parameters, List<String> samples,
			String userFullName, int maxResults, int n) throws IcatException {
		JsonArray result = Json.createReader(
				new ByteArrayInputStream(session.searchInvestigations(user, text, lower, upper,
						parameters, samples, userFullName, maxResults).getBytes())).readArray();
		assertEquals(n, result.size());
		return result;
	}

	@Test
	public void testCreate() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);

		// Get known configuration
		wSession.clear();
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.USER);
		wSession.setAuthz();

		Long fid = ((EntityBaseBean) wSession.search("Facility INCLUDE InvestigationType").get(0))
				.getId();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator jw = Json.createGenerator(baos)) {
			jw.writeStartArray();

			jw.writeStartObject();
			jw.writeStartObject("InvestigationType");
			jw.writeStartObject("facility");
			jw.write("id", fid);
			jw.writeEnd();
			jw.write("name", "ztype");
			jw.writeEnd();
			jw.writeEnd();

			jw.writeStartObject().writeStartObject("Facility").write("name", "another fred")
					.writeEnd();
			jw.writeEnd();

			jw.writeEnd();
		}

		List<Long> ids = session.create(baos.toString());
		assertEquals(2, ids.size());

	}

	@Test
	public void testSession() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "notroot");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		assertEquals("db/notroot", session.getUserName());
		double remainingMinutes = session.getRemainingMinutes();
		assertTrue(remainingMinutes > 119 && remainingMinutes < 120);
		session.logout();
		try {
			session.getRemainingMinutes();
			fail();
		} catch (org.icatproject.integration.client.IcatException e) {
			// No action
		}
		session = icat.login("db", credentials);
		Thread.sleep(1000);
		remainingMinutes = session.getRemainingMinutes();
		session.refresh();
		assertTrue(session.getRemainingMinutes() > remainingMinutes);
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
		wSession.clear();
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		Map<String, String> rootCredentials = new HashMap<>();
		rootCredentials.put("username", "root");
		rootCredentials.put("password", "password");
		Session rootSession = icat.login("db", rootCredentials);

		// Get known configuration
		rootSession.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);
		wSession.setAuthz();

		Path dump1 = Files.createTempFile("dump1", ".tmp");
		Path dump2 = Files.createTempFile("dump2", ".tmp");
		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData(Attributes.ALL)) {
			Files.copy(stream, dump1, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump1 for " + credentials.get("username"));
		wSession.clear();
		wSession.clearAuthz();
		System.out.println(dump1);
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
	public void exportMetaDataQuery() throws Exception {
		wSession.clear();
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		// Get known configuration
		session.importMetaData(path, DuplicateAction.CHECK, Attributes.ALL);
		wSession.setAuthz();

		Path dump = Files.createTempFile("dump1", ".tmp");
		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset",
				Attributes.USER)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump USER");
		assertEquals(1919, dump.toFile().length());
		session.importMetaData(dump, DuplicateAction.CHECK, Attributes.USER);

		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset",
				Attributes.ALL)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump ALL");
		long n = dump.toFile().length();
		assertTrue("Size is dependent upon time zone in which test is run " + n, n == 2686
				|| n == 1518);
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
		wSession.clear();
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		Path path = Paths.get(ClassLoader.class.getResource("/icat.port").toURI());

		start = System.currentTimeMillis();
		session.importMetaData(path, DuplicateAction.CHECK, attributes);
		ts("Simple import");

		long facilityId = (Long) wSession.search("Facility.id [name = 'Test port facility']")
				.get(0);

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

}
