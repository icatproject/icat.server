/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 27 juil. 2010
 */

package uk.icat3;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.investigationmanager.TestInstrument;

/**
 *
 * @author cruzcruz
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    uk.icat3.parametersearch.TestAllParameterSearch.class,
    uk.icat3.restriction.TestAllRestrictions.class,
    TestInstrument.class
})
        
public class TestAllParameterSearchRestriction {
     public static Test suite() {
        return new JUnit4TestAdapter(TestAllParameterSearchRestriction.class);
    }
}
