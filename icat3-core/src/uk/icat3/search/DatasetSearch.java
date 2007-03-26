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
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;
import static uk.icat3.util.Queries.*;
/**
 *
 * @author gjd37
 */
public class DatasetSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(DatasetSearch.class);
    
    /**
     * From a sample name, return all the datasets a user can view asscoiated with the sample name 
     *
     * @param userId 
     * @param sampleName 
     * @param manager     
     * @return Collection of datasets
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
    
}
