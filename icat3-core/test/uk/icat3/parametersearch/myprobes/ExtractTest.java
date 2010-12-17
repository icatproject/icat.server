/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 8 juil. 2010
 */

package uk.icat3.parametersearch.myprobes;

import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.ParameterSearchException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.OperatorINException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterType;

import org.apache.log4j.Logger;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.parametersearch.BaseParameterSearchTest;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;


/**
 *
 * @author cruzcruz
 */
public class ExtractTest extends BaseParameterSearchTest {


    public void cosa (String sid, String name, ParameterType... t) {
    }

    @Test
    public void restriction () throws CyclicException, NoParameterTypeException, RestrictionOperatorException, OperatorINException, RestrictionException {
        try {
            RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(RestrictionAttributes.INVESTIGATION_TITLE, ComparisonOperator.CONTAINS, "gation 1");
            ParameterLogicalCondition op1 = new ParameterLogicalCondition(LogicalOperator.OR);
            op1.add(pcDataset.get(0));
            op1.add(pcDataset.get(1));
            op1.add(pcSample.get(0));
            op1.add(pcDatafile.get(1));
            List<Dataset> li = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION, op1, restriction1, DatasetInclude.NONE, 1, -1, em);

            System.out.println("");
                System.out.println("---> " + li.size());
                System.out.println("");
        } catch (RestrictionNullException ex) {
        	 log.error(ex);
        } catch (NoStringComparatorException ex) {
        	log.error(ex);
        } catch (NoNumericComparatorException ex) {
        	log.error(ex);
        } catch (NoSearchableParameterException ex) {
        	log.error(ex);
        } catch (NullParameterException ex) {
        	log.error(ex);
        } catch (EmptyOperatorException ex) {
        	log.error(ex);
        } catch (NoParametersException ex) {
        	log.error(ex);
        } catch (ParameterNoExistsException ex) {
        	log.error(ex);
        } catch (NoDatetimeComparatorException ex) {
        	log.error(ex);
        } catch (DatevalueException ex) {
        	log.error(ex);
        } catch (NumericvalueException ex) {
        	log.error(ex);
        } catch (DatevalueFormatException ex) {
        	log.error(ex);
        } catch (RestrictionEmptyListException ex) {
        	log.error(ex);
        }
    }

