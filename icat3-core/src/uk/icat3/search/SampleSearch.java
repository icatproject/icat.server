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
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoStringComparatorException;
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
import uk.icat3.manager.ParameterManager;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ExtractedJPQL;
import uk.icat3.search.parameter.util.ParameterSearchUtilSingleton;
import uk.icat3.search.parameter.util.ParameterValued;
import uk.icat3.util.ElementType;

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
    private static Collection<Sample> searchByParameter(String userId, ExtractedJPQL ejpql, int startIndex, int numberResults, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");

            // Make sure the parameter are searchable before continue.
            ParameterManager.existsSearchableParameters(ejpql.getSampleParameter().values(), ParameterType.SAMPLE, manager);

            String jpql = RETURN_ALL_SAMPLES_JPQL + ", " + ejpql.getParametersJPQL(ElementType.SAMPLE) + QUERY_USERS_SAMPLES_JPQL + " AND " + ejpql.getCondition();
            Query q = manager.createQuery(jpql);
            for (Entry<String, Object> e : ejpql.getAllJPQLParameter().entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }
            q.setParameter("objectType", ElementType.INVESTIGATION);
            q.setParameter("userId", userId);
            if (numberResults < 0) {
                return q.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            } else {
                return q.setMaxResults(numberResults).setFirstResult(startIndex).getResultList();
            }
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
     public static Collection<Sample> searchByParameterListComparators(String userId, List<ParameterComparisonCondition> listComparators, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoStringComparatorException, NoNumericComparatorException, NullParameterException {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators);

        return searchByParameter(userId, ejpql, startIndex, numberResults, manager);
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
     public static Collection<Sample> searchByParameterOperable(String userId, ParameterCondition parameterOperable, int startIndex, int numberResults, EntityManager manager) throws EmptyOperatorException, NoSearchableParameterException, NullParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, NoParametersException, ParameterNoExistsException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable);

        return searchByParameter(userId, ejpql, startIndex, numberResults, manager);
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
    public static Collection<Sample> searchByParameterOperable(String userId, ParameterCondition parameterOperable, EntityManager manager) throws ParameterSearchException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable);

        return searchByParameter(userId, ejpql, -1, -1, manager);
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
    public static Collection<Sample> searchByParameterListParameter(String userId, List<ParameterValued> listParam, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NoParametersException, NullParameterException, ParameterNoExistsException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam);

        return searchByParameter(userId, ejpql, startIndex, numberResults, manager);
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
    public static Collection<Sample> searchByParameterListParameter(String userId, List<ParameterValued> listParam, EntityManager manager) throws ParameterSearchException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam);

        return searchByParameter(userId, ejpql, -1, 1, manager);
    }
}
