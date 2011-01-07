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
import uk.icat3.entity.IcatAuthorisation;

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
    private IcatAuthorisation testAuth;
    @Before
    public void Before(){
        setUpEntityManagerOnly();
    }
    
    @After
    public void After(){
        tearDownEntityManagerOnly();
    }
    
     @BeforeClass
    public static void BeforeClassSetUp(){
        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

         setUpEntityManagerFactoryOnly();
    }
    
    @AfterClass
    public static void AfterClassTearDown(){
        tearDownEntityManagerFactoryOnly();
    }

    protected IcatAuthorisation getTestAutho(){
        return testAuth;
    }
}
