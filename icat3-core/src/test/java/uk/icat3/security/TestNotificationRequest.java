package uk.icat3.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.NotificationRequest;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.BeanManager;
import uk.icat3.manager.CreateResponse;
import uk.icat3.manager.NotificationMessages.Message;
import uk.icat3.manager.RuleManager;
import uk.icat3.util.TestConstants;

public class TestNotificationRequest {

	static private EntityManagerFactory emf;
	private EntityManager em;
	private final static Logger logger = Logger.getLogger(TestNotificationRequest.class);

	@Before
	public void setUpManager() throws Exception {
		RuleManager.addUserGroupMember("Group", "Person", em);

		RuleManager.addRule("Group", "NotificationRequest", "CRUD", null, em);
		RuleManager.addRule("Group", "Facility", "CRUD", null, em);
		RuleManager.addRule("Group", "InvestigationType", "CRUD", null, em);
		RuleManager.addRule("Group", "DatasetType", "CRUD", null, em);
		RuleManager.addRule("Group", "Investigation", "CRUD", null, em);
		RuleManager.addRule("Group", "Dataset", "CRUD", null, em);

		Facility f = new Facility();
		f.setFacilityShortName("TestFacility");
		f.setDaysUntilRelease(90L);
		BeanManager.create("Person", f, em);

		InvestigationType type = new InvestigationType();
		type.setName("TestExperiment");
		BeanManager.create("Person", type, em);

		DatasetType dst = new DatasetType();
		dst.setName("GQ");
		BeanManager.create("Person", dst, em);

	}

	private CreateResponse createInvestigation(String invNumber) throws Exception {
		Investigation inv = new Investigation();
		inv.setInvNumber(invNumber);
		inv.setTitle("Not null");
		inv.setInvType("TestExperiment");
		inv.setFacility("TestFacility");
		return BeanManager.create("Person", inv, em);
	}

	private CreateResponse createDataset(Long invid, String name) throws Exception {
		Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setDatasetType("GQ");
		dataset.setInvestigationId(invid);
		return BeanManager.create("Person", dataset, em);
	}

	@Test(expected = BadParameterException.class)
	public void testBadCrud() throws Exception {
		NotificationRequest nr = new NotificationRequest();
		nr.setCrudFlags("cRqud");
		BeanManager.create("Person", nr, em);
	}

	@Test
	public void testGood0() throws Exception {
		NotificationRequest nr = new NotificationRequest();
		nr.setName("T");
		nr.setDestType(NotificationRequest.DestType.PUBSUB);
		nr.setWhat("Dataset");
		nr.setCrudFlags("C");
		BeanManager.create("Person", nr, em);
		CreateResponse resp = createInvestigation("A");
		long invId = (Long) resp.getPk();
		System.out.println(resp.getPk());
		System.out.println(resp.getNotificationMessages());
		resp = createDataset(invId, "Wibble");
		System.out.println(resp.getPk());
		System.out.println(resp.getNotificationMessages());
	}

	@Test
	public void testGood1() throws Exception {
		NotificationRequest nr = new NotificationRequest();
		nr.setName("T");
		nr.setDestType(NotificationRequest.DestType.P2P);
		nr.setWhat("Dataset");
		nr.setCrudFlags("R");
		BeanManager.create("Person", nr, em);
	}

	@Test
	public void testGood2() throws Exception {
		NotificationRequest nr = new NotificationRequest();
		nr.setName("T");
		nr.setDestType(NotificationRequest.DestType.P2P);
		nr.setWhat("Dataset [name='fred']");
		nr.setCrudFlags("C");
		nr.setJmsOptions("ptp");
		nr.setDatatypes("notificationName userId entityName entityKey callArgs");
		CreateResponse resp = BeanManager.create("Person", nr, em);
		nr.setId((Long) resp.getPk());
		resp = createInvestigation("A");

		long invId = (Long) resp.getPk();
		System.out.println();
		assertEquals(0, resp.getNotificationMessages().getMessages().size());
		resp = createDataset(invId, "freda");
		assertEquals(0, resp.getNotificationMessages().getMessages().size());
		resp = createDataset(invId, "fred");
		assertEquals(1, resp.getNotificationMessages().getMessages().size());
		Message msg = resp.getNotificationMessages().getMessages().get(0);
		assertEquals("T", msg.getNotificationName());
		assertEquals(NotificationRequest.DestType.P2P, msg.getDestType());
		assertEquals("Person", msg.getUserId());
		assertEquals("Dataset", msg.getEntityName());
		assertEquals(resp.getPk(), msg.getPk().get("Id"));
		assertNull(msg.getArgs());

		nr.setDatatypes(null);
		nr.setWhat("Dataset");
		BeanManager.update("Person", nr, em);
		resp = createDataset(invId, "bill");
		assertEquals(1, resp.getNotificationMessages().getMessages().size());
		msg = resp.getNotificationMessages().getMessages().get(0);
		assertNull(msg.getNotificationName());
		assertEquals(NotificationRequest.DestType.P2P, msg.getDestType());
		assertNull(msg.getUserId());
		assertNull(msg.getEntityName());
		assertNull(msg.getPk());
		assertNull(msg.getArgs());
	}

	@Test
	public void testGood3() throws Exception {
		NotificationRequest nr = new NotificationRequest();
		nr.setName("T");
		nr.setDestType(NotificationRequest.DestType.P2P);
		nr.setCrudFlags("d");
		nr.setWhat("Dataset <-> Investigation [invNumber = '901234']");
		BeanManager.create("Person", nr, em);
	}

	@Before
	public void beginTX() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}

	@After
	public void closeTX() throws NoSuchObjectFoundException {
		try {
			em.getTransaction().rollback();
		} catch (RuntimeException t) {
			logger.error(t);
			throw t;
		} finally {
			em.close();
		}
	}

	@BeforeClass
	public static void BeforeClassSetUp() {
		emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
	}

	@AfterClass
	public static void AfterClassTearDown() {
		emf.close();
	}
}
