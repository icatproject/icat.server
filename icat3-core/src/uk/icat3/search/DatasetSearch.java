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
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.CyclicException;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.DatevalueFormatException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoDatetimeComparatorException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NumericvalueException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.exceptions.RestrictionEmptyListException;
import uk.icat3.exceptions.OperatorINException;
import uk.icat3.exceptions.RestrictionNullException;
import uk.icat3.exceptions.RestrictionOperatorException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.exceptions.EmptyListParameterException;
import uk.icat3.exceptions.EmptyOperatorException;
import uk.icat3.exceptions.NoElementTypeException;
import uk.icat3.exceptions.NoNumericComparatorException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoParametersException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.NoStringComparatorException;
import uk.icat3.exceptions.NullParameterException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionType;
import uk.icat3.restriction.util.RestrictionUtil;
import uk.icat3.search.parameter.util.ExtractedJPQL;
import uk.icat3.search.parameter.util.ParameterSearchUtilSingleton;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import static uk.icat3.util.Queries.*;
/**
 * Searchs on the datasets for samples and list types and status' of datasets.
 *
 * @author gjd37
 */
public class DatasetSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(DatasetSearch.class);
    
    /**
     * From a sample name, return all the samples a user can view asscoiated with the sample name
     *
     * @param userId federalId of the user.
     * @param sampleName sample name wiching to search on
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of {@link Sample}s returned from search
     */
    public static Collection<Sample> getSamplesBySampleName(String userId, String sampleName, EntityManager manager) {
        log.trace("getSamplesBySampleName("+userId+", "+sampleName+", EntityManager)");
        
        //get the sample id from sample name
        Collection<Sample> samples = (Collection<Sample>)manager.createNamedQuery(SAMPLES_BY_NAME).
                //setParameter("objectType", ElementType.INVESTIGATION).
                //setParameter("userId", userId).
                setParameter("name", "%"+sampleName+"%").getResultList();
        
        //now see which investigations they can see from these samples.
        Collection<Sample> samplesPermssion = new ArrayList<Sample>();
        
        //check have permission
        for(Sample sample : samples){
            try{
                //check read permission
                GateKeeper.performAuthorisation(userId, sample, AccessType.READ, manager);
                
                //add dataset to list returned to user
                log.trace("Adding "+ sample+" to returned list");
                samplesPermssion.add(sample);
                
            } catch(InsufficientPrivilegesException ignore){
                //user does not have read access to these to dont add
            }
        }
        
        return samplesPermssion;
    }
    
    /**
     * From a sample, return all the datasets a user can view asscoiated with the sample
     *
     * @param userId federalId of the user.
     * @param sample sample object
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Dataset} objects
     */
    public static Collection<Dataset> getDatasetsBySample(String userId, Sample sample, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("getDatasetsBySample("+userId+", "+sample+", EntityManager)");
        
        //get the sample id from sample name
        Sample sampleFound = ManagerUtil.find(Sample.class, sample.getId(), manager);
        
        //check read permission
        GateKeeper.performAuthorisation(userId, sampleFound, AccessType.READ, manager);
        
        Investigation investigation = sampleFound.getInvestigationId();
        
        Collection<Dataset> datasets = investigation.getDatasetCollection();
        Collection<Dataset> datasetsPermission = new ArrayList<Dataset>();
        
        for (Dataset dataset : datasets) {
            if(!dataset.isDeleted() && sampleFound.getId().equals(dataset.getSampleId())){
                //check read permission
                try{
                    GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
                    datasetsPermission.add(dataset);
                    log.trace("Adding "+ dataset+" to returned list");
                    
                } catch(InsufficientPrivilegesException ignore){}
            }
        }
        
        //need to filter out datafiles
        ManagerUtil.getDatasetInformation(userId, datasetsPermission,DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS, manager);
        
        return datasetsPermission;
    }
    
    /**
     *
     *
     * @param userId
     * @param manager
     * @return
     */
    public static Collection<Dataset> listMyDeletedDataSets(String userId, EntityManager manager){
        log.trace("listAllDeletedDataSets(EntityManager)");
        
        return manager.createNamedQuery(LIST_MY_DELETED_DATASETS).setMaxResults(MAX_QUERY_RESULTSET).getResultList();
    }
    
    /**
     *  List all the valid avaliable types for datasets
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of types
     */
    public static Collection<String> listDatasetTypes(EntityManager manager) {
        log.trace("listDatasetTypes(EntityManager)");
        
        return manager.createNamedQuery(ALL_DATASET_TYPE).getResultList();
    }
    
    /**
     * List all the valid avaliable status for datasets
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of status
     */
    public static Collection<String> listDatasetStatus(EntityManager manager) {
        log.trace("listDatasetStatus(EntityManager)");
        
        return manager.createNamedQuery(ALL_DATASET_STATUS).getResultList();
    }

    /**
     * This method returns list of datasets that match the dataset name.
     * @param userId      : user id performing the search
     * @param datasetName : input dataset name that is being searched for
     * @param manager
     * @return : list of datasets that match the datasetname.
     */
    public static Collection<Dataset> getDatasetsByName(String userId, String datasetName, EntityManager manager) {
        //Get the list of datasets that match the dataset name
        Collection<Dataset> datasets = (Collection<Dataset>) manager.createNamedQuery(DATASET_FINDBY_NAME_NOTDELETED).setParameter("name", datasetName).getResultList();


        Collection<Dataset> datasetsPermission = new ArrayList<Dataset>();
        //Perform the permission checks
        for (Dataset dataset : datasets) {
            try {
                //check read permission
                GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);

                //add dataset to list returned to user
                log.trace("Adding " + dataset + " to returned list");
                datasetsPermission.add(dataset);

            } catch (InsufficientPrivilegesException ignore) {
                //user does not have read access to these to dont add
            }
        }
        return datasetsPermission;
    }

    /**
     * This method is the implementation for all restriction searchs, and
     * return datasets which match with restriction.
     *
     * @param userId User identification
     * @param restrUtil Restriction util
     * @param include Include options
     * @param startIndex Start index
     * @param numberResults Number of results to return
     * @param manager Entity manager to database
     *
     * @return Collection of datasets which match restriction condition
     */
    private static Collection searchByRestrictionImpl (String userId, RestrictionUtil restrUtil, DatasetInclude include, int startIndex, int numberResults, EntityManager manager){
        log.trace("searchByRestrictionImpl(" + ", restrCond, " + startIndex + ", " + numberResults + ", EntityManager)");
        // Check if there exists include options defined inside restrictions
        if (restrUtil.hasInclude()) {
            include = (DatasetInclude) restrUtil.getInclude();
        }
        // Return type
        String returnJPQL = RETURN_ALL_DATASETS_JPQL;
        // Return ids
//        if (include == DatasetInclude.ALL_DATASET_ID) {
        if (restrUtil.isReturnLongId()) {
            returnJPQL = RETURN_ALL_DATASETS_ID_JPQL;
            numberResults = NO_LIMITED_RESULTS;
        }
        String restrictionParam = restrUtil.getParameterJPQL(ElementType.DATASET);
        // Construction JPQL sentence
        String jpql = returnJPQL
                + restrictionParam
                + QUERY_USERS_DATASETS_JPQL;
        // Object returns and check number of results
        Collection res = ManagerUtil.getResultList(jpql, restrUtil
                , ElementType.DATASET, userId, startIndex, numberResults
                , manager);
        // Return type is a Collection of Long
//        if (include == DatasetInclude.ALL_DATASET_ID)
        if (restrUtil.isReturnLongId())
            return res;
        // Check if the dataset should include other objects (Datafiles, Parameters)
        ManagerUtil.getDatasetInformation(userId, res, include, manager);
        // Return results
        return res;
    }
    /**
     * Search dataset which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param manager Entity manager to database
     *
     * @return Collection of datasets which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, EntityManager manager) throws DatevalueException, CyclicException, RestrictionException, RestrictionOperatorException, OperatorINException, EmptyOperatorException, RestrictionNullException, RestrictionEmptyListException {
        log.trace("searchByRestriction( restrCond , EntityManager)");
        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATASET);
        return searchByRestrictionImpl(userId, restric, DatasetInclude.NONE, NO_PAGINATION, NO_PAGINATION, manager);
    }
    /**
     * Search dataset which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param manager Entity manager to database
     *
     * @return Collection of datasets which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, DatasetInclude include, EntityManager manager) throws DatevalueException, RestrictionException, RestrictionOperatorException, OperatorINException, EmptyOperatorException, CyclicException, RestrictionNullException, RestrictionEmptyListException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATASET);
        return searchByRestrictionImpl(userId, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
    }
    /**
     * Search dataset which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @param include Include options
     * @param startIndex Start index of results
     * @param numberResults Number of results
     * @param manager Entity manager to database
     *
     * @return Collection of datasets which match restriction condition
     *
     * @throws DatevalueException
     * @throws RestrictionOperatorException
     * @throws OperatorINException
     * @throws RestrictionNullException
     * @throws RestrictionEmptyListException
     */
    public static Collection searchByRestriction (String userId, RestrictionCondition restriction, DatasetInclude include, int startIndex, int numberResults, EntityManager manager) throws DatevalueException, EmptyOperatorException, RestrictionException, RestrictionOperatorException, CyclicException, OperatorINException, RestrictionNullException, RestrictionEmptyListException {
        log.trace("searchByRestriction( restrCond , EntityManager)");

        RestrictionUtil restric = new RestrictionUtil(restriction, RestrictionType.DATASET);
        return searchByRestrictionImpl(userId, restric, include, startIndex, numberResults, manager);
    }

    /**
     * Search the datasets from parameter selection.
     *
     * @param userId User identification
     * @param ejpql Parameter information container
     * @param startIndex Start index for results
     * @param numberResults Number of results to return
     * @param manager Entity manager to access the database
     * @return Dataset collection matched.
     * @throws ParameterNoExistsException
     * @throws NoSearchableParameterException
     * @throws NoParametersException
     * @throws NoParameterTypeException
     */
    private static Collection searchByParameterImpl(String userId, ExtractedJPQL ejpql, RestrictionUtil restricion, DatasetInclude include, int startIndex, int numberResults, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");
            // Check if there exists include options defined inside restrictions
            if (restricion.hasInclude()) {
                include = (DatasetInclude) restricion.getInclude();
            }
            // Return type
            String returnJPQL = RETURN_ALL_DATASETS_JPQL;
            // Return ids
//            if (include == DatasetInclude.ALL_DATASET_ID) {
            if (restricion.isReturnLongId()) {
                returnJPQL = RETURN_ALL_DATASETS_ID_JPQL;
                numberResults = NO_LIMITED_RESULTS;
            }
            // Check for restriction parameters
            String restrictionParam = "";
            // Check if the parameter are defined in parameter search
            if (ejpql.getDatafileParameter().isEmpty())
                restrictionParam += restricion.getParameterJPQL(ElementType.DATASET, ElementType.DATAFILE);
            if (ejpql.getSampleParameter().isEmpty())
                restrictionParam += restricion.getParameterJPQL(ElementType.DATASET, ElementType.SAMPLE);
            // Add investigator parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.DATASET, ElementType.INVESTIGATOR);
            // Add keyword parameter if exists
            restrictionParam += restricion.getParameterJPQL(ElementType.DATASET, ElementType.KEYWORD);
            // Construction JPQL sentence
            String jpql = returnJPQL + restrictionParam + ", " + ejpql.getParametersJPQL(ElementType.DATASET)
                    + QUERY_USERS_DATASETS_JPQL;
            // Object returns and check number of results
            Collection res = ManagerUtil.getResultList(jpql, ejpql, restricion
                    , ElementType.DATASET, userId, startIndex, numberResults
                    , manager);
            log.trace("Got " + res.size() + " results"+ (res.isEmpty()? "":" of type " +res.iterator().next().getClass()));
            
            // Return type is a Collection of Long
            if (restricion.isReturnLongId()) {
                return res;
            }
            
            // Check if the dataset should include other objects (Datafiles, Parameters)
            ManagerUtil.getDatasetInformation(userId, res, include, manager);
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
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     * @throws NoDatetimeComparatorException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatasetInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, CyclicException, EmptyOperatorException, NoParameterTypeException, NoStringComparatorException, RestrictionException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException {

        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLComparators (listComparators, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATASET);

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
     * @throws NoStringComparatorException
     * @throws NoNumericComparatorException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     * @throws NoDatetimeComparatorException
     * @throws DatevalueException
     * @throws NumericvalueException
     * @throws DatevalueFormatException
     */
     public static Collection searchByParameterComparisonList(String userId, List<ParameterComparisonCondition> listComparators, RestrictionCondition restrCond, DatasetInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoStringComparatorException, EmptyOperatorException, NoNumericComparatorException, NoSearchableParameterException, RestrictionException, CyclicException, NullParameterException, ParameterNoExistsException, NoParametersException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException {
        return searchByParameterComparisonList(userId, listComparators, restrCond, include, NO_PAGINATION, NO_PAGINATION, manager);
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
      * @throws NoParameterTypeException
      * @throws NoStringComparatorException
      * @throws NoNumericComparatorException
      * @throws NoSearchableParameterException
      * @throws NullParameterException
      * @throws EmptyOperatorException
      * @throws NoParametersException
      * @throws ParameterNoExistsException
      * @throws NoDatetimeComparatorException
      * @throws DatevalueException
      * @throws NumericvalueException
      * @throws DatevalueFormatException
      * @see ParameterCondition
      */
     public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatasetInclude include, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, CyclicException, NullParameterException, RestrictionException, EmptyOperatorException, NoParametersException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATASET);

        return searchByParameterImpl(userId, ejpql, restric, include, startIndex, numberResults, manager);
    }

     /**
      * Search by parameters from a parameterOperable.
      *
      * @param userId federalId of the user.
      * @param parameterOperable ParameterCondition where the conditions are defined
      * @param manager Object that will facilitate interaction with underlying database
      * @return
      * @throws EmptyListParameterException
      * @throws NoParameterTypeException
      * @throws NoStringComparatorException
      * @throws NoNumericComparatorException
      * @throws NoSearchableParameterException
      * @throws NullParameterException
      * @throws EmptyOperatorException
      * @throws NoParametersException
      * @throws ParameterNoExistsException
      * @throws NoDatetimeComparatorException
      * @throws DatevalueException
      * @throws NumericvalueException
      * @throws DatevalueFormatException
      */
    public static Collection searchByParameterCondition(String userId, ParameterCondition parameterOperable, RestrictionCondition restrCond, DatasetInclude include, EntityManager manager) throws NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, EmptyOperatorException, CyclicException, RestrictionException, NoParametersException, ParameterNoExistsException, NoDatetimeComparatorException, DatevalueException, NumericvalueException, DatevalueFormatException, RestrictionEmptyListException, RestrictionOperatorException, OperatorINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLOperable(parameterOperable, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATASET);
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
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     */
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatasetInclude include, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoSearchableParameterException, EmptyOperatorException, NullParameterException, ParameterNoExistsException, RestrictionException, CyclicException, NoParametersException, RestrictionEmptyListException, DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATASET);
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
     * @throws EmptyListParameterException
     * @throws NoParameterTypeException
     * @throws NoSearchableParameterException
     * @throws NullParameterException
     * @throws ParameterNoExistsException
     * @throws NoParametersException
     */
    public static Collection searchByParameterList(String userId, List<ParameterSearch> listParam, RestrictionCondition restrCond, DatasetInclude include, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, EmptyOperatorException, NoParametersException, CyclicException, RestrictionException, RestrictionEmptyListException, DatevalueException, RestrictionOperatorException, OperatorINException, RestrictionNullException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam, manager);
        RestrictionUtil restric = new RestrictionUtil(restrCond, RestrictionType.DATASET);
        
        return searchByParameterImpl(userId, ejpql, restric, include, NO_PAGINATION, NO_PAGINATION, manager);
    }
    
}
