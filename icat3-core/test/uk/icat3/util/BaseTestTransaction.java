package uk.icat3.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

abstract public class BaseTestTransaction extends BaseTest {

	static private EntityManagerFactory emf;

	@Before
	public void beginTX() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}

	@After
	public void closeTX() {
		em.getTransaction().rollback();
		em.close();
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
