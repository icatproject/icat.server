/*
 * DatasetUtil.java
 *
 * Created on 27 February 2007, 15:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Collection;
import uk.icat3.entity.Dataset;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
public class DatasetUtil {
    
    public static void getDatasetInformation(Collection<Dataset> datasets, DatasetInclude include){
        
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
}
