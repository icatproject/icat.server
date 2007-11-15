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
    
    protected  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestSearch2() {
    }
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        // emf = Persistence.createEntityManagerFactory("icatisis_dev");
        em = emf.createEntityManager();
        
        
        // Begin transaction
        //em.getTransaction().begin();
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        //em.getTransaction().commit();
        
        em.close();
    }
    
    
    public void test() throws Exception {
        
        
        setUp();
        /// old way
        
        long time = System.currentTimeMillis();
        String LIST_ALL = "SELECT i FROM Investigation i, IcatAuthorisation ia WHERE i.id = ia.elementId AND ia.elementType = :investigationType AND i.markedDeleted = 'N' " +
                " AND ia.userId IN('test','ANY')" +
                " AND ia.markedDeleted = 'N' AND i.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y' "+
                
                //  " AND i.instrument = 'mari' "+   //instrument
                
              //  " AND EXISTS (SELECT sample FROM i.sampleCollection sample WHERE sample.name LIKE '%a%' AND " +
              //  " sample.markedDeleted = 'N') "+
                
                //  " AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE '%a%' AND " +
                //  " kw.markedDeleted = 'N')  "+ //iterate, remove if no keyword is null
                
                //  " AND EXISTS (SELECT kw1 FROM i.keywordCollection kw1 WHERE kw1.keywordPK.name LIKE '%b%' AND " +
                //  " kw1.markedDeleted = 'N')  "+ //iterate, remove if no keyword is null
                
                " AND EXISTS (SELECT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
                " df.id = iadf3.elementId AND iadf3.elementType = :dataFileType AND df.markedDeleted = 'N' " +
                " AND (iadf3.userId = 'test' OR iadf3.userId = 'ANY')" +
                " AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y' " +
                " AND df.dataset.investigation = i AND (df.createTime > :lowerTime OR :lowerTime IS NULL AND df.createTime < :upperTime OR :upperTime IS NULL) AND " +
                " df.markedDeleted = 'N' AND (df.name LIKE :datafileName OR :datafileName IS NULL))  " + //remove if all are null
                
                "";
              /*  " AND  i.visitId = :visitId   AND" +
                  " i.invType.name = :invType    AND " +
                  " i.invAbstract LIKE :invAbstract   AND" +
                  " i.grantId = :grantId  AND" +
                  " i.title = :title   AND" +
                  " i.bcatInvStr = :bcatInvStr   AND " +
                  " i.invNumber = :invNumber  " +*/
        
        
        //   " AND i.id IN (SELECT dfp.datafile.dataset.investigation.id FROM DatafileParameter dfp, IcatAuthorisation ia2  " +
        // " WHERE dfp.datafile.id = ia2.elementId AND ia2.elementType = :dataFileType AND dfp.markedDeleted = 'N' " +
        //    " AND (ia2.userId = 'gjd37' OR ia2.userId = 'ANY')" +
        //  " AND ia2.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND ia2.role.actionCanSelect = 'Y' AND dfp.datafile.dataset.investigation = i AND dfp.numericValue BETWEEN 1398 AND 1400 AND " +
        //   " dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N')"; //remove this if run number null
        
        System.out.println(em.createQuery(LIST_ALL).setParameter("dataFileType", ElementType.DATAFILE).
                setParameter("investigationType", ElementType.INVESTIGATION).setMaxResults(100)
               /* .setParameter("visitId", null)
                .setParameter("invType", null)
                .setParameter("grantId", null)
                .setParameter("invAbstract", null)
                .setParameter("invNumber", null)
                .setParameter("bcatInvStr", null)
                .setParameter("title", null)*/
                //  .setParameter("datafileName", "SXD01409.RAW")
                .setParameter("datafileName", "%A%")
               // .setParameter("lowerTime", new Date(1,1,1))
                // .setParameter("upperTime", new Date())
                    .setParameter("lowerTime", null)
                .setParameter("upperTime", null)
                .getResultList());
        
        System.out.println("This method takes " +(System.currentTimeMillis()-time)/1000f + "s to execute");
        
        tearDown();
        
    }
    
    
    public void test2() throws Exception {
        
        
        setUp();
        /// old way
        
        long time = System.currentTimeMillis();
        String LIST_ALL =  "SELECT DISTINCT df.dataset.investigation FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
                " df.id = iadf3.elementId AND iadf3.elementType = :dataFileType AND df.markedDeleted = 'N' " +
                " AND (iadf3.userId = 'gjd37' OR iadf3.userId = 'ANY')" +
                " AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y' " +
                " AND ( df.datafileCreateTime BETWEEN :lowerTime AND  :upperTime  ) AND " +
                " df.markedDeleted = 'N'  ";
        
        Date date = new Date();
        date.setMonth(8);
        date.setDate(1);
        date.setYear(107);
        System.out.println(date);
        
        System.out.println(em.createQuery(LIST_ALL).setParameter("dataFileType", ElementType.DATAFILE)
                .setParameter("lowerTime", date)
                .setParameter("upperTime", new Date())
                .getResultList().size());
        
        System.out.println("This method takes " +(System.currentTimeMillis()-time)/1000f + "s to execute");
        
        tearDown();
        
    }
    
    public void test3() throws Exception {
        
        
        setUp();
        /// old way
        
        long time = System.currentTimeMillis();
        String LIST_ALL =  "SELECT DISTINCT k FROM Keyword k WHERE " +
                " LOWER(k.keywordPK.name) LIKE '%a%'";
       
        
        System.out.println(em.createQuery(LIST_ALL)
                .getResultList());
        
        System.out.println("This method takes " +(System.currentTimeMillis()-time)/1000f + "s to execute");
        
        tearDown();
        
    }
    
     public void test4() throws Exception {
        
        
        setUp();
        /// old way
        
        long time = System.currentTimeMillis();
        
        //Dataset dataset = em.find(Dataset.class, 3L);
        Investigation invest = em.find(Investigation.class,  3L);
        //System.out.println(dataset.getSample().getName());
        
       System.out.println(invest.getSampleCollection().size());
        
        tearDown();
        
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch2 ts = new TestSearch2();
        
        ts.test4();
        
        //   ts.test2();
        
        
    }
    
    
    
}
