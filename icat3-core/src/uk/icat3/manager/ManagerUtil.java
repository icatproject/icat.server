/*
 * ManagerUtil.java
 *
 * Created on 27 February 2007, 15:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;

/**
 *  Class to be extended to allow util methods for EJB3/JPA
 *
 * @author gjd37
 */
public class ManagerUtil {
    
    // Global class logger
    static Logger log = Logger.getLogger(ManagerUtil.class);
    
    /**
     * Goes and collects the information associated with the investigation depending on the InvestigationInclude.
     * <br /><br />
     * These are:<br /><br />
     *
     * INVESTIGATORS_ONLY - list of investigators.<br />
     * KEYWORDS_ONLY - list of keywords.<br />
     * INVESTIGATORS_AND_KEYWORDS - all keywords and investigators.<br />
     * DATASETS_ONLY - list of datasets, without the list of data files.<br />
     * DATASETS_AND_DATAFILES - list of all datasets with their list of data file.<br />
     * ALL - all, datasets with file, keywords and investigators.<br />
     * NONE -  only the investigation object with no default lazy information.<br />
     *
     * @param investigations list of investigations
     * @param include The information that is needed to be returned with the investigation
     */
    protected static void getInvestigationInformation(Collection<Investigation> investigations, InvestigationInclude include){
        
        // now collect the information associated with the investigations requested
        if(include.toString().equals(InvestigationInclude.ALL.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information, other wise the collections are null
                
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
                investigation.getDatasetCollection().size();
                investigation.getSampleCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            // return datasets with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_ONLY.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
            }
            // return sample with these investigations
        } else if(include.toString().equals(InvestigationInclude.SAMPLES_ONLY.toString())){
            for(Investigation investigation : investigations){
                investigation.getSampleCollection().size();
            }
            // return datasets and their datafiles with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_AND_DATAFILES.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            // return keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.KEYWORDS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getKeywordCollection().size();
            }
            // return c with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getInvestigatorCollection().size();
            }
            // return investigators and keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_AND_KEYWORDS.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
            }
        } else {
            log.trace("No additional info requested.");
        }
        
        //set the investigation includes in the class
        //This is because of JAXWS, it would down load all of the relationships with out this workaround
        // See in Investigation.getInvestigatorCollection_() method
        for(Investigation investigation : investigations){
            investigation.setInvestigationInclude(include);
            if(include.toString().equals(InvestigationInclude.DATASETS_AND_DATAFILES.toString()) || include.toString().equals(InvestigationInclude.ALL.toString())){
                for(Dataset dataset : investigation.getDatasetCollection()){
                    log.trace("Setting data sets to include: "+DatasetInclude.DATASET_FILES_AND_PARAMETERS);
                    dataset.setDatasetInclude(DatasetInclude.DATASET_FILES_AND_PARAMETERS);
                }
            }
        }
    }
    
    /**
     * Goes and collects the information associated with the dataset depending on the DatasetInclude.
     * <br /><br />
     * These are:<br /><br />
     *
     * DATASET_FILES_ONLY - list of data files. <br />
     * DATASET_PARAMETERS_ONY - list of data parameters.<br />
     * DATASET_FILES_AND_PARAMETERS - both data files and data parameters , ALL.<br />
     * NONE- only the Dataset object with no default lazy information.<br />
     *
     * @param datasets
     * @param include
     */
    protected static void getDatasetInformation(Collection<Dataset> datasets, DatasetInclude include){
        
        // now collect the information associated with the investigations requested
        if(include.toString().equals(DatasetInclude.DATASET_FILES_ONLY.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatafileCollection().size();
            }
        } else  if(include.toString().equals(DatasetInclude.DATASET_FILES_AND_PARAMETERS.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatafileCollection().size();
                dataset.getDatasetParameterCollection().size();
            }
        } else  if(include.toString().equals(DatasetInclude.DATASET_PARAMETERS_ONY.toString())){
            for(Dataset dataset : datasets){
                //size invokes the JPA to get the information, other wise the collections are null
                dataset.getDatasetParameterCollection().size();
            }
        }
    }
    
    /**
     * Checks that the investigation with investigationId exists in the database, if so
     * it is returned
     *
     * @param investigationId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException
     * @return Investigation if found
     */
    protected static Investigation checkInvestigation(Long investigationId, EntityManager manager) throws NoSuchObjectFoundException {
        Investigation investigation = manager.find(Investigation.class, investigationId);
        //check if the id exists in the database
        if(investigation == null) throw new NoSuchObjectFoundException("Investigation: id: "+investigationId+" not found.");
        
        log.trace("Investigation: id: "+investigationId+" exists in the database");
        
        return investigation;
    }
    
    /**
     * Checks that the Dataset with datasetId exists in the database, if so
     * it is returned
     *
     * @param datasetId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException
     * @return Dataset if found
     */
    protected static Dataset checkDataSet(Long datasetId, EntityManager manager) throws NoSuchObjectFoundException {
        Dataset dataset = manager.find(Dataset.class, datasetId);
        //check if the id exists in the database
        if(dataset == null) throw new NoSuchObjectFoundException("Dataset: id: "+datasetId+" not found.");
        
        log.trace("DataSet: id: "+datasetId+" exists in the database");
        
        return dataset;
    }
    
    /**
     * Checks that the Datafile with dataFileId exists in the database, if so
     * is returned
     *
     * @param dataFileId
     * @param manager
     * @throws javax.persistence.EntityNotFoundException
     * @return Datafile if found
     */
    protected static Datafile checkDataFile(Long dataFileId, EntityManager manager) throws NoSuchObjectFoundException {
        Datafile dataFile = manager.find(Datafile.class, dataFileId);
        //check if the id exists in the database
        if(dataFile == null) throw new NoSuchObjectFoundException("DataFile: id: "+dataFileId+" not found.");
        
        log.trace("DataFile: id: "+dataFileId+" exists in the database");
        
        return dataFile;
    }
    
    public static <T> T find(Class<T> entityClass, Object primaryKey, EntityManager manager) throws NoSuchObjectFoundException{
        T object = manager.find(entityClass, primaryKey);
        
        if(object == null) throw new NoSuchObjectFoundException(entityClass.getSimpleName()+": id: "+primaryKey+" not found.");
        
        log.trace(entityClass.getSimpleName()+": id: "+primaryKey+" exists in the database");
        
        return object;
    }
}
