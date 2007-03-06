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
import javax.persistence.EntityNotFoundException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;

/**
 *
 * @author gjd37
 */
public class DataFileManager {
    
    static Logger log = Logger.getLogger(DataFileManager.class);
    
    public void deleteDataFile(String userId, Datafile dataFile, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        
        deleteDataFile(userId, dataFile.getId(), manager);
        
    }
    
    public void deleteDataFile(String userId, Long dataFileId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFile("+userId+", "+dataFileId+", EntityManager)");
        
        Datafile dataFile = checkDataFile(dataFileId, manager);
        
        //check user has delete access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.DELETE, manager);
        
        //delete DataFile (Cascade is true);
        //TODO might have to remove all the datafiles first
        manager.remove(dataFile);
        
    }
    
    public void deleteDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFiles("+userId+", "+dataFileIds+", EntityManager)");
        
        for(Long dataFileId : dataFileIds){
            deleteDataFile(userId, dataFileId, manager);
        }
    }
    
    //added boolean to avoid erausre name clash
    //TODO
    public boolean deleteDataFiles(String userId, Collection<Datafile> dataFiles, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("deleteDataFiles("+userId+", "+dataFiles+", EntityManager)");
        
        for(Datafile dataFile : dataFiles){
            deleteDataFile(userId, dataFile, manager);
        }
        
        return true;
    }
    
    public void updateDataFile(String userId, Datafile dataFile, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("updateDataFile("+userId+", "+dataFile+", EntityManager)");
        
        //check to see if DataFile exists, dont need the returned DataFile as merging
        checkDataFile(dataFile.getId(), manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.UPDATE, manager);
        
        manager.merge(dataFile);
        
    }
    
    public void addDataFile(String userId, Datafile dataFile, Long investigationId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataFile("+userId+", "+dataFile+" "+investigationId+", EntityManager)");
        
        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
        dataFiles.add(dataFile);
        
        addDataFiles(userId, dataFiles, investigationId, manager);
    }
    
    public void addDataFiles(String userId, Collection<Datafile> dataFiles, Long datasetId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("addDataFile("+userId+", "+dataFiles+" "+datasetId+", EntityManager)");
        
        //check dataset exist
        Dataset dataset  = DataSetManager.checkDataSet(datasetId, manager);
        
        //check user has update access
        GateKeeper.performAuthorisation(userId, dataset, AccessType.UPDATE, manager);
        
        for(Datafile dataFile : dataFiles){
            dataset.addDataFile(dataFile);
        }
    }
    
    public Collection<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        log.trace("getDataFile("+userId+", "+dataFileIds+" EntityManager)");
        
        Collection<Datafile> dataFiles = new ArrayList<Datafile>();
        
        for(Long dataFileId : dataFileIds) {
            
            //check DataFile exist
            Datafile dataFile  = checkDataFile(dataFileId, manager);
            
            //check user has read access
            GateKeeper.performAuthorisation(userId, dataFile, AccessType.READ, manager);
            
            //add to arraylist
            dataFiles.add(dataFile);
        }
        
        return dataFiles;
    }
    
    
    public Collection<Datafile> getDataFile(String userId, Long dataFileId, EntityManager manager) throws EntityNotFoundException, InsufficientPrivilegesException{
        Collection<Long> dataFiles = new ArrayList<Long>();
        dataFiles.add(dataFileId);
        
        return getDataFiles(userId, dataFiles, manager);
    }
    
    private Datafile checkDataFile(Long dataFileId, EntityManager manager) throws EntityNotFoundException {
        Datafile dataFile = manager.find(Datafile.class, dataFileId);
        //check if the id exists in the database
        if(dataFile == null) throw new EntityNotFoundException("DataFile: id: "+dataFileId+" not found.");
        
        log.trace("DataFile: id: "+dataFileId+" exists in the database");
        
        return dataFile;
    }
    
}
