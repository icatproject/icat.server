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
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ParameterNoExistsException;
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
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.manager.ParameterManager;
import uk.icat3.search.parameter.ParameterType;
import uk.icat3.search.parameter.util.ExtractedJPQL;
import uk.icat3.search.parameter.util.ParameterSearchUtilSingleton;
import uk.icat3.search.parameter.util.ParameterValued;
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
    private static Collection<Dataset> searchByParameter(String userId, ExtractedJPQL ejpql, int startIndex, int numberResults, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParametersException, NoParameterTypeException {
        try {
            log.trace("searchByParameter(" + ", " + ejpql.getCondition() + ", " + startIndex + ", " + numberResults + ", EntityManager)");

            // Make sure the parameter are searchable before continue.
            ParameterManager.existsSearchableParameters(ejpql.getDatasetParameter().values(), ParameterType.DATASET, manager);

            String jpql = RETURN_ALL_DATASETS_JPQL + ", " + ejpql.getParametersJPQL(ElementType.DATASET) + QUERY_USERS_DATASETS_JPQL + " AND " + ejpql.getCondition();
            Query q = manager.createQuery(jpql);
            for (Entry<String, Object> e : ejpql.getAllJPQLParameter().entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }
            q.setParameter("objectType", ElementType.DATASET);
            q.setParameter("userId", userId);
            if (numberResults < 0) {
                return q.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
            } else {
                return q.setMaxResults(numberResults).setFirstResult(startIndex).getResultList();
            }
        } catch (NoElementTypeException ex) {
            java.util.logging.Logger.getLogger(DatasetSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Dataset>();
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
     public static Collection<Dataset> searchByParameterListComparators(String userId, List<ParameterComparisonCondition> listComparators, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException {

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
     public static Collection<Dataset> searchByParameterOperable(String userId, ParameterCondition parameterOperable, int startIndex, int numberResults, EntityManager manager) throws NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, EmptyOperatorException, NoParametersException, ParameterNoExistsException {
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
    public static Collection<Dataset> searchByParameterOperable(String userId, ParameterCondition parameterOperable, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoStringComparatorException, NoNumericComparatorException, NoSearchableParameterException, NullParameterException, EmptyOperatorException, NoParametersException, ParameterNoExistsException {
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
    public static Collection<Dataset> searchByParameterListParameter(String userId, List<ParameterValued> listParam, int startIndex, int numberResults, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException {
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
    public static Collection<Dataset> searchByParameterListParameter(String userId, List<ParameterValued> listParam, EntityManager manager) throws EmptyListParameterException, NoParameterTypeException, NoSearchableParameterException, NullParameterException, ParameterNoExistsException, NoParametersException {
        ExtractedJPQL ejpql = ParameterSearchUtilSingleton.getInstance().extractJPQLParameters(listParam);

        return searchByParameter(userId, ejpql, -1, 1, manager);
    }
    
}
