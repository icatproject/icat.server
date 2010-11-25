/*
 * DatafileSearch.java
 *
 * Created on 22 February 2007, 08:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.RestrictionINException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoElementTypeException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.util.RestrictionUtil;
import uk.icat3.search.parameter.util.ExtractedJPQL;
import uk.icat3.search.parameter.util.ParameterSearchUtilSingleton;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.ElementType;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatafileInclude;
import static uk.icat3.util.Queries.*;
/**
 * Searchs on the datafiles for run number on the datafile parameter table.
 *
 * @author gjd37
 */
public class DatafileSearch {

    // Global class logger
    static Logger log = Logger.getLogger(DatafileSearch.class);

    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param userId federalId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of datafiles returned from search
     */
    private static Collection<Datafile> searchByRunNumberImpl(String userId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results, EntityManager manager) {
        if (instruments == null || instruments.isEmpty()) {
            throw new IllegalArgumentException("Instrument collection cannot be null or empty");
        }
        log.trace("searchByRunNumber(" + userId + ", " + instruments + ", " + startRun + ", " + endRun + ", "+ startIndex +", "+ number_results+", EntityManager)");
                

        Collection<Datafile> datafiles = null;

        //dynamically create the JPQL
        String JPQL = DATAFILE_BY_INSTRUMENT_AND_RUN_NUMBER_JPQL_START;

        //add in the instruments,  AND i.dataset.investigation.instrument.name IN ('SXD') AND
        int i = 1;
        JPQL += " AND i.dataset.investigation.instrument IN (";
        for (String instrument : instruments) {
            if (i == instruments.size()) {
                JPQL += ":instrument" + (i++);
            } else {
                JPQL += ":instrument" + (i++) + ", ";
            }
        }
        JPQL += ") AND " + DATAFILE_BY_INSTRUMENT_AND_RUN_NUMBER_JPQL_END;

        log.trace("DYNAMIC JPQL: " + JPQL);
        
        //set query with datafile as entity object
        Query query = manager.createQuery(JPQL);

        //sets the paramters
        query = query.setParameter("userId", userId);
        query = query.setParameter("lower", startRun);
        query = query.setParameter("upper", endRun);
        query = query.setParameter("objectType", ElementType.DATASET);

        //set instruments
        int j = 1;
        for (String instrument : instruments) {
            query = query.setParameter("instrument" + j++, instrument);
        }
                
        if (number_results < 0) {
            //get all, maybe should limit this to 500?
            datafiles = query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            datafiles = query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }

        log.debug("number of datefiles found: "+datafiles.size()+" : "+datafiles);
        //now get the role to add to the file
        for (Datafile file : datafiles) {
            try {
                GateKeeper.performAuthorisation(userId, file, AccessType.READ, manager);
            } catch(Exception ignore){
                log.fatal("A datafile "+file+" was returned from a search that had no read access for user "+userId, ignore);
            }
        }

        return datafiles;
    }

    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param userId federalId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of datafiles returned from search
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, float startRun, float endRun, EntityManager manager) {
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, -1, -1, manager);
    }

    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param userId federalId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of datafiles returned from search
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results, EntityManager manager) {
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, startIndex, number_results, manager);
    }

    /**
     *  List all the valid avaliable formats for datafiles
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of types
     */
    public static Collection<DatafileFormat> listDatafileFormats(EntityManager manager) {
        log.trace("listDatafileFormats(EntityManager)");

        return manager.createNamedQuery(ALL_DATAFILE_FORMAT).getResultList();
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
    private static Collection searchByParameterImpl(String userId, ExtractedJPQL ejpql, RestrictionUtil restricion, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws  NoSearchableParameterException, ParameterNoExistsException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");
            // Return type
            String returnJPQL = RETURN_ALL_DATAFILES_JPQL;
            // Return ids
            if (include == DatafileInclude.ALL_DATAFILE_ID) {
                returnJPQL = RETURN_ALL_DATAFILES_ID_JPQL;
                numberResults = NO_LIMITED_RESULTS;
            }
            // Check for restriction parameters
            String restrictionParam = "";
            if (restricion.isContainSampleAttributes() && ejpql.getSampleParameter().isEmpty())
                restrictionParam += ", IN(i.investigation.sampleCollection) " + SAMPLE_NAME;
            // Construction JPQL sentence
            String jpql =  returnJPQL + restrictionParam + ", " + ejpql.getParametersJPQL(ElementType.DATAFILE) + QUERY_USERS_DATAFILES_JPQL + " AND " + ejpql.getCondition();
            if (!restricion.isEmpty())
                jpql += " AND " + restricion.getSentenceJPQL();
            // Create Query
            Query q = manager.createQuery(jpql);
            // Set JPQL parameters
            for (Entry<String, Object> e : ejpql.getAllJPQLParameter().entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }
            q.setParameter("objectType", ElementType.DATASET);
            q.setParameter("userId", userId);
            // Object returns and check number of results
            Collection res = ManagerUtil.getResultList (q, startIndex, numberResults);
            // Return is a Collection of Long
            if (include == DatafileInclude.ALL_DATAFILE_ID)
                return res;
            // Check if the datafile should include other objects
            ManagerUtil.getDatafileInformation (res, include);
            // Return results
            return res;
            
        } catch (NoElementTypeException ex) {
            java.util.logging.Logger.getLogger(DatafileSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ArrayList ();
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
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoNumericComparatorException
     * @throws NoStringComparatorException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     * @throws NoDatetimeComparatorException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoNumericComparatorException, NoStringComparatorException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException  {
        
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

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
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoNumericComparatorException
     * @throws NoStringComparatorException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     * @throws NoDatetimeComparatorException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoNumericComparatorException, NoStringComparatorException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException  {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

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
      * @see ParameterCondition
      * @throws NoParametersException
      * @throws EmptyOperatorException
      * @throws NullParameterException
      * @throws NoSearchableParameterException
      * @throws NoStringComparatorException
      * @throws NoNumericComparatorException
      * @throws NoParameterTypeException
      * @throws ParameterNoExistsException
      * @throws NoDatetimeComparatorException
      * @throws DatevalueException
      * @throws NumericvalueException
      * @throws DatevalueFormatException
      */
     public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParametersException, EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

        return searchByParameterImpl(userId, ejpql, restric, include, startIndex, numberResults, manager);
    }

     /**
      * Search by parameters from a parameterOperable.
      *
      * @param userId federalId of the user.
      * @param parameterOperable ParameterCondition where the conditions are defined
      * @param manager Object that will facilitate interaction with underlying database
      * @return
      * @throws EmptyOperatorException
      * @throws NullParameterException
      * @throws NoSearchableParameterException
      * @throws ParameterNoExistsException
      * @throws NoParametersException
      * @throws NoParameterTypeException
      * @throws NoStringComparatorException
      * @throws NoNumericComparatorException
      * @throws NoDatetimeComparatorException
      * @throws DatevalueException
      * @throws NumericvalueException
      * @throws DatevalueFormatException
      */
    public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, ParameterNoExistsException, NoParametersException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

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
     * * @throws NoParameterTypeException
     * @throws EmptyListParameterException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     */
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);
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
     * @throws NoParameterTypeException
     * @throws EmptyListParameterException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     */
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, RestrictionEmptyListException, RestrictionOperatorException, RestrictionINException, RestrictionNullException, DatevalueException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

        return searchByParameterImpl(userId, ejpql, restric, include, -1, 1, manager);
    }
}
