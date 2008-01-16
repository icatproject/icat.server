/*
 * BaseTest.java
 *
 * Created on 22 February 2007, 12:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;

/**
 *
 * @author gjd37
 */
public class BaseTest {
    
    private static Logger log = Logger.getLogger(BaseTest.class);
    
    
    // TODO code application logic here
    static protected  EntityManagerFactory  emf = null;
    // Create new EntityManager
    static protected EntityManager  em = null;
    
    public static void setUp(){
        
        emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
        em = emf.createEntityManager();
        log.debug("setUp(), creating entityManager");
        
       // EntityManagerResource.getInstance().set(em);
        
        // Begin transaction
        log.debug("beginning transaction on entityManager");
        
        em.getTransaction().begin();
        
    }
    
    public static void setUpEntityManagerOnly(){
        
        emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
        em = emf.createEntityManager();
        log.debug("setUp(), creating entityManager");
        
       // EntityManagerResource.getInstance().set(em);
        
    }
    
    public static void tearDownEntityManagerOnly(){
        
        log.debug("tearDown(), closing entityManager");
        em.close();
    }
    
    public static void tearDown(){
        
        // Commit the transaction
        log.debug("commiting transaction on entityManager");
        em.getTransaction().commit();
        log.debug("tearDown(), closing entityManager");
        em.close();
    }
    
    
    public static Collection<?> executeListResultCmd(String sql){
        return em.createQuery(sql).getResultList();
    }
    
    public static Object executeSingleResultCmd(String sql){
        return em.createQuery(sql).getSingleResult();
    }
    
    public static Collection<?> executeNativeListResultCmd(String sql, Class className){
        return em.createNativeQuery(sql).getResultList();
    }
    
    public static Object executeNativeSingleResultCmd(String sql, Class className){
        return em.createNativeQuery(sql, className).getSingleResult();
    }
}
