/*
 * DataFileManager.java
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
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.ElementType;

/**
 * This is the manager class for all operations for data files.
 *
 * These are update, remove, delete, create on data files and data file paramters
 *
 * @author gjd37
 */
public class DataFileManager extends ManagerUtil {

    static Logger log = Logger.getLogger(DataFileManager.class);

    /**
     * Deletes the data file for a user depending if the user's id has delete permissions to delete the data file.
     * Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataFile  object to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        deleteDataFile(userId, dataFile.getId(), manager);
    }

    /**
     * Deletes the data file with ID, for a users depending if the users id has delete permissions to delete the data file from
     * the ID. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataFileId Id of data file to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFile(String userId, Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataFile(" + userId + ", " + dataFileId + ", EntityManager)");

        Datafile dataFile = findObject(Datafile.class, dataFileId, manager);

        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.DELETE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        dataFile.setCascade(Cascade.DELETE, new Boolean(!dataFile.isDeleted()), manager, userId);
    }

    /**
     * Deletes the collection of files for a users depending if the users id has delete permissions to delete the files from
     * their ids. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataFileIds Ids of data files to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataFiles(" + userId + ", " + dataFileIds + ", EntityManager)");

        for (Long dataFileId : dataFileIds) {
            deleteDataFile(userId, dataFileId, manager);
        }
    }
    // TODO:  added boolean to avoid erausre name clash with above method

    /**
     * Deletes the collection of data files for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param userId federalId of the user.
     * @param dataFiles objects to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return to avoid erausre name clash with another method
     */
    public static boolean deleteDataFiles(String userId, Collection<Datafile> dataFiles, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("deleteDataFiles(" + userId + ", " + dataFiles + ", EntityManager)");

        for (Datafile dataFile : dataFiles) {
            deleteDataFile(userId, dataFile, manager);
        }

        return true;
    }

    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param userId federalId of the user.
     * @param dataFileId Id of data file to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataFile(String userId, Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("removeDataFile(" + userId + ", " + dataFileId + ", EntityManager)");

        Datafile dataFile = findObject(Datafile.class, dataFileId, manager);

        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.REMOVE, manager);

        //remove all entries for all of the inv, ds, df from the table
        //Changed, do not need to remove this row for datafile.
        //removeElementAuthorisations(dataFile.getId(), ElementType.DATAFILE, manager);

        manager.remove(dataFile);
    }

    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID
     *
     * @param userId federalId of the user.
     * @param dataFile object to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        removeDataFile(userId, dataFile.getId(), manager);
    }
    ////////////////   End of delete commands       ///////////////////////////

    ////////////////////     Add/Update Commands    ///////////////////
    /**
     * Adds a role for a user to an datafile.
     *
     * @param userId
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @param manager
     */
    public static IcatAuthorisation addAuthorisation(String userId, String toAddUserId, String toAddRole, Long id, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        return addAuthorisation(userId, toAddUserId, toAddRole, id, ElementType.DATAFILE, manager);
    }

    /**
     * Updates data file depending on whether the user has permission to update this data file.
     *
     * @param userId federalId of the user.
     * @param dataFile object to be updated
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the updated {@link Datafile} object
     */
    public static Datafile updateDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("updateDataFile(" + userId + ", " + dataFile + ", EntityManager)");

        Datafile datafileManaged = find(Datafile.class, dataFile.getId(), manager);

        //check user has update access
        GateKeeper.performAuthorisation(userId, datafileManaged, AccessType.UPDATE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        datafileManaged.setModId(userId);
        datafileManaged.merge(dataFile);

        return datafileManaged;
    }

    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data file
     *
     * @param userId federalId of the user.
     * @param dataFile object to be created
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the created data file object
     */
    public static Datafile createDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataFile(" + userId + ", " + dataFile + ", EntityManager)");

        //check investigation exists
        if (dataFile.getDataset() == null) {
            throw new NoSuchObjectFoundException(dataFile + " has no assoicated dataset.");
        }
        Dataset dataset = find(Dataset.class, dataFile.getDataset().getId(), manager);
        dataFile.setDataset(dataset);
        //check id is null
