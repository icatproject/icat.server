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
import client.Investigator;
import java.util.ArrayList;
import java.util.List;
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
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        getInvestigation(SID, INVESTIGATION_ID);
        getInvestigations(SID, INVESTIGATION_ID);
    }
    
}
