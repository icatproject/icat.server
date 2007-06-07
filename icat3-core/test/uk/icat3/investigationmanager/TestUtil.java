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
import javax.persistence.FlushModeType;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestUtil extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestManagerUtil.class);
    private Random random = new Random();
    
    /**
     * Tests own dataset as unique
     */
    @Test
    public void test() throws ICATAPIException {
        Investigation in =  em.find(Investigation.class, 127L);
        
        for (Investigator object1 : in.getInvestigatorCollection()) {
            log.trace(object1);
        }
        
        
        
    }
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestUtil.class);
    }
}
