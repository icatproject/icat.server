package org.icatproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
	public void testSession() throws Exception {
		ICAT icat = new ICAT(System.getProperty("serverUrl"));
		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "root");
		credentials.put("password", "password");
		Session session = icat.login("db", credentials);
		assertEquals("db/root", session.getUserName());
		Thread.sleep(500);
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
		remainingMinutes = session.getRemainingMinutes();
		session.refresh();
		assertTrue(session.getRemainingMinutes() < remainingMinutes);
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
