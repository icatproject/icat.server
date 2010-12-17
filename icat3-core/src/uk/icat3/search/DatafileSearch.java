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
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.OperatorINException;
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
import uk.icat3.exceptions.RestrictionException;
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
     * This method is the implementation for all restriction searchs, and
     * return datafiles which match with restriction.
     *
     * @param userId User identification
     * @param restrUtil Restriction util
     * @param include Include options
     * @param startIndex Start index
     * @param numberResults Number of results to return
     * @param manager Entity manager to database
     *
     * @return Collection of datafiles which match restriction condition
     */
    private static Collection searchByRestrictionImpl (String userId, RestrictionUtil restrUtil, DatafileInclude include, int startIndex, int numberResults, EntityManager manager){
        log.trace("searchByRestrictionImpl(" + ", restrCond, " + startIndex + ", " + numberResults + ", EntityManager)");
        // Check if there exists include options defined inside restrictions
        if (restrUtil.hasInclude()) {
            include = (DatafileInclude) restrUtil.getInclude();
        }
        // Return type
        String returnJPQL = RETURN_ALL_DATAFILES_JPQL;
        // Return ids
//        if (include == DatafileInclude.ALL_DATAFILE_ID) {
        if (restrUtil.isReturnLongId()) {
            returnJPQL = RETURN_ALL_DATAFILES_ID_JPQL;
            numberResults = NO_LIMITED_RESULTS;
        }
        String restrictionParam = restrUtil.getParameterJPQL(ElementType.DATAFILE);
        // Construction JPQL sentence
        String jpql = returnJPQL
                + restrictionParam
                + QUERY_USERS_DATAFILES_JPQL;
        // Object returns and check number of results
        Collection res = ManagerUtil.getResultList(jpql, restrUtil
                , ElementType.DATASET, userId, startIndex, numberResults
                , manager);
        // Return type is a Collection of Long
        if (restrUtil.isReturnLongId())
//        if (include == DatafileInclude.ALL_DATAFILE_ID)
            return res;
        // Check if the dataset should include other objects (Datafiles, Parameters)
        ManagerUtil.getDatafileInformation(res, include);
        // Return results
        return res;
    }
    /**
     * Search datafile which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param manager Entity manager to database
     *
     * @return Collection of datafiles which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, EntityManager manager) throws DatevalueException, RestrictionOperatorException, RestrictionException, OperatorINException, RestrictionNullException, RestrictionEmptyListException, CyclicException, EmptyOperatorException {
        log.trace("searchByRestriction( restrCond , EntityManager)");
        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATAFILE);
        return searchByRestrictionImpl(userId, restric, DatafileInclude.NONE, NO_PAGINATION, NO_LIMITED_RESULTS, manager);
    }
    /**
     * Search datafile which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param manager Entity manager to database
     *
     * @return Collection of datafiles which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, DatafileInclude include, EntityManager manager) throws DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionException, RestrictionNullException, RestrictionEmptyListException, EmptyOperatorException, CyclicException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATAFILE);
        return searchByRestrictionImpl(userId, restric, include, NO_PAGINATION, NO_LIMITED_RESULTS, manager);
    }
    /**
     * Search datafile which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param startIndex Start index of results
     * @param numberResults Number of results
     * @param manager Entity manager to database
     *
     * @return Collection of datafiles which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws DatevalueException, RestrictionException, RestrictionOperatorException, OperatorINException, RestrictionNullException, EmptyOperatorException, RestrictionEmptyListException, CyclicException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATAFILE);
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
    private static Collection searchByParameterImpl(String userId, ExtractedJPQL ejpql, RestrictionUtil restricion, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws  NoSearchableParameterException, ParameterNoExistsException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");
            // Check if there exists include options defined inside restrictions
            if (restricion.hasInclude()) {
                include = (DatafileInclude) restricion.getInclude();
            }
            // Return type
            String returnJPQL = RETURN_ALL_DATAFILES_JPQL;
            // Return ids
//            if (include == DatafileInclude.ALL_DATAFILE_ID) {
            if (restricion.isReturnLongId()) {
                returnJPQL = RETURN_ALL_DATAFILES_ID_JPQL;
                numberResults = NO_LIMITED_RESULTS;
            }
            // Check for restriction parameters
            String restrictionParam = "";
            if (ejpql.getSampleParameter().isEmpty())
                restrictionParam += restricion.getParameterJPQL(ElementType.DATAFILE, ElementType.SAMPLE);
            // Add investigator parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.DATAFILE, ElementType.INVESTIGATOR);
            // Add Keyword parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.DATAFILE, ElementType.KEYWORD);
            // Construction JPQL sentence
            String jpql =  returnJPQL + restrictionParam + ", " + ejpql.getParametersJPQL(ElementType.DATAFILE)
                    + QUERY_USERS_DATAFILES_JPQL;
            // Object returns and check number of results
            Collection res = ManagerUtil.getResultList(jpql, ejpql, restricion
                    , ElementType.DATASET, userId, startIndex, numberResults
                    , manager);
            // Return is a Collection of Long
//            if (include == DatafileInclude.ALL_DATAFILE_ID)
            if (restricion.isReturnLongId())
                return res;
            // Check if the datafile should include other objects
            ManagerUtil.getDatafileInformation (res, include);
            // Return results
            return res;
            
        } catch (NoElementTypeException ex) {
            log.error(ex);
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
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, EmptyOperatorException, NoNumericComparatorException, NoStringComparatorException, RestrictionException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, CyclicException  {
        
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
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoNumericComparatorException, EmptyOperatorException, NoStringComparatorException, NoSearchableParameterException, NullParameterException, RestrictionException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, CyclicException  {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

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
     public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParametersException, EmptyOperatorException, NullParameterException, NoSearchableParameterException, NoStringComparatorException, NoNumericComparatorException, NoParameterTypeException, RestrictionException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, CyclicException {
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
    public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws EmptyOperatorException, NullParameterException, NoSearchableParameterException, ParameterNoExistsException, NoParametersException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, RestrictionException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, CyclicException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

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
     * * @throws NoParameterTypeException
     * @throws EmptyListParameterException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     */
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatafileInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, EmptyOperatorException, NullParameterException, ParameterNoExistsException, NoParametersException, RestrictionException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException, CyclicException {
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
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatafileInclude include, EntityManager manager) throws NoParameterTypeException, EmptyListParameterException, NoSearchableParameterException, NullParameterException, EmptyOperatorException, ParameterNoExistsException, NoParametersException, RestrictionEmptyListException, RestrictionException, RestrictionOperatorException, OperatorINException, RestrictionNullException, DatevalueException, CyclicException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATAFILE);

        return searchByParameterImpl(userId, ejpql, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
    }
}
