package uk.icat3.security;

import java.util.logging.Level;
import org.apache.log4j.Logger;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.AccessType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;
import static uk.icat3.util.Util.*;
/**
 * This grants or denies access to all the objects within the database
 */
public class GateKeeper {
    
    /** Creates a new instance of GateKeeper */
    public GateKeeper() {
    }
    
    // Global class logger
    static Logger log = Logger.getLogger(GateKeeper.class);
    
    /**
     * Decides if a user has permission to perform an operation of type
     * {@link AccessType} on a {@link Study} element/entity.  If the
     * user does not have permission to perform aforementioned operation
     * then an {@link InsufficientPrivilegesException} will be thrown.
     *
     * <p>A Study can have multiple Investigations, so find them all and
     *  use each one to check authorisation. If a user has authorisation
     *  on any one of the investigations contained within the study then
     *  those permissions are extended to the parent Study element.</p>
     *
     * @param user          username or dn of user who is to be authorised.
     * @param object        object entitybasebean of the  element/entity that
     *                      the user wishes to perform operation on.
     * @param access        type of operation that the user is trying to
     *                      perform.
     * @param manager       manager object that will facilitate interaction
     *                      with underlying database
     * @throws InsufficientPrivilegesException  if user does not have
     *                      permission to perform operation.
     */
    public static void performAuthorisation(String user, EntityBaseBean object, AccessType access, EntityManager manager) throws InsufficientPrivilegesException {
        
        Investigation investigation = null;
        //this id of the root parent, ie inv, ds, df
        Long rootParentsId = null;
        if(object instanceof Publication){
            investigation  = ((Publication)object).getInvestigationId();
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.PUBLICATION, manager);
        } else if(object instanceof Investigation){
            investigation  = (Investigation)object;
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.INVESTIGATION, manager);
        } else if(object instanceof Keyword){
            investigation  = ((Keyword)object).getInvestigation();
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.KEYWORD, manager);
        } else if(object instanceof Dataset){
            investigation  = ((Dataset)object).getInvestigation();
            rootParentsId = ((Dataset)object).getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.DATASET, manager);
        } else if(object instanceof Datafile){
            investigation  = ((Datafile)object).getDataset().getInvestigation();
            rootParentsId = ((Datafile)object).getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.DATAFILE, manager);
        } else if(object instanceof DatasetParameter){
            investigation  = ((DatasetParameter)object).getDataset().getInvestigation();
            rootParentsId = ((DatasetParameter)object).getDataset().getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.DATASET_PARAMETER, manager);
        } else if(object instanceof DatafileParameter){
            investigation  = ((DatafileParameter)object).getDatafile().getDataset().getInvestigation();
            rootParentsId = ((DatafileParameter)object).getDatafile().getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.DATAFILE_PARAMETER, manager);
        } else if(object instanceof SampleParameter){
            investigation  = ((SampleParameter)object).getSample().getInvestigationId();
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.SAMPLE_PARAMETER, manager);
        } else if(object instanceof Sample){
            investigation  = ((Sample)object).getInvestigationId();
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.SAMPLE, manager);
        } else if(object instanceof Investigator){
            investigation  =((Investigator)object).getInvestigation();
            rootParentsId = investigation.getId();
            performAuthorisation(user, investigation, access, object, rootParentsId, ElementType.INVESTIGATOR, manager);
        } /* else if(object instanceof Study){
            for (StudyInvestigation si :  ((Study)object).getStudyInvestigationCollection()) {
                invList.add(si.getInvestigation());
            }//end for
            performAuthorisation(user, invList, access, ((DatafileParameter)object), manager);
        }*/ else throw new InsufficientPrivilegesException(object.getClass().getSimpleName()+" not supported for security check.");;
        
        
        
    }//end method
    
    /**
     * Private method that ultimately does the low-level permission check
     * against the database.  This method retrieves all permission elements
     * associated with a given user and investigation pair.  If user has
     * been granted the appropriate access permission in the database then
     * the method returns without error.  Otherwise an exception with
     * appropriate details is raised, logged and thrown back to the caller.
     *
     * @param user              username or dn of user who is to be authorised.
     * @param investigations    collection if elements/entities that the user wishes
     *                          to perform operation on.
     * @param access            type of operation that the user is trying to perform.
     * @param element           name of element/entity type that user is really trying to
     *                          access in some way e.g. datafile.  This is used for
     *                          purposes only.
     * @param elementId         primary key of specific element/entity that user is trying
     *                          to access.
     * @param manager           manager object that will facilitate interaction
     *                          with underlying database
     * @throws InsufficientPrivilegesException  if user does not have
     *                          permission to perform operation.
     */
    private static void performAuthorisation(String userId, Investigation investigation, AccessType access, EntityBaseBean object, Long rootParentsId, ElementType elementType, EntityManager manager) throws InsufficientPrivilegesException {
        log.trace("performAuthorisation(): "+userId+", AccessType: "+access+", "+object+", parentId: "+rootParentsId);
        IcatAuthorisation icatAuthorisation = null;
        boolean success = false;
        
        //get icat authroisation
        try{
            if(elementType.isInvestigationType()) {
                icatAuthorisation = findIcatAuthorisation(userId, investigation, ElementType.INVESTIGATION, rootParentsId, rootParentsId, manager);
            } else if(elementType.isDatasetType()) {
                icatAuthorisation = findIcatAuthorisation(userId, investigation, ElementType.DATASET, rootParentsId, ((Dataset)object).getInvestigationId(), manager);
            } else if(elementType.isDatafileType()) {
                icatAuthorisation = findIcatAuthorisation(userId, investigation, ElementType.DATAFILE, rootParentsId, ((Datafile)object).getDatasetId(), manager);
            }
        } catch(InsufficientPrivilegesException ipe){
            log.warn("User: " + userId + " does not have permission to perform '" + access + "' operation on " + object );
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform '" + access + "' operation on " + object);
        }
        
        //now check the access permission from the icat authroisation
        if(access == AccessType.READ){
            if(parseBoolean(icatAuthorisation.getRole().getActionSelect())) success = true; //user has access to read element
        } else if(access == AccessType.REMOVE){
            //cannot do if facility acquired
            if(!object.isFacilityAcquiredSet()){
                //check if element type is a root, if so check root remove permissions
                if(elementType.isRootType()){
                    log.trace("Create ID: "+object.getCreateId()+" "+userId.equals(object.getCreateId()));
                    // to remove something, create Id must also be the same as your Id.
                    if(parseBoolean(icatAuthorisation.getRole().getActionRootRemove()) && userId.equals(object.getCreateId())) {
                        success = true; //user has access to remove root element
                    }
                } else if(parseBoolean(icatAuthorisation.getRole().getActionRemove()) && userId.equals(object.getCreateId())) {
                    success = true; //user has access to remove element
                }
            }
        } else if(access == AccessType.CREATE){
            //check if element type is a root, if so check root insert permissions
            if(elementType.isRootType()){
                log.trace("Trying to create a root type: "+elementType);
                //if null in investigation id then can create investigations
                if(elementType == ElementType.INVESTIGATION && icatAuthorisation.getElementId() == null && parseBoolean(icatAuthorisation.getRole().getActionRootInsert())){
                    success = true; //user has access to insert root root element
                } else if(elementType == ElementType.DATASET &&
                        icatAuthorisation.getElementId() == null &&
                        icatAuthorisation.getParentElementType().equals(ElementType.DATASET.toString()) &&
                        icatAuthorisation.getParentElementId().equals(((Dataset)object).getInvestigationId()) &&
                        parseBoolean(icatAuthorisation.getRole().getActionRootInsert())){
                    success = true; //user has access to insert root root element
                } else if(elementType == ElementType.DATAFILE &&
                        icatAuthorisation.getElementId() == null &&
                        icatAuthorisation.getParentElementType().equals(ElementType.DATAFILE.toString()) &&
                        icatAuthorisation.getParentElementId().equals(((Datafile)object).getDatasetId()) &&
                        parseBoolean(icatAuthorisation.getRole().getActionRootInsert())){
                    success = true; //user has access to insert root root element
                }
            } else if(parseBoolean(icatAuthorisation.getRole().getActionInsert())){
                success = true; //user has access to insert element
            }
        } else if(access == AccessType.UPDATE){
            //cannot do if facility acquired
            if(parseBoolean(icatAuthorisation.getRole().getActionUpdate()) && !object.isFacilityAcquiredSet()) success = true; //user has access to update element
        } else if(access == AccessType.DELETE){
            //cannot do if facility acquired
            if(parseBoolean(icatAuthorisation.getRole().getActionDelete()) && !object.isFacilityAcquiredSet()) success = true; //user has access to delete element
        } else if(access == AccessType.DOWNLOAD){
            if(parseBoolean(icatAuthorisation.getRole().getActionDownload())) success = true; //user has access to download element
        } else if(access == AccessType.SET_FA){
            if(parseBoolean(icatAuthorisation.getRole().getActionSetFa())) success = true; //user has access to set FA element
        } else if(access == AccessType.MANAGE_USERS){
            if(parseBoolean(icatAuthorisation.getRole().getActionManageUsers())) success = true; //user has access to set manage users
        }
        
        //now check if has permission and if not throw exception
        if(success){
            //now append the role to the investigation, dataset or datafile 
            if(elementType.isRootType()) object.setIcatRole(icatAuthorisation.getRole());
            log.debug("User: " + userId + " granted " + access + " permission on " + object +" with role "+icatAuthorisation.getRole());
        } else {
            log.warn("User: " + userId + " does not have permission to perform '" + access + "' operation on " + object );
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform '" + access + "' operation on " + object );
        }
    }
    
    /**
     * This finds the IcatAuthorisation for the user, it first tries to find the one corresponding
     * to the users fedId, if this is not found, it looks for the ANY as the fedId
     *
     */
    private static IcatAuthorisation findIcatAuthorisation(String userId, Investigation investigation, ElementType type, Long id, Long parentId, EntityManager manager) throws InsufficientPrivilegesException {
        if(!type.isRootType()) throw new RuntimeException("ElementType: "+type+" not supported");
        log.trace("Looking for ICAT AUTHORISATION: UserId: "+userId+", elementId: "+id+", type: "+type+", investigationId: "+investigation.getId() );
        
        IcatAuthorisation icatAuthorisation = null;
        if(id == null){
            log.trace("user "+ userId+" trying to create a "+type);
            Query nullQuery = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_NULL);
            
            //try and find user with null as investigation
            nullQuery.setParameter("elementType", type).
                    setParameter("userId", userId);
            
            if(type == ElementType.INVESTIGATION){
                nullQuery.setParameter("parentElementId", null);
                nullQuery.setParameter("parentElementType", null);
            } else if(type == ElementType.DATASET){
                nullQuery.setParameter("parentElementType", ElementType.DATASET);
                nullQuery.setParameter("parentElementId", parentId);
            } else if(type == ElementType.DATAFILE){
                nullQuery.setParameter("parentElementType", ElementType.DATAFILE);
                nullQuery.setParameter("parentElementId", parentId);
            }
            
            try{
                icatAuthorisation = (IcatAuthorisation)nullQuery.getSingleResult();
                log.trace("Found stage 3 (nulls): "+icatAuthorisation);
            } catch(NoResultException nre3){
                log.debug("None found for : UserId: "+userId+", elementId: null, type: "+type+", elementId: null, throwing exception");
                throw new InsufficientPrivilegesException();
            }
        } else {
            Query query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE);
            query.setParameter("elementType", type).
                    setParameter("elementId", id).
                    setParameter("userId", userId);
            
            try{
                icatAuthorisation = (IcatAuthorisation)query.getSingleResult();
                log.trace("Found stage 1 (normal): "+icatAuthorisation);
            } catch(NoResultException nre){
                
                //try find ANY
                log.trace("None found, searching for ANY in userId");
                query.setParameter("userId", "ANY");
                
                try{
                    icatAuthorisation = (IcatAuthorisation)query.getSingleResult();
                    log.trace("Found stage 2 (ANY): "+icatAuthorisation);
                } catch(NoResultException nre2){
                    log.debug("None found for : UserId: "+userId+", type: "+type+", elementId: "+id+", throwing exception");
                    throw new InsufficientPrivilegesException();
                }
            }
        }
               
        //return the icat authoruisation
        return icatAuthorisation;
        
    }
}
