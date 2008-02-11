/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.test;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
public class TestSearch2 {

    protected static Logger log = Logger.getLogger(TestSearch.class);
    // TODO code application logic here
    static EntityManagerFactory emf = null;
    // Create new EntityManager
    static EntityManager em = null;

    /** Creates a new instance of TestSearch */
    public TestSearch2() {
    }

    protected static void setUp() {
        emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        // emf = Persistence.createEntityManagerFactory("icatisis_dev");
        em = emf.createEntityManager();


    // Begin transaction
    //em.getTransaction().begin();

    }

    protected static void tearDown() {
        // Commit the transaction
        //em.getTransaction().commit();

        em.close();
    }

    public void testP1() throws Exception {
        setUp();

        long time = System.currentTimeMillis();
        
        String QUERY = "SELECT DISTINCT i from Datafile i , IcatAuthorisation ia, FacilityInstrumentScientist fis WHERE " +
                " ((:userId = 'SUPER_USER') OR  ((:userId = fis.facilityInstrumentScientistPK.federalId " +
                "AND  fis.facilityInstrumentScientistPK.instrumentName = i.dataset.investigation.instrument) " +
                "AND fis.markedDeleted = 'N') OR  (i.id = ia.elementId AND ia.elementType = :objectType  " +
                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' " +
                "AND ia.role.actionCanSelect = 'Y')) AND i.markedDeleted = 'N'"; 

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.DATASET);
        nullQuery.setParameter("userId", "facility_scientist");

        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");
        tearDown();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        TestSearch2 ts = new TestSearch2();

        ts.testP1();

    //   ts.test2();


    }
}
