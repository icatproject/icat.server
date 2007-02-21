/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.search.Search;
import uk.icat3.util.EntityManagerResource;

/**
 *
 * @author gjd37
 */
public class TestSearch {
    
    private  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestSearch() {
    }
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-core-testing-PU");
        em = emf.createEntityManager();
        EntityManagerResource.getInstance().set(em);
        
        // Begin transaction
        em.getTransaction().begin();
        
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
    
    public  void seachByKeyword(String userId, String keyword) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = Search.searchByKeyword(userId,keyword,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public  void seachBySurname(String userId, String surname) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = Search.searchByUser(userId,surname,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public void seachByUserID(String userId, String searchUserID) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = Search.searchByUserID(userId,searchUserID,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch ts = new TestSearch();
        
       // ts.seachByKeyword("885", "Applicatio");
        
      //  ts.seachBySurname("885", "Stuar,");
        
       //   ts.seachByUserID("885", "dis79");
           ts.seachByUserID("885", "gjd37");
    }
    
}
