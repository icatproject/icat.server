/*
 * InjestXML.java
 *
 * Created on 24-Sep-2007, 13:20:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstestother;

import icat3wstest.*;
import uk.icat3.client.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import static icat3wstest.Constants.*;

/**
 *
 * @author gjd37
 */
public class InjestXML {
    
    /** Create s a new instance of InjestXML */
    public static void injestXML(String sid, String xml) {
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Collection<Long> ids = ICATSingleton.getInstance().ingestMetadata(sid, xml);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Ids returned: "+ids);
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to inject XML for SID: "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/icat3wstestother/isis.xml")));
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/icat3wstestother/clf.xml")));
            String line = "";
            String  buffer = "";
            while((line = br.readLine()) != null) {
                buffer += line;
            }
            //System.out.println("buffer: " + buffer);
            injestXML(SID, buffer);
            
        } catch (Exception e) {
            System.out.println("Unable to load XML file in.\n"+e);
        }        
    }    
}
