/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.functional;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static uk.icat3.acctests.util.Constants.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author df01
 * This class will test that the xml ingestion mechansim for ICAT3.3 
 * still works (i.e. now that the code is using the updated API)
 * 
 */
public class ICAT_F_4 {
    
    private static Logger log = Logger.getLogger(ICAT_F_6.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;        
    private java.lang.String sessionId = null;    
    private static List<String> keywords = null;

    public ICAT_F_4() {
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
    public void ingestMetadata() {
        try {
            log.info("ICAT_F_4 #1 Testing ingest with '" + ISIS_GUARDIAN + "'...");            
            sessionId = adminPort.loginAdmin(ISIS_GUARDIAN); 
                        
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test/uk/icat3/acctests/functional/EMU00001927.xml")));            
            String line = "";
            String buffer = "";
            while((line = br.readLine()) != null) {
                buffer += line;
            }//end while
            log.info("ICAT_F_4 #1 Reading xml file: " + buffer);
        
            List<Long> ids = port.ingestMetadata(sessionId, buffer);
            assertTrue("No ids returned so ingest failed", ids.size() >0);
            
            log.info("ICAT_F_4 #1 Returned '" + ids.size() + "' ids...");
            for (Long id : ids) {
                log.info("ICAT_F_4 #1 Looping and retrieving info for recently stored investigation#" + id);
                uk.icat3.client.Investigation investigation = port.getInvestigation(sessionId, id);
                assertNotNull("Investigation not returned...", investigation);
                log.info("ICAT_F_4 #1 Details: [Inv Number: '" + investigation.getInvNumber() + "'], [Instrument: '" + investigation.getInstrument() + "'], [Title: '" + investigation.getTitle() + "']");
            }//end for
            
            log.info("ICAT_F_4 #1 PASSED");
            
        } catch (Exception e) {
            e.printStackTrace();                    
            assertTrue("An error occurred", false);
        }
    }
}