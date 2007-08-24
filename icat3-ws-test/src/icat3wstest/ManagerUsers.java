/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import client.Datafile;
import client.Dataset;
import client.ElementType;
import client.IcatAuthorisation;
import client.Sample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class ManagerUsers {
    
    /** Creates a new instance of SearchKeyword */
    public static void listInvestigationAuthorisations(String sid, Long investigationId) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for investigation Id: investigationId
            java.util.List<client.IcatAuthorisation> results = port.getAuthorisations(sid, investigationId, ElementType.INVESTIGATION);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of IcatAuthorisation for Investigation ID: "+ investigationId +" is "+results.size());
            System.out.println("Results:");
            for (IcatAuthorisation icatAuthorisation : results) {
                System.out.println("    "+icatAuthorisation.getId()+"  "+icatAuthorisation.getUserId()+" "+icatAuthorisation.getRole().getRole());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for listInvestigationAuthorisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listDatasetAuthorisations(String sid, Long datasetId) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            java.util.List<client.IcatAuthorisation> results = port.getAuthorisations(sid, datasetId, ElementType.DATASET);
            //java.util.List<client.IcatAuthorisation> results = port.getAuthorisations(sid, datafileId, ElementType.DATAFILE); //or to list datafiles authorisations
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of IcatAuthorisation for Dataset ID: "+ datasetId +" is "+results.size());
            System.out.println("Results:");
            for (IcatAuthorisation icatAuthorisation : results) {
                System.out.println("    "+icatAuthorisation.getId()+"  "+icatAuthorisation.getUserId()+" "+icatAuthorisation.getRole().getRole());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for listDatasetAuthroisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void listDatafileAuthorisations(String sid, Long datafileId) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            java.util.List<client.IcatAuthorisation> results = port.getAuthorisations(sid, datafileId, ElementType.DATAFILE);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of IcatAuthorisation for Datafile ID: "+ datafileId +" is "+results.size());
            System.out.println("Results:");
            for (IcatAuthorisation icatAuthorisation : results) {
                System.out.println("     "+icatAuthorisation.getId()+"  "+icatAuthorisation.getUserId()+" "+icatAuthorisation.getRole().getRole());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for listDatafileAuthroisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void addDatafileAuthorisations(String sid, Long datafileId, String userId, String role) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            port.addAuthorisation(sid, userId, role, datafileId, ElementType.DATAFILE);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: addDatafileAuthorisations");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for addDatafileAuthorisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static IcatAuthorisation addDatasetAuthorisations(String sid, Long datasetId, String userId, String role) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            IcatAuthorisation icatAuthorisation  = port.addAuthorisation(sid, userId, role, datasetId, ElementType.DATASET);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: addDatasetAuthorisations: "+icatAuthorisation.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
            return icatAuthorisation;
        } catch (Exception ex) {
            System.out.println("Unable to search for addDatasetAuthorisations with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeDatasetAuthorisations(String sid, IcatAuthorisation icatAuthorisation) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            port.removeAuthorisation(sid, icatAuthorisation.getId());
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: removeDatasetAuthorisations: "+icatAuthorisation.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for removeDatasetAuthorisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void deleteDatasetAuthorisations(String sid, IcatAuthorisation icatAuthorisation) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            port.deleteAuthorisation(sid, icatAuthorisation.getId());
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: deleteDatasetAuthorisations: "+icatAuthorisation.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for deleteDatasetAuthorisations with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    public static void updateDatasetAuthorisations(String sid, IcatAuthorisation icatAuthorisation, String newRole) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // Get me all authorisation for dataset Id: datasetId
            port.updateAuthorisation(sid, newRole, icatAuthorisation.getId());
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: updateDatasetAuthorisations: "+icatAuthorisation.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for updateDatasetAuthorisations with SID "+sid);
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
        listInvestigationAuthorisations(SID, INVESTIGATION_ID);
        listDatafileAuthorisations(SID, DATAFILE_ID);
        
        IcatAuthorisation icatAuthorisation = addDatasetAuthorisations(SID, DATASET_ID, "added"+new Random().nextInt(), "DOWNLOADER");        
        updateDatasetAuthorisations(SID, icatAuthorisation, "READER");
        deleteDatasetAuthorisations(SID, icatAuthorisation); //delete
        deleteDatasetAuthorisations(SID, icatAuthorisation); //undelete        
        listDatasetAuthorisations(SID, DATASET_ID);
        removeDatasetAuthorisations(SID, icatAuthorisation);
        
        // listDatafileAuthorisations(SID, DATAFILE_ID);
        // addDatafileAuthorisations(SID, DATAFILE_ID, INVESTIGATOR, "DOWNLOADER");
        // listDatafileAuthorisations(SID, DATAFILE_ID);
        
        //removeDatasetAuthorisations(SID, icatAuthorisation);
    }
    
}
