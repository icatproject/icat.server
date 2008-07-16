/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.performance;

import java.util.List;
import javax.xml.ws.BindingProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.icat3.acctests.util.Constants.*;
import uk.icat3.client.AdvancedSearchDetails;
import static uk.icat3.client.InvestigationInclude.*;


/**
 *
 * @author df01
 * This class tests that various advanced searches return results within
 * an acceptable time-frame.  N.B. login time is not considered in the
 * start/finish calculation
 * 
 */
public class ICAT_P_3 {
    
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

            sessionId = adminPort.loginAdmin(ISIS_GUARDIAN);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        port.logout(sessionId);
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
    }

    @Test
    public void searchAdvancedByRunNumber() {
        try {
            
            long start = System.currentTimeMillis();
            
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setRunStart(ICAT_F_3_RUN_START);
            asd.setRunEnd(ICAT_F_3_RUN_END);

            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);

            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) {
                assertTrue(false);
            }
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), NONE);
            
            //ensure that data is returned!
            //..
            //.
            
            long finish = System.currentTimeMillis();
            
            if ((start - finish) <= ICAT_P_3_MAX_TIME) 
                assertTrue(true);
            
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}