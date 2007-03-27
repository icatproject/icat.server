/*
 * InvestigationManager.java
 *
 * Created on 27 February 2007, 13:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;

import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.InvestigationInclude;

/**
 *
 * @author gjd37
 */
public class InvestigationManager extends ManagerUtil {
    
    // Global class logger
    static Logger log = Logger.getLogger(InvestigationManager.class);
    
    
    ////////////////////    Get Commands    /////////////////////////
    /**
     * Returns a list of {@link Investigation} investigations from a list of {@link Investigation} investigation ids
     * if the user has access to the investigations.
     * Also gets extra information regarding the investigation.  See {@link InvestigationInclude}
     *
     * @param userId userId of the user.
     * @param investigationIds
     * @param include The information that is needed to be returned with the investigation
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, InvestigationInclude includes, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("getInvestigations("+userId+", "+investigationIds+", EntityManager)");
        
        Collection<Investigation> investigations = new ArrayList<Investigation>();
        
        for(Long investigationId : investigationIds) {
            
            //check investigation exist
            Investigation investigation  = checkInvestigation(investigationId, manager);
            
            //check user has read access
            GateKeeper.performAuthorisation(userId, investigation, AccessType.READ, manager);
            
            //add to arraylist
            investigations.add(investigation);
        }
        
        //add include information
        getInvestigationInformation(investigations, includes);
        
        return investigations;
    }
    
    /**
     * Returns a {@link Investigation} investigation from a {@link Investigation} investigation id
     * if the user has access to the investigation.
     *
     * @param userId userId of the user.
     * @param investigationId
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return A {@link Investigation} investigation object
     */
    public static Investigation getInvestigation(String userId, Long investigationId, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        Collection<Long> investigationIds = new ArrayList<Long>();
        investigationIds.add(investigationId);
        return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager).iterator().next();
    }
    
    /**
     * Returns a list of {@link Investigation} investigations from a list of {@link Investigation} investigation ids
     * if the user has access to the investigations.
     *
     * @param userId userId of the user.
     * @param investigationIds
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    public static  Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager);
    }
    ////////////////////    End of get Commands    /////////////////////////
    
    
    
    ////////////////////    Delete Command /  Should be removed??  /////////////////////////
    
    /**
     * Deletes a investigation for a user depending if the user's id has delete permissions to delete the investigation
     *
     * @param userId userId of the user.
     * @param investigation
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        
        deleteInvestigation(userId, investigation.getId(), manager);
        
    }
    
    /**
     * Deletes investigation for a user depending if the user's id has delete permissions to delete the investigation frome the
     * investigation id.
     *
     * @param userId userId of the user.
     * @param investigationId
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigation(String userId, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigation("+userId+", "+investigationId+", EntityManager)");
        
        Investigation investigation = checkInvestigation(investigationId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.DELETE, manager);
        
        //delete dataset (Cascade is true);
        //TODO might have to remove all the datafiles first
        //manager.remove(investigation);
        //not deleting anymore, jsut changing deleted to Y
        
        log.info("Deleting: "+investigation);
        investigation.setCascadeDeleted(true);
    }
    
    /**
     * Deletes a collection of investigations for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids.
     *
     * @param userId userId of the user.
     * @param investigationIds
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigationIds+", EntityManager)");
        
        for(Long investigationId : investigationIds){
            deleteInvestigation(userId, investigationId, manager);
        }
    }
    
    
    //TODO added boolean to avoid erausre name clash
    /**
     * Deletes a collection of investigations for a user depending if the user's id has delete permissions to delete the investigations f
     *
     * @param userId userId of the user.
     * @param investigations
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return dummy boolean
     */
    public static boolean deleteInvestigations(String userId, Collection<Investigation> investigations, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigations+", EntityManager)");
        
        for(Investigation investigation : investigations){
            deleteInvestigation(userId, investigation, manager);
        }
        
        return true;
    }
    ////////////////////        End of Delete Commands           /////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    
    /**
     * Updates a Investigation depending on whether the user has permission to update this Investigation
     *
     * @param userId userId of the user.
     * @param investigation
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void updateInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateInvestigation("+userId+", "+investigation+", EntityManager)");
        
        //check to see if DataSet exists, dont need the returned dataset as merging
        if(investigation.getId() != null){
            checkInvestigation(investigation.getId(), manager);
        }
        
        //check if valid investigation
        investigation.isValid(manager);
         //check if unique
        if(!investigation.isUnique(manager)) throw new ValidationException(investigation+" is not unique.");
      
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.UPDATE, manager);
        
        
        //if null then update
        if(investigation.getId() != null){
            investigation.setModId(userId);
            manager.merge(investigation);
        } else {
            //new dataset, set createid
            investigation.setId(null); //should never be null at this point but check
            investigation.setCreateId(userId);
            manager.persist(investigation);
        }
    }
    
    /**
     * Adds a data set to the list a data sets for a investigation, depending if the user has update permission on the investigation
     *
     * @param userId userId of the user.
     * @param dataSet
     * @param investigationId
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void addDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("addDataSet("+userId+", "+dataSet+" "+investigationId+", EntityManager)");
        
        //make sure id is null
        dataSet.setId(null);
        
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        datasets.add(dataSet);
        
        addDataSets(userId, datasets, investigationId, manager);
    }
    
    /**
     * Adds a collection of data sets to the list a data sets for a investigation, depending if the user has update permission on the investigation
     *
     * @param userId userId of the user.
     * @param dataSets
     * @param investigationId
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void addDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("addDataSet("+userId+", "+dataSets+" "+investigationId+", EntityManager)");
        
        //check investigation exist
        Investigation investigation  = InvestigationManager.checkInvestigation(investigationId, manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.UPDATE, manager);
        
        for(Dataset dataset : dataSets){
            dataset.isValid(manager);
            dataset.setModId(userId);
            dataset.setCreateId(userId);
            //make sure id is null
            dataset.setId(null);
            investigation.addDataSet(dataset);
        }
    }
    ///////////////   End of add/Update Commands    ///////////////////
}
