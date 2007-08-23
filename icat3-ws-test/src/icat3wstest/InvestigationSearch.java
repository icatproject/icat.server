/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;
import client.AdvancedSearchDetails;
import client.Investigation;
import client.InvestigationInclude;
import java.util.ArrayList;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class InvestigationSearch {
    
    /** Creates a new instance of SearchKeyword */
    public static void searchMyInvestigations(String sid) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<client.Investigation> result = port.getMyInvestigations(sid); //get my investigations, default limit to 500, include no other info
            //java.util.List<client.Investigation> result =
            //      port.getMyInvestigationsIncludesPagination(sid, InvestigationInclude.ALL, 3, 10);
            // Find my investigations, include all info, bring back 3 to 10
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of MyInvestigations is "+result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for MyInvestigations with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void searchAdvanced(String sid, AdvancedSearchDetails asd) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<client.Investigation> result =
                    port.searchByAdvancedPagination(sid, asd, 0, 440); //bring back first 440 results
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of Advanced is "+result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for Advanced with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    /** Creates a new instance of SearchKeyword */
    public static void searchKeyword(String sid, String keyword) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            java.util.List<java.lang.String> keywords = new ArrayList<String>();
            keywords.add(keyword);
            
            // TODO process result here
            java.util.List<client.Investigation> result = port.searchByKeywords(sid, keywords);
            //java.util.List<client.Investigation> result =
            //      port.searchByKeywordsPaginationFuzzyAndInclude(sid, keywords,
            //    InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES, true, 0, 20);
            // Search by keywords, include only investigation info (ie, investigators, keywords etc not datasets/datafiles)
            // using LIKE operator on keywords, bring back first 20 results
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of investigations with "+keyword+" as a keyword is "+result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for keyword: "+keyword+" with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
    }
    
    /** Creates a new instance of SearchKeyword */
    public static void searchUserId(String sid, String userId) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<client.Investigation> result = port.searchByUserID(sid, userId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of investigations with "+userId+" as a federalId is "+result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for federalId: "+userId+" with SID "+sid);
            System.out.println(ex);
            assert false;
            // TODO handle custom exceptions here
        }
        
    }
    
    /** Creates a new instance of SearchKeyword */
    public static void searchSurname(String sid, String surname) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<client.Investigation> result = port.searchByUserSurname(sid, surname);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of investigations with "+surname+" as a surname is "+result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: "+investigation.getId()+", TITLE: "+investigation.getTitle());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for surname: "+surname+" with SID "+sid);
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
        // searchKeyword(SID, "calibration");
        // searchSurname(SID, "in");
        // searchUserId(SID, "gjd37");
        
        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.getKeywords().add(KEYWORD);
        asd.getInvestigators().add(SURNAME);
        
        InvestigationSearch.searchAdvanced(SID, asd);
    }
    
}
