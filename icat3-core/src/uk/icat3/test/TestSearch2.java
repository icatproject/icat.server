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
        emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icatisis");
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
        String QUERY = "SELECT DISTINCT i from Investigation i , IcatAuthorisation ia " +
                "WHERE (i.id = ia.elementId AND ia.elementType = :objectType  AND " +
                "(ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N'" +
                " AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N'  AND " +
                "EXISTS (SELECT DISTINCT df FROM Datafile df, IcatAuthorisation iadf3 WHERE  " +
                "df.dataset = i.datasetCollection AND (df.datafileCreateTime > :lowerTime " +
                "OR :lowerTime IS NULL) AND (df.datafileCreateTime < :upperTime " +
                "OR :upperTime IS NULL) AND   (df.name  =  :datafileName OR :datafileName IS NULL) " +
                "AND  iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' " +
                "AND df.dataset.markedDeleted = 'N' AND  (df.dataset.id = iadf3.elementId " +
                "AND iadf3.elementType = :dataSetType  AND (iadf3.userId = :userId" +
                " OR iadf3.userId = 'ANY')  AND iadf3.role.actionCanSelect = 'Y'))";

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.INVESTIGATION);
        nullQuery.setParameter("userId", "test");
        nullQuery.setParameter("lowerTime", new Date(1, 1, 1));
        nullQuery.setParameter("upperTime", new Date());
        nullQuery.setParameter("dataSetType", ElementType.DATASET);

        nullQuery.setParameter("datafileName", "SXD01300.RAW");
         nullQuery.setParameter("datafileName", "SXD015554.RAW");
        

        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        time = System.currentTimeMillis();


        tearDown();
    }

    public void testP3() throws Exception {
        setUp();

        long time = System.currentTimeMillis();
        String QUERY = "SELECT DISTINCT i from Datafile i , DatafileParameter dfp,  IcatAuthorisation ia, FacilityInstrumentScientist fis " +
                "WHERE  " +
                "((:userId = 'SUPER_USER') OR (:userId = fis.facilityInstrumentScientistPK.federalId AND  fis.facilityInstrumentScientistPK.instrumentName = i.dataset.investigation.instrument AND fis.markedDeleted = 'N') " +
                "OR (i.id = ia.elementId AND ia.elementType = :objectType  " +
                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' " +
                "AND ia.role.actionCanSelect = 'Y')) AND i = dfp.datafile " +
                "AND i.markedDeleted = 'N'  " +
                "AND i.dataset.investigation.instrument IN (:instrument1, :instrument2) " +
                "AND ((dfp.numericValue BETWEEN :lower AND :upper)) " +
                "AND (dfp.datafileParameterPK.name = 'run_number') " +
                "AND dfp.markedDeleted = 'N' AND dfp.markedDeleted = 'N' " +
                "AND i.dataset.markedDeleted = 'N'  " +
                "";

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.DATASET);
        nullQuery.setParameter("userId", "facility_scientist");

        nullQuery.setParameter("lower", 0);
        nullQuery.setParameter("upper", 10000);
        nullQuery.setParameter("instrument1", "SXD");
        nullQuery.setParameter("instrument2", "SXD2");

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

        ts.testP1();

    //   ts.test2();


    }
}
