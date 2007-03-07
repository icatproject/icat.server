/*
 * TestNothing.java
 *
 * Created on 07 March 2007, 14:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.util.BaseTest;
import uk.icat3.util.BaseTestClass;

/**
 *
 * @author df01
 */
public class TestNothing {
    
    private static Logger log = Logger.getLogger(BaseTestClass.class);
    
    @Test 
    public void myTest(){
        log.debug("Test Nothing Test worked");
        assertTrue("Test worked", true);
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestNothing.class);
    }
    
}
