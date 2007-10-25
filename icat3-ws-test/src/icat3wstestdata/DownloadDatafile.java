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
        
        try { // Call Web Service Operation
            data.ICATDataService service = new data.ICATDataService();
            data.ICATData port = service.getICATDataPort();
            // TODO initialize WS operation arguments here
            java.lang.String sessionId = Constants.SID;
            java.lang.Long datafileId = 2L;
            // TODO process result here
            for (int i = 0; i < 1; i++) {
                String url = port.downloadDatafile(sessionId, datafileId);
                System.out.println("Result = "+url);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            // TODO handle custom exceptions here
        }
        
        //System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump","true");
        
      /*  try { // Call Web Service Operation
            data.ICATDataService service = new data.ICATDataService();
            data.ICATData port = service.getICATDataPort();
            SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
       
            System.out.println(binding.isMTOMEnabled());
       
            // TODO initialize WS operation arguments here
            java.lang.String sessionId = "421650c4-4f71-4001-867c-266687332ee3";
            java.lang.Long datasetId = 107L;
            // TODO process result here
            javax.activation.DataHandler result = port.downloadDataset(sessionId, datasetId);
            System.out.println("Result = "+result.getName()+" "+result.getContentType());
        } catch (Exception ex) {
            System.out.println(ex);
            // TODO handle custom exceptions here
        }*/
    }
    
}