//        dataFile.setId(null);

        //check user has update access
        IcatRole role = GateKeeper.performAuthorisation(userId, dataFile, AccessType.CREATE, manager);

        //now check for facility acquired, if user is icat_admin,set true, if not, its automatically set to false
        if (role.isIcatAdminRole()) {
            log.info("Role for " + dataFile + " is ICAT_ADMIN so setting to facility acquired true");
            dataFile.setCascade(Cascade.FACILITY_ACQUIRED, Boolean.TRUE);
        } else {
            log.info("Role for " + dataFile + " is not ICAT_ADMIN so setting to facility acquired false");
            dataFile.setCascade(Cascade.FACILITY_ACQUIRED, Boolean.FALSE);
        }

        //new dataset, set createid, this sets mod id and modtime
        dataFile.setCascade(Cascade.REMOVE_ID, Boolean.TRUE);
        dataFile.setCascade(Cascade.MOD_AND_CREATE_IDS, userId);


        dataFile.isValid(manager);
        manager.persist(dataFile);
        manager.flush();
        //add new creator role to investigation for the user creating the df
        //Changed, do not need to add a row for datafile.
        //persistAuthorisation(userId, userId, role, ElementType.DATAFILE, dataFile.getId(), ElementType.DATASET, dataset.getId(), null, manager);

        return dataFile;
    }

    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data file
     *
     * @param userId federalId of the user.
     * @param dataFile object to be created
     * @param datasetId Id of data set
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the created {@link Datafile} object
     */
    public static Datafile createDataFile(String userId, Datafile dataFile, Long datasetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataFile(" + userId + ", " + dataFile + ", " + datasetId + ", EntityManager)");

        //check dataset exist
        Dataset dataset = find(Dataset.class, datasetId, manager);
        dataFile.setDataset(dataset);

        return createDataFile(userId, dataFile, manager);
    }

    /**
     * Creates the collection of data files, depending if the user has update permission on the data set associated with the data file.
     *
     * @param userId federalId of the user.
     * @param dataFiles objects to be created
     * @param datasetId Id of data set
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the collection of created {@link Datafile} objects
     */
    public static Collection<Datafile> createDataFiles(String userId, Collection<Datafile> dataFiles, Long datasetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException {
        log.trace("createDataFiles(" + userId + ", " + dataFiles + " " + datasetId + ", EntityManager)");

        Collection<Datafile> datafilesCreated = new ArrayList<Datafile>();
        for (Datafile datafile : dataFiles) {
            Datafile datafileCreated = createDataFile(userId, datafile, datasetId, manager);
            datafilesCreated.add(datafileCreated);
        }

        return datafilesCreated;
    }
    ///////////////   End of add/Update Commands    ///////////////////

    public static Collection<IcatAuthorisation> getAuthorisations(String userId, Long id, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        return getAuthorisations(userId, id, ElementType.DATAFILE, manager);
    }

    /**
     * Gets the data file objects from a list of data file ids, depending if the user has access to read the data files.
     *
     * @param userId federalId of the user.
     * @param dataFileIds Ids of data files
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return a collection of {@link Datafile} objects
     */
    public static Collection<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("getDataFile(" + userId + ", " + dataFileIds + " EntityManager)");

        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
      
        for (Long dataFileId : dataFileIds) {

            //check DataFile exist
            Datafile dataFile = find(Datafile.class, dataFileId, manager);
                    
            //check user has read access
            GateKeeper.performAuthorisation(userId, dataFile, AccessType.READ, manager);

            //add to arraylist
            dataFiles.add(dataFile);
        }

        return dataFiles;
    }

    /**
     * Gets a data file object from a data file id, depending if the user has access to read the data file
     *
     * @param userId federalId of the user.
     * @param dataFileId Id of data file
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return {@link Datafile}
     */
    public static Datafile getDataFile(String userId, Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        Collection<Long> dataFiles = new ArrayList<Long>();
        dataFiles.add(dataFileId);

        Collection<Datafile> datafiles = getDataFiles(userId, dataFiles, manager);
        return datafiles.iterator().next();
    }
    /////////////////////   Util commands /////////////////////////

    /**
     * Updates the data file paramter object, depending if the user has access to update the data file parameter.
     *
     * @param userId federalId of the user.
     * @param datafileParameter object to be updated
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     */
    public static void updateDatafileParameter(String userId, DatafileParameter datafileParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("updateDataSetParameter(" + userId + ", " + datafileParameter + ", EntityManager)");

        DatafileParameter datafileParameterFound = find(DatafileParameter.class, datafileParameter.getDatafileParameterPK(), manager);

        //ok, now check permissions on found data set
        GateKeeper.performAuthorisation(userId, datafileParameterFound, AccessType.UPDATE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        //update model with changed wanted
        datafileParameterFound.merge(datafileParameter);
        datafileParameterFound.setModId(userId);

        datafileParameterFound.isValid(manager);
    }

    /**
     * Adds a data file paramter object to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * If the paramter is marked as deleted then it will be undeleted, if not present a new paramter is added.
     *
     * @param userId federalId of the user.
     * @param datafileParameter object to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the added {@link DatafileParameter} object
     */
    public static DatafileParameter addDataFileParameter(String userId, DatafileParameter datafileParameter, Long datafileId, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataFileParameter(" + userId + ", " + datafileParameter + ", " + datafileId + ", EntityManager)");

        //get datafile,
        Datafile datafile = find(Datafile.class, datafileId, manager);

        //set id for datafileParameter
        datafileParameter.setDatafile(datafile);

        //check is valid, check parent datafile is in the private key
        datafileParameter.setCreateId(userId);
        datafileParameter.isValid(manager);

        datafileParameter.getDatafileParameterPK().setDatafileId(datafileId);

        //ok, now check permissions
        IcatRole role = GateKeeper.performAuthorisation(userId, datafileParameter, AccessType.CREATE, manager);

        //now check for facility acquired, if user is icat_admin,set true, if not, its automatically set to false
        if (role.isIcatAdminRole()) {
            log.info("Role for " + datafileParameter + " is ICAT_ADMIN so setting to facility acquired true");
            datafileParameter.setFacilityAcquiredSet(true);
        }

        try {
            //check dataSetParameterManaged not already added
            DatafileParameter dataFileParameterManaged = findObject(DatafileParameter.class, datafileParameter.getDatafileParameterPK(), manager);

            log.warn(dataFileParameterManaged + " already added to dataset.");
            throw new ValidationException(dataFileParameterManaged + " is not unique");
        } catch (NoSuchObjectFoundException ex) {
            //not already in DB so add
            //sets modId for persist
            datafileParameter.setCreateId(userId);

            manager.persist(datafileParameter);
            return datafileParameter;
        }
    }

    /**
     * Adds a data file paramter object to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * If the paramter is marked as deleted then it will be undeleted, if not present a new paramter is added.
     *
     * @param userId federalId of the user.
     * @param datafileParameter object to be added
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the added {@link DatafileParameter} object
     */
    public static DatafileParameter addDataFileParameter(String userId, DatafileParameter datafileParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataFileParameter(" + userId + ", " + datafileParameter + ", EntityManager)");

        Long datafileId = datafileParameter.getDatafileParameterPK().getDatafileId();
        return addDataFileParameter(userId, datafileParameter, datafileId, manager);
    }

    /**
     * Adds a collection of data file paramter objects to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * If the paramter is marked as deleted then it will be undeleted, if not present a new paramter is added.
     *
     * @param userId federalId of the user.
     * @param datafileParameters collection of objects to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.ValidationException if the data file is invalid
     * @return the added collection of {@link DatafileParameter} objects
     */
    public static Collection<DatafileParameter> addDataFileParameters(String userId, Collection<DatafileParameter> datafileParameters, Long dataFileId, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataFileParameters(" + userId + ", " + datafileParameters + ", " + dataFileId + ", EntityManager)");

        Collection<DatafileParameter> datafileParametersCreated = new ArrayList<DatafileParameter>();
        for (DatafileParameter datafileParameter : datafileParameters) {
            datafileParameter.getDatafileParameterPK().setDatafileId(dataFileId);
            DatafileParameter datafileParameterCreated = addDataFileParameter(userId, datafileParameter, dataFileId, manager);
            datafileParametersCreated.add(datafileParameterCreated);
        }

        return datafileParametersCreated;
    }

    /**
     * Removes (from the database) a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param userId federalId of the user.
     * @param datafileParameter object to be removed
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void removeDatafileParameter(String userId, DatafileParameter datafileParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("removeDatafileParameter(" + userId + ", " + datafileParameter + ", EntityManager)");

        //if(datafileParameter.getDatafileParameterPK() == null) throw new NoSuchObjectFoundException(datafileParameter+" has no assoicated primary key.");
        //Long datafileId = datafileParameter.getDatafileParameterPK().getDatafileId();
        //find the dataset
        DatafileParameter datafileParameterManaged = find(DatafileParameter.class, datafileParameter.getDatafileParameterPK(), manager);

        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, datafileParameterManaged, AccessType.REMOVE, manager);

        datafileParameterManaged.setDatafile(null);

        manager.remove(datafileParameterManaged);
    }

    /**
     * Deletes a data file paramter object, depending if the user has access to delete the data file parameter from
     * the associated data file id.
     *
     * @param userId federalId of the user.
     * @param datafileParameter object to be deleted
     * @param manager manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDatafileParameter(String userId, DatafileParameter datafileParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
        log.trace("deleteDatafileParameter(" + userId + ", " + datafileParameter + ", EntityManager)");

        // if(datafileParameter.getDatafileParameterPK() == null) throw new ValidationException(datafileParameter+" has no assoicated primary key.");
        //  Long datafileId = datafileParameter.getDatafileParameterPK().getDatafileId();
        //find the dataset
        DatafileParameter datafileParameterManaged = findObject(DatafileParameter.class, datafileParameter.getDatafileParameterPK(), manager);

        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, datafileParameterManaged, AccessType.DELETE, manager);
        //String facilityUserId = getFacilityUserId(userId, manager);
        datafileParameterManaged.setModId(userId);
        datafileParameterManaged.setDeleted(!datafileParameterManaged.isDeleted());
    }
}