/*
 * ISISUser.java
 *
 * Created on 20 February 2007, 16:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user.facility;

import com.cclrc.ral.isis.userdb.session.userdb.PersonDetailsDTO;
import com.cclrc.ral.isis.userdb.session.userdb.UserDBFacade;
import com.cclrc.ral.isis.userdb.session.userdb.UserDBFacadeHome;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import uk.icat3.user.User;
import uk.icat3.user.UserDetails;
import uk.icat3.user.exceptions.LoginException;

/**
 *
 * @author df01
 */
public class ISISUser implements User {
    
    UserDBFacade userDBFacade;
    
    /** Creates a new instance of ISISUser */
    public ISISUser() throws LoginException {
        try {
            Context context = getInitialContext();
            UserDBFacadeHome userDBFacadeHome = (UserDBFacadeHome)PortableRemoteObject.narrow(context.lookup("ejb/userdb/UserDBFacade"), UserDBFacadeHome.class);                
            userDBFacade = userDBFacadeHome.create();      
        } catch (Exception e) {
            e.printStackTrace();
            throw new LoginException("Unable to establish connection to ISIS User database");
        }//end try/catch
    }      
    
    public String getUserIdFromSessionId (String sessionId) throws LoginException {        
        Long userNum = null;
        try {
            userNum = userDBFacade.getUserNumberFromSessionId(sessionId);
            if (userNum == null) throw new LoginException();
        } catch (Exception e) {
            e.printStackTrace();
            throw new LoginException("Unable to retrieve userid/distinguished name from ISIS user database using sessionId '" + sessionId + "'. Please try logging in again." );
        }//end try/catch
       return userNum.toString(); 
    }
    
    public String login (String username, String password) throws LoginException {              
      String token = null;
      
      try {
          System.out.println("about to log on");
          token = userDBFacade.login("damian.flannery@rl.ac.uk", "helloworld");
          System.out.println("logged on...sessionId is: " + token);      
      } catch (Exception e) {
          e.printStackTrace();
          throw new LoginException();
      }
      
       return token;
    }
    
    public void logout (String sessionId) {
        try {
            userDBFacade.logout(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }//end try/catch
    }
    
    public UserDetails getUserDetails(String sessionId, String user) throws LoginException {
        UserDetails details = new UserDetails();
        
        try {
            
            Long userNum = userDBFacade.getUserNumberFromSessionId(sessionId);
            if (userNum == null) throw new LoginException();
            
            PersonDetailsDTO dto = userDBFacade.getPersonDetails(sessionId, new Long(user));
            details.setTitle(dto.getTitle());
            details.setInitial(dto.getInitials());
            details.setFirstName(dto.getFirstNameKnownAs());
            details.setLastName(dto.getFamilyName());
            details.setDepartment(dto.getDeptName());
            details.setInstitution(dto.getOrgName());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new LoginException("An error occured while trying to retrieve UserDetails from ISIS user database for user# " + user + " with sessionId# " + sessionId);
        }//end try/catch
        
        return details;
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
