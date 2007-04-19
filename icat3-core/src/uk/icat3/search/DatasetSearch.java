/*
 * DatafileSearch.java
 *
 * Created on 22 February 2007, 08:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;
import static uk.icat3.util.Queries.*;
/**
 * Searchs on the datasets for samples and list types and status' of datasets.
 *
 * @author gjd37
 */
public class DatasetSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(DatasetSearch.class);
    
    /**
     * From a sample name, return all the datasets a user can view asscoiated with the sample name
     *
        * @param userId federalId of the user.    
     * @param sampleName sample name wiching to search on
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of datasets returned from search
     */
    public static Collection<Dataset> getBySampleName(String userId, String sampleName, EntityManager manager) {
        log.trace("getBySampleId("+userId+", "+sampleName+", EntityManager)");
        
        Collection<Dataset> dataSets = (Collection<Dataset>)manager.createNamedQuery("Dataset.getBySampleId").setParameter("sampleName",sampleName).getResultList();
        
        Collection<Dataset> dataSetsPermssion = new ArrayList<Dataset>();
        
        //check have permission
        for(Dataset dataset : dataSets){
            try{
                //check read permission
                GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
                
                //add dataset to list returned to user
                log.trace("Adding dataset Id:"+dataset.getId()+" to returned list");
                dataSetsPermssion.add(dataset);
                
                //add the DataSetInclude for JAXB
                dataset.setDatasetInclude(DatasetInclude.DATASET_FILES_ONLY);
                
            } catch(InsufficientPrivilegesException ignore){
                //user does not have read access to these to dont add
            }
        }
        
        return dataSets;
    }
    
    /**
     *  List all the valid avaliable types' for datasets
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of types'
     */
    public Collection<DatasetType> listDatasetTypes(EntityManager manager) {
        log.trace("listDatasetTypes(EntityManager)");
        
        return manager.createNamedQuery("DatasetType.getAll").getResultList();
    }
    
    /**
     * List all the valid avaliable status' for datasets
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return collection of status'
     */
    public Collection<DatasetStatus> listDatasetStatus(EntityManager manager) {
        log.trace("listDatasetStatus(EntityManager)");
        
        return manager.createNamedQuery("DatasetStatus.getAll").getResultList();
    }
    
}
