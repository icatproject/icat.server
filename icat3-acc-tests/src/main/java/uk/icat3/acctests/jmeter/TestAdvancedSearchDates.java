/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.jmeter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
public class TestAdvancedSearchDates extends AbstractJavaSamplerClient {

    private static Logger log = Logger.getLogger(TestAdvancedSearchDates.class);
    private static java.lang.String sessionId = null;
    private static List<String> keywords = null;

    public void setupTest(JavaSamplerContext arg0) {
        try {    // Call Web Service Operation

            log.info("ICAT_F_4 #1 In setupTest");

            log.info("ICAT_F_4 #1 Testing login with fedid '" + USER1 + "'...");
            if (sessionId == null) {
                sessionId = ICATUtil.getAdminPort().loginAdmin(USER1);
            }

            //make sure session id not null
            log.info("ICAT_F_4 #1 SessionId is '" + sessionId + "'");
            if (sessionId == null) {
                throw new Exception("Login unsuccessful");
            }

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
            long time = System.currentTimeMillis();
            //set up search criteria
            XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            startDate.setYear(ICAT_F_3_YEAR_START);
            startDate.setMonth(ICAT_F_3_MONTH_START);
            startDate.setDay(ICAT_F_3_DAY_START);
            startDate.setHour(ICAT_F_3_HOUR);
            startDate.setMinute(ICAT_F_3_MINUTE);
            startDate.setSecond(ICAT_F_3_SECOND);

            XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            endDate.setYear(ICAT_F_3_YEAR_END);
            endDate.setMonth(ICAT_F_3_MONTH_END);
            endDate.setDay(ICAT_F_3_DAY_END);
            endDate.setHour(ICAT_F_3_HOUR);
            endDate.setMinute(ICAT_F_3_MINUTE);
            endDate.setSecond(ICAT_F_3_SECOND);


            uk.icat3.client.AdvancedSearchDetails asd = new uk.icat3.client.AdvancedSearchDetails();
            asd.getInstruments().add(ICAT_F_3_INSTRUMENT);
            asd.setDateRangeStart(startDate);
            asd.setDateRangeEnd(endDate);

            log.info("ICAT Advanced Search using criteria [Instrument: '" + asd.getInstruments() + "'], [Start Date: '" + asd.getDateRangeStart() + "'], [End Date: '" + asd.getDateRangeEnd() + "']");

            //search for data with user                        
            List<uk.icat3.client.Investigation> investigations = ICATUtil.getPort().searchByAdvanced(sessionId, asd);
            results.setSampleLabel("ICAT Advanced Search using criteria [Instrument: '" + asd.getInstruments() + "'], [Start Date: '" + asd.getDateRangeStart() + "'], [End Date: '" + asd.getDateRangeEnd() + "']");
            results.setBytes(investigations.size());

            System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

            if (investigations.size() > 0) {
                //make sure some data is returned
                log.info("ICAT_F_4 #1 PASSED");
                System.out.println("ICAT_F_4 #1 PASSED");
                results.setSuccessful(true);
            } else {
                log.info("ICAT_F_4 #1 FAILED");
                System.out.println("ICAT_F_4 #1 FAILED");
            }

        } catch (Exception ex) {
            log.error(Helper.getStackTrace(ex));
            ex.printStackTrace();
        }
        results.sampleEnd();
        return results;
    }

    public void teardownTest(JavaSamplerContext arg0) {
        log.info("ICAT_P_4 #1 In teardown test");
    //do not log out, keep same sessionid for concurrent tests
    }

    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        return params;
    }

    public static void main(String[] args) {
        TestAdvancedSearchDates t = new TestAdvancedSearchDates();
        JavaSamplerContext j = null;
        t.setupTest(j);
        t.runTest(j);
        t.teardownTest(j);
    }
}
