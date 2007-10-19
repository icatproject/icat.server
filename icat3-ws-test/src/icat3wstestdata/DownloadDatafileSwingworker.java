/*
 * DownloadDatafile.java
 *
 * Created on 17-Oct-2007, 15:30:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstestdata;

import data.DownloadException_Exception;
import data.InsufficientPrivilegesException_Exception;
import data.MalformedURLException_Exception;
import data.NoSuchObjectFoundException_Exception;
import data.NoSuchUserException_Exception;
import data.SessionException_Exception;
import data.TotalSizeExceededException_Exception;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.swing.SwingWorker;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

/**
 *
 * @author gjd37
 */
public class DownloadDatafileSwingworker {
    
    /** Creates a new instance of DownloadDatafile */
    public DownloadDatafileSwingworker() {
    }
    static final data.ICATData port = new data.ICATDataService().getICATDataPort();
    static java.lang.String sessionId = "b5c56210-557f-43cc-a327-8861b4c8ce3e";
    static  java.lang.Long datafileId = 2L;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final int[] finished = {0};
        try { // Call Web Service Operation
            
            Collection<SwingWorker> sws = new ArrayList<SwingWorker>();
            
            for (int i = 0; i < 3; i++) {
                
                SwingWorker sw = new SwingWorker<String, String>(){
                    @Override
                    public String doInBackground() {
                        try {
                            javax.activation.DataHandler result = port.downloadDatafile(sessionId, datafileId);
                            java.lang.System.out.println("Result = " + result.getName() + " " + result.getContentType());
                            
                        } catch (Exception ex) {
                            System.out.println("Error with: "+ex);
                        }
                        return "finsihed";
                    }
                    
                    @Override
                    public void done() {
                        try {
                            get();
                            System.out.println("finished "+finished[0]++);
                        } catch (Exception ex) {
                            finished[0]++;
                            System.out.println(ex.getMessage());
                        }
                    }
                };
                
                sws.add(sw);
            }
            
            //now execute them
            int i = 0;
            for (SwingWorker swingWorker : sws) {
                System.out.println("Starting" +i++);
                swingWorker.execute();
            }
            
        } catch (Exception ex) {
            System.out.println(ex);
            // TODO handle custom exceptions here
        }
    }
}
