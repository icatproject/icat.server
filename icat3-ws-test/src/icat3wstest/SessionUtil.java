/*
 * Main.java
 *
 * Created on 15-Aug-2007, 12:49:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class SessionUtil {
    
    /** Creates a new instance of Main */
    public static String login(String username, String password) throws Exception{
       
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.lang.String result = ICATSingleton.getInstance().login(username, password);
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
    
    public static String loginLifetime(String username, String password, int lifetime) throws Exception{
        
        try {
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            java.lang.String result = ICATSingleton.getInstance().loginLifetime(username, password, lifetime);
            SID = result;
            System.out.println(" Logged in (lifetime) successfully with SID = "+result);
            
            System.out.println("\nTime taken: "+(System.currentTimeMillis() - time)/1000f+" seconds");
            System.out.println("------------------------------------------------------------------\n");
            assert true;
            return result;
        } catch (Exception ex) {
            System.out.println("Exception logging in (lifetime)\n"+ex);
            assert false;
            throw ex;
            // TODO handle custom exceptions here
        }
        
    }
    
    public static void logout(String sid) throws Exception{
        
        try {
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().logout(sid);
            
            System.out.println(" Logged out successfully with SID = "+sid);
            
            System.out.println("\nTime taken: "+(System.currentTimeMillis() - time)/1000f+" seconds");
            System.out.println("------------------------------------------------------------------\n");
            assert true;
            
        } catch (Exception ex) {
            System.out.println("Exception logging out\n"+ex);
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
        String sid = login(System.getProperty("user.name"), System.getProperty("usersso.password"));
      
        //String sid = loginLifetime(System.getProperty("user.name"), System.getProperty("usersso.password"), 2);
        //if(sid != null) logout(sid);
    }
    
}
