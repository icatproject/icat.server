/*
 * BaseTestMethod.java
 *
 * Created on 22 February 2007, 12:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

/**
 * Extend this when you want setUp and tear down to be called after and before every @Test method
 *
 * @author gjd37
 */
public class BaseTestMethod extends BaseTest {
    
    private static Logger log = Logger.getLogger(BaseTestMethod.class);
    
    
    @Before
    public static void setUp(){
        
        log.trace("setUp(), creating entityManager");
        
        emf = Persistence.createEntityManagerFactory("icat3-core-testing-PU");
        em = emf.createEntityManager();
        EntityManagerResource.getInstance().set(em);
        
        // Begin transaction
        em.getTransaction().begin();
        
        
    }
    
    @After
    public static void tearDown(){
        
        log.trace("tearDown(), closing entityManager");
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
}
