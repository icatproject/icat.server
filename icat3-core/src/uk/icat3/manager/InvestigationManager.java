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
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import uk.icat3.entity.EntityBaseBean;
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
            Investigation investigation = find(Investigation.class, investigationId, manager);
            deleteInvestigationObject(userId, investigation, AccessType.DELETE, manager);
        }
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
    public static void deleteInvestigation(String userId, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteInvestigation("+userId+", "+investigationId+", EntityManager)");
        
        Investigation investigation = find(Investigation.class, investigationId, manager);
        deleteInvestigationObject(userId, investigation, AccessType.DELETE, manager);
        
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
            deleteInvestigationObject(userId, investigation,  AccessType.DELETE, manager);
        }
        
        return true;
    }
    ////////////////////        End of Delete Commands           /////////////////////////
    
    
    
    ////////////////////     Add/Update Commands    ///////////////////
    public static Investigation createInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createInvestigation("+userId+", "+investigation+", EntityManager)");
        
        //check if valid investigation
        investigation.isValid(manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.CREATE, manager);
        
        //new dataset, set createid
        investigation.setId(null); //should never be null at this point but check
        investigation.setCreateId(userId);
        manager.persist(investigation);
        
        return investigation;
        
    }
    /**
     * Updates a Investigation depending on whether the user has permission to update this Investigation
     *
     * @param userId userId of the user.
     * @param investigation
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Investigation updateInvestigation(String userId, Investigation investigation, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateInvestigation("+userId+", "+investigation+", EntityManager)");
        
        Investigation  investigationManaged = find(Investigation.class, investigation.getId(), manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, investigation, AccessType.UPDATE, manager);
        
        investigationManaged.setModId(userId);
        investigationManaged.merge(investigation);
        
        //check if valid investigation
        investigationManaged.isValid(manager, false);
        
        return investigationManaged;
        
    }
    
///////////////   End of add/Update Commands    ///////////////////
    
    
/////////////////////   Util add commands /////////////////////////
    public static void updateInvestigationObject(String userId, EntityBaseBean object, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException{
        log.trace("updateInvestigationObject("+userId+", "+object+", EntityManager)");
        
        if(object instanceof Keyword){
            Keyword keyword = (Keyword)object;
            
            //check keyword
            Keyword keywordManaged = find(Keyword.class, keyword.getKeywordPK(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, keywordManaged, AccessType.UPDATE, manager);
            
            keywordManaged.merge(keyword);
            keywordManaged.isValid(manager, false);
        } else if(object instanceof Sample){
            Sample sample = (Sample)object;
            
            //check keyword
            Sample sampleManaged = find(Sample.class, sample.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, sampleManaged, AccessType.UPDATE, manager);
            
            sampleManaged.merge(sample);
            
            sampleManaged.isValid(manager, false);
            
        }else if(object instanceof SampleParameter){
            SampleParameter sampleParameter = (SampleParameter)object;
            
            //check keyword
            SampleParameter sampleParameterManaged = find(SampleParameter.class, sampleParameter.getSampleParameterPK(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, sampleParameterManaged, AccessType.UPDATE, manager);
            
            sampleParameterManaged.merge(sampleParameter);
            
            sampleParameterManaged.isValid(manager, false);
            
        } else if(object instanceof Publication){
            Publication publication = (Publication)object;
            
            //check keyword
            Publication PublicationManaged = find(Publication.class, publication.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, PublicationManaged, AccessType.UPDATE, manager);
            
            PublicationManaged.merge(publication);
            
            PublicationManaged.isValid(manager, false);
            
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            //check keyword
            Investigator investigatorManaged = find(Investigator.class, investigator.getInvestigatorPK(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, investigatorManaged, AccessType.UPDATE, manager);
            
            investigatorManaged.merge(investigator);
            
            investigatorManaged.isValid(manager, false);
            
        } else if(object instanceof Investigation){
            Investigation investigation = (Investigation)object;
            //check investigation
            Investigation investigationManaged = find(Investigation.class, investigation.getId(), manager);
            
            //check user has update access
            GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.UPDATE, manager);
            
            investigationManaged.merge(investigation);
            
            investigationManaged.isValid(manager, false);
        }
        
    }
    
    /**
     *
     * @param userId
     * @param object
     * @param investigationId
     * @param type
     * @param manager
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
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
                
                //ok here fo delete
                keywordManaged.setDeleted(true);
                keywordManaged.setModId(userId);
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
                
                //ok here fo delete
                sampleManaged.setDeleted(true);
                sampleManaged.setModId(userId);
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
                
                //ok here fo delete
                sampleParameterManaged.setDeleted(true);
                sampleParameterManaged.setModId(userId);
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
                
                //ok here fo delete
                PublicationManaged.setDeleted(true);
                PublicationManaged.setModId(userId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, PublicationManaged, AccessType.REMOVE, manager);
                
                //ok here fo delete
                PublicationManaged.setInvestigationId(null);
                
                manager.remove(PublicationManaged);
            }
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            //check keyword
            Investigator investigatorManaged = find(Investigator.class, investigator.getInvestigatorPK(), manager);
            
            if(type == AccessType.DELETE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigatorManaged, AccessType.DELETE, manager);
                
                //ok here fo delete
                investigatorManaged.setDeleted(true);
                investigatorManaged.setModId(userId);
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
                
                //ok here fo delete
                investigationManaged.setCascadeDeleted(true);
                investigationManaged.setModId(userId);
            } else if(type == AccessType.REMOVE){
                //check user has delete access
                GateKeeper.performAuthorisation(userId, investigationManaged, AccessType.REMOVE, manager);
                
                manager.remove(investigationManaged);
            }
        }
    }
    
    /**
     *
     * @param userId
     * @param object
     * @param investigationId
     * @param manager
     * @throws uk.icat3.exceptions.ValidationException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
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
            
            //sets modId for persist
            publication.setCreateId(userId);
            
            manager.persist(publication);
            
            return publication;
        } else if(object instanceof Sample){
            Sample sample = (Sample)object;
            
            sample.setInvestigationId(investigation);
            sample.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            
            //TODO check for primary key
            try {
                //check investigator not already added
                Sample sampleManaged = sample.find(manager);
                if(sampleManaged.isDeleted()){
                    sampleManaged.setDeleted(false);
                    log.info(sampleManaged +" been deleted, undeleting now.");
                    return sampleManaged;
                } else {
                    //do nothing, throw exception
                    log.warn(sampleManaged +" already added to investigation.");
                    throw new ValidationException(sampleManaged+" is not unique");
                }
            } catch (NoResultException ex) {
                //not already in DB so add
                //sets modId for persist
                sample.setCreateId(userId);
                manager.persist(sample);
                
                return sample;
            }
        } else if(object instanceof SampleParameter){
            SampleParameter sampleParamter = (SampleParameter)object;
            
            Sample sample = find(Sample.class, sampleParamter.getSampleParameterPK().getSampleId(),  manager);
            sampleParamter.setSample(sample);
            
            sampleParamter.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            
            //TODO check for primary key
            try {
                //check investigator not already added
                SampleParameter sampleManaged = find(SampleParameter.class, sampleParamter.getSampleParameterPK(), manager);
                if(sampleManaged.isDeleted()){
                    sampleManaged.setDeleted(false);
                    log.info(sampleManaged +" been deleted, undeleting now.");
                    return sampleManaged;
                } else {
                    //do nothing, throw exception
                    log.warn(sampleManaged +" already added to investigation.");
                    throw new ValidationException(sampleManaged+" is not unique");
                }
            } catch (NoResultException ex) {
                //not already in DB so add
                //sets modId for persist
                sampleParamter.setCreateId(userId);
                manager.persist(sampleParamter);
                return sampleParamter;
            }
        } else if(object instanceof Keyword){
            Keyword keyword = (Keyword)object;
            
            keyword.setInvestigation(investigation);
            keyword.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            
            //TODO check for primary key
            try {
                //check investigator not already added
                Keyword keywordManaged = find(Keyword.class, keyword.getKeywordPK(), manager);
                if(keywordManaged.isDeleted()){
                    keywordManaged.setDeleted(false);
                    log.info(keywordManaged +" been deleted, undeleting now.");
                    return keywordManaged;
                } else {
                    //do nothing, throw exception
                    log.warn(keywordManaged +" already added to investigation.");
                    throw new ValidationException(keywordManaged+" is not unique");
                }
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                keyword.setCreateId(userId);
                manager.persist(keyword);
                return keyword;
            }
        } else if(object instanceof Investigator){
            Investigator investigator = (Investigator)object;
            
            investigator.setInvestigation(investigation);
            investigator.isValid(manager);
            
            //check user has delete access
            GateKeeper.performAuthorisation(userId, object, AccessType.CREATE, manager);
            
            try {
                //check investigator not already added
                Investigator investigatorManaged = find(Investigator.class, investigator.getInvestigatorPK(), manager);
                if(investigatorManaged.isDeleted()){
                    investigatorManaged.setDeleted(false);
                    log.info(investigatorManaged +" been deleted, undeleting now.");
                    return investigatorManaged;
                } else {
                    //do nothing, throw exception
                    log.warn(investigatorManaged +" already added to investigation.");
                    throw new ValidationException(investigatorManaged+" is not unique");
                }
            } catch (NoSuchObjectFoundException ex) {
                //not already in DB so add
                //sets modId for persist
                investigator.setCreateId(userId);
                manager.persist(investigator);
                return investigator;
            }
        }
        return null;
    }
}
