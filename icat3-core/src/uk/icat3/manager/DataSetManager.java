/*
 * DataSetManager.java
 *
 * Created on 27 February 2007, 13:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;

/**
 * This is the manager class for all operations for data sets.
 *
 * These are update, remove, delete, create on data sets and data set paramters and dataset sample
 *
 * @author gjd37
 */
public class DataSetManager extends ManagerUtil {

    static Logger log = Logger.getLogger(DataSetManager.class);
    ////////////////   Delete commands       ///////////////////////////

    /**
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set.
     * Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     *
     *  @param userId federalId of the user.
     * @param dataSet  object to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        deleteDataSet(userId, dataSet.getId(), manager);
    }

    /**
     *
     * Deletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID. Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataSetId  object Id to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataSet(" + userId + ", " + dataSetId + ", EntityManager)");

        Dataset dataset = findObject(Dataset.class, dataSetId, manager);

        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.DELETE, manager);
        dataset.setCascade(Cascade.DELETE, new Boolean(!dataset.isDeleted()), manager, userId);
    }

    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param userId federalId of the user.
     * @param dataSetId  object Id to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("removeDataSet(" + userId + ", " + dataSetId + ", EntityManager)");

        Dataset dataset = findObject(Dataset.class, dataSetId, manager);

        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.REMOVE, manager);

        log.info("Removing: " + dataset);

        //remove all entries for all of the inv, ds, df from the table
        removeElementAuthorisations(dataset.getId(), ElementType.DATASET, manager);

        manager.remove(dataset);
    }

    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param userId federalId of the user.
     * @param dataSet object to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        removeDataSet(userId, dataSet.getId(), manager);
    }

    /**
     *
     * Removes a collection of data set for a user depending if the users id has remove permissions to delete the data set from the
     * data set ID.
     *
     * @param userId federalId of the user.
     * @param dataSetIds collection of dataset ids
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return true to avoid erausre name clash with another method
     *
     */
    public static boolean removeDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("removeDataSet(" + userId + ", " + dataSetIds + ", EntityManager)");

