/*
 * ManagerUtil.java
 *
 * Created on 27 February 2007, 15:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;

/**
 *  Class to be extended to allow util methods for EJB3/JPA
 *
 * @author gjd37
 */
public class ManagerUtil {
    
    // Global class logger
    static Logger log = Logger.getLogger(ManagerUtil.class);
    
    /**
     * Goes and collects the information associated with the investigation depending on the InvestigationInclude.
     *  See {@link InvestigationInclude}
     *
     * @param investigations list of investigations
     * @param include The information that is needed to be returned with the investigation
     */
    protected static void getInvestigationInformation(Collection<Investigation> investigations, InvestigationInclude include){
        
        // now collect the information associated with the investigations requested
        if(include.toString().equals(InvestigationInclude.ALL.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information, other wise the collections are null
                
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
                investigation.getDatasetCollection().size();
                investigation.getSampleCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            // return datasets with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_ONLY.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
            }
            // return sample with these investigations
        } else if(include.toString().equals(InvestigationInclude.SAMPLES_ONLY.toString())){
            for(Investigation investigation : investigations){
                investigation.getSampleCollection().size();
            }
            // return datasets and their datafiles with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_AND_DATAFILES.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            // return keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.KEYWORDS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getKeywordCollection().size();
            }
            // return c with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getInvestigatorCollection().size();
            }
            // return investigators and keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_AND_KEYWORDS.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
            }
        } else {
            log.trace("No additional info requested.");
        }
        
        //set the investigation includes in the class
        //This is because of JAXWS, it would down load all of the relationships with out this workaround
        // See in Investigation.getInvestigatorCollection_() method
        for(Investigation investigation : investigations){
            investigation.setInvestigationInclude(include);
            if(include.toString().equals(InvestigationInclude.DATASETS_AND_DATAFILES.toString()) || include.toString().equals(InvestigationInclude.ALL.toString())){
                for(Dataset dataset : investigation.getDatasetCollection()){
                    log.trace("Setting data sets to include: "+DatasetInclude.DATASET_FILES_AND_PARAMETERS);
                    dataset.setDatasetInclude(DatasetInclude.DATASET_FILES_AND_PARAMETERS);
                }
            }
        }
    }
    
    /**
     * Goes and collects the information associated with the dataset depending on the DatasetInclude.
     * See {@link DatasetInclude}
     *
     * @param datasets collection of datasets for gettting more info about them
     * @param include include info
     */
    protected static void getDatasetInformation(Collection<Dataset> datasets, DatasetInclude include){
        
        // now collect the information associated with the investigations requested
        if(include.toString().equals(DatasetInclude.DATASET_FILES_ONLY.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatafileCollection().size();
            }
        } else  if(include.toString().equals(DatasetInclude.DATASET_FILES_AND_PARAMETERS.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatafileCollection().size();
                dataset.getDatasetParameterCollection().size();
            }
        } else  if(include.toString().equals(DatasetInclude.DATASET_PARAMETERS_ONY.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatasetParameterCollection().size();
            }
        }
    }    
    
    /**
     * Checks that the object with primary key exists in the database, if so
     * is returned
     *
     * @param entityClass entity class that you are looking for
     * @param primaryKey primary key of object wanting to find
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @return object if found
     */
    public static <T> T find(Class<T> entityClass, Object primaryKey, EntityManager manager) throws NoSuchObjectFoundException{
        if(primaryKey == null) throw new NoSuchObjectFoundException(entityClass.getSimpleName()+": id: "+primaryKey+" not found.");
        
        T object = manager.find(entityClass, primaryKey);
        
        if(object == null) throw new NoSuchObjectFoundException(entityClass.getSimpleName()+": id: "+primaryKey+" not found.");
        
        log.trace(entityClass.getSimpleName()+": id: "+primaryKey+" exists in the database");
        
        return object;
    }
    
    /**
     * Gets the facilityUserId of the user from the federalId 
     *
     * @param userId federalId of user
     * @param manager manager object that will facilitate interaction with underlying database   
     * @return facilityUserId
     */
    public static String getFacilityUserId(String userId, EntityManager manager) {
        FacilityUser facilityUser = null;
        try {
            facilityUser = (FacilityUser) manager.createQuery("SELECT f FROM FacilityUser f where f.federalId = :fedId").setParameter("fedId", userId).getSingleResult();
            log.trace(""+facilityUser.getFacilityUserId()+" corresponds to "+userId);
            return facilityUser.getFacilityUserId();
        } catch(NoResultException nre) {
            log.warn("federalId:" +userId+" has no associated facility user");
            throw new RuntimeException("FederalId:" +userId+" has no associated facility user in DB.");
        } catch(NonUniqueResultException nonue){
            log.warn("federalId:" +userId+" has more than one associated facility user.");
            throw new RuntimeException("federalId:" +userId+" has more than one associated facility user.  DB should never allow this error to be thrown.");
        }
        
    }
}
