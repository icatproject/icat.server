/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import client.KeywordType;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class KeywordSearch {
    
    /** Creates a new instance of SearchKeyword */
    public static void searchKeyword(String sid) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
                        
            // TODO process result here
            java.util.List<String> result = port.getKeywordsForUserMax(sid, 20); //search all users keywords, return max 20 results
            //java.util.List<String> result = port.getKeywordsForUserStartWithMax(sid, "s", 20); 
            // Search all users keywords that start with 's', return max 20 results
                       
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of keywords for user is "+result.size());
            System.out.println("Results:");
            for (String kw : result) {
                System.out.println("    "+kw);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search user keywords with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void searchAllKeywords(String sid, KeywordType type) throws Exception {
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            
            // TODO process result here
            java.util.List<String> result = port.getAllKeywords(sid, type);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of all "+type+" keywords is "+result.size());
            System.out.println("Results:");
            for (String kw : result) {
                System.out.println("    "+kw);
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search all "+type+" keywords with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        searchKeyword(SID);
       // searchAllKeywords(SID, KeywordType.ALL);
       // searchAllKeywords(SID, KeywordType.ALPHA);
       // searchAllKeywords(SID, KeywordType.ALPHA_NUMERIC);
    }
    
}
