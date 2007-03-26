/*
 * BaseTestClass.java
 *
 * Created on 22 February 2007, 12:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * Extend this when you want setUp and tear down to be called one in the class and not after and before every @Test method
 *
 * Basically, manager is loaded  and one transaction per class 
 *
 * @author gjd37
 */
public class BaseTestClass extends BaseTest{
    
    private static Logger log = Logger.getLogger(BaseTestClass.class);
    
    @BeforeClass
    public static void BeforeClassSetUp(){
        setUp();
    }
    
    @AfterClass
    public static void AfterClassTearDown(){
        tearDown();
    }
    
    
}
