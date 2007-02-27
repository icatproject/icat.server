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
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.InvestigationUtil;

import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.InvestigationInclude;

/**
 *
 * @author gjd37
 */
public class InvestigationManager {
    
    // Global class logger
    static Logger log = Logger.getLogger(InvestigationManager.class);
    
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
        InvestigationUtil.getInvestigationInformation(investigations, includes);
        
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
    
    public static Investigation checkInvestigation(Long investigationId, EntityManager manager) throws EntityNotFoundException {
        Investigation investigation = manager.find(Investigation.class, investigationId);
        //check if the id exists in the database
        if(investigation == null) throw new EntityNotFoundException("Investigation: id: "+investigationId+" not found.");
        
        log.trace("Investigation: id: "+investigationId+" exists in the database");
        
        return investigation;
        
    }
}
