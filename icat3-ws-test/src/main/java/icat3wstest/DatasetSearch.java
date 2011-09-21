/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import uk.icat3.client.*;
import java.util.Collection;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class DatasetSearch {
    
    /** Creates a new instance of SearchKeyword */
    public static Collection<Sample> searchBySampleName(String sid, String sampleName) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.Sample> result = ICATSingleton.getInstance().searchSamplesBySampleName(sid, sampleName);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of samples with '"+ sampleName +"' is "+result.size());
            System.out.println("Results:");
            for (Sample sample : result) {
                System.out.println("  ID: "+sample.getId()+", NAME: "+sample.getName());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return result;
            
        } catch (Exception ex) {
            System.out.println("Unable to search for sample with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
            // TODO handle custom exceptions here
        }
    }
    
    public static void searchDatasetsBySample(String sid, Sample sample) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.Dataset> result = ICATSingleton.getInstance().searchDatasetsBySample(sid, sample);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of datasets for sample '"+ sample +"' is "+result.size());
            System.out.println("Results:");
            for (Dataset dataset : result) {
                System.out.println("  ID: "+dataset.getId()+", NAME: "+dataset.getName());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for datasets with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Collection<Sample> samples = searchBySampleName(SID, "calibration");
        int i = 0;
        for (Sample sample : samples) {            
            searchDatasetsBySample(SID, sample);
            i++;
            if(i == 3) break;
        }
    }
}
