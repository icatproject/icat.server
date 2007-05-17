/*
 * BaseTestMethod.java
 *
 * Created on 22 February 2007, 12:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.util;

import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

/**
 * Extend this when you want setUp and tear down to be called after and before every @Test method
 *
 * Basically, manager and new transactions with every method
 *
 * @author gjd37
 */
public class BaseTestMethod extends BaseTest {
    
    private static Logger log = Logger.getLogger(BaseTestMethod.class);
        
    @Before
    public static void BeforeSetUp(){
        setUp();
    }
    
    @After
    public static void AftertearDown(){
        tearDown();
    }
    
}
