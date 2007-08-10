/*
 * TestGateKeeperUtil.java
 *
 * Created on 27-Jul-2007, 08:15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.security;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;
import static uk.icat3.util.Util.*;
import static org.junit.Assert.*;
/**
 *
 * @author gjd37
 */
public class TestGateKeeperUtil extends BaseTestClassTX {
    
    
    protected Investigation getInvestigation(boolean valid){
        if(valid){
            Investigation investigation = em.find(Investigation.class, VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST);            
            return investigation;
        } else {
            //create invalid investigation
            Investigation investigation = new Investigation();
            return investigation;
        }
    }
    
     protected Investigation getInvestigationNotFA_Acquired(){
        
        Investigation investigation = em.find(Investigation.class, VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED);        
        return investigation;
        
    }
    
    protected Dataset getDataset(boolean valid){
        if(valid){
            Dataset dataset = em.find(Dataset.class, VALID_DATA_SET_ID_GATEKEEPER_TEST);            
            return dataset;
        } else {
            //create invalid investigation
            Dataset dataset = new Dataset();
            return dataset;
        }
    }
    
     protected Dataset getDatasetNotFA_Acquired(){
        
        Dataset dataset = em.find(Dataset.class, VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);        
        return dataset;
        
    }
     
     protected Datafile getDatafile(boolean valid){
        if(valid){
            Datafile datafile = em.find(Datafile.class, VALID_DATA_FILE_ID_GATEKEEPER_TEST);            
            return datafile;
        } else {
            //create invalid investigation
            Datafile datafile = new Datafile();
            return datafile;
        }
    }
    
     protected Datafile getDatafileNotFA_Acquired(){
        
        Datafile datafile = em.find(Datafile.class, VALID_DATAFILE_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED);        
        return datafile;
        
    }
     
   
}
