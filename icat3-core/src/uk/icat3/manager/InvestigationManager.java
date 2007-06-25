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
import org.apache.log4j.Logger;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;

import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.InvestigationInclude;

/**
 * This is the manager class for all operations for investigations.
 * 
 * These are update, remove, delete, create on investigations investigation objects.
 * <br /><br />
 * Objects can be {@link Keyword}, {@link Sample}, {@link SampleParameter}, {@link Publication}, {@link Investigator}
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
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @param includes information that is needed to be returned with the investigation
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    public static Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, InvestigationInclude includes, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("getInvestigations("+userId+", "+investigationIds+", EntityManager)");
        
        Collection<Investigation> investigations = new ArrayList<Investigation>();
        
        for(Long investigationId : investigationIds) {
            
            //check investigation exist
            Investigation investigation  = find(Investigation.class, investigationId, manager);
            
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
     * Returns a {@link Investigation} investigation from a {@link Investigation} id
     * if the user has access to the investigation.
     *
     * @param userId federalId of the user.
     * @param investigationId Id of investigations
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return {@link Investigation} object
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
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    public static  Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager);
    }
    ////////////////////    End of get Commands    /////////////////////////
    
    
    
    ////////////////////    Delete / Remove Commands  /////////////////////////
    
    /**
     * Deletes a collection of investigations for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids. Deleting a investigation marks it, and all of its paramters, datasets, datafiles, keywords
     * investigators etc as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigations(String userId, Collection<Long> investigationIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigationIds+", EntityManager)");
        
        for(Long investigationId : investigationIds){
            Investigation investigation = find(Investigation.class, investigationId, manager);
            deleteInvestigationObject(userId, investigation, AccessType.DELETE, manager);
        }
    }
    
    /**
     * Deletes the investigation for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids.  Deleting a investigation marks it, and all of its paramters, datasets, datafiles, keywords
     * investigators etc as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param investigationId Id of investigations
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigation(String userId, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigation("+userId+", "+investigationId+", EntityManager)");
        
        Investigation investigation = find(Investigation.class, investigationId, manager);
        deleteInvestigationObject(userId, investigation, AccessType.DELETE, manager);
        
    }
    
    /**
     * Deletes the investigation for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids. Deleting a investigation marks it, and all of its paramters, datasets, datafiles, keywords
     * investigators etc as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param investigation investigation object
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigation("+userId+", "+investigation+", EntityManager)");
        
        deleteInvestigationObject(userId, investigation, AccessType.DELETE, manager);
        
    }
    
    
    /**
     * Removes (from the database) the investigation for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids.
     *
     * @param userId federalId of the user.
     * @param investigationId Id of investigation
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeInvestigation(String userId, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("removeInvestigation("+userId+", "+investigationId+", EntityManager)");
        
        Investigation investigation = find(Investigation.class, investigationId, manager);
        deleteInvestigationObject(userId, investigation, AccessType.REMOVE, manager);
        
    }
    
    /**
     * Removes (from the database) the investigation for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids.
     *
     * @param userId federalId of the user.
     * @param investigation investigation object
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("removeInvestigation("+userId+", "+investigation+", EntityManager)");
        
        deleteInvestigationObject(userId, investigation, AccessType.REMOVE, manager);
        
    }
    
    
    //TODO added boolean to avoid erausre name clash
    /**
     * Deletes the investigations for a user depending if the user's id has delete permissions to delete the investigations from the
     * investigation ids. Deleting a investigation marks it, and all of its paramters, datasets, datafiles, keywords
     * investigators etc as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param investigations objects to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return boolean dummy
     */
    public static boolean deleteInvestigations(String userId, Collection<Investigation> investigations, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigations("+userId+", "+investigations+", EntityManager)");
        
        for(Investigation investigation : investigations){
            deleteInvestigationObject(userId, investigation,  AccessType.DELETE, manager);
        }
        
        return true;
    }
    ////////////////////        End of Delete Commands           /////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    /**
     * Creates a investigation, depending if the user has create permission to create the investigation
     *
     * @param userId federalId of the user.
     * @param investigation objects to be created
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation is invalid
     * @return created investigation
     */
    public static Investigation createInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createInvestigation("+userId+", "+investigation+", EntityManager)");
        
        investigation.setId(null); //should never be null at this point but check
        
        //check if valid investigation
        investigation.isValid(manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.CREATE, manager);
        FacilityUser facilityUser = getFacilityUser(userId, manager);
        
        //new dataset, set createid
        
        investigation.setCascade(Cascade.MOD_AND_CREATE_IDS, facilityUser.getFacilityUserId());
        investigation.setCascade(Cascade.REMOVE_ID, Boolean.TRUE);
        manager.persist(investigation);
        
        
        //need to add this user to the list of investigators now
        Investigator investigator = new Investigator(facilityUser.getFacilityUserId(), investigation.getId());
        investigator.setCreateId(facilityUser.getFacilityUserId());
        investigator.setFacilityUser(facilityUser);
        investigator.setInvestigation(investigation);
        
        log.trace("Adding "+investigator+" to investigation "+investigation);
        investigation.addInvestigator(investigator);
        
        return investigation;
    }
    
    /**
     * Updates a Investigation depending on whether the user has permission to update this Investigation
     *
     * @param userId federalId of the user.
     * @param investigation object to be updated
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation is invalid
     * @return updated investigation
     */
    public static Investigation updateInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateInvestigation("+userId+", "+investigation+", EntityManager)");
        
        Investigation  investigationManaged = find(Investigation.class, investigation.getId(), manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.UPDATE, manager);
        String facilityUserId = getFacilityUserId(userId, manager);
        
        investigationManaged.setModId(facilityUserId);
        investigationManaged.merge(investigation);
        
        //check if valid investigation
        investigationManaged.isValid(manager, false);
        
        return investigationManaged;
        
    }
    
