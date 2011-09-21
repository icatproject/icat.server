package uk.icat3.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;

abstract public class BaseClassTransaction extends BaseTest {

	static private EntityManagerFactory emf;

	@AfterClass
	public static void AfterClassTearDown() {
		BaseTest.em.getTransaction().rollback();
		BaseTest.em.close();
		BaseClassTransaction.emf.close();
	}

	@BeforeClass
	public static void BeforeClassSetUp() {
		BaseClassTransaction.emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
		BaseTest.em = BaseClassTransaction.emf.createEntityManager();
		BaseTest.em.getTransaction().begin();
	}

}
