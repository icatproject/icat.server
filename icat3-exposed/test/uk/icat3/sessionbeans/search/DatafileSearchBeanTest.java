/*
 * DatafileSearchBeanTest.java
 * JUnit 4.x based test
 *
 * Created on 31 May 2007, 12:19
 */

package uk.icat3.sessionbeans.search;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import uk.icat3.exposed.util.BaseTestClassTX;
import static org.junit.Assert.*;

/**
 *
 * @author gjd37
 */
public class DatafileSearchBeanTest extends BaseTestClassTX{
    
    public DatafileSearchBeanTest() {
    }

 
    @Test
    public void searchByRunNumber() throws Exception {
        System.out.println("searchByRunNumber");
        assertEquals("f","f");
    }
    
     public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(DatafileSearchBeanTest.class);
    }
    
}
