/*
 * DatasetInclude.java
 *
 * Created on 27 February 2007, 15:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 * Set of information to return with datasets, ie their datafile and datafile parameters.
 * Having more information returned means the query will take longer.
 *
 * @author gjd37
 */
public enum DatasetInclude {
    
    /**
     * list of data files
     */
    
    DATASET_AND_DATAFILES_ONLY,
    DATASET_PARAMETERS_ONLY,
    DATASET_DATAFILES_AND_PARAMETERS,
    NONE;
    
    public boolean isDatafiles(){
        if(this == DatasetInclude.DATASET_AND_DATAFILES_ONLY ||
                this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
    public boolean isDatafilesAndParameters(){
        if(this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
    public boolean isDatasetParameters(){
        if(this == DatasetInclude.DATASET_PARAMETERS_ONLY ||
                this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS  ) return true;
        else return false;
    }    
  
}
