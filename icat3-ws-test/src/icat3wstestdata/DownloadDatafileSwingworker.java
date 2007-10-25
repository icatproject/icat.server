/*
 * DownloadDatafile.java
 *
 * Created on 17-Oct-2007, 15:30:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstestdata;

import client.ICAT;
import icat3wstest.Constants;
import icat3wstest.ICATSingleton;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingWorker;

/**
 *
 * @author gjd37
 */
public class DownloadDatafileSwingworker {
    
    /** Creates a new instance of DownloadDatafile */
    public DownloadDatafileSwingworker() {
    }
    static final ICAT icat = ICATSingleton.getInstance();

    static java.lang.String sessionId = Constants.SID;
    static java.lang.Long datafileId = 2L;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final int[] finished = {0};
        try { // Call Web Service Operation
            
            Collection<SwingWorker> sws = new ArrayList<SwingWorker>();
            
            for (int i = 0; i < 1 ;i++) {
                
                SwingWorker sw = new SwingWorker<String, String>(){
                    
                     int total = 0;
                     long timeTotal = 0;
                     float totalTime = 0;
                    @Override
                    public String doInBackground() {
                        long time = System.currentTimeMillis();
                         timeTotal = System.currentTimeMillis();
                        try {
                             String result = icat.downloadDatafile(sessionId, datafileId);
                             java.lang.System.out.println("Result = " + result);
                            
                            totalTime = (System.currentTimeMillis() - time)/1000f;
                            
                            System.out.println("\nTime taken to get URL back: "+totalTime+" seconds");
                            time = System.currentTimeMillis(); //reset time
                           
                          //  String result = "http://volga.dl.ac.uk:9080/icat-clf-download/DownloadServlet?sid=2259ce54-140f-45c6-929b-9c9504edffba&name=images.jpeg&file=large";
                           // String result = "https://escvig6.dl.ac.uk:8181/icat-clf-download/DownloadServlet?sid=a65c2afa-acc4-4ca0-b0c1-c02ffb310538&name=images.jpeg&file=0.23202901090405093\\images.jpeg";
                            
                            URL url = new URL(result);
                            InputStream is = null;
                            DataInputStream dis;
                            int s;
                           
                            try {            
                                //System.out.println("opening");
                                is = url.openStream();
                                //System.out.println("open");
                                byte[] buff = new byte[32*1024];
                                while ((s = is.read(buff)) != -1) {
                                    //System.out.println(s);
                                    total += s;
                                    //System.out.println(total);
                                }
                                totalTime = (System.currentTimeMillis() - time)/1000f;
                                System.out.println("\nTime taken to download: "+totalTime+" seconds");
                                
                            } finally {
                                try {
                                    if(is != null) is.close();
                                } catch (IOException ioe) {}
                            }
                          
                        } catch (Exception ex) {
                            System.out.println("Error with: "+ex);
                            ex.printStackTrace();
                        }
                        return "finished";
                    }
                    
                    @Override
                    public void done() {
                        try {
                            get();
                            float totalTime = (System.currentTimeMillis() - timeTotal)/1000f;
                            System.out.println("finished ok "+finished[0]++ +" : total "+total/1024f/1024f +" Mb, Total time: "+totalTime+" seconds");
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
            Thread.sleep(2000);
            for (SwingWorker swingWorker : sws) {
                //Thread.sleep(2000);
                System.out.println("Starting" +i++);
                swingWorker.execute();
            }
            
        } catch (Exception ex) {
            System.out.println(ex);
            // TODO handle custom exceptions here
        }
    }
}

