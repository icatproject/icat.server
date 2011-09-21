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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class InvestigationKeywordManager {
    
    
    public static Keyword addKeyword(String sid, String keywordName, Long investigationId) throws Exception {
        
        try {             
            Keyword keyword = new Keyword();
            KeywordPK keywordPK = new KeywordPK();
            keywordPK.setName(keywordName);
            keywordPK.setInvestigationId(investigationId);
            keyword.setKeywordPK(keywordPK);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().addKeyword(sid, keyword, investigationId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addKeyword");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return keyword;
        } catch (Exception ex) {
            System.out.println("Unable to addKeyword with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
        }
    }
    
    
    
    public static void delete_undeleteKeyword(String sid, KeywordPK PK) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteKeyword(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteKeyword");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteKeyword with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removeKeyword(String sid, KeywordPK PK) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeKeyword(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeKeyword");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeKeyword with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Keyword keyword = addKeyword(SID, "new keyword "+new Random().nextInt(), INVESTIGATION_ID);
        if(keyword !=null){
            delete_undeleteKeyword(SID, keyword.getKeywordPK()); //deleted keyword
            delete_undeleteKeyword(SID, keyword.getKeywordPK()); //undeleted keyword
            removeKeyword(SID, keyword.getKeywordPK());            
        }
    }
    
}
