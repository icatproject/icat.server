/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.parametersearch.myprobes;

import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.search.parameter.util.ParameterSearchUtil;
import uk.icat3.search.parameter.util.ParameterSearch;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.util.ParameterValueType;

/**
 *
 * @author cruzcruz
 */
public class ExtractTest {


    public void cosa (String sid, String name, ParameterType... t) {
    }

    /**
     * Test the JPQL generated for parameter search when a list
     * of parameter comparator is passed to the web service function.
     * 
     * @throws ParameterSearchException
     */
    @Test
    public void listComparators () throws ParameterSearchException {
//        new ParameterSearchUtil().extractJPQLComparators(getListComparators());
        cosa("hola", "hola", ParameterType.DATAFILE, ParameterType.SAMPLE);
    }

    /**
     * Return a list of comparator examples.
     * 
     * @return
     */
    private List<ParameterComparisonCondition> getListComparators () {
        Parameter p1 = new Parameter();
        p1.setParameterPK(new ParameterPK("string", "scanType"));
        p1.setIsDatafileParameter("Y");
        p1.setValueType(ParameterValueType.STRING);


        Parameter p3 = new Parameter();
        p3.setParameterPK(new ParameterPK("string", "scanType"));
        p3.setIsDatafileParameter("Y");
        p3.setValueType(ParameterValueType.STRING);

        Parameter p4 = new Parameter();
        p4.setParameterPK(new ParameterPK("string", "scanType"));
        p4.setIsDatafileParameter("Y");
        p4.setValueType(ParameterValueType.STRING);

        Parameter p2 = new Parameter();
        p2.setParameterPK(new ParameterPK("string", "scanType"));
        p2.setIsDatafileParameter("Y");
        p2.setValueType(ParameterValueType.NUMERIC);

        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
        comp1.setParameterValued(new ParameterSearch(ParameterType.DATAFILE, p1));
        comp1.setComparator(ComparisonOperator.START_WITH);
        comp1.setStringValue("comp1");

        ParameterComparisonCondition comp2 = new ParameterComparisonCondition();
        comp2.setParameterValued(new ParameterSearch(ParameterType.DATAFILE, p2));
        comp2.setComparator(ComparisonOperator.LESS_EQUAL);
        comp2.setNumericValue(new Float("12.23423"));

        ParameterComparisonCondition comp3 = new ParameterComparisonCondition();
        comp3.setParameterValued(new ParameterSearch(ParameterType.DATAFILE, p3));
        comp3.setComparator(ComparisonOperator.START_WITH);
        comp3.setStringValue("comp3");

        ParameterComparisonCondition comp4 = new ParameterComparisonCondition();
        comp4.setParameterValued(new ParameterSearch(ParameterType.DATAFILE, p4));
        comp4.setComparator(ComparisonOperator.START_WITH);
        comp4.setStringValue("comp4");

        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
        lc.add(comp1);
        lc.add(comp2);
        lc.add(comp3);
        lc.add(comp4);

        return lc;
    }
}
