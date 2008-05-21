/*
 * SearchSampleParameter.java
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
public class InvestigationSampleParameterManager {
    
    
    public static SampleParameter addSampleParameter(String sid, String name, String units, Long sampleId) throws Exception {
        
        try {            
            SampleParameterPK PK = new SampleParameterPK();
            PK.setSampleId(sampleId);
            PK.setName(name);
            PK.setUnits(units);
            SampleParameter sp = new SampleParameter();
            sp.setSampleParameterPK(PK);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            SampleParameter sampleParameterAdded = ICATSingleton.getInstance().addSampleParameter(sid, sp, INVESTIGATION_ID);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addSampleParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return sampleParameterAdded;
        } catch (Exception ex) {
            System.out.println("Unable to addSampleParameter with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
        }
    }
    
      public static void updateSampleParameter(String sid, SampleParameter sampleParameter, String newDescription) throws Exception {
        
        try {             
            sampleParameter.setDescription(newDescription);            
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().modifySampleParameter(sid, sampleParameter);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updateSampleParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateSampleParameter with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    public static void delete_undeleteSampleParameter(String sid, String name, String units, Long sampleId) throws Exception {
        
        try {             
            SampleParameterPK PK = new SampleParameterPK();
            PK.setSampleId(sampleId);
            PK.setName(name);
            PK.setUnits(units);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteSampleParameter(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteSampleParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteSampleParameter with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeSampleParameter(String sid, String name, String units, Long sampleId) throws Exception {
        
        try {             
             SampleParameterPK PK = new SampleParameterPK();
            PK.setSampleId(sampleId);
            PK.setName(name);
            PK.setUnits(units);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeSampleParameter(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeSampleParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeSampleParameter with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        SampleParameter sampleParameter = addSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID);
        if(sampleParameter !=null){
            updateSampleParameter(SID, sampleParameter, "new description");
            delete_undeleteSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID); //deleted SampleParameter
            delete_undeleteSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID); //undeleted SampleParameter
            removeSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID);           
        }
    }
    
}
