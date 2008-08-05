/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.performance;

import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.icat3.acctests.util.Constants.*;
import uk.icat3.client.AdvancedSearchDetails;


/**
 *
 * @author df01
 * This class tests that various advanced searches return results within
 * an acceptable time-frame.  N.B. login time is not considered in the
 * start/finish calculation
 * 
 */
public class ICAT_P_3 {
    
    private static Logger log = Logger.getLogger(ICAT_P_3.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;
    private static java.lang.String sessionId = null;

    public ICAT_P_3() {
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
    public void searchAdvancedByRunNumber() {
        try {
            
            sessionId = adminPort.loginAdmin(ISIS_GUARDIAN);
            
            //make sure session id not null
            log.info("ICAT_P_3 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            long start = System.currentTimeMillis();
            
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setRunStart(ICAT_F_3_RUN_START);
            asd.setRunEnd(ICAT_F_3_RUN_END);
            asd.getInstruments().add(ICAT_F_3_INSTRUMENT);                        

            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_P_3 #1 Searching for data using parameters : " + asd + ", found '" + investigations.size() + "' investigations");
                                              
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_3 #1 Time in ms '" + (finish - start) + "'");
           
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_3_MAX_TIME);
            
            log.info("ICAT_P_3 #1 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByDatafileName() {
        try {
            
            log.info("ICAT_P_3 #2 searchAdvancedByDatafileName");            
            sessionId = adminPort.loginAdmin(ISIS_GUARDIAN);
            
            //make sure session id not null
            log.info("ICAT_P_3 #2 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);                                                
            
            log.info("ICAT_P_3 #2 Criteria [DATAFILE_NAME: '" + ICAT_F_3_DATAFILE_NAME + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_F_3_DATAFILE_NAME);
                           
            long start = System.currentTimeMillis();
            
            //do search
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_P_3 #2 Searching, found #" + investigations.size() + " results");
                                             
            long finish = System.currentTimeMillis();
            log.info("ICAT_P_3 #2 Time in ms '" + (finish - start) + "'");
            
             //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
            
            assertTrue("To sloooooowwwwwwww", (finish - start) <= ICAT_P_3_MAX_TIME);
            
            //if we get here then all is ok
            log.info("ICAT_P_3 #2 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }

}