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
import uk.icat3.search.parameter.util.ParameterValued;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.icat3.entity.Investigation;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.InvestigationSearch;

/**
 *
 * @author cruzcruz
 */
public class ListParameterTest extends BaseParameterSearchTest {
    
    @Test
    public void datafileParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        
        List<ParameterValued> lp = new ArrayList<ParameterValued>();

        ParameterValued pv3 = new ParameterValued(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterValued pv4 = new ParameterValued(ParameterType.DATAFILE, parameter.get("datafile2"));

        lp.add(pv3);
        lp.add(pv4);

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListParameter("SUPER_USER", lp, 1, -1, em);

       showInv(li);
       assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
    }

    @Test
    public void datasetParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        List<ParameterValued> lp = new ArrayList<ParameterValued>();

        ParameterValued pv2 = new ParameterValued(ParameterType.DATASET, parameter.get("dataset1"));
        ParameterValued pv3 = new ParameterValued(ParameterType.DATASET, parameter.get("dataset2"));

        lp.add(pv2);
//        lp.add(pv3);
//        lp.add(pv4);

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListParameter("SUPER_USER", lp, 1, -1, em);

        showInv(li);
        assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
        
    }

    @Test
    public void sampleParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        List<ParameterValued> lp = new ArrayList<ParameterValued>();

        ParameterValued pv3 = new ParameterValued(ParameterType.SAMPLE, parameter.get("sample1"));
        
        lp.add(pv3);

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListParameter("SUPER_USER", lp, 1, -1, em);

        showInv(li);
        assertFalse("Results of investigations should not be ZERO", (li.size() == 0));
    }

    @Test
    public void allParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        List<ParameterValued> lp = new ArrayList<ParameterValued>();
        
        ParameterValued pv1 = new ParameterValued(ParameterType.SAMPLE, parameter.get("sample1"));
        ParameterValued pv2 = new ParameterValued(ParameterType.DATAFILE, parameter.get("datafile1"));
        ParameterValued pv3 = new ParameterValued(ParameterType.DATASET, parameter.get("dataset1"));

        lp.add(pv1);
        lp.add(pv2);
        lp.add(pv3);

        List<Investigation> li = (List<Investigation>) InvestigationSearch
                .searchByParameterListParameter("SUPER_USER",lp, 1, -1, em);

        showInv(li);
        assertTrue("Results of investigations should not be ZERO", (li.size() == 1));
    }

    

    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(ListParameterTest.class);
    }

}
