/*
 * TestInvestigationSearch.java
 *
 * Created on 22 February 2007, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.util.BaseTestClass;
import static org.junit.Assert.*;
/**
 *
 * @author gjd37
 */
public class TestInvestigationSearch extends BaseTestClass{
    
    private static Logger log = Logger.getLogger(TestInvestigationSearch.class);    
    
    @Test public void firstTest(){        
        log.debug("Test worked");
        assertTrue("Test worked", true);        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigationSearch.class);
    }
    
}
