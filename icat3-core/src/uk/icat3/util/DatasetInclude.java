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
    DATASET_FILES_ONLY,
    /**
     * list of data parameters
     */
    DATASET_PARAMETERS_ONY,
    /**
     *  both data files and data parameters, all
     */
    DATASET_FILES_AND_PARAMETERS,
    /**
     * only the Dataset object with no default lazy information
     */
    NONE;
    
}
