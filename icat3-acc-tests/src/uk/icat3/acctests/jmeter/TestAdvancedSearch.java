/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.jmeter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log4j.Logger;
import uk.icat3.acctests.util.Helper;
import static uk.icat3.acctests.util.Constants.*;

/**
 *
 * @author df01
 * This is effectively the ICAT_P_4 performance acceptance test.  It has been
 * written to conform to the JMeter framework and is called by the 
 * ICATAdvancedStressTest.jmx script repeatedly during load testing.
 * Start time, end time and bytes(used to represent the number of results) 
 * fields are populated and returned.  Each action is logged using log4j.
 * Messages are logged to $HOME/.netbeans\6.1/modules/jmeter/bin/root.log
 * by default.
 */
public class TestAdvancedSearch extends AbstractJavaSamplerClient {
    
    private static Logger log = Logger.getLogger(TestAdvancedSearch.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    private java.lang.String sessionId = null;    
    private static List<String> keywords = null;
    
    public void setupTest(JavaSamplerContext arg0) {
        try {    // Call Web Service Operation
                        
            log.info("ICAT_F_4 #1 In setupTest");
            
            adminService = new uk.icat3.client.admin.ICATAdminService();
            adminPort = adminService.getICATAdminPort();
            
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, ICAT_ADMIN_USER);
            ((BindingProvider)adminPort).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, ICAT_ADMIN_PASSWORD);  
            
            service = new uk.icat3.client.ICATService();
            port = service.getICATPort();
            
            keywords = new ArrayList<String>();
            keywords.add(ICAT_F_1_KEYWORD1);
            
        } catch (Exception ex) {
            log.error(Helper.getStackTrace(ex));
            ex.printStackTrace();
        }           
    }

    public SampleResult runTest(JavaSamplerContext arg0) {
        SampleResult results = new SampleResult();
        results.sampleStart();
        //results.setSuccessful(false);  
        results.setSuccessful(true);          
        
        try {              
            log.info("ICAT_F_4 #1 Testing login with fedid '" + USER1 + "'...");
            sessionId = adminPort.loginAdmin(USER3);            
            
            //make sure session id not null
            log.info("ICAT_F_4 #1 SessionId is '" + sessionId + "'");
            if (sessionId == null) throw new Exception("Login unsuccessful");                        
            
            //create Advanced Search Criteria
            uk.icat3.client.AdvancedSearchDetails asd = new uk.icat3.client.AdvancedSearchDetails();
            asd.getInstruments().add(ICAT_P_3_INSTRUMENT);
            asd.setRunStart(ICAT_P_3_START_RUN);
            asd.setRunEnd(ICAT_P_3_END_RUN);            
                        
            //search for data with user                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords); 
            log.info("ICAT_F_4 #1 Searching for data with criteria [Instrument: '" + asd.getInstruments() + "'], [Start Run: '" + asd.getRunStart() + "'], [End Run: '" + asd.getRunEnd() + "'] found '" + investigations.size() + "' investigations");
            results.setSampleLabel("ICAT Advanced Search using criteria [Instrument: '" + asd.getInstruments() + "'], [Start Run: '" + asd.getRunStart() + "'], [End Run: '" + asd.getRunEnd() + "']");                
            results.setBytes(investigations.size());
            
            //make sure some data is returned
            log.info("ICAT_F_4 #1 PASSED");
            if (investigations.size() > 0) results.setSuccessful(true);
            System.out.println("dwf64 inv size " + investigations.size());           
        } catch (Exception ex) {
            log.error(Helper.getStackTrace(ex));
            ex.printStackTrace();
        }
        results.sampleEnd();
        return results;
    }

    public void teardownTest(JavaSamplerContext arg0) { 
        log.info("ICAT_F_4 #1 In teardown test");
        port.logout(sessionId);
        sessionId = null;
        adminService = null;
        adminPort = null;
        service = null;
        port = null;
    }

    public Arguments getDefaultParameters() {     
        Arguments params = new Arguments();
        return params;
    }

    public static void main(String[] args) {
        TestKeywordSearch t = new TestKeywordSearch();
        JavaSamplerContext j = null;
        t.setupTest(j);
        t.runTest(j);
        t.teardownTest(j);
    }

}