        for (Long dataSetId : dataSetIds) {
            removeDataSet(userId, dataSetId, manager);
        }
        return true;
    }

    /**
     * Deletes the collection of data set for a user depending if the users id has delete permissions to delete the data sets from the
     * data set IDs.  Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataSetIds collection of dataset ids
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataSets(" + userId + ", " + dataSetIds + ", EntityManager)");

        for (Long dataSetId : dataSetIds) {
            deleteDataSet(userId, dataSetId, manager);
        }
    }

    //TODO:  added boolean to avoid erausre name clash with above method
    /**
     *
     * Deletes the collection of data sets for a user depending if the user's id has delete permissions to delete the data sets.
     * Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataSets collection of datasets
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return true to avoid erausre name clash with another method
     */
    public static boolean deleteDataSets(String userId, Collection<Dataset> dataSets, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataSets(" + userId + ", " + dataSets + ", EntityManager)");

        for (Dataset dataSet : dataSets) {
            deleteDataSet(userId, dataSet, manager);
        }

        return true;
    }
    ////////////////   End of delete commands       ///////////////////////////

    ////////////////////     Add/Update Commands    ///////////////////
    /**
     * Adds a role for a user to an dataset.
     *
     * @param userId
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @param manager
     */
    public static IcatAuthorisation addAuthorisation(String userId, String toAddUserId, String toAddRole, Long id, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        return addAuthorisation(userId, toAddUserId, toAddRole, id, ElementType.DATASET, manager);
    }

    /**
     * Creates a data set, depending if the user has create permission on the data set associated with the investigation
     *
     * @param userId federalId of the user.
     * @param dataSet object to be created
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return {@link Dataset} that was created
     */
    public static Dataset createDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataSet(" + userId + ", " + dataSet + ", EntityManager)");

        //check investigation exists
        if (dataSet.getInvestigation() == null) {
            throw new NoSuchObjectFoundException(dataSet + " has no assoicated investigation");
        }
        Investigation investigation = find(Investigation.class, dataSet.getInvestigation().getId(), manager);

        dataSet.setInvestigation(investigation);

        //check user has update access
        IcatRole role = GateKeeper.performAuthorisation(userId, dataSet, AccessType.CREATE, manager);

        //now check for facility acquired, if user is icat_admin,set true, if not, its automatically set to false
        if (role.isIcatAdminRole()) {
            log.info("Role for " + dataSet + " is ICAT_ADMIN so setting to facility acquired true");
            dataSet.setCascade(Cascade.FACILITY_ACQUIRED, Boolean.TRUE);
        } else {
            log.info("Role for " + dataSet + " is not ICAT_ADMIN so setting to facility acquired false");
            dataSet.setCascade(Cascade.FACILITY_ACQUIRED, Boolean.FALSE);
        }

        //new dataset, set createid, this sets mod id and modtime
        dataSet.setCascade(Cascade.MOD_AND_CREATE_IDS, userId);
        dataSet.setCascade(Cascade.REMOVE_ID, Boolean.TRUE);

        dataSet.isValid(manager);

        //iterate over datafiles and create them manually and then remove them before creating dataset
        Collection<Datafile> datafiles = dataSet.getDatafileCollection();
        //dont let JPA create datasets
        dataSet.setDatafileCollection(null);
        manager.persist(dataSet);

        //need to add a another row for creating datasets for this ds
        IcatAuthorisation IcatAuthorisationChild = persistAuthorisation(userId, userId, role, ElementType.DATAFILE, null, ElementType.DATASET, dataSet.getId(), null, manager);
        //add new creator role to ds for the user creating the ds
        persistAuthorisation(userId, userId, role, ElementType.DATASET, dataSet.getId(), ElementType.INVESTIGATION, investigation.getId(), IcatAuthorisationChild.getId(), manager);
        //add SUPER_USER to ds for the user creating the ds
        IcatRole superRole = manager.find(IcatRole.class, "SUPER");
        if(superRole == null) superRole = role;
        persistAuthorisation(userId, IcatRoles.SUPER_USER.toString(), superRole, ElementType.DATASET, dataSet.getId(), ElementType.INVESTIGATION, investigation.getId(), null, manager);
        
        //now manually create the data files
        if (datafiles != null) {
            DataFileManager.createDataFiles(userId, datafiles, dataSet.getId(), manager);
        }
        return dataSet;
    }

    /**
     * Updates a data set depending on whether the user has permission to update this data set or its investigation
     *
     * @param userId federalId of the user.
     * @param dataSet object to be created
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return {@link Dataset} that was updated
     */
    public static Dataset updateDataSet(String userId, Dataset dataSet, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("updateDataSet(" + userId + ", " + dataSet + ", EntityManager)");

        Dataset datasetManaged = find(Dataset.class, dataSet.getId(), manager);

        //check user has update access
        GateKeeper.performAuthorisation(userId, datasetManaged, AccessType.UPDATE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        datasetManaged.setModId(userId);
        datasetManaged.merge(dataSet);

        datasetManaged.isValid(manager);

        return datasetManaged;
    }

    /**
     * Creates a data set, depending if the user has create permission on the data set associated with the investigation
     *
     * @param userId federalId of the user.
     * @param dataSet object to be created
     * @param investigationId id of investigations to added the datasets to
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return {@link Dataset} that was created
     */
    public static Dataset createDataSet(String userId, Dataset dataSet, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataSet(" + userId + ", " + dataSet + ", " + investigationId + ", EntityManager)");

        //check investigation exists
        Investigation investigation = find(Investigation.class, investigationId, manager);
        dataSet.setInvestigation(investigation);

        return createDataSet(userId, dataSet, manager);
    }

    /**
     * Creates a collection of data sets, depending if the user has update permission on the data set associated with the investigation
     *
     * @param userId federalId of the user.
     * @param dataSets collection of the datasets
     * @param investigationId id of investigations to added the datasets to
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return collection of {@link Dataset}s that were created
     */
    public static Collection<Dataset> createDataSets(String userId, Collection<Dataset> dataSets, Long investigationId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataSets(" + userId + ", " + dataSets + " " + investigationId + ", EntityManager)");

        Collection<Dataset> datasetsCreated = new ArrayList<Dataset>();
        for (Dataset dataset : dataSets) {
            Dataset datasetCreated = createDataSet(userId, dataset, investigationId, manager);
            datasetsCreated.add(datasetCreated);
        }

        return datasetsCreated;
    }
    ///////////////   End of add/Update Commands    ///////////////////

    ////////////////////    Get Commands    /////////////////////////
    public static Collection<IcatAuthorisation> getAuthorisations(String userId, Long id, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        return getAuthorisations(userId, id, ElementType.DATASET, manager);
    }

    /**
     * Gets the data sets objects from a list of data set ids, depending if the users has access to read the data sets.
     * Also gets extra information regarding the data set.  See {@link DatasetInclude}
     *
     * @param userId federalId of the user.
     * @param dataSetIds Ids of objects
     * @param includes other information wanted with the data set
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Dataset}s
     */
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, DatasetInclude includes, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("getDataSets(" + userId + ", " + dataSetIds + ", " + includes + ", EntityManager)");

        Collection<Dataset> datasets = new ArrayList<Dataset>();

        for (Long dataSetId : dataSetIds) {

            //check Dataset exist
            Dataset dataset = find(Dataset.class, dataSetId, manager);

            //check user has read access
            GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);

            //add to arraylist
            datasets.add(dataset);
        }

        //add include information
        getDatasetInformation(userId, datasets, includes, manager);

        return datasets;
    }

    /**
     *  Gets the data sets objects from a list of data set ids, depending if the users has access to read the data sets
     *
     * @param userId federalId of the user.
     * @param dataSetIds Ids of objects
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Dataset}s
     */
    public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        return getDataSets(userId, dataSetIds, DatasetInclude.NONE, manager);
    }

    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     *
     * @param userId federalId of the user.
     * @param dataSetId Id of object
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return {@link Dataset}
     */
    public static Dataset getDataSet(String userId, Long dataSetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("getDataSet(" + userId + ", " + dataSetId + " EntityManager)");

        Collection<Long> datasets = new ArrayList<Long>();
        datasets.add(dataSetId);

        Collection<Dataset> datasetsReturned = getDataSets(userId, datasets, DatasetInclude.NONE, manager);
        return datasetsReturned.iterator().next();
    }

    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     *
     * @param userId federalId of the user.
     * @param dataSetId Id of object
     * @param includes other information wanted with the data set
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return {@link Dataset}
     */
    public static Dataset getDataSet(String userId, Long dataSetId, DatasetInclude includes, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("getDataSet(" + userId + ", " + dataSetId + ", " + includes + ", EntityManager)");

        Collection<Long> datasets = new ArrayList<Long>();
        datasets.add(dataSetId);

        Collection<Dataset> datasetsReturned = getDataSets(userId, datasets, includes, manager);
        return datasetsReturned.iterator().next();
    }
    ////////////////////    End of get Commands    /////////////////////////

    /////////////////////   Util commands /////////////////////////
    /**
     * Sets the dataset sample id, depending if the users has access to update the data set
     *
     * @param userId federalId of the user.
     * @param sampleId Id of sample
     * @param datasetid Id of dataset
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     */
    public static void setDataSetSample(String userId, Long sampleId, Long datasetid, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("setDataSetSample(" + userId + ", " + sampleId + ", " + datasetid + ", EntityManager)");

        //check valid sample id
        Sample sampleRef = find(Sample.class, sampleId, manager);

        //get dataset,
        Dataset datasetManaged = find(Dataset.class, datasetid, manager);

        outer:
        if (sampleId != null) {
            Collection<Sample> samples = datasetManaged.getInvestigation().getSampleCollection();
            for (Sample sample : samples) {
                if (sample.getId().equals(sampleId)) {
                    //invest has for this sample in
                    break outer;
                }
            }
            //if here not got sample in
            throw new ValidationException("Sample[id=" + sampleId + "] is not associated with Dataset[id=" + datasetManaged.getId() + "]'s invesigation.");
        }
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, datasetManaged, AccessType.UPDATE, manager);

        //String facilityUserId = getFacilityUserId(userId, manager);
        //add the dataset parameter to the dataset
        datasetManaged.setSampleId(sampleId);
        datasetManaged.setModId(userId);

        //TODO her eto put this check is valid,
        datasetManaged.isValid(manager, false);
    }

    /**
     * Adds a data set paramter to a dataset, depending if the users has access to create the data set paramter
     *
     * @param userId federalId of the user.
     * @param dataSetParameter object to be created
     * @param datasetId Id of dataset
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return created {@link DatasetParameter}
     */
    public static DatasetParameter addDataSetParameter(String userId, DatasetParameter dataSetParameter, Long datasetId, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataSetParameter(" + userId + ", " + dataSetParameter + ", " + datasetId + ", EntityManager)");

        //get dataset,
        Dataset dataset = find(Dataset.class, datasetId, manager);

        //set id for dataSetParameter
        dataSetParameter.setDataset(dataset);

        //check is valid, check parent dataset is in the private key
        dataSetParameter.setCreateId(userId);
        dataSetParameter.isValid(manager);

        dataSetParameter.getDatasetParameterPK().setDatasetId(datasetId);

        //ok, now check permissions
        IcatRole role = GateKeeper.performAuthorisation(userId, dataSetParameter, AccessType.CREATE, manager);

        //now check for facility acquired, if user is icat_admin,set true, if not, its automatically set to false
        if (role.isIcatAdminRole()) {
            log.info("Role for " + dataSetParameter + " is ICAT_ADMIN so setting to facility acquired true");
            dataSetParameter.setFacilityAcquiredSet(true);
        }

        try {
            //check dataSetParameterManaged not already added
            DatasetParameter dataSetParameterManaged = findObject(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);
            /*if(dataSetParameterManaged.isDeleted()){
            dataSetParameterManaged.setDelete(false);
            dataSetParameterManaged.setModId(facilityUserId);
            log.info(dataSetParameterManaged +" been deleted, undeleting now.");
            return dataSetParameterManaged;
            } else {*/
            //do nothing, throw exception
            log.warn(dataSetParameterManaged + " already added to dataset.");
            throw new ValidationException(dataSetParameterManaged + " is not unique");
            //}
        } catch (NoSuchObjectFoundException ex) {
            //not already in DB so add
            //sets modId for persist
            dataSetParameter.setCreateId(userId);
            manager.persist(dataSetParameter);
            return dataSetParameter;
        }
    }

    /**
     * Adds a data set paramter to a dataset, depending if the users has access to create the data set paramter
     *
     * @param userId federalId of the user.
     * @param dataSetParameter object to be created
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return created {@link DatasetParameter}
     */
    public static DatasetParameter addDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataSetParameter(" + userId + ", " + dataSetParameter + ", EntityManager)");

        //check investigation exists
        if (dataSetParameter.getDatasetParameterPK() == null) {
            throw new ValidationException(dataSetParameter + " has no assoicated primary key.");
        }
        Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        return addDataSetParameter(userId, dataSetParameter, datasetId, manager);
    }

    /**
     * Adds a collection of data set paramters to a dataset, depending if the users has access to create the data set paramter
     *
     * @param userId federalId of the user.
     * @param dataSetParameters object to be created
     * @param dataSetId id of data set
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data set is invalid
     * @return created {@link DatasetParameter}
     */
    public static Collection<DatasetParameter> addDataSetParameters(String userId, Collection<DatasetParameter> dataSetParameters, Long dataSetId, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataSetParameters(" + userId + ", " + dataSetParameters + ", " + dataSetId + ", EntityManager)");

        Collection<DatasetParameter> datasetParametersCreated = new ArrayList<DatasetParameter>();
        for (DatasetParameter dataSetParameter : dataSetParameters) {
            dataSetParameter.getDatasetParameterPK().setDatasetId(dataSetId);
            DatasetParameter datafileParameterCreated = addDataSetParameter(userId, dataSetParameter, dataSetId, manager);
            datasetParametersCreated.add(datafileParameterCreated);
        }

        return datasetParametersCreated;
    }

    /**
     * Removes (from the database) a data set paramter object, depending if the user has access to remove the data set parameter from
     * the associated data set id.
     *
     * @param userId federalId of the user.
     * @param dataSetParameter object to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("removeDataSetParameter(" + userId + ", " + dataSetParameter + ", EntityManager)");

        //check investigation exists
        //if(dataSetParameter.getDatasetParameterPK() == null) throw new ValidationException(dataSetParameter+" has no assoicated primary key.");
        // Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        //find the dataset
        DatasetParameter dataSetParameterManaged = find(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);

        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, dataSetParameterManaged, AccessType.REMOVE, manager);

        dataSetParameterManaged.setDataset(null);

        manager.remove(dataSetParameterManaged);
    }

    /**
     * Deletes a data set paramter object, depending if the user has access to delete the data set parameter from
     * the associated data set id.   Deleting the set marks it as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataSetParameter object to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("deleteDataSetParameter(" + userId + ", " + dataSetParameter + ", EntityManager)");

        //check investigation exists
        //  if(dataSetParameter.getDatasetParameterPK() == null) throw new ValidationException(dataSetParameter+" has no assoicated primary key.");
        // Long datasetId = dataSetParameter.getDatasetParameterPK().getDatasetId();
        //find the dataset
        DatasetParameter dataSetParameterManaged = findObject(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);

        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, dataSetParameterManaged, AccessType.DELETE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        dataSetParameterManaged.setModId(userId);
        dataSetParameterManaged.setDeleted(!dataSetParameterManaged.isDeleted());
    }

    /**
     * Updates the data file paramter object, depending if the user has access to update the data file parameter.
     *
     * @param userId federalId of the user.
     * @param dataSetParameter object to be updated
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     */
    public static void updateDataSetParameter(String userId, DatasetParameter dataSetParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("updateDataSetParameter(" + userId + ", " + dataSetParameter + ", EntityManager)");

        // if(dataSetParameter.getDatasetParameterPK() == null) throw new ValidationException(dataSetParameter+" has no assoicated primary key.");
        DatasetParameter dataSetParameterFound = find(DatasetParameter.class, dataSetParameter.getDatasetParameterPK(), manager);

        //ok, now check permissions on found data set
        GateKeeper.performAuthorisation(userId, dataSetParameterFound, AccessType.UPDATE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        //update model with changed wanted
        dataSetParameterFound.merge(dataSetParameter);
        dataSetParameterFound.setModId(userId);

        //check is valid, check parent dataset is in the private key
        dataSetParameter.isValid(manager, false);
    }
}