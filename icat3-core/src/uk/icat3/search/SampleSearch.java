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
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionException;
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
import uk.icat3.exceptions.OperatorINException;
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
     * This method is the implementation for all restriction searchs, and
     * return samples which match with restriction.
     *
     * @param userId User identification
     * @param restrUtil Restriction util
     * @param include Include options
     * @param startIndex Start index
     * @param numberResults Number of results to return
     * @param manager Entity manager to database
     *
     * @return Collection of samples which match restriction condition
     */
    private static Collection searchByRestrictionImpl (String userId, RestrictionUtil restrUtil, SampleInclude include, int startIndex, int numberResults, EntityManager manager){
        log.trace("searchByRestrictionImpl(" + ", restrCond, " + startIndex + ", " + numberResults + ", EntityManager)");
        // Check if there exists include options defined inside restrictions
        if (restrUtil.hasInclude()) {
            include = (SampleInclude) restrUtil.getInclude();
        }
        // Return type
        String returnJPQL = RETURN_ALL_SAMPLES_JPQL;
        // Return ids
//        if (include == SampleInclude.ALL_SAMPLE_ID) {
        if (restrUtil.isReturnLongId()) {
            returnJPQL = RETURN_ALL_SAMPLE_ID_JPQL;
            numberResults = NO_LIMITED_RESULTS;
        }
        // Get ejpql parameters
        String restrictionParam = restrUtil.getParameterJPQL(ElementType.SAMPLE);
        // Construction JPQL sentence
        String jpql = returnJPQL
                + restrictionParam
                + QUERY_USERS_SAMPLES_JPQL;
        // Object returns and check number of results
        Collection res = ManagerUtil.getResultList(jpql, restrUtil
                , ElementType.INVESTIGATION, userId, startIndex, numberResults
                , manager);
        // Return results
        return res;
    }
    /**
     * Search sample which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param manager Entity manager to database
     *
     * @return Collection of samples which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, EntityManager manager) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException, EmptyOperatorException, RestrictionEmptyListException, CyclicException, RestrictionException {
        log.trace("searchByRestriction( restrCond , EntityManager)");
        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.SAMPLE);
        return searchByRestrictionImpl(userId, restric, SampleInclude.NONE, NO_PAGINATION, NO_PAGINATION, manager);
    }
    /**
     * Search sample which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param manager Entity manager to database
     *
     * @return Collection of samples which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, SampleInclude include, EntityManager manager) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException, EmptyOperatorException, RestrictionEmptyListException, RestrictionException, CyclicException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.SAMPLE);
        return searchByRestrictionImpl(userId, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
    }
    /**
     * Search sample which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param startIndex Start index of results
     * @param numberResults Number of results
     * @param manager Entity manager to database
     *
     * @return Collection of samples which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws DatevalueException, RestrictionOperatorException, OperatorINException, EmptyOperatorException, RestrictionException, RestrictionNullException, RestrictionEmptyListException, CyclicException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.SAMPLE);
        return searchByRestrictionImpl(userId, restric, include, startIndex, numberResults, manager);
    }

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
    private static Collection searchByParameterImpl(String userId, ExtractedJPQL ejpql, RestrictionUtil restricion, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");
            // Check if there exists include options defined inside restrictions
            if (restricion.hasInclude()) {
                include = (SampleInclude) restricion.getInclude();
            }
            // Return type
            String returnJPQL = RETURN_ALL_SAMPLES_JPQL;
            // Return ids
//            if (include == SampleInclude.ALL_SAMPLE_ID) {
            if (restricion.isReturnLongId()) {
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
            // Add Investigator parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.SAMPLE, ElementType.INVESTIGATOR);
            // Add Keyword parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.SAMPLE, ElementType.KEYWORD);
            
            // Construction JPQL sentence
            String jpql = returnJPQL + restrictionParam + ", " + ejpql.getParametersJPQL(ElementType.SAMPLE)
                    + QUERY_USERS_SAMPLES_JPQL;
            // Object returns and check number of results
            Collection res = ManagerUtil.getResultList(jpql, ejpql, restricion
                    , ElementType.INVESTIGATION, userId, startIndex, numberResults
                    , manager);
            // Return results
            return res;
        } catch (NoElementTypeException ex) {
            log.error(ex);
        }

        return new ArrayList();
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
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, RestrictionException, EmptyOperatorException, NoParameterTypeException, ParameterNoExistsException, CyclicException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NullParameterException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException   {

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
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, RestrictionException, EmptyOperatorException, ParameterNoExistsException, NoSearchableParameterException, NoParametersException, CyclicException, NoStringComparatorException, NoNumericComparatorException, NullParameterException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException   {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
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
     public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyOperatorException, NoSearchableParameterException, RestrictionException, NullParameterException, NoStringComparatorException, CyclicException, NoNumericComparatorException, NoParameterTypeException, NoParametersException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException   {
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
    public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws DatevalueFormatException, NoParameterTypeException, EmptyListParameterException, RestrictionException, NoSearchableParameterException, NoParametersException, NullParameterException, CyclicException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException, EmptyOperatorException, NoStringComparatorException, NoNumericComparatorException, NoDatetimeComparatorException, NumericvalueException    {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
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
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restCond, SampleInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, RestrictionException, EmptyOperatorException, NoSearchableParameterException, NoParametersException, NullParameterException, CyclicException, ParameterNoExistsException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException {
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
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restCond, SampleInclude include, EntityManager manager) throws DatevalueFormatException, NoParameterTypeException, EmptyListParameterException, EmptyOperatorException, RestrictionException, NoSearchableParameterException, NoParametersException, NullParameterException, CyclicException, ParameterNoExistsException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restCond, RestrictionType.SAMPLE);
        return searchByParameterImpl(userId, ejpql, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
    }
}
