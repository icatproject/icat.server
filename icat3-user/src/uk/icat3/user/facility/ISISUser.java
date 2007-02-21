/*
 * ISISUser.java
 *
 * Created on 20 February 2007, 16:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user.facility;

import com.cclrc.ral.isis.userdb.session.userdb.UserDBFacade;
import com.cclrc.ral.isis.userdb.session.userdb.UserDBFacadeHome;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import uk.icat3.user.User;
import uk.icat3.user.UserDetails;

/**
 *
 * @author df01
 */
public class ISISUser implements User {
    
    UserDBFacade userDBFacade;
    
    /** Creates a new instance of ISISUser */
    public ISISUser() {
        try {
        Context context = getInitialContext();
        UserDBFacadeHome userDBFacadeHome = (UserDBFacadeHome)PortableRemoteObject.narrow(context.lookup("ejb/userdb/UserDBFacade"), UserDBFacadeHome.class);                
        userDBFacade = userDBFacadeHome.create();      
        } catch (Exception e) {
            e.printStackTrace();
        }
    }      
    
    public String getUserIdFromSessionId (String sessionId) {
       return null; 
    }
    
    public String login (String username, String password) {              
      String token = null;
      
      try {
          System.out.println("about to log on");
          token = userDBFacade.login("damian.flannery@rl.ac.uk", "helloworld");
          System.out.println("logged on...sessionId is: " + token);      
      } catch (Exception e) {
          e.printStackTrace();
      }
      
       return token;
    }
    
    public void logout (String sessionId) {
        
    }
    
    public UserDetails getUserDetails(String sessionId, String user) {
        return null;
    }
    
    private static Context getInitialContext() throws NamingException {
        Hashtable env = new Hashtable();
        // Standalone OC4J connection details
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        //env.put(Context.PROVIDER_URL, "130.246.49.147");
        env.put(Context.PROVIDER_URL, "localhost");
        System.out.println("ddfdfdf");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
        return new InitialContext(env);
  }
}
