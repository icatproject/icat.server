
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.functional;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
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
    
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    private java.lang.String sessionId = null;    
    private static List<String> keywords = null;

    public ICAT_F_1() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    @Test
    public void loginAndSearch1() {                        
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            System.out.println("size: " + investigations.size());
            assertTrue(investigations.size() > 0);
                                   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void loginAndSearch2() {                        
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER2);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            System.out.println("size: " + investigations.size());
            assertTrue(investigations.size() > 0);
                                   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void loginAndSearch3() {                        
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER3);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            System.out.println("size: " + investigations.size());
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