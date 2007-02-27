/*
 * InvestigationUtil.java
 *
 * Created on 27 February 2007, 11:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Collection;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.util.InvestigationInclude;

/**
 *
 * @author gjd37
 */
public class InvestigationUtil {
    
    public static void getInvestigationInformation(Collection<Investigation> investigations, InvestigationInclude include){
        
        // now collect the information associated with the investigations requested
        if(include.toString().equals(InvestigationInclude.ALL.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information, other wise the collections are null
                
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
                investigation.getDatasetCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            
            // return datasets with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_ONLY.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
            }
            // return datasets and their datafiles with these investigations
        } else if(include.toString().equals(InvestigationInclude.DATASETS_AND_DATAFILES.toString())){
            for(Investigation investigation : investigations){
                investigation.getDatasetCollection().size();
                
                for(Dataset dataset : investigation.getDatasetCollection()){
                    dataset.getDatafileCollection().size();
                }
            }
            // return keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.KEYWORDS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getKeywordCollection().size();
            }
            // return c with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_ONLY.toString())){
            for(Investigation investigation : investigations){
                //size invokes teh JPA to get the information
                investigation.getInvestigatorCollection().size();
            }
            // return investigators and keywords with these investigations
        } else if(include.toString().equals(InvestigationInclude.INVESTIGATORS_AND_KEYWORDS.toString())){
            for(Investigation investigation : investigations){
                //size invokes the JPA to get the information
                investigation.getKeywordCollection().size();
                investigation.getInvestigatorCollection().size();
            }
        }
    }
}
