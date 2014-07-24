package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.icatproject.EntityBaseBean;
import org.icatproject.Facility;
import org.icatproject.integration.client.ICAT;
import org.icatproject.integration.client.IcatException;
import org.icatproject.integration.client.IcatException.IcatExceptionType;
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
			wSession.setAuthz();
			wSession.clearAuthz();
			wSession.setAuthz();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void search() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		System.out.println(session.search("InvestigationType INCLUDE 1"));
		System.out.println(session.search("Facility INCLUDE InvestigationType"));
	}

	@Test
	public void create() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		Long fid = ((EntityBaseBean) wSession.search("Facility INCLUDE InvestigationType").get(0))
				.getId();
		System.out.println(session.create("[{\"InvestigationType\":{\"facility\":{\"id\":" + fid
				+ "},\"name\":\"ztype\"}}]"));
	}

	@Test
	public void testSession() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		assertEquals("db/root", session.getUserName());
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
		Thread.sleep(500);
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
		wSession.clearAuthz();
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
		wSession.clearAuthz();
		wSession.setAuthz();

		Path dump = Files.createTempFile("dump1", ".tmp");
		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset",
				Attributes.USER)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump USER");
		assertEquals(1031, dump.toFile().length());
		session.importMetaData(dump, DuplicateAction.CHECK, Attributes.USER);

		start = System.currentTimeMillis();
		try (InputStream stream = session.exportMetaData("Investigation INCLUDE Facility, Dataset",
				Attributes.ALL)) {
			Files.copy(stream, dump, StandardCopyOption.REPLACE_EXISTING);
		}
		ts("Create dump ALL");
		assertEquals(1563, dump.toFile().length());
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
