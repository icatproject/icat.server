/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.investigation;

import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.entity.Investigation;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.InvestigationSearch;

/**
 *
 * @author cruzcruz
 */
public class ListComparatorTest extends BaseParameterSearchTest {

    @Test
    public void datafileParameterTest () throws NoParameterTypeException, ParameterSearchException {
        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDatafile.get(0));
        lc.add(pcDatafile.get(1));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListComparators("SUPER_USER", lc, -1, -1, em);
        
       assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
    }

    @Test
    public void datasetParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDataset.get(0));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListComparators("SUPER_USER", lc, -1, -1, em);

       assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
        
    }

    @Test
    public void sampleParameterTest () throws NoParameterTypeException, ParameterSearchException {

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcSample.get(0));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListComparators("SUPER_USER", lc, 1, -1, em);

        showInv(li);

        assertTrue("Results of investigations should not be ZERO", (li.size() == 1));

    }

    @Test
    public void allParameterTest () throws NoParameterTypeException, ParameterSearchException {

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(pcDatafile.get(0));
        lc.add(pcDataset.get(0));
        lc.add(pcSample.get(0));

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListComparators("SUPER_USER", lc, 1, -1, em);
        
       assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
    }

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(ListComparatorTest.class);
    }
}