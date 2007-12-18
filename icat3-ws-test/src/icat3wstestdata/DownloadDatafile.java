/*
 * DownloadDatafile.java
 *
 * Created on 17-Oct-2007, 15:30:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstestdata;

import icat3wstest.Constants;
import icat3wstest.ICATSingleton;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

/**
 *
 * @author gjd37
 */
public class DownloadDatafile {

    /** Creates a new instance of DownloadDatafile */
    public DownloadDatafile() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        long timeTotal = 0;
        float totalTime = 0;
        long time = System.currentTimeMillis();
          timeTotal = System.currentTimeMillis();
          
        try { // Call Web Service Operation
            String url = ICATSingleton.getInstance().downloadDatafile(Constants.SID, 2L);

            javax.activation.DataHandler result = new DataHandler(new URL(url));
            System.out.println("Result = " + result.getName() + " " + result.getContentType());
            
            totalTime = (System.currentTimeMillis() - time)/1000f;
                            
            System.out.println("\nTime taken to get URL back: "+totalTime+" seconds");
            time = System.currentTimeMillis(); //reset time
                            
            InputStream is = null;
            DataInputStream dis;
            int s;
            int total = 0;    
            
            try {
               is = result.getInputStream();
                //System.out.println("open");
                byte[] buff = new byte[32 * 1024];
                while ((s = is.read(buff)) != -1) {
                    //System.out.println(s);
                    total += s;
                //System.out.println(total);
                }
                totalTime = (System.currentTimeMillis() - time) / 1000f;
                System.out.println("\nTime taken to download: "+total/1024f/1024f+ " Mbs, " + totalTime + " seconds");
                
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
