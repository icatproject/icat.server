/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.parametersearch.myprobes;

import java.util.Date;
import java.util.List;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.manager.FacilityManager;
import uk.icat3.restriction.RestrictionComparisonCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.attribute.RestrictionAttributes;
import uk.icat3.restriction.util.RestrictionUtil;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.parameter.ComparisonOperator;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author cruzcruz
 */
public class Main {

    public static void main (String argv[]) throws RestrictionOperatorException {
//        String value =" ^*+`+`*^*�+�-��-.�`�`��+`+`��:+`:+'�')((/&%najor  234234-  ";
//        value = value.replaceAll("\\s*,\\s*", "','")
//                .replaceAll("^\\s+", "'")
//                .replaceAll("\\s+$", "'");
//        System.out.println(value.replaceAll("[^\\w\\s-:]", ""));
//        try {
//            RestrictionComparisonCondition restriction1 = new RestrictionComparisonCondition(RestrictionAttributes.DATASET_NAME, ComparisonOperator.CONTAIN, "s");
//            RestrictionLogicalCondition log = new RestrictionLogicalCondition(LogicalOperator.OR)
//                    .add(comp)
//                    .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.DATASET_DESCRIPTION, ComparisonOperator.IN, "'s', 'd'"))
//                    .add (new RestrictionLogicalCondition(LogicalOperator.AND)
//                        .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.INVESTIGATION_END_DATE, ComparisonOperator.GREATER_EQUAL, new Date(0)))
//                        .add(new RestrictionComparisonCondition(
//                            RestrictionAttributes.DATAFILE_NAME, ComparisonOperator.CONTAIN, "0876")
//                    ));
//
//            System.out.println("----> " + new RestrictionUtil(log, RestrictionType.DATASET).getSentenceJPQL());


//             // Get the parameter, manually or get from a service
//        Parameter dateTime = new Parameter(new ParameterPK("yyyy-MM-dd HH:mm:ss", "time1"));
//        // Create the parameter to compare with. Two argument: type of the parameter
//        //  to compare and the parameter.
//        ParameterSearch pamVal = new ParameterSearch(ParameterType.DATAFILE, dateTime);
//        // Create the comparasion
//        ParameterComparisonCondition comp1 = new ParameterComparisonCondition();
//        // Add the parameterValued
//        comp1.setParameterValued(pamVal);
//        // Add the comparator
//        comp1.setComparator(ComparisonOperator.BETWEEN);
//        // Add the value to compare
//        comp1.setDatetimeValue(new Date(0));
//        // Add a second value if needed (only for BETWEEN)
//        comp1.setDatetimeValueRight("2010-10-10 00:00:00");
//            try {
//                List<Dataset> ld = (List<Dataset>) DatasetSearch
//                .searchByParameterCondition("", comp1, restriction1, DatasetInclude.NONE,  null);
//            } catch (NoParameterTypeException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoStringComparatorException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoNumericComparatorException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoSearchableParameterException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NullParameterException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (EmptyOperatorException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoParametersException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (ParameterNoExistsException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoDatetimeComparatorException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (DatevalueException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NumericvalueException ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } catch (DatevalueFormatException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }  catch (RestrictionEmptyListException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
