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
     * list of publications
     */
    PUBLICATIONS_ONLY,
    /**
     * all keywords and investigators.
     */
    INVESTIGATORS_AND_KEYWORDS,
    
    /**
     * all shifts and investigators.
     */
    INVESTIGATORS_AND_SHIFTS,
     /**
     * all shifts, samples and investigators.
     */
    INVESTIGATORS_SHIFTS_AND_SAMPLES,
    /**
     * all shifts, samples and investigators.
     */
    INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS,
    /**
     * list of datasets, without the list of data files.
     */
    DATASETS_ONLY,
    /**
     * list of datasets and datset parameters, without the list of data files.
     */
    DATASETS_AND_DATASET_PARAMETERS_ONLY,
    /**
     * list of all datasets with their list of data file, without datafile parameters
     */
    DATASETS_AND_DATAFILES,
    /**
     * list of all datasets with their list of data file
     */
    DATASETS_DATAFILES_AND_PARAMETERS,
    /**
     * list of all the investigation samples
     */
    SAMPLES_ONLY,
    /**
     * The investigation role
     */
    ROLE_ONLY,
    /**
     * The shift information
     */
    SHIFT_ONLY,
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
    
    public boolean isDatasets(){
        if(this == InvestigationInclude.DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.DATASETS_ONLY  ||
                this == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY  ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
     public boolean isDatasetsAndParameters(){
        if(this == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY  ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
    public boolean isRoles(){
        if(this == InvestigationInclude.ROLE_ONLY ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL) return true;
        else return false;
    }
    
    public boolean isDatasetsAndDatafiles(){
        if(this == InvestigationInclude.DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL || 
                this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
    public boolean isDatasetsDatafilesAndParameters(){
        if(this == InvestigationInclude.ALL ||
                this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS) return true;
        else return false;
    }
    
    public boolean isInvestigators(){
        if(this == InvestigationInclude.INVESTIGATORS_AND_KEYWORDS ||
                this == InvestigationInclude.INVESTIGATORS_ONLY  ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES  ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS  ||
                this == InvestigationInclude.INVESTIGATORS_AND_SHIFTS  ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES) return true;
        else return false;
    }
    
    public boolean isKeywords(){
        if(this == InvestigationInclude.INVESTIGATORS_AND_KEYWORDS ||
                this == InvestigationInclude.KEYWORDS_ONLY  ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES) return true;
        else return false;
    }
    
    public boolean isPublications(){
        if(this == InvestigationInclude.PUBLICATIONS_ONLY ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES) return true;
        else return false;
    }
    
    public boolean isShifts(){
        if(this == InvestigationInclude.SHIFT_ONLY ||
                this == InvestigationInclude.INVESTIGATORS_AND_SHIFTS  ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES  ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS  ||
                this == InvestigationInclude.ALL ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES) return true;
        else return false;
    }
    
    public boolean isSamples(){
        if(this == InvestigationInclude.SAMPLES_ONLY ||
                this == InvestigationInclude.ALL ||
                  this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES  ||
                this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS  ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES ||
                this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES) return true;
        else return false;
    }
    
}
