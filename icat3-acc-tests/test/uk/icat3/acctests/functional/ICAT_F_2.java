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
import uk.icat3.acctests.util.Constants;
import static org.junit.Assert.*;

/**
 *
 * @author df01
 */
public class ICAT_F_2 {

    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    
    private java.lang.String sessionId = null;
    private static String USER1 = "ISIS_GUARDIAN";
    private static List<String> keywords = null;
    
    public ICAT_F_2() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {    // Call Web Service Operation
            adminService = new uk.icat3.client.admin.ICATAdminService();
            adminPort = adminService.getICATAdminPort();
            
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, Constants.ICAT_ADMIN_USER);
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, Constants.ICAT_ADMIN_PASSWORD);  
            
            service = new uk.icat3.client.ICATService();
            port = service.getICATPort();
            
            keywords = new ArrayList<String>();            
            keywords.add(Constants.ICAT_F_2_KEYWORD1);
            keywords.add(Constants.ICAT_F_2_KEYWORD2);
            keywords.add(Constants.ICAT_F_2_KEYWORD3);
            
                        
        } catch (Exception ex) {
            ex.printStackTrace();
        }   
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

    @After
    public void tearDown() {
        port.logout(sessionId);
        sessionId = null;
    }

   
    @Test
    public void keywordSearchAll() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            if (investigations.size() == 0) assertTrue(false);
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), uk.icat3.client.InvestigationInclude.ALL);
            
            System.out.println("name: " + i.getTitle());            
            assertTrue(investigations.size() > 0);
                      
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}