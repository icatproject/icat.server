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
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;

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
     *
     * @param userId
     * @param investigationIds
     * @param includes
     * @param manager
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws javax.persistence.EntityNotFoundException
     * @return
     */
    public static  Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, InvestigationInclude includes, EntityManager manager) throws InsufficientPrivilegesException, EntityNotFoundException {
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
    
    public static Investigation getInvestigation(String userId, Long investigationId, EntityManager manager) throws InsufficientPrivilegesException, EntityNotFoundException {
        Collection<Long> investigationIds = new ArrayList<Long>();
        investigationIds.add(investigationId);
        return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager).iterator().next();
    }
    
    public static  Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws InsufficientPrivilegesException, EntityNotFoundException {
        return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager);
    }
    ////////////////////    End of get Commands    /////////////////////////
    
    
    
    ////////////////////    Delete Command /  Should be removed??  /////////////////////////
    
    public static void deleteInvestigation(String userId, Investigation investigation, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        
        deleteInvestigation(userId, investigation.getId(), manager);
        
    }
    
    public static void deleteInvestigation(String userId, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigation("+userId+", "+investigationId+", EntityManager)");
        
        Investigation investigation = checkInvestigation(investigationId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.DELETE, manager);
        
        //delete dataset (Cascade is true);
        //TODO might have to remove all the datafiles first
        manager.remove(investigation);
        
    }
    
    public static void deleteInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigationIds+", EntityManager)");
        
        for(Long investigationId : investigationIds){
            deleteInvestigation(userId, investigationId, manager);
        }
    }
    
    //added boolean to avoid erausre name clash
    //TODO
    public static boolean deleteInvestigations(String userId, Collection<Investigation> investigations, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigations+", EntityManager)");
        
        for(Investigation investigation : investigations){
            deleteInvestigation(userId, investigation, manager);
        }
        
        return true;
    }
    ////////////////////        End of Delete Commands           /////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    
    public static void updateInvestigation(String userId, Investigation investigation, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("updateInvestigation("+userId+", "+investigation+", EntityManager)");
        
        //check to see if DataSet exists, dont need the returned dataset as merging
        checkInvestigation(investigation.getId(), manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.UPDATE, manager);
        
        manager.merge(investigation);
        
    }
    
    public static void addDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataSet("+userId+", "+dataSet+" "+investigationId+", EntityManager)");
        
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        datasets.add(dataSet);
        
        addDataSets(userId, datasets, investigationId, manager);
    }
    
    public static void addDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataSet("+userId+", "+dataSets+" "+investigationId+", EntityManager)");
        
        //check investigation exist
        Investigation investigation  = InvestigationManager.checkInvestigation(investigationId, manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.UPDATE, manager);
        
        for(Dataset dataset : dataSets){
            investigation.addDataSet(dataset);
        }
    }    
    ///////////////   End of add/Update Commands    ///////////////////
    
    
    
   
}
