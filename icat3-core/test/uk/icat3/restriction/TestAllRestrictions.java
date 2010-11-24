/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 27 juil. 2010
 */

package uk.icat3.restriction;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.restriction.dataset.DatasetTest;
import uk.icat3.restriction.dataset.ValueTypeDatasetTest;
import uk.icat3.restriction.exception.DatasetExceptionTest;

/**
 *
 * @author cruzcruz
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DatasetTest.class,
    ValueTypeDatasetTest.class,
    DatasetExceptionTest.class,
    UsesExamples.class
})
        
public class TestAllRestrictions {
     public static Test suite() {
        return new JUnit4TestAdapter(TestAllRestrictions.class);
    }
}
