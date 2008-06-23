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
 * Set of information to return with datafiles, ie their datafile and datafile parameters.
 * Having more information returned means the query will take longer.
 *
 * @author gjd37
 */
public enum DatafileInclude {
        
    DATAFILE_PARAMETERS,
    RELATED_DATAFILES,
    ALL,
    NONE;
    
    public boolean isDatafileParameters(){
        if(this == DatafileInclude.DATAFILE_PARAMETERS || this == DatafileInclude.ALL) return true;
        else return false;
    }    
    
    public boolean isRelatedDatafiles(){
        if(this == DatafileInclude.RELATED_DATAFILES || this == DatafileInclude.ALL) return true;
        else return false;
    }    
  
}
