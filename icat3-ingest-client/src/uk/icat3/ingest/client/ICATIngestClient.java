package uk.icat3.ingest.client;

/*
 * ICATIngestClient.java
 * 
 * Created on 17-Oct-2007, 09:50:55
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.xml.ws.BindingProvider;
import uk.icat3.ingest.client.ws.clf.ICATCLFService;
import uk.icat3.ingest.client.ws.clfadmin.ICATAdminCLFService;
import uk.icat3.ingest.client.ws.isis.ICATISISService;
import uk.icat3.ingest.client.ws.isisadmin.ICATAdminISISService;
import java.io.BufferedReader;

/**
 *
 * @author df01
 */
public class ICATIngestClient {

    /** Creates a new instance of ICATIngestClient */
    public ICATIngestClient() {
    }
    
    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: java -jar ICATIngestClient.jar [instance] [username] [filename]");
        System.out.println("e.g.   java -jar ICATIngestClient.jar CLF CLFAdmin /usr/local/ll56/ASTRA00001.XML");
        System.out.println("e.g.   java -jar ICATIngestClient.jar ISIS ISIS_GUARDIAN C:/dwf64/MAP00001.XML");
        System.out.println();
    }

    /**
     * @param args the command line arguments
     * arg[0] = instance e.g. ISIS | CLF
     * arg[1] = user name e.g. ISIS_GUARDIAN
     * arg[2] = file location e.g. /usr/local/dwf/MAP00001.XML
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        if ((args == null) || (args.length < 2)) {
            System.err.println("[ERROR] - Incorrect number of arguments supplied");
            printHelp();
            System.exit(-1);
        }
        
        //check args[0] for instance name        
        String _instance = args[0];
        if (!((_instance.equalsIgnoreCase("ISIS")) || (_instance.equalsIgnoreCase("CLF")))) {
            System.err.println("[ERROR] - Incorrect usage - instance must be 'ISIS' or 'CLF'");
            printHelp();
            System.exit(-1);
        }
        
        //this will be proven good/bad when connecting to api
        String _username = args[1];
        
        //check supplied file exists and can be read
        String _filename = args[2];
        String buffer = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_filename)));            
            String line = "";            
            while((line = br.readLine()) != null) {
                buffer += line;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] - Supplied filename does not exist or cannot be read");
            printHelp();
            System.exit(-1);
        }              
        
        uk.icat3.ingest.client.ws.isis.ICAT isisPort = null;              
        uk.icat3.ingest.client.ws.isisadmin.ICATAdmin isisIcatAdmin = null;  
        ICATISISService isisService = null;
        
        uk.icat3.ingest.client.ws.clf.ICAT clfPort = null;
        uk.icat3.ingest.client.ws.clfadmin.ICATAdmin clfIcatAdmin = null;
        ICATCLFService clfService = null;
        
        
        String sessionId = null;
        List<Long> ids = null;
        
        if (_instance.equalsIgnoreCase("ISIS")) {
            isisIcatAdmin = new ICATAdminISISService().getICATAdminPort();
            ((BindingProvider)isisIcatAdmin).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "isis_test");
            ((BindingProvider)isisIcatAdmin).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "1s1ste5tp455");  
            isisService = new ICATISISService();
            isisPort = isisService.getICATPort();
        }
        
        if (_instance.equalsIgnoreCase("CLF")) {
            clfIcatAdmin = new ICATAdminCLFService().getICATAdminPort();
            ((BindingProvider)clfIcatAdmin).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "clf_test");
            ((BindingProvider)clfIcatAdmin).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "stup1dp4ss");  
            clfService = new ICATCLFService();
            clfPort = clfService.getICATPort();
        }
        
       
        try {
            if (_instance.equalsIgnoreCase("ISIS")) {
                sessionId = isisIcatAdmin.loginAdmin(_username);
                ids = isisPort.ingestMetadata(sessionId, buffer);
            }//end if
            
            if (_instance.equalsIgnoreCase("CLF")) {
                sessionId = clfIcatAdmin.loginAdmin(_username);
                ids = clfPort.ingestMetadata(sessionId, buffer);
            }//end if
            
            
            if ((ids != null) && (ids.size() >0)) {
                System.out.println("[SUCCESS] Investigation Ingested. Returned Investigation Id #" + ids.get(0));
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] - An error occurred while trying to ingest XML file, see details below");
            e.printStackTrace();
        }
               
        if (_instance.equalsIgnoreCase("ISIS")) {
            isisPort.logout(sessionId);
        }
        
        if (_instance.equalsIgnoreCase("CLF")) {
            clfPort.logout(sessionId);
        }
    }

}
