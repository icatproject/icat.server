/*
 * DownloadFileTest.java
 *
 * Created on 18-Oct-2007, 09:56:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author gjd37
 */
public class DownloadFileTest {
    
    protected  static Logger log = Logger.getLogger(DownloadFileTest.class);
    
    /** Creates a new instance of DownloadFileTest */
    public DownloadFileTest() {
        PropertyConfigurator.configure(this.getClass().getResource("log4j.properties"));
    }
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-apitest");
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
    
   
    
   
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
       
    }
    
}
