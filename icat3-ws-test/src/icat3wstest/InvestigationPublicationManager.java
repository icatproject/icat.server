/*
 * SearchPublication.java
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
public class InvestigationPublicationManager {
    
    
    public static Publication addPublication(String sid, String publicationName, Long investigationId) throws Exception {
        
        try {             
            Publication publication = new Publication();
            publication.setFullReference("http://pub.com");
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Publication publicationAdded = ICATSingleton.getInstance().addPublication(sid, publication, investigationId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addPublication");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return publicationAdded;
        } catch (Exception ex) {
            System.out.println("Unable to addPublication with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
        }
    }
    
      public static void updatePublication(String sid, Publication publication, String newUrl) throws Exception {
        
        try {             
            publication.setUrl(newUrl);
            publication.setFullReference(newUrl);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().modifyPublication(sid, publication);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updatePublication");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updatePublication with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    public static void delete_undeletePublication(String sid, Long publicationId) throws Exception {
        
        try {             
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deletePublication(sid, publicationId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeletePublication");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeletePublication with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void removePublication(String sid, Long publicationId) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removePublication(sid, publicationId);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removePublication");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removePublication with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Publication publication = addPublication(SID, "new Publication "+new Random().nextInt(), INVESTIGATION_ID);
        if(publication !=null){
            updatePublication(SID, publication, "http://newUrl.com");
            delete_undeletePublication(SID, publication.getId()); //deleted Publication
            delete_undeletePublication(SID, publication.getId()); //undeleted Publication
            removePublication(SID, publication.getId());            
        }
    }
    
}
