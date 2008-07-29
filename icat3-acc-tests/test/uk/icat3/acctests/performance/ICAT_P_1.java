/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.performance;

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
 * This class tests that various keyword searches return results within
 * an acceptable time-frame.  N.B. login time is not considered in the
 * start/finish calculation
 * 
 */
public class ICAT_P_1 {

    private static Logger log = Logger.getLogger(ICAT_P_1.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;
    private static java.lang.String sessionId = null;
    
    public ICAT_P_1() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {    // Call Web Service Operation
            adminService = new uk.icat3.client.admin.ICATAdminService();
            adminPort = adminService.getICATAdminPort();

            ((BindingProvider) adminPort).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, ICAT_ADMIN_USER);
            ((BindingProvider) adminPort).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, ICAT_ADMIN_PASSWORD);

            service = new uk.icat3.client.ICATService();
            port = service.getICATPort();

            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {        
        sessionId = null;
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
    }

    @Test
    public void searchKeywords1() {
        try {
            
            sessionId = adminPort.loginAdmin(USER5);
            
            //make sure session id not null
            log.info("ICAT_P_1 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD1);         
            
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #1 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
                                                           
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #1 Time in ms '" + (finish - start) + "'");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                         
            log.info("ICAT_P_1 #1 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords2() {
        try {
            
            sessionId = adminPort.loginAdmin(USER5);
            
            //make sure session id not null
            log.info("ICAT_P_1 #2 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD5);         
            
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #2 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
                                              
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #2 Time in ms '" + (finish - start) + "'");
           
             //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                                 
            log.info("ICAT_P_1 #2 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords3() {
        try {
            
            sessionId = adminPort.loginAdmin(USER4);
            
            //make sure session id not null
            log.info("ICAT_P_1 #3 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD6);         
            
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #3 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                                                
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #3 Time in ms '" + (finish - start) + "'");
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                           
            log.info("ICAT_P_1 #3 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords4() {
        try {
            
            sessionId = adminPort.loginAdmin(USER3);
            
            //make sure session id not null
            log.info("ICAT_P_1 #4 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD7);         
            
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #4 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                                                
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #4 Time in ms '" + (finish - start) + "'");
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                        
            log.info("ICAT_P_1 #4 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords5() {
        try {
            
            sessionId = adminPort.loginAdmin(USER2);
            
            //make sure session id not null
            log.info("ICAT_P_1 #5 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD5); 
            keywords.add(ICAT_F_2_KEYWORD7);         
            
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #5 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                                                
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #5 Time in ms '" + (finish - start) + "'");
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                             
            log.info("ICAT_P_1 #5 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords6() {
        try {
            
            sessionId = adminPort.loginAdmin(USER1);
            
            //make sure session id not null
            log.info("ICAT_P_1 #6 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD8); 
                        
            long start = System.currentTimeMillis();
                       
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #6 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                                                
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #6 Time in ms '" + (finish - start) + "'");
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                                   
            log.info("ICAT_P_1 #6 PASSED");   
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchKeywords7() {
        try {
            
            sessionId = adminPort.loginAdmin(USER6);
            
            //make sure session id not null
            log.info("ICAT_P_1 #7 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            List<String>keywords = new ArrayList<String>();            
            keywords.add(ICAT_F_2_KEYWORD8);
            keywords.add(ICAT_F_2_KEYWORD9);
                        
            long start = System.currentTimeMillis();
                                   
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_1 #7 Searching for data using keywords: " + keywords + ", found '" + investigations.size() + "' investigations");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                                                
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_1 #7 Time in ms '" + (finish - start) + "'");
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_1_MAX_TIME);
                                  
            log.info("ICAT_P_1 #7 PASSED");   
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}