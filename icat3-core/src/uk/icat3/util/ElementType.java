package uk.icat3.util;

/*
 * ElementType.java
 *
 * Created on 13 February 2007, 09:43
 *
 * ElementType is simply an enum (data structure whose fields 
 * consist of a fixed set of constants) that contains the possible
 * elements / entities of the icat3 model that a user may wish to
 * interact with.  This enum is used in the GateKeeper (Security,
 * authorisation) service.
 *
 * @author df01
 * @version 1.0
 */
public enum ElementType {
    /**
     * Investigation objects
     */
    STUDY, 
    INVESTIGATION, 
    INVESTIGATOR,
    KEYWORD,
    SAMPLE, 
    SAMPLE_PARAMETER,
    PUBLICATION,
    
    /**
     * Dataset objects
     */
    DATASET, 
    DATASET_PARAMETER,
    
    /**
     * Datafile objects
     */
    DATAFILE,
    DATAFILE_PARAMETER;
    
    /**
     * 
     */
    
    /**
     * Checks wheather this type of element belongs to an datafile
     */
    public boolean isDatafileType(){
        if(this == ElementType.DATAFILE || this == ElementType.DATAFILE_PARAMETER)
            return true;
        else return false;
    }
    
      /**
     * Checks wheather this type of element belongs to an investigation
     */
    public boolean isInvestigationType(){
        if(this == ElementType.INVESTIGATION ||
                this == ElementType.SAMPLE || this == ElementType.SAMPLE_PARAMETER ||
                this == ElementType.KEYWORD || this == ElementType.INVESTIGATOR ||
                this == ElementType.SAMPLE_PARAMETER || this == ElementType.STUDY || this == ElementType.PUBLICATION)
            return true;
        else return false;
    }
    
    /**
     * Checks wheather this type of element belongs to an dataset
     */
    public boolean isDatasetType(){
        if(this == ElementType.DATASET || this == ElementType.DATASET_PARAMETER)
            return true;
        else return false;
    }
        
    /**
     * Checks wheather this type of element is a root element, ie inv, df, ds
     */
    public boolean isRootType(){
        if(this == ElementType.DATAFILE || this == ElementType.INVESTIGATION || this == ElementType.DATASET)
            return true;
        else return false;
    }  
    
    /**
     * Checks wheather this type of element is a root element, ie inv, ds, not df as this is changed for auth
     */
    public boolean isRootAuthType(){
        if(this == ElementType.INVESTIGATION || this == ElementType.DATASET)
            return true;
        else return false;
    }  
}
