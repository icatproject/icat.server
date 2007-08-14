/*
 * InvestigationInclude.java
 *
 * Created on 27 February 2007, 14:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 * Set of information to return with investigations, ie their keywords, investigators, datasets, default none.  
 * Having more information returned means the query will take longer.
 * 
 * @author gjd37
 */
public enum InvestigationInclude {
    
    /**
     * list of investigators.
     */
    INVESTIGATORS_ONLY,
    /**
     * list of keywords
     */
    KEYWORDS_ONLY,
    /**
     * all keywords and investigators.
     */
    INVESTIGATORS_AND_KEYWORDS,
    /**
     * list of datasets, without the list of data files.
     */
    DATASETS_ONLY,
    /**
     * list of all datasets with their list of data file
     */
    DATASETS_AND_DATAFILES,
    /**
     * list of all the investigation samples
     */
    SAMPLES_ONLY,
    /**
     * The investigation role
     */
    ROLE_ONLY,
    /**
     * all, datasets with files, keywords, sample and investigators, icatrole
     */
    ALL,
    /**
     * only the investigation object with no default lazy information.
     */
    NONE,
    /**
     * all information except datasets and datafiles, ie keywords, sample and investigators
     */
     ALL_EXCEPT_DATASETS_AND_DATAFILES,
    /**
     * all information except datasets, datafiles and icatroles, ie keywords, sample and investigators
     */
     ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
    
}
