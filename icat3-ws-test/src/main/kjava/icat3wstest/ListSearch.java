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
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class ListSearch {
    
    /** Creates a new instance of SearchKeyword */
    public static void listDatasetStatus(String sid) throws Exception {
        
        try {                
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<String> result = ICATSingleton.getInstance().listDatasetStatus(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of dataset status is "+result.size());
            System.out.println("Results:");
            for (String datasetStatus : result) {
                System.out.println(" "+datasetStatus);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for DatasetStatus with SID "+sid);
             System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listDatasetType(String sid) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<String> result = ICATSingleton.getInstance().listDatasetTypes(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of datasetType is "+result.size());
            System.out.println("Results:");
            for (String datasetType : result) {
                System.out.println(" "+datasetType);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for DatasetType with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listInstruments(String sid) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<String> result = ICATSingleton.getInstance().listInstruments(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of Instruments status is "+result.size());
            System.out.println("Results:");
            for (String instrument : result) {
                System.out.println(" "+instrument);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for Instruments with SID "+sid);
             System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listInvestigationTypes(String sid) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<String> result = ICATSingleton.getInstance().listInvestigationTypes(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of investigationTypes is "+result.size());
            System.out.println("Results:");
            for (String investigationType : result) {
                System.out.println(" "+investigationType);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for investigationTypes with SID "+sid);
              System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listParameters(String sid) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.Parameter> result = ICATSingleton.getInstance().listParameters(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of Parameters is "+result.size());
            System.out.println("Results:");
            for (Parameter parameter : result) {
                System.out.println(" "+parameter.getParameterPK().getName());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for Parameters with SID "+sid);
             System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listRoles(String sid) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.IcatRole> result = ICATSingleton.getInstance().listRoles(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of Roles is "+result.size());
            System.out.println("Results:");
            for (IcatRole role : result) {
                System.out.println(" "+role.getRole());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for Roles with SID "+sid);
           System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
     public static void listDatafileFormats(String sid) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.DatafileFormat> result = ICATSingleton.getInstance().listDatafileFormats(sid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of DatafileFormats is "+result.size());
            System.out.println("Results:");
            for (DatafileFormat dff : result) {
                System.out.println(" "+dff.getFormatType());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for DatafileFormats with SID "+sid);
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
        listDatasetStatus(SID);
        listParameters(SID);
        listInvestigationTypes(SID);
        listInstruments(SID);
        listDatasetType(SID);
        listRoles(SID);
        listDatafileFormats(SID);
    }
    
}
