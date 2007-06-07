package uk.icat3.security;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.AccessType;
import javax.persistence.EntityManager;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationLevelPermission;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.Study;
import uk.icat3.entity.StudyInvestigation;

/*
 * GateKeeper.java
 *
 * Created on 13 February 2007, 08:41
 *
 * GateKeeper is the principal authorisation service for the ICAT3 API.
 * This class is typically used internally by the coarser-grained SOA
 * services offered by the API.  Authorisation can be performed on a request
 * to perform an operation on any of the core elements (or entities) of the
 * ICAT3 database model/schema.
 *
 * <p>Each operation will require a user e.g. 'distinguished name', elementType
 * e.g. 'Datafile' and an access type e.g. 'READ'. An EntityManager will also
 * be passed in to each of the authorisation methods.  This allows each method
 * to be self-contained allowing its publication via standalone JPA or via a
 * J2EE application server</p>
 *
 * <p>Each step within the authorisation process is logged via LOG4J at the
 * appropriate log levels.  Typed exceptions are thrown where necessary.</p>
 *
 * @author Damian Flannery
 * @version 1.0
 */
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
        //now check modifiable
        if(access == AccessType.REMOVE || access == AccessType.DELETE || access == AccessType.UPDATE){
            if(!object.isModifiable()){
                InsufficientPrivilegesException e = new InsufficientPrivilegesException("User: " + user + " does not have permission to perform '" + access + "' operation on " + object.getClass().getSimpleName()+", as it cannot be modified.");
                log.warn("User: " + user + " does not have permission to perform '" + access + "' operation on " + object+", as it cannot be modified.");
                throw(e);
            }
        }
        ArrayList<Investigation> invList = new ArrayList<Investigation>();
        
        if(object instanceof Publication){
            invList.add(((Publication)object).getInvestigationId());
            performAuthorisation(user, invList, access, ((Publication)object), manager);
        } else if(object instanceof Investigation){
            invList.add((Investigation)object);
            performAuthorisation(user, invList, access, ((Investigation)object), manager);
        } else if(object instanceof Keyword){
            invList.add(((Keyword)object).getInvestigation());
            performAuthorisation(user, invList, access, ((Keyword)object), manager);
        } else if(object instanceof Dataset){
            invList.add(((Dataset)object).getInvestigationId());
            performAuthorisation(user, invList, access, ((Dataset)object), manager);
        }else if(object instanceof Datafile){
            invList.add(((Datafile)object).getDatasetId().getInvestigationId());
            performAuthorisation(user, invList, access, ((Datafile)object), manager);
        } else if(object instanceof DatasetParameter){
            invList.add(((DatasetParameter)object).getDataset().getInvestigationId());
            performAuthorisation(user, invList, access, ((DatasetParameter)object), manager);
        }else if(object instanceof DatafileParameter){
            invList.add(((DatafileParameter)object).getDatafile().getDatasetId().getInvestigationId());
            performAuthorisation(user, invList, access, ((DatafileParameter)object), manager);
        } else if(object instanceof Study){
            for (StudyInvestigation si :  ((Study)object).getStudyInvestigationCollection()) {
                invList.add(si.getInvestigation());
            }//end for
            performAuthorisation(user, invList, access, ((DatafileParameter)object), manager);
        } else if(object instanceof SampleParameter){
            invList.add(((SampleParameter)object).getSample().getInvestigationId());
            performAuthorisation(user, invList, access,(SampleParameter)object, manager);
        } else if(object instanceof Sample){
            invList.add(((Sample)object).getInvestigationId());
            performAuthorisation(user, invList, access, ((Sample)object), manager);
        } else if(object instanceof Investigator){
            invList.add(((Investigator)object).getInvestigation());
            performAuthorisation(user, invList, access, ((Investigator)object), manager);
        } else throw new InsufficientPrivilegesException(object.getClass().getSimpleName()+" not supported for security check.");;
        
        
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
    private static void performAuthorisation(String user, Collection<Investigation> investigations, AccessType access, EntityBaseBean element, EntityManager manager) throws InsufficientPrivilegesException {
        
        //TODO
        //if creating investigation, anyone allowed to do that?
        if(access == AccessType.CREATE && element instanceof Investigation) {
            try {
                //check if user in facilityuser table
                ManagerUtil.getFacilityUserId(user, manager);
            } catch(Exception e) {
                //if we get to here then user does not have permission so we need to throw an exception
                InsufficientPrivilegesException inse = new InsufficientPrivilegesException("User: " + user + " does not have permission to perform '" + access + "' operation on " + element.getClass().getSimpleName() );
                log.warn("User: " + user + " does not have permission to perform '" + access + "' operation on " + element );
                throw(inse);
            }
            log.debug("User: " + user + " granted " + access + " permission on " + element );
            return ;
        }
        
        //if user is a system administrator then return (no need to check each request)
        //TBI...
        
        //changed:  gjd37, iterators, not nice!
        for(Investigation investigation : investigations){
            //user is instrument scientist for instrument in investigation then return (no need to check individual permissions)
            //TBI...
            
            //TODO: added by gjd37, if user one of investigators then allow access
            if(investigation.getInvestigatorCollection() == null) {
                if(access == AccessType.READ){
                    log.debug("User: " + user + " granted " + access + " permission on " + element +" as there are no investigators");
                    return;
                } else break;
            }
            for(Investigator investigator : investigation.getInvestigatorCollection()){
                log.trace(""+investigator.getFacilityUser());
                if(investigator.getFacilityUser().getFederalId().equals(user)){
                    //passed for this investigation
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return ;
                }
            }
            
            //if get to here, then user not a investigator so check investigations level permissions
            for(InvestigationLevelPermission perm : investigation.getInvestigationLevelPermissionCollection()){
                
                //READ, UPDATE, DELETE, CREATE, ADMIN, FINE_GRAINED_ACCESS;
                switch (access) {
                case READ:      if (perm.getPrmRead() == 1) {
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return;
                }//end if
                break;
                
                case UPDATE:    if (perm.getPrmUpdate() == 1){
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return;
                }//end if
                break;
                
                case DELETE:    if (perm.getPrmDelete() == 1) {
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return;
                }//end if
                break;
                
                case CREATE:    if (perm.getPrmCreate() == 1) {
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return;
                }//end if
                break;
                
                case ADMIN:     if (perm.getPrmAdmin() == 1) {
                    log.debug("User: " + user + " granted " + access + " permission on " + element );
                    return;
                }//end if
                break;
                
                //not yet used
                case FINE_GRAINED_ACCESS:   log.warn("User: " + user + " granted " + access + " permission on " + element );
                break;
                
                }//end switch
                
            }//end for
        }//end for
        
        //if we get to here then user does not have permission so we need to throw an exception
        InsufficientPrivilegesException e = new InsufficientPrivilegesException("User: " + user + " does not have permission to perform '" + access + "' operation on " + element );
        log.warn("User: " + user + " does not have permission to perform '" + access + "' operation on " + element );
        throw(e);
    }//end method
}
