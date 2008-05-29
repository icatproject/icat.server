/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import uk.icat3.util.BaseTestClass;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestAuthorisation  extends BaseTestClass {
    
    private static Logger log = Logger.getLogger(TestAuthorisation.class);
    private Random random = new Random();
    
   
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestAuthorisation.class);
    }
}