//    @Test
    public void probe () throws OperatorINException, RestrictionException {
        try {
            RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(RestrictionAttributes.INVESTIGATION_ID, ComparisonOperator.GREATER_THAN, "123");
//            RestrictionLogicalCondition log = new RestrictionLogicalCondition(LogicalOperator.OR)
//                    .add(comp)
//                    .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.DATASET_DESCRIPTION, ComparisonOperator.IN, "'s', 'd'"))
//                    .add (new RestrictionLogicalCondition(LogicalOperator.AND)
//                        .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.INVESTIGATION_END_DATE, ComparisonOperator.GREATER_EQUAL, new Date(0)))
//                        .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.CONTAINS, "0876")
//                    ));
//
//            System.out.println("----> " + new RestrictionUtil(log, RestrictionType.DATASET).getSentenceJPQL());


             // Get the parameter, manually or get from a service
//        Parameter dateTime = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));
            Parameter p = new Parameter(new ParameterPK("Å", "Wavelength"));
        // Create the parameter to compare with. Two argument: type of the parameter
        //  to compare and the parameter.
        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, p);
        // Create the comparasion
        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
        // Add the parameterValued
        comp1.setParameterSearch(pamVal);
        // Add the comparator
        comp1.setComparator(ComparisonOperator.EQUALS);
        comp1.setValue(5);
        restriction1.setReturnLongId(true);
        // Add the value to compare
//        comp1.setValue(new Date(0));
        // Add a second value if needed (only for BETWEEN)
//        comp1.setValueRight("2010-10-10 00:00:00");
            try {
                List<Dataset> ld = (List<Dataset>) DatasetSearch
                .searchByParameterCondition(VALID_USER_FOR_INVESTIGATION
                        , comp1
                        , restriction1
                        , DatasetInclude.NONE
                        , Queries.NO_LIMITED_RESULTS
                        , Queries.NO_LIMITED_RESULTS
                        , em);
                System.out.println("");
                System.out.println("---> " + ld.size());
                System.out.println("");
            }catch (CyclicException ex) {
                Logger.getLogger(ExtractTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RestrictionNullException ex) {
                Logger.getLogger(ExtractTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RestrictionOperatorException ex) {
                Logger.getLogger(ExtractTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoParameterTypeException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoStringComparatorException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoNumericComparatorException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSearchableParameterException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullParameterException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EmptyOperatorException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoParametersException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParameterNoExistsException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoDatetimeComparatorException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DatevalueException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumericvalueException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (DatevalueFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (RestrictionEmptyListException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



//    @Test
    public void listParameterTest () throws NoParameterTypeException, NoParametersException, ParameterSearchException {
        List<ParameterSearch> lp = new ArrayList<ParameterSearch>();

        Parameter p = new Parameter(new ParameterPK("Å", "Wavelength"));

        ParameterSearch pv1 = new ParameterSearch(ParameterType.DATAFILE, p);
//        ParameterSearch pv2 = new ParameterSearch(ParameterType.DATASET, parameter.get("dataset1"));
//        ParameterSearch pv3 = new ParameterSearch(ParameterType.SAMPLE, parameter.get("sample1"));

        lp.add(pv1);
//        lp.add(pv2);
//        lp.add(pv3);

//        List<Long> li = (List<Long>) DatasetSearch
//                .searchByParameterList(VALID_USER_FOR_INVESTIGATION, lp, DatasetInclude.ALL_DATASET_ID, Queries.NO_PAGINATION, Queries.NO_LIMITED_RESULTS, em);
//
//        int cont = 0;
//        for (Long l : li) {
//            System.out.println(cont++ + "--->> " + l);
//        }
//
//        System.out.println("");
//
//        assertTrue("Results of investigations should be 1 not " + li.size(), (li.size() == 1));

    }


//    @Test
//    public void getParameter () {
//        Collection<Parameter> li = ParameterSearch.getParameterByName("", "a", em);
//
//        for (Parameter l : li) {
//            System.out.println("--> " + l.getParameterPK().getName());
//        }
//
//        li = ParameterSearch.getParameterByUnits("", "a", em);
//
//        for (Parameter l : li) {
//            System.out.println("--> " + l.getParameterPK().getName());
//        }
//
//        li = ParameterSearch.getParameterByNameUnits("", "a", "", em);
//
//        for (Parameter l : li) {
//            System.out.println("--> " + l.getParameterPK().getName());
//        }
//    }

    /**
     * Test the JPQL generated for parameter search when a list
     * of parameter comparator is passed to the web service function.
     * 
     * @throws ParameterSearchException
     */
//    @Test
    public void listComparators () throws ParameterSearchException {
//        new ParameterSearchUtil().extractJPQLComparators(getListComparators());
        cosa("hola", "hola", ParameterType.DATAFILE, ParameterType.SAMPLE);
    }

    /**
     * Return a list of comparator examples.
     * 
     * @return
     */
//    private List<ParameterComparisonCondition> getListComparators () {
//        Parameter p1 = new Parameter();
//        p1.setParameterPK(new ParameterPK("string", "scanType"));
//        p1.setIsDatafileParameter("Y");
//        p1.setValueType(ParameterValueType.STRING);
//
//
//        Parameter p3 = new Parameter();
//        p3.setParameterPK(new ParameterPK("string", "scanType"));
//        p3.setIsDatafileParameter("Y");
//        p3.setValueType(ParameterValueType.STRING);
//
//        Parameter p4 = new Parameter();
//        p4.setParameterPK(new ParameterPK("string", "scanType"));
//        p4.setIsDatafileParameter("Y");
//        p4.setValueType(ParameterValueType.STRING);
//
//        Parameter p2 = new Parameter();
//        p2.setParameterPK(new ParameterPK("string", "scanType"));
//        p2.setIsDatafileParameter("Y");
//        p2.setValueType(ParameterValueType.NUMERIC);
//
//        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
//        comp1.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, p1));
//        comp1.setComparator(ComparisonOperator.STARTS_WITH);
//        comp1.setValue("comp1");
//
//        ParameterComparisonCondition comp2 = new ParameterComparisonCondition();
//        comp2.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, p2));
//        comp2.setComparator(ComparisonOperator.LESS_EQUAL);
//        comp2.setValue(new Float("12.23423"));
//
//        ParameterComparisonCondition comp3 = new ParameterComparisonCondition();
//        comp3.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, p3));
//        comp3.setComparator(ComparisonOperator.STARTS_WITH);
//        comp3.setValue("comp3");
//
//        ParameterComparisonCondition comp4 = new ParameterComparisonCondition();
//        comp4.setParameterSearch(new ParameterSearch(ParameterType.DATAFILE, p4));
//        comp4.setComparator(ComparisonOperator.STARTS_WITH);
//        comp4.setValue("comp4");
//
//        List<ParameterComparisonCondition> lc = new ArrayList<ParameterComparisonCondition>();
//        lc.add(comp1);
//        lc.add(comp2);
//        lc.add(comp3);
//        lc.add(comp4);
//
//        return lc;
//    }
}
