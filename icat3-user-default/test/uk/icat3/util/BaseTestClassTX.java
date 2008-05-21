/*
 * BaseTestClass.java
 *
 * Created on 22 February 2007, 12:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.util;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * Extend this when you want setUp and tear down to be called one in the class and not after and before every @Test method
 * But you want a new transaction to start and finish before and after evey method
 *
 * Basically, manager is loaded per class and new transactions with every method
 *
 * @author gjd37
 */
public class BaseTestClassTX extends BaseTest{
    
    private static Logger log = Logger.getLogger(BaseTestClass.class);
    
    @Before
    public void beginTX(){
        // Begin transaction
        log.debug("beginning transaction on entityManager");
        
        em.getTransaction().begin();
    }
    
    @After
    public void closeTX(){
        // Commit the transaction
        log.debug("commiting transaction on entityManager");
        em.getTransaction().commit();
    }
    
    @BeforeClass
    public static void BeforeClassSetUp(){
        setUpEntityManagerOnly();
    }
    
    @AfterClass
    public static void AfterClassTearDown(){
        tearDownEntityManagerOnly();
    }      
}
