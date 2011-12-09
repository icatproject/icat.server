/*
 * SearchInvestigator.java
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
public class InvestigationInvestigatorManager {
    
    
    public static Investigator addInvestigator(String sid,  String name, Long investigationid) throws Exception {
        
        try {             
            Investigator investigator = new Investigator();
            InvestigatorPK investigatorPK = new InvestigatorPK();
            investigatorPK.setFacilityUserId(name);
            investigatorPK.setInvestigationId(investigationid);
            investigator.setInvestigatorPK(investigatorPK);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Investigator investigatorCreated = ICATSingleton.getInstance().addInvestigator(sid, investigator, investigationid);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addInvestigator : "+investigatorCreated.getInvestigatorPK().getFacilityUserId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return investigatorCreated;
        } catch (Exception ex) {
            System.out.println("Unable to addInvestigator with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
        }
    }
    
    public static void updateInvestigator(String sid, Investigator investigator, String newRole) throws Exception {
        
        try {             
            investigator.setRole(newRole);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().modifyInvestigator(sid, investigator);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updateInvestigator");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateInvestigator with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    public static void delete_undeleteInvestigator(String sid, InvestigatorPK investigatorPK) throws Exception {
        
        try { 
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteInvestigator(sid, investigatorPK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteInvestigator");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteInvestigator with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeInvestigator(String sid, InvestigatorPK investigatorPK) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeInvestigator(sid, investigatorPK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeInvestigator");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeInvestigator with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Investigator investigator = addInvestigator(SID, INVESTIGATOR, INVESTIGATION_ID);
        if(investigator !=null){
            updateInvestigator(SID, investigator, "new role");
            delete_undeleteInvestigator(SID, investigator.getInvestigatorPK()); //deleted Investigator
            delete_undeleteInvestigator(SID, investigator.getInvestigatorPK()); //undeleted Investigator
            removeInvestigator(SID, investigator.getInvestigatorPK());
        }
    }
    
}
