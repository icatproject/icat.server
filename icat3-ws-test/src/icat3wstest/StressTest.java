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
import static icat3wstest.Constants.*;

/**
 *
 * @author gjd37
 */
public class StressTest {
    
   
    
    /** Creates a new instance of SearchKeyword */
    public static void stress(String sid) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().getMyInvestigationsIncludes(sid,
                    InvestigationInclude.KEYWORDS_ONLY); //get my investigations, default limit to 500, include no other info
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of MyInvestigations is "+result.size());
            System.out.println("Results:");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for MyInvestigations with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 300; i++) {
            stress(SID);  
            //SessionUtil.login(System.getProperty("user.name"),System.getProperty("usersso.password") );
            System.out.println("----------------------"+i+"------------------------");
        }        
    }
    
}