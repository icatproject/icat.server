

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.logging.Logger;

import org.icatproject.BadParameterException;
import org.icatproject.CreateResponse;
import org.icatproject.Dataset;
import org.icatproject.DatasetType;
import org.icatproject.Facility;
import org.icatproject.Investigation;
import org.icatproject.InvestigationType;
import org.icatproject.NotificationRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestNotificationRequest {

	
	private static Session session;
	
	private Facility facility;
	private InvestigationType investigationType;
	private DatasetType datasetType;


	@Before
	public void setUpManager() throws Exception {
		facility = new Facility();
		facility.setName("TestFacility");
		facility.setDaysUntilRelease(90);
		BeanManager.create("Person", facility, em);

		investigationType = new InvestigationType();
		investigationType.setName("TestExperiment");
		BeanManager.create("Person", investigationType, em);

		datasetType = new DatasetType();
		datasetType.setName("GQ");
		BeanManager.create("Person", datasetType, em);
	}

	private CreateResponse createInvestigation(String invNumber) throws Exception {
		Investigation inv = new Investigation();
		inv.setName(invNumber);
		inv.setTitle("Not null");
		inv.setType(investigationType);
		inv.setFacility(facility);
		return BeanManager.create("Person", inv, em);
	}

	private CreateResponse createDataset(Investigation inv, String name) throws Exception {
		Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(datasetType);
		dataset.setInvestigation(inv);
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
		Investigation inv = (Investigation) BeanManager.get("Person", "Investigation", resp.getPk(), em).getBean();
		System.out.println(resp.getPk());
		System.out.println(resp.getNotificationMessages());
		resp = createDataset(inv, "Wibble");
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
		Investigation inv = (Investigation) BeanManager.get("Person", "Investigation", (Long) resp.getPk(), em)
				.getBean();
		System.out.println();
		assertEquals(0, resp.getNotificationMessages().getMessages().size());
		resp = createDataset(inv, "freda");
		assertEquals(0, resp.getNotificationMessages().getMessages().size());
		resp = createDataset(inv, "fred");
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
		resp = createDataset(inv, "bill");
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
		nr.setWhat("Dataset <-> Investigation [name = '901234']");
		BeanManager.create("Person", nr, em);
	}

	@BeforeClass
	public static void setup() throws Exception {
		session = new Session();
		session.setAuthz();
		session.clearAuthz();
		session.setAuthz();
		session.clear();
	}

	@AfterClass
	public static void zap() throws Exception {
		// session.clear();
		// session.clearAuthz();
	}
}
