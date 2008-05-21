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
import java.util.Collection;
import java.util.List;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class DatafileSearch {
    
    /** Creates a new instance of SearchKeyword */
    public static void searchByRunNumber(String sid, String instrument, Float start, Float end) throws Exception {
        ICATSingleton.getInstance();
        try {             
            long time = System.currentTimeMillis();
            
            List<String> instruments = new ArrayList<String>();
            instruments.add(instrument);
            
            // TODO process result here
            java.util.List<uk.icat3.client.Datafile> result = ICATSingleton.getInstance().searchByRunNumber(sid, instruments, start, end);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of datafiles with '"+ instrument +"' is "+result.size());
            System.out.println("Results:");
            for (Datafile datafile : result) {
                System.out.println("  ID: "+datafile.getId()+", NAME: "+datafile.getIcatRole());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");     
            
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for datafiles with SID "+sid);
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
        searchByRunNumber(SID, "sxd", 8374f, 8400f);       
    }
    
}
