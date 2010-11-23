/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 27 juil. 2010
 */

package uk.icat3.parametersearch;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.icat3.parametersearch.datafile.DatafileTest;
import uk.icat3.parametersearch.datafile.ValueTypeDatafileTest;
import uk.icat3.parametersearch.dataset.DatasetTest;
import uk.icat3.parametersearch.dataset.ValueTypeDatasetTest;
import uk.icat3.parametersearch.exception.DatafileExceptionTest;
import uk.icat3.parametersearch.exception.DatasetExceptionTest;
import uk.icat3.parametersearch.exception.InvestigationExceptionTest;
import uk.icat3.parametersearch.exception.SampleExceptionTest;
import uk.icat3.parametersearch.investigation.ListComparatorTest;
import uk.icat3.parametersearch.investigation.ListParameterTest;
import uk.icat3.parametersearch.investigation.OperableTest;
import uk.icat3.parametersearch.investigation.ValueTypeInvestigationTest;
import uk.icat3.parametersearch.sample.SampleTest;
import uk.icat3.parametersearch.sample.ValueTypeSampleTest;
import uk.icat3.search.TestParameterSearch;

/**
 *
 * @author cruzcruz
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DatafileTest.class,
    ValueTypeDatafileTest.class,
    DatasetTest.class,
    ValueTypeDatasetTest.class,
    SampleTest.class,
    ValueTypeSampleTest.class,
    ListParameterTest.class,
    ListComparatorTest.class,
    OperableTest.class,
    ValueTypeInvestigationTest.class,
    InvestigationExceptionTest.class,
    DatafileExceptionTest.class,
    DatasetExceptionTest.class,
    SampleExceptionTest.class,
    UsesExamples.class,
    TestParameterSearch.class
})
        
public class TestAllParameterSearch {
     public static Test suite() {
        return new JUnit4TestAdapter(TestAllParameterSearch.class);
    }
}
