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
 String QUERY = "SELECT DISTINCT i from Investigation i, FacilityInstrumentScientist fis, IcatAuthorisation ia WHERE " +
                " ((:userId = 'SUPER_USER') OR  (i.id = ia.elementId AND ia.elementType = :objectType  " +
                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' " +
                "AND ia.role.actionCanSelect = 'Y') OR " +
                " (:userId = fis.facilityInstrumentScientistPK.federalId AND  fis.facilityInstrumentScientistPK.instrumentName = i.instrument AND fis.markedDeleted = 'N')) AND " +         
                " i.markedDeleted = 'N' AND " +
                " EXISTS (SELECT DISTINCT ds FROM Datafile df, FacilityInstrumentScientist fis2, IcatAuthorisation iadf3  WHERE " +                
                "  (df.datafileCreateTime < :upperTime OR :upperTime IS NULL) " +
                " AND (df.name = :datafileName OR :datafileName IS NULL)  "+
                " AND (df.datafileCreateTime > :lowerTime OR :lowerTime IS NULL ) AND " +
                " iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N'" +
                " AND ((:userId = 'SUPER_USER') OR (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType  " +
                "AND (iadf3.userId = :userId OR iadf3.userId = 'ANY') AND iadf3.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y') OR " +
                "(:userId = fis2.facilityInstrumentScientistPK.federalId AND " +
                " fis2.facilityInstrumentScientistPK.instrumentName = df.dataset.investigation.instrument AND fis2.markedDeleted = 'N'))) " ;
             
       
        /* QUERY = "SELECT DISTINCT i from Investigation i WHERE " +
                 "EXISTS (SELECT df FROM Datafile df WHERE " +                
                 " df.dataset = i.datasetCollection AND df.datafileCreateTime < :upperTime)";
       */
 
        /*String QUERY = "SELECT df FROM Datafile df, FacilityInstrumentScientist fis2, IcatAuthorisation iadf3 "+
        " WHERE  ((:userId = 'SUPER_USER') OR " +
                " (:userId = fis2.facilityInstrumentScientistPK.federalId AND  fis2.facilityInstrumentScientistPK.instrumentName = df.dataset.investigation.instrument AND fis2.markedDeleted = 'N') OR " +
                " (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType  AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')  AND iadf3.role.actionCanSelect = 'Y')) AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N'  AND df.dataset.investigation = i AND (df.datafileCreateTime > :lowerTime OR :lowerTime IS NULL AND df.datafileCreateTime < :upperTime OR :upperTime IS NULL) AND  df.dataset.markedDeleted = 'N' AND df.markedDeleted = 'N' AND (df.name  =  :datafileName OR :datafileName IS NULL)";  
        */
        //SELECT DISTINCT i from Investigation i , IcatAuthorisation ia, FacilityInstrumentScientist fis WHERE  
        //((:userId = 'SUPER_USER') OR  (:userId = fis.facilityInstrumentScientistPK.federalId 
        //AND  (fis.facilityInstrumentScientistPK.instrumentName = i.instrument) AND fis.markedDeleted = 'N') OR  (i.id = ia.elementId AND ia.elementType = :objectType  AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y')) AND i.markedDeleted = 'N' 
        //AND EXISTS 
        //(SELECT df FROM Datafile df, FacilityInstrumentScientist fis2, IcatAuthorisation iadf3 
        //WHERE  ((:userId = 'SUPER_USER') OR  (:userId = fis2.facilityInstrumentScientistPK.federalId AND  fis2.facilityInstrumentScientistPK.instrumentName = df.dataset.investigation.instrument AND fis2.markedDeleted = 'N') OR  (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType  AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')  AND iadf3.role.actionCanSelect = 'Y')) AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N'  AND df.dataset.investigation = i AND (df.datafileCreateTime > :lowerTime OR :lowerTime IS NULL AND df.datafileCreateTime < :upperTime OR :upperTime IS NULL) AND  df.dataset.markedDeleted = 'N' AND df.markedDeleted = 'N' AND (df.name  =  :datafileName OR :datafileName IS NULL))  

        System.out.println(QUERY);

        Query nullQuery = em.createQuery(QUERY);

        nullQuery.setParameter("objectType", ElementType.DATASET);
        nullQuery.setParameter("userId", "test");

        nullQuery.setParameter("dataSetType", ElementType.DATASET);
        nullQuery.setParameter("lowerTime", new Date(1, 1, 1));
        nullQuery.setParameter("upperTime", new Date());
        nullQuery.setParameter("datafileName", null);

        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");
        
         time = System.currentTimeMillis();
        
          nullQuery.setParameter("userId", "SUPER_USER");
        System.out.println(nullQuery.getResultList());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");
        
        time = System.currentTimeMillis();
         
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
