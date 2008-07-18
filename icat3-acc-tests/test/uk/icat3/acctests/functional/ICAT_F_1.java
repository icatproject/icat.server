
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.functional;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static uk.icat3.acctests.util.Constants.*;
import static org.junit.Assert.*;

/**
 *
 * @author df01
 * This following unit tests are to ensure that any valid fedid can be used to 
 * log in and interact with the ICAT API.  A random  sample of fedids have been 
 * chosen for this test.  The ICAT admin wsdl has been used to avoid password
 * being compromised
 */
public class ICAT_F_1 {
    
    private static Logger log = Logger.getLogger(ICAT_F_1.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    private java.lang.String sessionId = null;    
    private static List<String> keywords = null;

    public ICAT_F_1() {
    }

    
    @Test
    public void loginAndSearch1() {                        
        try {              
            log.info("ICAT_F_1 #1 Testing login with fedid '" + USER1 + "'...");
            sessionId = adminPort.loginAdmin(USER1);            
            
            //make sure session id not null
            log.info("ICAT_F_1 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                        
            
            //search for data with user                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            log.info("ICAT_F_1 #1 Searching for data, found '" + investigations.size() + "' investigations");
            
            //make sure some data is returned
            assertTrue(investigations.size() > 0);
                                   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void loginAndSearch2() {                        
        try {  
            log.info("ICAT_F_1 #2 Testing login with fedid '" + USER2 + "'...");
            sessionId = adminPort.loginAdmin(USER2);            
            
            //make sure session id not null
            log.info("ICAT_F_1 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
                        
            //search for data
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            log.info("ICAT_F_1 #2 Searching for data, found '" + investigations.size() + "' investigations");
            
            //make sure data is returned
            assertTrue(investigations.size() > 0);
                                   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void loginAndSearch3() {                        
        try {  
            log.info("ICAT_F_1 #3 Testing login with fedid '" + USER3 + "'...");
            sessionId = adminPort.loginAdmin(USER3);            
            
            //make sure session id not null
            log.info("ICAT_F_1 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            log.info("ICAT_F_1 #3 Searching for data, found '" + investigations.size() + "' investigations");
            
            //make sure data is returned
            assertTrue(investigations.size() > 0);
                                   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        port.logout(sessionId);
        sessionId = null;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        adminService = null;
        adminPort = null;
        service = null;
        port = null;
    }

    @Before
    public void setUp() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {    // Call Web Service Operation
            adminService = new uk.icat3.client.admin.ICATAdminService();
            adminPort = adminService.getICATAdminPort();
            
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, ICAT_ADMIN_USER);
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, ICAT_ADMIN_PASSWORD);  
            
            service = new uk.icat3.client.ICATService();
            port = service.getICATPort();
            
            keywords = new ArrayList<String>();
            keywords.add(ICAT_F_1_KEYWORD1);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }   
            
    }
}