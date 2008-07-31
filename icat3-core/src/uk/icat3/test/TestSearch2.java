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

    public void testSQL() throws Exception {
        setUp();

        long time = System.currentTimeMillis();

        Query nullQuery = em.createNativeQuery("SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.FACILITY, t0.CREATE_ID, t0.RELEASE_DATE, t0.MOD_ID, t0.INV_START_DATE, t0.INV_END_DATE, t0.VISIT_ID, t0.INSTRUMENT, t0.INV_ABSTRACT, t0.INV_TYPE, t0.BCAT_INV_STR, t0.FACILITY_ACQUIRED, t0.INV_NUMBER, t0.CREATE_TIME, t0.TITLE, t0.INV_PARAM_VALUE, t0.PREV_INV_NUMBER, t0.DELETED, t0.INV_PARAM_NAME, t0.FACILITY_CYCLE FROM INVESTIGATION t0, ICAT_ROLE t2, ICAT_AUTHORISATION t1 WHERE ((((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND ((t1.USER_ID = 'ISIS_GUARDIAN') OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t0.DELETED = 'N')) AND EXISTS (SELECT 1 FROM DATASET t7, ICAT_AUTHORISATION t6, INVESTIGATION t5, DATASET t4, DATAFILE t3, ICAT_ROLE t8 WHERE ((((((((t3.DATASET_ID = t4.ID) AND ((t3.NAME = 'GEM35639_STATUS.TXT') OR ('GEM35639_STATUS.TXT' IS NULL))) AND (t6.DELETED = 'N')) AND (t3.DELETED = 'N')) AND (t7.DELETED = 'N')) AND ((((t7.ID = t6.ELEMENT_ID) AND (t6.ELEMENT_TYPE = 'DATASET')) AND ((t6.USER_ID = 'ISIS_GUARDIAN') OR (t6.USER_ID = 'ANY'))) AND (t8.ACTION_SELECT = 'Y'))) AND (t0.ID = t5.ID)) AND (((t4.INVESTIGATION_ID = t5.ID) AND (t7.ID = t3.DATASET_ID)) AND (t8.ROLE = t6.ROLE)))) ) AND (t2.ROLE = t1.ROLE))");

        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        time = System.currentTimeMillis();

        tearDown();
    }

    public void testP() throws Exception {
        setUp();

        //df.dataset = i.datasetCollection AND 
        long time = System.currentTimeMillis();
        String QUERY = "SELECT DISTINCT i from Investigation i , IcatAuthorisation ia " +
                "WHERE (i.id = ia.elementId AND ia.elementType = :objectType  " +
                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' " +
                "AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N'  " +
                "AND EXISTS (SELECT DISTINCT df FROM Datafile df, IcatAuthorisation iadf3 " +
                "WHERE  df.dataset = i.datasetCollection AND  LOWER(df.name)  =  :datafileName " +
                " AND  iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' " +
                "AND df.dataset.markedDeleted = 'N' AND  (df.dataset.id = iadf3.elementId " +
                "AND iadf3.elementType = :dataSetType  AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')  " +
                "AND iadf3.role.actionCanSelect = 'Y'))";

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.INVESTIGATION);
        nullQuery.setParameter("dataSetType", ElementType.DATASET);
        nullQuery.setParameter("userId", "ISIS_GUARDIAN");
        nullQuery.setParameter("datafileName", "gem35639_status.txt");
        // nullQuery.setParameter("dataSetType", ElementType.DATASET);

        System.out.println(nullQuery.getResultList());

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

    //   ts.test2();


    }
}
