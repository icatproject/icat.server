/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.entity.Parameter;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import static org.junit.Assert.*;

/**
 *
 * @author gjd37
 */
public class TestParameterSearch extends BaseParameterSearchTest {
    
    private static Logger log = Logger.getLogger(TestParameterSearch.class);
    
    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByName(){
        Collection<Parameter> li = ParameterSearch.getParameterByName("", "Datafile", em);
        assertEquals("Number of parameter incorrect", 5, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByUnits("", "str", em);
        assertEquals("Number of parameter incorrect", 2, li.size());
    }

    /**
     * Tests parameters
     */
    @Test
    public void testgetParameterByNameUnits(){
        Collection<Parameter> li = ParameterSearch.getParameterByNameUnits("", "scan", "str", em);
        assertEquals("Number of parameter incorrect", 1, li.size());
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestParameterSearch.class);
    }
}
