/*
 * Main.java
 *
 * Created on 15-Aug-2007, 12:49:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;
import javax.xml.ws.BindingProvider;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class SessionUtil {
    
    /** Creates a new instance of Main */
    public static String login(String username, String password) throws Exception{
        
        try { // Call Web Service Operation
            client.ICATService service = new client.ICATService();
            client.ICAT port = service.getICATPort();
            
            ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, END_POINT_ADDRESS); 
            // TODO initialize WS operation arguments here
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.lang.String result = port.login(username, password);
            SID = result;
            System.out.println(" Logged in successfully with SID = "+result);
            
            System.out.println("\nTime taken: "+(System.currentTimeMillis() - time)/1000f+" seconds");
            System.out.println("------------------------------------------------------------------\n");
            assert true;
            return result;
        } catch (Exception ex) {
            System.out.println("Exception logging in\n"+ex);
            assert false;
            throw ex;
            // TODO handle custom exceptions here
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        login(System.getProperty("user.name"), System.getProperty("user.password"));
    }
    
}
