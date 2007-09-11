/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.util.ElementType;

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
        //    emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        emf = Persistence.createEntityManagerFactory("icatisis_dev");
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
        String LIST_ALL = "SELECT count(i) FROM Investigation i, IcatAuthorisation ia WHERE i.id = ia.elementId AND ia.elementType = :investigationType AND i.markedDeleted = 'N' " +
                " AND (ia.userId = 'gjd37' OR ia.userId = 'ANY')" +
                " AND ia.markedDeleted = 'N' AND i.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y' "+
                
                
                " AND (:visitId IS NULL OR i.visitId = :visitId   ) AND" +
                " (:invType IS NULL OR i.invType.name = :invType   ) AND " +
                " (:invAbstract IS NULL OR i.invAbstract LIKE :invAbstract  ) AND" +
                " (:grantId IS NULL OR i.grantId = :grantId  ) AND" +
                " (:title IS NULL OR i.title = :title  ) AND" +
                " (:bcatInvStr IS NULL OR i.bcatInvStr = :bcatInvStr  ) AND " +
                " (:invNumber IS NULL OR i.invNumber = :invNumber   ) " +
                
                " AND EXISTS (SELECT dfp FROM DatafileParameter dfp, IcatAuthorisation ia2 " +
                " WHERE dfp.datafile.id = ia2.elementId AND ia2.elementType = :dataFileType AND dfp.markedDeleted = 'N' " +
                " AND (ia2.userId = 'gjd37' OR ia2.userId = 'ANY')" +
                " AND ia2.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND ia2.role.actionCanSelect = 'Y' AND dfp.datafile.dataset.investigation = i AND dfp.numericValue BETWEEN 0 AND 1400 AND " +
                " dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N')"; //remove this if run number null
        
        System.out.println(em.createQuery(LIST_ALL).setParameter("dataFileType", ElementType.DATAFILE).
                setParameter("investigationType", ElementType.INVESTIGATION)
                .setParameter("visitId", null)
                .setParameter("invType", null)
                .setParameter("grantId", null)
                .setParameter("invAbstract", null)
                .setParameter("invNumber", null)
                .setParameter("bcatInvStr", null)
                .setParameter("title", null)
                .getResultList());
        
        System.out.println("This method takes " +(System.currentTimeMillis()-time)/1000f + "s to execute");
        
        tearDown();
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch2 ts = new TestSearch2();
        
        ts.test();
        
        
        
        
    }
    
    
    
}