///////////////   End of add/Update Commands    ///////////////////
    
    
/////////////////////   Util add commands /////////////////////////
    /**
     * Updates an investigation object, depending on whether the user has permission to update this Investigation object.
     * <br /><br />
     * Objects can be {@link Sample}, {@link SampleParameter}, {@link Publication}, {@link Investigator}, {@link Investigation}
     * <br /><br />
     * throws {@link java.lang.RuntimeException} if the {@link EntityBaseBean} object is not allowed.
     *
     * @param userId federalId of the user.
     * @param object {@link EntityBaseBean} object to be updated
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     */
    public static void updateInvestigationObject(String userId, EntityBaseBean object, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException{
        log.trace("updateInvestigationObject("+userId+", "+object+", EntityManager)");
        
        if(object instanceof Sample){
            Sample sample = (Sample)object;
            
            //check keyword
            Sample sampleManaged = find(Sample.class, sample.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, sampleManaged, AccessType.UPDATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            sampleManaged.merge(sample);
            sampleManaged.setModId(facilityUserId);
            
            sampleManaged.isValid(manager, false);
            
        }else if(object instanceof SampleParameter){
            SampleParameter sampleParameter = (SampleParameter)object;
            
            //check keyword
            SampleParameter sampleParameterManaged = find(SampleParameter.class, sampleParameter.getSampleParameterPK(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, sampleParameterManaged, AccessType.UPDATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            sampleParameterManaged.merge(sampleParameter);
            sampleParameterManaged.setModId(facilityUserId);
            
            sampleParameterManaged.isValid(manager, false);
            
        } else if(object instanceof Publication){
            Publication publication = (Publication)object;
            
            //check keyword
            Publication PublicationManaged = find(Publication.class, publication.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, PublicationManaged, AccessType.UPDATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            PublicationManaged.merge(publication);
            PublicationManaged.setModId(facilityUserId);
            
            PublicationManaged.isValid(manager, false);
            
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            //check keyword
            Investigator investigatorManaged = find(Investigator.class, investigator.getInvestigatorPK(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, investigatorManaged, AccessType.UPDATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            investigatorManaged.merge(investigator);
            investigatorManaged.setModId(facilityUserId);
            
            investigatorManaged.isValid(manager, false);
            
        } else if(object instanceof Investigation){
            Investigation investigation = (Investigation)object;
            //check investigation
            Investigation investigationManaged = find(Investigation.class, investigation.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.UPDATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            investigationManaged.merge(investigation);
            investigationManaged.setModId(facilityUserId);
            
            investigationManaged.isValid(manager, false);
        } else throw new RuntimeException(object +" is not avaliable to be modified");
        
    }
    
    /**
     * Deletes or removed an investigation object, depending on whether the user has permission to delete this Investigation object.
     * <br /><br />
     * Objects can be {@link Keyword}, {@link Sample}, {@link SampleParameter}, {@link Publication}, {@link Investigator}, {@link Investigation}
     * <br /><br />
     * throws {@link java.lang.RuntimeException} if the {@link EntityBaseBean} object is not allowed.
     *
     * @param userId federalId of the user.
     * @param object {@link EntityBaseBean} object to be updated
     * @param type {@link AccessType} object, either REMOVE or DELETE
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteInvestigationObject(String userId, EntityBaseBean object, AccessType type, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException{
        log.trace("modifyInvestigationObject("+userId+", "+object+", "+type+", EntityManager)");
        
        if(object instanceof Keyword){
            Keyword keyword = (Keyword)object;
            
            //check keyword
            Keyword keywordManaged = find(Keyword.class, keyword.getKeywordPK(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, keywordManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                keywordManaged.setDeleted(true);
                keywordManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, keywordManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                keywordManaged.setInvestigation(null);
                
                manager.remove(keywordManaged);
            }
        } else if(object instanceof Sample){
            Sample sample = (Sample)object;
            
            //check keyword
            Sample sampleManaged = find(Sample.class, sample.getId(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, sampleManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                sampleManaged.setDeleted(true);
                sampleManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, sampleManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                // sampleManaged.setInvestigationId(null);
                
                manager.remove(sampleManaged);
            }
        }else if(object instanceof SampleParameter){
            SampleParameter sampleParameter = (SampleParameter)object;
            
            //check keyword
            SampleParameter sampleParameterManaged = find(SampleParameter.class, sampleParameter.getSampleParameterPK(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, sampleParameterManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                sampleParameterManaged.setDeleted(true);
                sampleParameterManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, sampleParameterManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                sampleParameterManaged.setSample(null);
                
                manager.remove(sampleParameterManaged);
            }
        } else if(object instanceof Publication){
            Publication publication = (Publication)object;
            
            //check keyword
            Publication PublicationManaged = find(Publication.class, publication.getId(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, PublicationManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                PublicationManaged.setDeleted(true);
                PublicationManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, PublicationManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                //PublicationManaged.setInvestigationId(null);
                
                manager.remove(PublicationManaged);
            }
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            //check keyword
            Investigator investigatorManaged = find(Investigator.class, investigator.getInvestigatorPK(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigatorManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                investigatorManaged.setDeleted(true);
                investigatorManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigatorManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                investigatorManaged.setInvestigation(null);
                
                manager.remove(investigatorManaged);
            }
        } else if(object instanceof Investigation){
            Investigation investigation = (Investigation)object;
            //check investigation
            Investigation investigationManaged = find(Investigation.class, investigation.getId(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.DELETE, manager);
                String facilityUserId = getFacilityUserId(userId, manager);
                
                //ok here fo delete
                investigationManaged.setCascade(Cascade.DELETE, Boolean.TRUE);
                investigationManaged.setModId(facilityUserId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.REMOVE, manager);
                
                manager.remove(investigationManaged);
            }
        }  else throw new RuntimeException(object +" is not avaliable to be deleted");
    }
    
    /**
     * Created an investigation object, depending on whether the user has permission to delete this Investigation object.
     * <br /><br />
     * Objects can be {@link Keyword}, {@link Sample}, {@link SampleParameter}, {@link Publication}, {@link Investigator}
     * <br /><br />
     * throws {@link java.lang.RuntimeException} if the {@link EntityBaseBean} object is not allowed.
     * @param userId federalId of the user.
     * @param object {@link EntityBaseBean} object to be updated
     * @param investigationId Id of investigation to add to
     * @param manager manager object that will facilitate interaction with underlying database
     * @return {@link EntityBaseBean} obejct that was added
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the investigation object is invalid
     */
    public static EntityBaseBean addInvestigationObject(String userId, EntityBaseBean object, Long investigationId, EntityManager manager) throws ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        log.trace("addObject("+userId+", "+object+", "+investigationId+", EntityManager)");
        
        //check investigation
        Investigation investigation = find(Investigation.class, investigationId, manager);
        
        if(object instanceof Publication){
            Publication publication = (Publication)object;
            
            publication.setInvestigationId(investigation);
            publication.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            try {
                //check investigator not already added                
                Publication publicationManaged = findObject(Publication.class, publication.getId(), manager);
                /*if(publicationManaged.isDeleted()){
                    publicationManaged.setDelete(false);
                    publicationManaged.setModId(facilityUserId);
                    log.info(publicationManaged +" been deleted, undeleting now.");
                    return publicationManaged;
                } else {*/
                    //do nothing, throw exception
                    log.warn(publicationManaged +" already added to investigation.");
                    throw new ValidationException(publicationManaged+" is not unique");
                //}
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                //sets modId for persist
                publication.setCreateId(facilityUserId);
                
                manager.persist(publication);
                return publication;
            }
            
        } else if(object instanceof Sample){
            Sample sample = (Sample)object;
            
            sample.setInvestigationId(investigation);
            sample.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            //TODO check for primary key
            try {
                //check investigator not already added
                Sample sampleManaged = findObject(Sample.class,sample.getId() ,manager);
               /* if(sampleManaged.isDeleted()){
                    sampleManaged.setDelete(false);
                    sampleManaged.setModId(facilityUserId);
                    log.info(sampleManaged +" been deleted, undeleting now.");
                    return sampleManaged;
                } else {*/
                    //do nothing, throw exception
                    log.warn(sampleManaged +" already added to investigation.");
                    throw new ValidationException(sampleManaged+" is not unique");
                //}
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                sample.setCreateId(facilityUserId);
                manager.persist(sample);
                
                return sample;
            }
        } else if(object instanceof SampleParameter){
            SampleParameter sampleParamter = (SampleParameter)object;
            
            if(sampleParamter.getSampleParameterPK() == null) throw new ValidationException("SampleParameter PK cannot be null");
            
            Sample sample = find(Sample.class, sampleParamter.getSampleParameterPK().getSampleId(),  manager);
            sampleParamter.setSample(sample);
            
            sampleParamter.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            //TODO check for primary key
            try {
                //check SampleParameter not already added
                SampleParameter sampleManaged = findObject(SampleParameter.class, sampleParamter.getSampleParameterPK(), manager);
               /* if(sampleManaged.isDeleted()){
                    sampleManaged.setDelete(false);
                    sampleManaged.setModId(facilityUserId);
                    log.info(sampleManaged +" been deleted, undeleting now.");
                    return sampleManaged;
                } else {*/
                    //do nothing, throw exception
                    log.warn(sampleManaged +" already added to investigation.");
                    throw new ValidationException(sampleManaged+" is not unique");
                //}
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                sampleParamter.setCreateId(facilityUserId);
                manager.persist(sampleParamter);
                return sampleParamter;
            }
        } else if(object instanceof Keyword){
            Keyword keyword = (Keyword)object;
            
            keyword.setInvestigation(investigation);
            keyword.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            //TODO check for primary key
            try {
                //check investigator not already added
                Keyword keywordManaged = findObject(Keyword.class, keyword.getKeywordPK(), manager);
               /* if(keywordManaged.isDeleted()){
                    keywordManaged.setDelete(false);
                    keywordManaged.setModId(facilityUserId);
                    log.info(keywordManaged +" been deleted, undeleting now.");
                    return keywordManaged;
                } else {*/
                    //do nothing, throw exception
                    log.warn(keywordManaged +" already added to investigation.");
                    throw new ValidationException(keywordManaged+" is not unique");
                //}
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                keyword.setCreateId(facilityUserId);
                manager.persist(keyword);
                return keyword;
            }
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            investigator.setInvestigation(investigation);
            investigator.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            String facilityUserId = getFacilityUserId(userId, manager);
            
            try {
                //check investigator not already added
                Investigator investigatorManaged = findObject(Investigator.class, investigator.getInvestigatorPK(), manager);
                /*if(investigatorManaged.isDeleted()){
                    investigatorManaged.setDelete(false);
                    investigatorManaged.setModId(facilityUserId);
                    log.info(investigatorManaged +" been deleted, undeleting now.");
                    return investigatorManaged;
                } else {*/
                    //do nothing, throw exception
                    log.warn(investigatorManaged +" already added to investigation.");
                    throw new ValidationException(investigatorManaged+" is not unique");
                //}
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                investigator.setCreateId(facilityUserId);
                manager.persist(investigator);
                return investigator;
            }
        }  else throw new RuntimeException(object +" is not avaliable to be added");
    }
}
