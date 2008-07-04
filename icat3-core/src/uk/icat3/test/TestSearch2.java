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
import uk.icat3.util.ElementType;

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
        //emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        emf = Persistence.createEntityManagerFactory("icatisis");
        em = emf.createEntityManager();


    // Begin transaction
    //em.getTransaction().begin();

    }

    protected static void tearDown() {
        // Commit the transaction
        //em.getTransaction().commit();

        em.close();
    }

    public void testP() throws Exception {
        setUp();
//df.dataset = i.datasetCollection AND 
        long time = System.currentTimeMillis();
        String QUERY = "SELECT i FROM IcatAuthorisation i WHERE " +
                "i.elementType = :elementType AND i.elementId = :elementId AND " +
                "i.userId = :userId AND i.parentElementType IS NULL AND " +
                "i.parentElementId IS NULL AND i.markedDeleted = 'N'";

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("elementType", ElementType.INVESTIGATION);
        nullQuery.setParameter("userId", "ANY");
        nullQuery.setParameter("elementId", 7256755);

        System.out.println(nullQuery.getResultList().size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        time = System.currentTimeMillis();


        tearDown();
    }

    public void testP1() throws Exception {
        setUp();
//df.dataset = i.datasetCollection AND 
        long time = System.currentTimeMillis();
        String QUERY = "SELECT DISTINCT i from Investigation i , IcatAuthorisation ia WHERE " +
                "(i.id = ia.elementId AND ia.elementType = :objectType  AND (ia.userId = :userId " +
                "OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') " +
                "AND i.markedDeleted = 'N'  AND i.instrument IN(:instrument1) and (i.invStartDate > :lowerTime ) AND (I.invEndDate < :upperTime )  ";


        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.INVESTIGATION);
        nullQuery.setParameter("userId", "gjd37");
        nullQuery.setParameter("instrument1", "maps");
        // nullQuery.setParameter("dataSetType", ElementType.DATASET);

        nullQuery.setParameter("lowerTime", new Date(108, 1, 1));
        nullQuery.setParameter("upperTime", new Date(108, 2, 1));

        System.out.println(nullQuery.getResultList().size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        time = System.currentTimeMillis();


        tearDown();
    }

    public void testP3() throws Exception {
        setUp();

        long time = System.currentTimeMillis();
        String QUERY = "SELECT DISTINCT i from Datafile i , DatafileParameter dfp , IcatAuthorisation ia " +
                "WHERE  (i.dataset.id = ia.elementId AND ia.elementType = :objectType  " +
                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N'" +
                " AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N'  " +
                "AND i.dataset.investigation.instrument IN (:instrument1) " +
                "AND  i = dfp.datafile AND i.markedDeleted = 'N'  AND ((dfp.numericValue " +
                "BETWEEN :lower AND :upper)) AND (dfp.datafileParameterPK.name = 'run_number') " +
                "AND dfp.markedDeleted = 'N' AND dfp.markedDeleted = 'N' AND i.dataset.markedDeleted = 'N'";


        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.DATASET);
        nullQuery.setParameter("userId", "gjd37");

        nullQuery.setParameter("lower", 11757.0);
        nullQuery.setParameter("upper", 11759.0);
        nullQuery.setParameter("instrument1", "maps");

        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");


        tearDown();
    }

    public void testP4() throws Exception {
        setUp();

        long time = System.currentTimeMillis();
        String QUERY = "SELECT DISTINCT i from Datafile i , DatafileParameter dfp , IcatAuthorisation ia " +
                "WHERE  (i.dataset.id = ia.elementId AND ia.elementType = :objectType  AND (ia.userId = :userId OR ia.userId = 'ANY') " +
                "AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N'  " +
                "AND i.dataset.investigation.instrument IN (:instrument1) AND  " +
                "i = dfp.datafile AND i.markedDeleted = 'N'  AND ((dfp.numericValue BETWEEN :lower AND :upper)) " +
                "AND (dfp.datafileParameterPK.name = 'run_number') AND dfp.markedDeleted = 'N' AND dfp.markedDeleted = 'N' " +
                "AND i.dataset.markedDeleted = 'N'";

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.DATASET);
        nullQuery.setParameter("userId", "SUPER_USER");
        nullQuery.setParameter("lower", 0.0f);
        nullQuery.setParameter("upper", 100000f);
        nullQuery.setParameter("instrument1", "SXD");

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

        ts.testP();
        ts.testP();
        ts.testP();
        ts.testP();
    //   ts.test2();


    }
}
