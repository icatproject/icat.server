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
import uk.icat3.entity.DatasetParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;

/**
 *
 * @author gjd37
 */
public class DataFileManager extends ManagerUtil {
    
    static Logger log = Logger.getLogger(DataFileManager.class);
    
    /**
     * Deletes the data file for a user depending if the user's id has delete permissions to delete the data file
     *
     * @param userId
     * @param dataFile
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        deleteDataFile(userId, dataFile.getId(), manager);
    }
    
    /**
     * Deletes the data file with ID, for a users depending if the users id has delete permissions to delete the data file from
     * the ID
     *
     * @param userId
     * @param dataFileId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFile(String userId, Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFile("+userId+", "+dataFileId+", EntityManager)");
        
        Datafile dataFile = find(Datafile.class, dataFileId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.DELETE, manager);
        
        //delete DataFile (Cascade is true);
        //TODO might have to remove all the datafiles first
        //manager.remove(dataFile);
        //not deleting anymore, jsut changing deleted to Y
        
        log.info("Deleting: "+dataFile);
        dataFile.setCascade(Cascade.DELETE,Boolean.TRUE);
        
    }
    
    /**
     *
     * Deletes the collection of files for a users depending if the users id has delete permissions to delete the files from
     * their ids
     *
     * @param userId
     * @param dataFileIds
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static void deleteDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFiles("+userId+", "+dataFileIds+", EntityManager)");
        
        for(Long dataFileId : dataFileIds){
            deleteDataFile(userId, dataFileId, manager);
        }
    }
    
    // TODO:  added boolean to avoid erausre name clash with above method
    
    /**
     * Deletes the collection of data files for a users depending if the users id has delete permissions to delete the data file
     *
     * @param userId
     * @param dataFiles
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static boolean deleteDataFiles(String userId, Collection<Datafile> dataFiles, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFiles("+userId+", "+dataFiles+", EntityManager)");
        
        for(Datafile dataFile : dataFiles){
            deleteDataFile(userId, dataFile, manager);
        }
        
        return true;
    }
    
    /**
     * Updates / Adds a data file depending on whether the user has permission to update this data file or data set
     *
     * @param userId
     * @param dataFile
     * @param manager
     * @param validate to validate the whole of the datafile
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Datafile updateDataFile(String userId, Datafile dataFile, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("updateDataFile("+userId+", "+dataFile+", EntityManager)");
        
        //check to see if DataFile exists, dont need the returned DataFile as merging
        if(dataFile.getId() != null){
            find(Datafile.class, dataFile.getId(), manager);
        }
        
        //check if valid datafile
        dataFile.isValid(manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.UPDATE, manager);
        
        //if null then update
        if(dataFile.getId() != null){
            dataFile.setModId(userId);
            return manager.merge(dataFile);
        } else {
            //new dataset, set createid, this set mod id
            dataFile.setCascade(Cascade.MOD_AND_CREATE_IDS, userId);
            manager.persist(dataFile);
            return dataFile;
        }
    }
    
    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data set
     *
     * @param userId
     * @param dataFile
     * @param datasetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Datafile createDataFile(String userId, Datafile dataFile, Long datasetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createDataFile("+userId+", "+dataFile+" "+datasetId+", EntityManager)");
        
        //check dataset exist
        Dataset dataset  = find(Dataset.class, datasetId, manager);
        dataFile.setDatasetId(dataset);
        dataFile.setId(null);
        
        return updateDataFile(userId, dataFile, manager);
    }
    
    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data set
     *
     * @param userId
     * @param dataFile
     * @param datasetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     */
    public static Collection<Datafile> createDataFiles(String userId, Collection<Datafile> dataFiles, Long datasetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        log.trace("createDataFiles("+userId+", "+dataFiles+" "+datasetId+", EntityManager)");
        
        Collection<Datafile> datafilesCreated = new ArrayList<Datafile>();
        for(Datafile datafile : dataFiles){
            Datafile datafileCreated = createDataFile(userId, datafile, datasetId, manager);
            datafilesCreated.add(datafileCreated);
        }
        
        return datafilesCreated;
    }
    ///////////////   End of add/Update Commands    ///////////////////
    
    
    /**
     * Gets the data file objects from a list of data file ids, depending if the users has access to read the data files
     *
     * @param userId
     * @param dataFileIds
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static Collection<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        log.trace("getDataFile("+userId+", "+dataFileIds+" EntityManager)");
        
        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
        
        for(Long dataFileId : dataFileIds) {
            
            //check DataFile exist
            Datafile dataFile  = find(Datafile.class, dataFileId, manager);
            
            //check user has read access
            GateKeeper.performAuthorisation(userId, dataFile, AccessType.READ, manager);
            
            //add to arraylist
            dataFiles.add(dataFile);
        }
        
        return dataFiles;
    }
    
    
    /**
     * Gets a data file object from a list of data file id, depending if the users has access to read the data file
     *
     * @param userId
     * @param dataFileId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return
     */
    public static Datafile getDataFile(String userId, Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        Collection<Long> dataFiles = new ArrayList<Long>();
        dataFiles.add(dataFileId);
        
        Collection<Datafile> datafiles =  getDataFiles(userId, dataFiles, manager);
        return datafiles.iterator().next();
    }
    
    /////////////////////   Util commands /////////////////////////
    
    
    public static void updateDatafileParameter(String userId, DatafileParameter datafileParameter, EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("updateDataSetParameter("+userId+", "+datafileParameter+", EntityManager)");
        
        DatafileParameter datafileParameterFound = find(DatafileParameter.class, datafileParameter.getDatafileParameterPK(), manager);
        
        //ok, now check permissions on found data set
        GateKeeper.performAuthorisation(userId, datafileParameterFound, AccessType.UPDATE, manager);
        
        //update model with changed wanted
        datafileParameterFound.merge(datafileParameter);
        
        datafileParameterFound.isValid(manager);
    }
    
    public static DatafileParameter addDataFileParameter(String userId, DatafileParameter datafileParameter, Long datasetId,  EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataFileParameter("+userId+", "+datafileParameter+", "+datasetId+", EntityManager)");
        
        //check if param already in DB
        if(manager.contains(datafileParameter))  throw new ValidationException("DatafileParameter: "+datafileParameter.getDatafileParameterPK().getName()+" with units: "+datafileParameter.getDatafileParameterPK().getUnits()+" is already is a parameter of the datafile.");
        
        //get datafile,
        Datafile datafile = find(Datafile.class, datasetId, manager);
        
        //set id for datafileParameter
        datafileParameter.setDatafile(datafile);
        datafileParameter.setCreateId(userId);
        
        //check is valid, check parent datafile is in the private key
        datafileParameter.isValid(manager);
        
        //ok, now check permissions
        GateKeeper.performAuthorisation(userId, datafileParameter, AccessType.CREATE, manager);
        
        manager.persist(datafileParameter);
        
        return datafileParameter;
    }
    
    public static DatafileParameter addDataFileParameter(String userId, DatafileParameter datafileParameter,  EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException {
        log.trace("addDataFileParameter("+userId+", "+datafileParameter+", EntityManager)");
        
        Long datafileId = datafileParameter.getDatafileParameterPK().getDatafileId();
        return  addDataFileParameter(userId, datafileParameter, datafileId, manager);
        
    }
}
