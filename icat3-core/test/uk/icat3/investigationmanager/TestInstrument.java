/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Instrument;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.BaseTestClassTX;

/**
 *
 * @author gjd37
 */
public class TestInstrument extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInstrument.class);
    private static Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addInvestigation() throws ICATAPIException {
        Collection<Instrument> li = InvestigationSearch.getAllInstruments(em);
        assertNotSame("Instruments is empty", li.size(), 0);
    }
    
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInstrument.class);
    }
}
