/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.log4j.Logger;
import uk.icat3.acctests.util.Helper;
import static uk.icat3.acctests.util.Constants.*;

/**
 *
 * @author df01
 * This is effectively the ICAT_P_2 performance acceptance test.  It has been
 * written to conform to the JMeter framework and is called by the 
 * ICATKeywordStressTest.jmx script repeatedly during load testing.
 * Start time, end time and bytes(used to represent the number of results) 
 * fields are populated and returned.  Each action is logged using log4j.
 * Messages are logged to $HOME/.netbeans\6.1/modules/jmeter/bin/root.log
 * by default.
 */
public class TestKeywordSearch extends AbstractJavaSamplerClient {

    private static Logger log = Logger.getLogger(TestKeywordSearch.class);
    private static java.lang.String sessionId = null;
    private static List<String> keywords = null;

    public void setupTest(JavaSamplerContext arg0) {
        try {    // Call Web Service Operation

            log.info("ICAT_P_2 #1 In setupTest");

            log.info("ICAT_P_2 #1 Testing login with fedid '" + USER1 + "'...");
            if (sessionId == null) {
                sessionId = ICATUtil.getAdminPort().loginAdmin(USER1);
            }

            //make sure session id not null
            log.info("ICAT_P_2 #1 SessionId is '" + sessionId + "'");
            if (sessionId == null) {
                throw new Exception("Login unsuccessful");
            }

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
        //results.setSuccessful(true);  
        results.setSuccessful(false);
        results.setSampleLabel("ICAT Keyword Search using '" + keywords + "'");

        try {
            long time = System.currentTimeMillis();

            //search for data with user                        
            List<uk.icat3.client.Investigation> investigations = ICATUtil.getPort().searchByKeywords(sessionId, keywords);
            log.info("ICAT_P_2 #1 Searching for data, found '" + investigations.size() + "' investigations");
            results.setBytes(investigations.size());

            System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

            if (investigations.size() > 0) {
                //make sure some data is returned
                log.info("ICAT_P_2 #1 PASSED");
                results.setSuccessful(true);
            } else {
                log.info("ICAT_P_2 #1 FAILED");
                results.setSuccessful(false);
            }
        } catch (Exception ex) {
            log.error(Helper.getStackTrace(ex));
            ex.printStackTrace();
        }
        results.sampleEnd();
        return results;
    }

    public void teardownTest(JavaSamplerContext arg0) {
        log.info("ICAT_P_2 #1 In teardown test");
    //do not log out, keep same sessionid for concurrent tests
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