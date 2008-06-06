/*
 * ManagerUtil.java
 *
 * Created on 27 February 2007, 15:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Facility;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.Queries;

/**
 *  Class to be extended to allow util methods for EJB3/JPA
 *
 * @author gjd37
 */
public class ManagerUtil {

    // Global class logger
    static Logger log = Logger.getLogger(ManagerUtil.class);

    /////////////////////////////  Getting / Filtering element collections /////////////////////////////
    /**
     * Goes and collects the information associated with the investigation depending on the InvestigationInclude.
     *  See {@link InvestigationInclude}
     *
     * @param userId federalId of the user.
     * @param investigation a investigation
     * @param include The information that is needed to be returned with the investigation
     * @param manager manager object that will facilitate interaction with underlying database
     */
    public static void getInvestigationInformation(String userId, Investigation investigation, InvestigationInclude include, EntityManager manager) {
        Collection<Investigation> investigations = new ArrayList<Investigation>();
        investigations.add(investigation);

        getInvestigationInformation(userId, investigations, include, manager);
    }

    /**
     * Goes and collects the information associated with the investigation depending on the InvestigationInclude.
     *  See {@link InvestigationInclude}
     *
     * @param userId federalId of the user.
     * @param investigations list of investigations
     * @param include The information that is needed to be returned with the investigation
     * @param manager manager object that will facilitate interaction with underlying database
     */
    public static void getInvestigationInformation(String userId, Collection<Investigation> investigations, InvestigationInclude include, EntityManager manager) {
        if (include != null) {
            if (include.toString().equals(InvestigationInclude.NONE.toString())) {
                //do nothing
                return;
            }
            for (Investigation investigation : investigations) {
                log.trace("Setting investigation to include: " + include);
                investigation.setInvestigationInclude(include);

                if (include.isInvestigators()) {
                    log.trace("Including investigators");
                    investigation.getInvestigatorCollection().size();
                }
                if (include.isKeywords()) {
                    log.trace("Including keywords");
                    investigation.getKeywordCollection().size();
                }
                if (include.isPublications()) {
                    log.trace("Including publications");
                    investigation.getPublicationCollection().size();
                }
                if (include.isSamples()) {
                    log.trace("Including samples");
                    investigation.getSampleCollection().size();
                }
                if (include.isShifts()) {
                    log.trace("Including shifts");
                    investigation.getShiftCollection().size();
                }
                if (include.isRoles()) {
                    log.trace("Including roles");
                    //get role (this adds the role)
                    try {
                        GateKeeper.performAuthorisation(userId, investigation, AccessType.READ, manager);
                    } catch (InsufficientPrivilegesException ex) {
                        log.fatal("User has not got access to investigation that search has returned", ex);
                    }
                }

                boolean cascade = false; //do want to cascade to Dfs
                if (include.isDatasetsAndDatafiles()) {
                    log.trace("Including Datasets And Datafiles");
                    investigation.getDatasetCollection().size();
                    filterDatasets(userId, investigation, true, manager);
                    cascade = true;
                } else if (include.isDatasets()) {
                    log.trace("Including Datasets only");
                    investigation.getDatasetCollection().size();
                    filterDatasets(userId, investigation, false, manager);
                }

                //set the investigation includes in the class
                //This is because of JAXWS, it would down load all of the relationships with out this workaround
                // See in Investigation.getInvestigatorCollection_() method
                if (include == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY) {
                    for (Dataset dataset : investigation.getDatasetCollection()) {
                        log.trace("Setting data sets to include: " + DatasetInclude.DATASET_PARAMETERS_ONLY);
                        dataset.setDatasetInclude(DatasetInclude.DATASET_PARAMETERS_ONLY);                       
                    }
                } else if (include.isDatasetsAndDatafiles() && !include.isDatasetsDatafilesAndParameters()) {
                    for (Dataset dataset : investigation.getDatasetCollection()) {
                        log.trace("Setting data sets to include: " + DatasetInclude.DATASET_AND_DATAFILES_ONLY);
                        dataset.setDatasetInclude(DatasetInclude.DATASET_AND_DATAFILES_ONLY);
                        for (Datafile datafile : dataset.getDatafileCollection()) {
                            log.trace("Setting data file to include: " + DatafileInclude.NONE);
                            datafile.setDatafileInclude(DatafileInclude.NONE);
                        }
                    }
                } //override above if only want datafiles and not its parameters
                else if (include.isDatasetsDatafilesAndParameters()) {
                    for (Dataset dataset : investigation.getDatasetCollection()) {
                        log.trace("Setting data sets to include: " + DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
                        dataset.setDatasetInclude(DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
                        for (Datafile datafile : dataset.getDatafileCollection()) {
                            log.trace("Setting data file to include: " + DatafileInclude.ALL);
                            datafile.setDatafileInclude(DatafileInclude.ALL);
                        }
                    }
                }

                //now remove deleted items
                try {
                    investigation.setCascade(Cascade.REMOVE_DELETED_ITEMS, Boolean.valueOf(cascade));
                } catch (InsufficientPrivilegesException ignore) {
                /**not going to thrown on Cascade.REMOVE_DELETED_ITEMS */
                }

            }
        }
    }

    /**
     * Goes and collects the information associated with the dataset depending on the DatasetInclude.
     * See {@link DatasetInclude}
     *
     * @param userId federalId of the user.
     * @param dataset a dataset for gettting more info about it
     * @param include include info
     * @param manager manager object that will facilitate interaction with underlying database
     */
    public static void getDatasetInformation(String userId, Dataset dataset, DatasetInclude include, EntityManager manager) {
        Collection<Dataset> datasets = new ArrayList<Dataset>();
        datasets.add(dataset);

        getDatasetInformation(userId, datasets, include, manager);
    }

    /**
     * Goes and collects the information associated with the dataset depending on the DatasetInclude.
     * See {@link DatasetInclude}
     *
     * @param userId federalId of the user.
     * @param datasets collection of datasets for gettting more info about them
     * @param include include info
     * @param manager manager object that will facilitate interaction with underlying database
     */
    public static void getDatasetInformation(String userId, Collection<Dataset> datasets, DatasetInclude include, EntityManager manager) {
        for (Dataset dataset : datasets) {
            //set the investigation includes in the class
            //This is because of JAXWS, it would down load all of the relationships with out this workaround
            // See in Dataset.getInvestigatorCollection_() method
            dataset.setDatasetInclude(include);
            log.trace("Setting data sets to include: " + include);

            // now collect the information associated with the investigations requested
            if (include.isDatafiles()) {
                log.trace("Including datafiles");

                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatafileCollection().size();
                //now filter the datafiles collection
                filterDatafiles(userId, dataset, true, manager);
            }

            for (Datafile datafile : dataset.getDatafileCollection()) {
                if (include.isDatafilesAndParameters()) {
                    log.trace("Setting data file to include: " + DatafileInclude.ALL);
                    datafile.setDatafileInclude(DatafileInclude.ALL);
                } else {
                    log.trace("Setting data file to include: " + DatafileInclude.NONE);
                    datafile.setDatafileInclude(DatafileInclude.NONE);
                }
            }

            //now remove deleted items
            try {
                dataset.setCascade(Cascade.REMOVE_DELETED_ITEMS, Boolean.valueOf(true));
            } catch (InsufficientPrivilegesException ignore) {
            /**not going to thrown on Cascade.REMOVE_DELETED_ITEMS */
            }
        }
    }

    /**
     * Gets all the Datasets which the user can READ/SELECT depending on the roles in the DB
     * and by if they are deleted
     *
     * @param userId federalId of the user.
     * @param investigations
     * @param cascade does this cascade to datafiles or not
     * @param manager manager object that will facilitate interaction with underlying database
     */
    private static void filterDatasets(String userId, Investigation investigation, boolean cascade, EntityManager manager) {
        Collection<Dataset> datasetsAllowed = new ArrayList<Dataset>();
        for (Dataset dataset : investigation.getDatasetCollection()) {
            try {
                GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
                datasetsAllowed.add(dataset);

                //now filter datafiles
                if (cascade) {
                    filterDatafiles(userId, dataset, cascade, manager);
                }
            } catch (Exception ignore) {
            }
        }
        log.debug("Adding " + datasetsAllowed.size() + " datasets to " + investigation + " from a total of " + investigation.getDatasetCollection().size());
        //now add the datasets to the investigation
        investigation.setDatasetCollection(datasetsAllowed);

    }

    /**
     * Gets all the Datafiles which the user can READ/SELECT depending on the roles in the DB
     * and by if they are deleted
     *
     * @param userId federalId of the user.
     * @param cascade not needed
     * @param datasets
     * @param manager manager object that will facilitate interaction with underlying database
     */
    private static void filterDatafiles(String userId, Dataset dataset, boolean cascade, EntityManager manager) {
         //Changed, no need to check authorisation of single files, this is done  
        //implicitily by the dataset authorisation
        try{
            GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
            dataset.getDatafileCollection();  
            
            //need to add the icat role to each datafile
            for (Datafile datafile :  dataset.getDatafileCollection()) {
                datafile.setIcatRole(dataset.getIcatRole());
            }
            
            log.debug("Adding (allowed to read) " + dataset.getDatafileCollection().size() + " datafiles to " + dataset + " from a total of " + dataset.getDatafileCollection().size());        
        } catch (Exception ignore) {
            log.debug("Adding (allowed to read) 0 datafiles to " + dataset + " from a total of " + dataset.getDatafileCollection().size());        
        }
        
        //Changed, no need to check authorisation of single files, this is done  
        //implicitily by the dataset authorisation
        /*Collection<Datafile> datafilesAllowed = new ArrayList<Datafile>();
        for (Datafile datafile : dataset.getDatafileCollection()) {
            try {
                GateKeeper.performAuthorisation(userId, datafile, AccessType.READ, manager);
                datafilesAllowed.add(datafile);
            } catch (Exception ignore) {
            }
        }
        log.debug("Adding (allowed to read) " + datafilesAllowed.size() + " datafiles to " + dataset + " from a total of " + dataset.getDatafileCollection().size());
        //now add the datasets to the investigation
        dataset.setDatafileCollection(datafilesAllowed);*/

    }

    /**
     * Filters stuff of deleted items
     */
    private static void filterKeywords(Investigation investigation) {
        log.trace("Filtering " + investigation + " of deleted keywords");
        Collection<Keyword> noneDeleted = new ArrayList<Keyword>();

        for (Keyword keyword : noneDeleted) {
            if (!keyword.isDeleted()) {
                noneDeleted.add(keyword);
            }
        }
        investigation.setKeywordCollection(noneDeleted);
    }

    /**
     * Filters stuff of deleted items
     */
    private static void filterPublications(Investigation investigation) {
        log.trace("Filtering " + investigation + " of deleted publications");
        Collection<Publication> noneDeleted = new ArrayList<Publication>();

        for (Publication publication : noneDeleted) {
            if (!publication.isDeleted()) {
                noneDeleted.add(publication);
            }
        }
        investigation.setPublicationCollection(noneDeleted);
    }

    /**
     * Filters stuff of deleted items
     */
    private static void filterInvestigators(Investigation investigation) {
        log.trace("Filtering " + investigation + " of deleted investigators");
        Collection<Investigator> noneDeleted = new ArrayList<Investigator>();

        for (Investigator investigator : noneDeleted) {
            if (!investigator.isDeleted()) {
                noneDeleted.add(investigator);
            }
        }
        investigation.setInvestigatorCollection(noneDeleted);
    }

    /**
     * Filters stuff of deleted items
     */
    private static void filterSamples(Investigation investigation) {
        log.trace("Filtering " + investigation + " of deleted samples");
        Collection<Sample> noneDeleted = new ArrayList<Sample>();

        for (Sample sample : noneDeleted) {
            if (!sample.isDeleted()) {
                noneDeleted.add(sample);
                Collection<SampleParameter> noneDeletedSP = new ArrayList<SampleParameter>();
                for (SampleParameter sampleParameter : sample.getSampleParameterCollection()) {
                    if (!sampleParameter.isDeleted()) {
                        noneDeletedSP.add(sampleParameter);
                    }
                }
                sample.setSampleParameterCollection(noneDeletedSP);
            }
        }
        investigation.setSampleCollection(noneDeleted);
    }

    /////////////////////////////  End of Getting / Filtering element collections /////////////////////////////
    /////////////////////////////  Finding Objects  /////////////////////////////
    /**
     * Checks that the object with primary key exists in the database, if so
     * is returned, does not find deleted objects
     *
     * @param entityClass entity class that you are looking for
     * @param primaryKey primary key of object wanting to find
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @return object if found
     */
    public static <T> T find(Class<T> entityClass, Object primaryKey, EntityManager manager) throws NoSuchObjectFoundException {
        return findObject(entityClass, primaryKey, manager, false);
    }

    private static <T> T findObject(Class<T> entityClass, Object primaryKey, EntityManager manager, boolean findDeleted) throws NoSuchObjectFoundException {

        if (primaryKey == null) {
            throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
        }

        T object = manager.find(entityClass, primaryKey);

        if (object == null) {
            throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
        }
        //if dont want to find deleted items and it is deleted then throw exception
        if (((EntityBaseBean) object).isDeleted() && !findDeleted) {
            log.trace(entityClass.getSimpleName() + "[id: " + primaryKey + "] exists in the database but is deleted.");
            throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
        }
        log.trace(entityClass.getSimpleName() + "[id:" + primaryKey + "] exists in the database");

        return object;
    }

    /**
     * Checks that the object with primary key exists in the database, also finds deleted objects, if so
     * is returned
     *
     * @param entityClass entity class that you are looking for
     * @param primaryKey primary key of object wanting to find
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @return object if found
     */
    public static <T> T findObject(Class<T> entityClass, Object primaryKey, EntityManager manager) throws NoSuchObjectFoundException {
        return findObject(entityClass, primaryKey, manager, true);
    }

    /**
     * Gets the facilityUserId of the user from the federalId
     *
     * @param userId federalId of user
     * @param manager manager object that will facilitate interaction with underlying database
     * @return facilityUserId
     */
    public static String getFacilityUserId(String userId, EntityManager manager) {
        return getFacilityUser(userId, manager).getFacilityUserId();
    }

    /**
     * Gets the facilityUser of the user from the federalId
     *
     * @param userId federalId of user
     * @param manager manager object that will facilitate interaction with underlying database
     * @return facilityUser
     */
    public static FacilityUser getFacilityUser(String userId, EntityManager manager) {
        FacilityUser facilityUser = null;
        try {
            facilityUser = (FacilityUser) manager.createNamedQuery("FacilityUser.findByFederalId").setParameter("fedId", userId).getSingleResult();
            log.trace("" + facilityUser.getFacilityUserId() + " corresponds to " + userId);
            return facilityUser;
        } catch (NoResultException nre) {
            log.warn("federalId: " + userId + " has no associated facility user");
            throw new RuntimeException("FederalId:" + userId + " has no associated facility user in DB.");
        } catch (NonUniqueResultException nonue) {
            log.warn("federalId: " + userId + " has more than one associated facility user.");
            throw new RuntimeException("federalId:" + userId + " has more than one associated facility user.  DB should never allow this error to be thrown.");
        }
    }

    /////////////////////////////  End of Finding Objects  /////////////////////////////
    /**
     * Gets the facility this icat table
     *
     * @param manager
     * @return
     */
    public static Facility getFacility(EntityManager manager) {
        try {
            Collection<Facility> facilities = (Collection<Facility>) manager.createNamedQuery("FacilityUser.findAll").getResultList();
            if (facilities == null || facilities.size() == 0) {
                throw new RuntimeException("This Icat table set up incorrectly");
            }
            return facilities.iterator().next();
        } catch (Exception r) {
            log.warn("This Icat table set up incorrectly", r);
            throw new RuntimeException("This Icat table set up incorrectly");
        }
    }

    ////////////////////////////////////  ICAT AUTHORISATION METHODS //////////////////////////////////////////////
    /**
     * Gets all the IcatAuthorisations for a investigation/dataset/datafile if the user has manager users action on that investigation
     *
     */
    protected static Collection<IcatAuthorisation> getAuthorisations(String userId, Long elementId, ElementType type, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        EntityBaseBean entityObject = null;
        Query query = null;

        if (type == ElementType.INVESTIGATION) {
            entityObject = find(Investigation.class, elementId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE);
        } else if (type == ElementType.DATASET) {
            entityObject = find(Dataset.class, elementId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE);
        } else if (type == ElementType.DATAFILE) {
            entityObject = find(Datafile.class, elementId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE);
        }

        GateKeeper.performAuthorisation(userId, entityObject, AccessType.MANAGE_USERS, manager);

        //user has access to read all the roles etc
        query.setParameter("elementId", elementId);
        query.setParameter("userId", null); //not searching by userId
        query.setParameter("elementType", type);

        Collection<IcatAuthorisation> icatAuthorisations = (Collection<IcatAuthorisation>) query.getResultList();

        log.debug("Found " + icatAuthorisations.size() + " authorisation(s) for " + type + "[id:" + elementId + "]");
        return icatAuthorisations;
    }

    /**
     * Gets all the IcatAuthorisations for a investigation/dataset/datafile (authorisationId) and removes them
     *
     */
    protected static boolean removeElementAuthorisations(Long authorisationId, ElementType type, EntityManager manager) throws NoSuchObjectFoundException {
        EntityBaseBean entityObject = null;
        Query query = null;

        if (type == ElementType.INVESTIGATION) {
            entityObject = find(Investigation.class, authorisationId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID);
        } else if (type == ElementType.DATASET) {
            entityObject = find(Dataset.class, authorisationId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID);
        } else if (type == ElementType.DATAFILE) {
            entityObject = find(Datafile.class, authorisationId, manager);
            query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID);
        }

        //user has access to read all the roles etc
        query.setParameter("elementId", authorisationId);
        query.setParameter("elementType", type);

        Collection<IcatAuthorisation> icatAuthorisations = (Collection<IcatAuthorisation>) query.getResultList();

        log.debug("Found " + icatAuthorisations.size() + " authorisation(s) for " + type + "[id:" + authorisationId + "]");

        if (type == ElementType.INVESTIGATION) {
            Investigation investigation = findObject(Investigation.class, authorisationId, manager);
            for (Dataset ds : investigation.getDatasetCollection()) {
                removeElementAuthorisations(ds.getId(), ElementType.DATASET, manager);
            }
        } else if (type == ElementType.DATASET) {
            Dataset ds = findObject(Dataset.class, authorisationId, manager);
            for (Datafile df : ds.getDatafileCollection()) {
                removeElementAuthorisations(df.getId(), ElementType.DATAFILE, manager);
            }
        } else if (type == ElementType.DATAFILE) {
            //do nothing, deleted below
        }

        //remove root one
        for (IcatAuthorisation icatAuthorisation : icatAuthorisations) {
            log.trace("Removing: " + icatAuthorisation);
            if (icatAuthorisation.getUserChildRecord() != null) {
                IcatAuthorisation child = findObject(IcatAuthorisation.class, icatAuthorisation.getUserChildRecord(), manager);
                log.trace("Removing child to: " + icatAuthorisation + ", :" + child);
                manager.remove(child);
            }
            manager.remove(icatAuthorisation);
        }

        return true;
    }

    /**
     * Deletes Authorisation
     */
    public static void deleteAuthorisation(String userId, Long authorisationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        deleteAuthorisation(userId, authorisationId, AccessType.DELETE, manager);

    }

    /**
     * Removes Authorisation
     */
    public static void removeAuthorisation(String userId, Long authorisationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        deleteAuthorisation(userId, authorisationId, AccessType.REMOVE, manager);
    }

    /**
     * Deletes / Removes a entry in the icat authorisation table
     *
     * @param userId federalId of the user.
     * @param id PK of icatAuthorisation
     * @param type {@link AccessType} object, either REMOVE or DELETE
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    protected static void deleteAuthorisation(String userId, Long authorisationId, AccessType type, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteAuthorisation(" + userId + ", " + authorisationId + ", " + type + ", EntityManager)");

        IcatAuthorisation icatAuthorisation = findObject(IcatAuthorisation.class, authorisationId, manager);

        //if elementId is null, then not there
        if (icatAuthorisation.getElementId() == null) {
            throw new NoSuchObjectFoundException(icatAuthorisation + " not found.");
        }

        if (type == AccessType.DELETE) {
            //check user has delete access
            GateKeeper.performAuthorisation(userId, getRootElement(icatAuthorisation, manager), AccessType.MANAGE_USERS, manager);

            //ok here fo delete
            icatAuthorisation.setDeleted(!icatAuthorisation.isDeleted());
            icatAuthorisation.setModId(userId);

            //delete child record if there
            if (icatAuthorisation.getUserChildRecord() != null) {
                IcatAuthorisation icatAuthorisationChild = findObject(IcatAuthorisation.class, icatAuthorisation.getUserChildRecord(), manager);
                //ok here fo delete
                icatAuthorisationChild.setDeleted(!icatAuthorisation.isDeleted());
                icatAuthorisationChild.setModId(userId);
            }
        } else if (type == AccessType.REMOVE) {
            //check user has delete access
            GateKeeper.performAuthorisation(userId, getRootElement(icatAuthorisation, manager), AccessType.MANAGE_USERS, manager);
            manager.remove(icatAuthorisation);

            //remove child record if there
            if (icatAuthorisation.getUserChildRecord() != null) {
                IcatAuthorisation icatAuthorisationChild = findObject(IcatAuthorisation.class, icatAuthorisation.getUserChildRecord(), manager);
                //ok here fo delete
                manager.remove(icatAuthorisationChild);
            }
        }
    }

    /**
     * Adds a new IcatAuthorisation to the DB, depending on the permissions the level they are wanting to add to the DB
     *
     * @param userId
     * @param toAddUserId
     * @param toAddRole
     * @param id
     * @param manager manager object that will facilitate interaction with underlying database
     * @return
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    protected static IcatAuthorisation addAuthorisation(String userId, String toAddUserId, String toAddRole, Long elementId, ElementType type, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("addAuthorisation(" + userId + ", adding " + toAddUserId + " as role: " + toAddRole + " to investigation:" + elementId + ", EntityManager)");

        EntityBaseBean rootElement = null;

        //check id is a valid element
        if (type == ElementType.INVESTIGATION) {
            rootElement = find(Investigation.class, elementId, manager);
        } else if (type == ElementType.DATASET) {
            rootElement = find(Dataset.class, elementId, manager);
        } else if (type == ElementType.DATAFILE) {
            //Changed, unable to add DataFile authorisation nows
            throw new IllegalArgumentException("Unable to add authorisations for type: "+type);
            //rootElement = find(Datafile.class, elementId, manager);
        }

        //check permissions on investigation
        GateKeeper.performAuthorisation(userId, rootElement, AccessType.MANAGE_USERS, manager);

        //the weight of users role cannot add higher ranking, then all is good.
        IcatRole userIdsRole = rootElement.getIcatRole();
        IcatRole toBeAddedRole = getRole(toAddRole, manager);

        //user cannot add a higher role than themeselves
        if (userIdsRole.isGreaterEqualTo(toBeAddedRole)) {
            IcatAuthorisation icatAuthorisationChild = null;
            Long childId = null;

            if (type == ElementType.INVESTIGATION) {
                if (toBeAddedRole.isActionRootInsert()) {
                    //need to add a another row for creating datasets for this investigation
                    log.trace("Adding " + toAddUserId + " to create datasets on " + rootElement);
                    icatAuthorisationChild = persistAuthorisation(userId, toAddUserId, toBeAddedRole, ElementType.DATASET, null,
                            type, ((Investigation) rootElement).getId(), null, manager);
                }
                if (icatAuthorisationChild != null) {
                    childId = icatAuthorisationChild.getId();
                }
                return persistAuthorisation(userId, toAddUserId, toBeAddedRole, type, ((Investigation) rootElement).getId(),
                        null, null, childId, manager);

            } else if (type == ElementType.DATASET) {
                if (toBeAddedRole.isActionRootInsert()) {
                    //need to add a another row for creating datafiles for this dataset
                    log.trace("Adding " + toAddUserId + " to create datafiles on " + rootElement);
                    icatAuthorisationChild = persistAuthorisation(userId, toAddUserId, toBeAddedRole, ElementType.DATAFILE, null,
                            type, ((Dataset) rootElement).getId(), null, manager);
                }
                if (icatAuthorisationChild != null) {
                    childId = icatAuthorisationChild.getId();
                }
                return persistAuthorisation(userId, toAddUserId, toBeAddedRole,
                        type, ((Dataset) rootElement).getId(),
                        ElementType.INVESTIGATION, ((Dataset) rootElement).getInvestigationId(), childId, manager);

            } else if (type == ElementType.DATAFILE) {
                return persistAuthorisation(userId, toAddUserId, toBeAddedRole, type, ((Datafile) rootElement).getId(),
                        ElementType.DATASET, ((Datafile) rootElement).getDatasetId(), null, manager);
            } else {
                throw new RuntimeException("Element type not supported: " + type);
            }
        } else {
            throw new ValidationException("Cannot add a higher role " + toAddRole + ", than yours " + userIdsRole.getRole() + " for " + rootElement);
        }
    }

    /**
     * Changes a role for IcatAuthorisation to the DB, depending on the permissions the level they are wanting to add to the DB
     *
     * @param userId
     * @param toChangetoRole
     * @param authorisationId
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     */
    public static void updateAuthorisation(String userId, String toChangetoRole, Long authorisationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("updateAuthorisation(" + userId + ", " + toChangetoRole + ", " + authorisationId + ", EntityManager)");

        IcatAuthorisation icatAuthorisation = find(IcatAuthorisation.class, authorisationId, manager);

        //if elementId is null, then not there
        if (icatAuthorisation.getElementId() == null) {
            throw new NoSuchObjectFoundException(icatAuthorisation + " not found.");
        }

        //check investigationId is a valid investigation
        EntityBaseBean rootElement = getRootElement(icatAuthorisation, manager);

        GateKeeper.performAuthorisation(userId, rootElement, AccessType.MANAGE_USERS, manager);

        IcatRole userIdsRole = rootElement.getIcatRole();
        IcatRole newRole = getRole(toChangetoRole, manager);

        log.trace("Users role: " + userIdsRole + " wants to change : " + authorisationId + " to: " + newRole);
        //user cannot add a higher role than themeselves
        if (userIdsRole.isGreaterEqualTo(newRole)) {
            log.debug("Changing " + icatAuthorisation + " to new role " + newRole);
            icatAuthorisation.setRole(newRole);

            //need to check if new role has root insert, if not, check if row in DB with create Root (ie elementId null)
            //and delete it so they dont have create ds, df actions
            if (!newRole.isActionRootInsert() && icatAuthorisation.getUserChildRecord() != null) {

                IcatAuthorisation icatAuthorisationChild = findObject(IcatAuthorisation.class, icatAuthorisation.getUserChildRecord(), manager);

                log.debug("Removing: " + icatAuthorisationChild + " for user " + userId);
                manager.remove(icatAuthorisationChild);
                //removing child
                icatAuthorisation.setUserChildRecord(null);

            } else if (newRole.isActionRootInsert() && icatAuthorisation.getUserChildRecord() == null) {
                //if new role is creator then add a record
                if (rootElement.getRootElementType() == ElementType.INVESTIGATION) {
                    IcatAuthorisation icatAuthorisationChild = persistAuthorisation(userId, icatAuthorisation.getUserId(), newRole,
                            ElementType.DATASET, null, ElementType.INVESTIGATION, ((Investigation) rootElement).getId(), null, manager);

                    icatAuthorisation.setUserChildRecord(icatAuthorisationChild.getId());
                } else if (rootElement.getRootElementType() == ElementType.DATASET) {
                    IcatAuthorisation icatAuthorisationChild = persistAuthorisation(userId, icatAuthorisation.getUserId(), newRole,
                            ElementType.DATAFILE, null, ElementType.DATASET, ((Dataset) rootElement).getId(), null, manager);

                    icatAuthorisation.setUserChildRecord(icatAuthorisationChild.getId());
                }
            }
        } else {
            throw new ValidationException("Cannot change to a higher role " + newRole + ", than yours " + userIdsRole.getRole() + " for " + rootElement);
        }
    }

    /**
     * Gets the role out of the ICAT_ROLE table
     *
     * @param role
     * @param manager
     * @return
     * @throws uk.icat3.exceptions.ValidationException
     */
    protected static IcatRole getRole(String role, EntityManager manager) throws ValidationException {
        IcatRole icatRole = manager.find(IcatRole.class, role);
        if (icatRole == null) {
            throw new ValidationException(role + " is not a valid role.");
        } else {
            return icatRole;
        }
    }

    /**
     * Gets the inv, ds, or df associated with the icatAuthorisation
     */
    protected static EntityBaseBean getRootElement(IcatAuthorisation icatAuthorisation, EntityManager manager) throws NoSuchObjectFoundException {
        if (icatAuthorisation.getElementType() == ElementType.INVESTIGATION) {
            return find(Investigation.class, icatAuthorisation.getElementId(), manager);
        } else if (icatAuthorisation.getElementType() == ElementType.DATASET) {
            return find(Dataset.class, icatAuthorisation.getElementId(), manager);
        } else if (icatAuthorisation.getElementType() == ElementType.DATAFILE) {
            return find(Datafile.class, icatAuthorisation.getElementId(), manager);
        } else {
            throw new NoSuchObjectFoundException("No entity found for " + icatAuthorisation.getElementType() + " " + icatAuthorisation.getElementId());
        }
    }

    /**
     * Adds an authorisation to a inv, ds, df
     */
    protected static IcatAuthorisation persistAuthorisation(String userId, String addedId, IcatRole role, ElementType elementType, Long id, ElementType parentElementType, Long parentId, Long userChildRecord, EntityManager manager) throws ValidationException {
        //now add authorisation
        IcatAuthorisation icatAuthorisation = new IcatAuthorisation();

        icatAuthorisation.setUserId(addedId);
        icatAuthorisation.setElementType(elementType);
        icatAuthorisation.setElementId(id);
        icatAuthorisation.setParentElementType(parentElementType);
        icatAuthorisation.setParentElementId(parentId);
        icatAuthorisation.setUserChildRecord(userChildRecord);
        icatAuthorisation.setCreateId(userId);
        icatAuthorisation.setRole(role);

        //check if this is Valid
        icatAuthorisation.isValid(manager);

        log.debug("Adding: " + role + " to " + elementType + ": " + id + " with parent " + parentElementType + ": " + parentId + " with userChildRecord: " + userChildRecord + " for user " + addedId);
        manager.persist(icatAuthorisation);

        return icatAuthorisation;
    }

    /////////////////////////////////// END OF ICAT AUTHRORISATION METHODS /////////////////////////////////
    public static Parameter addParameter(String userId, EntityManager manager, String name, String units, boolean numeric) {
        log.trace("Adding '" + name + "', '" + units + "' to the parameter table");

        if (userId == null) {
            throw new RuntimeException("UserId should not be null for adding parameter");
        }

        ParameterPK PK = new ParameterPK(units, name);
        Parameter parameter = new Parameter(PK);
        parameter.setNumeric(numeric);

        parameter.setDatasetParameter(true);
        parameter.setDatafileParameter(true);
        parameter.setSampleParameter(true);
        parameter.setDescription("Added by ICAT API as an unverified parameter.");
        parameter.setUnitsLongVersion(units);
        parameter.setVerified(false);
        parameter.setSearchable("Y");
        parameter.setCreateId(userId);

        try {
            manager.persist(parameter);
            return parameter;
        } catch (Exception ex) {
            log.error("Unable to insert new unverified parameter: " + parameter + " into parameter table.", ex);
            return null;
        }
    }

    //TODO is this used??
    public static boolean isUnique(EntityBaseBean entityClass, EntityManager manager) {
        if (entityClass instanceof Dataset) {
            Dataset dataset = (Dataset) entityClass;
            Query query = (Query) manager.createNamedQuery(Queries.DATASET_FINDBY_UNIQUE);
            query = query.setParameter("sampleId", dataset.getSampleId());
            query = query.setParameter("investigation", dataset.getInvestigation());
            query = query.setParameter("datasetType", dataset.getDatasetType());
            query = query.setParameter("name", dataset.getName());

            try {
                log.trace("Looking for: sampleId: " + dataset.getSampleId());
                log.trace("Looking for: investigation: " + dataset.getInvestigation());
                log.trace("Looking for: datasetType: " + dataset.getDatasetType());
                log.trace("Looking for: name: " + dataset.getName());

                Dataset datasetFound = (Dataset) query.getSingleResult();
                log.trace("Returned: " + datasetFound);
                if (datasetFound.getId() != null && datasetFound.getId().equals(dataset.getId())) {
                    log.trace("Dataset found is this dataset");
                    return true;
                } else {
                    log.trace("Dataset found is not this dataset, so no unique");
                    return false;
                }
            } catch (NoResultException nre) {
                log.trace("No results so unique");
                //means it is unique
                return true;
            } catch (Throwable ex) {
                log.warn(ex);
                //means it is unique
                return false;
            }
        } else if (entityClass instanceof Investigation) {
            log.trace("Investigation");
        } else if (entityClass instanceof Datafile) {
            log.trace("Datafile");
        }

        return true;
    }
}
