package org.icatproject.download;

import java.util.ResourceBundle;

import javax.naming.NamingException;

import org.icatproject.core.IcatException;


public class SessionDelegate {
    
    private static SessionDelegate sd;
    private static UserSession usr ;
   
    public static SessionDelegate getInstance(){
            synchronized(SessionDelegate.class){
                if(sd == null){
                    try {
                        sd = new SessionDelegate();
                    } catch(Exception se) {
                        throw new RuntimeException(se);
                    }
                }
                return sd;
            }       
    }    
    
    /** Creates a new instance of SessionDelegate */
    private  SessionDelegate() throws NamingException {
        ResourceBundle facilityResources = ResourceBundle.getBundle("uk.icat3.download.facility");

        String facilityName = null;
        try {
            facilityName = facilityResources.getString("facility.name");
        } catch (Exception mre) {
            facilityName = "ISIS";
        }
        CachingServiceLocator csl =  CachingServiceLocator.getInstance();
        usr = (UserSession)csl.lookup("UserSession"+facilityName);
    }
    
    /*All SessionDelegate methods here*/
    public String getUserFromSessionId(String sessionId) throws IcatException {
        return usr.getUserIdFromSessionId(sessionId);
    }    
}
