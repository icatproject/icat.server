/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.ElementType;

/**
 *
 * @author gjd37
 */
public class TestJPA {
    
    protected  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestJPA() {
    }
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icat3-dls_dev_new");
        em = emf.createEntityManager();
        // Begin transaction
        em.getTransaction().begin();
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
    
    public void test() throws Exception{
        setUp();
        
        Investigation investigation = new Investigation();
        
        investigation.setTitle("investigation "+new Random().nextInt());
        investigation.setInvNumber(""+new Random().nextInt());
        investigation.setInvType(new InvestigationType("experiment"));
        
        InvestigationManager.createInvestigation("test_admin_investigation", investigation, em);
        
        tearDown();
    }
    
    public void changeRole() throws Exception {
        setUp();
        
        InvestigationManager.updateAuthorisation("test_admin_investigation", "DOWNLOADER", 101L, em);
        
        tearDown();
    }
    
    public void testJPA() throws Exception {
        setUp();
        String JPA ="SELECT i FROM IcatAuthorisation i WHERE i.elementType = :elementType AND i.elementId IS NULL AND (i.parentElementType = :parentElementType) AND (i.parentElementId = :parentElementId) AND i.userId = :userId AND i.markedDeleted = 'N'";
        
        System.out.println(em.createQuery(JPA).getResultList());
        
        tearDown();
    }
    
    public void getRoles() throws Exception {
        setUp();
        
        System.out.println(InvestigationManager.getAuthorisations("test_admin_investigation", 100L, em));
        
        tearDown();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestJPA ts = new TestJPA();
        
        ts.getRoles();
        // ts.test();
        // ts.testJPA();
        // ts.changeRole();
    }
    
    
    
}
