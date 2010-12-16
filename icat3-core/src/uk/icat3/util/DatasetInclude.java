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
//    ALL_DATASET_ID,
//    DATASET_NUMBER_OF_RESULTS,
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

//    /**
//     * Check if this object is a ALL_DATASET_ID enum. If this
//     * option is selected, only the Dataset Id are returned.
//     *
//     * @return True if this object is ALL_DATASET_ID, otherwise false.
//     */
//    public boolean isAllDatasetId () {
//        if (this == DatasetInclude.ALL_DATASET_ID)
//            return true;
//        return false;
//    }
  

    /**
     * Check if this object is a DATASET_NUMBER_OF_RESULTS enum. In that case
     * the objects returned will a number which represent the number of results.
     * @return
     */
//    public boolean isDatasetNumberOfResults () {
//        if (this == DatasetInclude.DATASET_NUMBER_OF_RESULTS)
//            return true;
//        return false;
//    }
}
