/*
 * SearchSample.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstest;

import uk.icat3.client.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class InvestigationSampleManager {
    
    
    public static Sample addSample(String sid, String instance, String name, Long investigationid) throws Exception {
        
        try {
            Sample sample = new Sample();
            sample.setInstance(instance);
            sample.setName(name);
            sample.setSafetyInformation("safety first");
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Sample sampleCreated = ICATSingleton.getInstance().addSample(sid, sample, investigationid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addSample : "+sampleCreated.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return sampleCreated;
        } catch (Exception ex) {
            System.out.println("Unable to addSample with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
        }
    }
    
    public static void updateSample(String sid, Sample sample, String newSafetyInformation) throws Exception {
        
        try {
            sample.setSafetyInformation(newSafetyInformation);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().modifySample(sid, sample);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updateSample");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateSample with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    public static void delete_undeleteSample(String sid, Long sampleId) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteSample(sid, sampleId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteSample");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteSample with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeSample(String sid, Long sampleId) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeSample(sid, sampleId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeSample");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeSample with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Sample sample = addSample(SID, PARAMETER_NAME, PARAMETER_UNITS+new Random().nextInt(), INVESTIGATION_ID);
        if(sample !=null){
            updateSample(SID, sample, "new safety");
            delete_undeleteSample(SID, sample.getId()); //deleted Sample
            delete_undeleteSample(SID, sample.getId()); //undeleted Sample
            removeSample(SID, sample.getId());
        }
    }
    
}
