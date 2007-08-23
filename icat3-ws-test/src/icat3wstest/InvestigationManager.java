/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstest;

import client.Dataset;
import client.Investigation;
import client.InvestigationType;
import client.Investigator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class InvestigationManager {
    
    /** Creates a new instance of SearchKeyword */
    public static void getInvestigation(String sid, Long id) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Investigation investigation = port.getInvestigation(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to getInvestigation with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void getInvestigations(String sid, Long id) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            List<Long> ids = new ArrayList<Long>();
            ids.add(id);
            //ids.add(id);
            
            // TODO process result here
            List<Investigation> investigations = port.getInvestigationsIncludes(sid, ids, client.InvestigationInclude.ALL);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of getInvestigations is "+investigations.size());
            System.out.println("Results:");
            for (Investigation investigation : investigations) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
                System.out.println("    Investigators: "+investigation.getInvestigatorCollection().size());
                System.out.println("    ----------------");
                for (Investigator investigator : investigation.getInvestigatorCollection()) {
                    System.out.println("       "+investigator.getFacilityUser().getLastName());
                }
                System.out.println("     Datasets: "+ investigation.getDatasetCollection().size());
                System.out.println("     ------------");
                for (Dataset ds : investigation.getDatasetCollection()) {
                    System.out.println("       "+ds.getName());
                }
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to getInvestigations with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static Investigation createInvestigation(String sid, String name) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            Investigation investigation = new Investigation();
            investigation.setTitle(name);
            investigation.setInvNumber(""+new Random().nextInt());
            
            List<InvestigationType> types = port.listInvestigationTypes(sid);
            investigation.setInvType(types.iterator().next());
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Investigation investigationCreated = port.createInvestigation(sid, investigation);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("  Created Investigation: ID: "+investigationCreated.getId()+", TITLE: "+investigationCreated.getTitle());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return investigationCreated;
        } catch (Exception ex) {
            System.out.println("Unable to createInvestigation with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
            // TODO handle custom exceptions here
        }
    }
    
    public static void updateInvestigation(String sid, Investigation investigation, String newName) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            investigation.setTitle(newName);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            port.modifyInvestigation(sid, investigation);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updateInvestigation");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateInvestigation with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void delete_undeleteInvestigation(String sid, Long id) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            port.deleteInvestigation(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteInvestigation");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteInvestigation with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeInvestigation(String sid, Long id) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            port.removeInvestigation(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeInvestigation");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeInvestigation with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //getInvestigation(SID, INVESTIGATION_ID);
        //getInvestigations(SID, INVESTIGATION_ID);
        
        Investigation investigation = createInvestigation(SID, "investigation for "+SID);
        if(investigation != null) {
            updateInvestigation(SID, investigation, "new investigation for "+SID);
            delete_undeleteInvestigation(SID, investigation.getId()); //deletes investigation
            delete_undeleteInvestigation(SID, investigation.getId()); //undeletes investigation
            removeInvestigation(SID, investigation.getId());
        }
    }
    
}
