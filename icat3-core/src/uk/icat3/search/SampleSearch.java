/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 20 ao�t 2010
 */

package uk.icat3.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.NoElementTypeException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.RestrictionINException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.util.RestrictionUtil;
import uk.icat3.search.parameter.util.ExtractedJPQL;
import uk.icat3.search.parameter.util.ParameterSearchUtilSingleton;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.ElementType;
import uk.icat3.util.SampleInclude;

import static uk.icat3.util.Queries.*;

/**
 *
 * @author cruzcruz
 */
public class SampleSearch {

    // Global class logger
    static Logger log = Logger.getLogger(SampleSearch.class);


    /**
     * Search the datafiles from parameter selection.
     *
     * @param userId User identification
     * @param ejpql Parameter information container
     * @param startIndex Start index for results
     * @param numberResults Number of results to return
     * @param manager Entity manager to access the database
     * @return
     * @throws NoParametersException
     * @throws ParameterSearchException
     */
    private static Collection<Sample> searchByParameterImpl(String userId, ExtractedJPQL ejpql, RestrictionUtil restricion, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");
            // Return type
            String returnJPQL = RETURN_ALL_SAMPLES_JPQL;
            // Return ids
            if (include == SampleInclude.ALL_SAMPLE_ID) {
                returnJPQL = RETURN_ALL_SAMPLE_ID_JPQL;
                numberResults = NO_LIMITED_RESULTS;
            }
            // Check for restriction parameters
            // Check for restriction parameters
            String restrictionParam = "";
            if (ejpql.getDatafileParameter().isEmpty())
                restrictionParam += restricion.getParameterJPQL(ElementType.SAMPLE, ElementType.DATAFILE);
            if (ejpql.getDatasetParameter().isEmpty() && ejpql.getDatafileParameter().isEmpty())
                restrictionParam += restricion.getParameterJPQL(ElementType.SAMPLE, ElementType.DATASET);
            
            // Construction JPQL sentence
            String jpql = returnJPQL + restrictionParam + ", " + ejpql.getParametersJPQL(ElementType.SAMPLE)
                    + QUERY_USERS_SAMPLES_JPQL;
            // Object returns and check number of results
            Collection res = ManagerUtil.getRestultList(jpql, ejpql, restricion
                    , ElementType.INVESTIGATION, userId, startIndex, numberResults
                    , manager);
            // Return type is a Collection of Long
            if (include == SampleInclude.ALL_SAMPLE_ID)
                return res;
            // Return results
            return res;
        } catch (NoElementTypeException ex) {
            java.util.logging.Logger.getLogger(SampleSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ArrayList<Sample>();
    }

    /**
     * Search by parameters in the database. The parameter object 'ejpql' contains some
     * JPQL statement (parameters, conditions).
     *
     * @param userId federalId of the user.
     * @param ejpql This object contains the jpql statement.
     * @param startIndex start index of the results found
     * @param numberResults number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return Collection of investigation matched
     * @throws NoParameterTypeException
     */
     public static Collection<Sample> searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NullParameterException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException   {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, startIndex, numberResults, manager);
    }

     /**
     * Search by parameters in the database. The parameter object 'ejpql' contains some
     * JPQL statement (parameters, conditions).
     *
     * @param userId federalId of the user.
     * @param ejpql This object contains the jpql statement.
     * @param startIndex start index of the results found
     * @param numberResults number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return Collection of investigation matched
     * @throws NoParameterTypeException
     */
     public static Collection<Sample> searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NullParameterException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException   {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, -1, -1, manager);
    }

     /**
      * Search by parameters from a parameterOperable.
      *
      * @param userId federalId of the user.
      * @param parameterOperable ParameterCondition where the conditions are defined
      * @param startIndex start index of the results found
      * @param numberResults number of results found from the start index
      * @param manager manager object that will facilitate interaction with underlying database
      * @return
      * @throws ParameterSearchException
      * @see ParameterCondition
      */
     public static Collection<Sample> searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyOperatorException, NoSearchableParameterException, NullParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, NoParametersException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException   {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, startIndex, numberResults, manager);
    }

     /**
      * Search by parameters from a parameterOperable.
      *
      * @param userId federalId of the user.
      * @param parameterOperable ParameterCondition where the conditions are defined
      * @param manager Object that will facilitate interaction with underlying database
      * @return
      * @throws ParameterSearchException
      */
    public static Collection<Sample> searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws DatevalueFormatException, NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NoParametersException, NullParameterException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException, EmptyOperatorException, NoStringComparatorException, NoNumericComparatorException, NoDatetimeComparatorException, NumericvalueException    {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, -1, -1, manager);
    }

    /**
     * Search by parameters where the investigation contains every parameter defined
     * in listParam.
     *
     * @param userId federalId of the user.
     * @param listParam List of parameters
     * @param startIndex start index of the results found
     * @param numberResults number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return Investigations which contains all the paremeters from listParam
     * @throws ParameterSearchException
     */
    public static Collection<Sample> searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NoParametersException, NullParameterException, ParameterNoExistsException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, startIndex, numberResults, manager);
    }

    /**
     * Search by parameters where the investigation contains every parameter defined
     * in listParam.
     *
     * @param userId federalId of the user.
     * @param listParam List of parameters
     * @param manager manager object that will facilitate interaction with underlying database
     * @return Investigations which contains all the paremeters from listParam
     * @throws ParameterSearchException
     */
    public static Collection<Sample> searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws DatevalueFormatException, NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NoParametersException, NullParameterException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, -1, 1, manager);
    }
}
