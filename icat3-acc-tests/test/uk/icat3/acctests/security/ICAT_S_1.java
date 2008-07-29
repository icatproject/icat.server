/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.security;

import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.icat3.client.AdvancedSearchDetails;
import static uk.icat3.acctests.util.Constants.*;
import static org.junit.Assert.*;
import static uk.icat3.client.InvestigationInclude.*;

/**
 *
 * @author df01
 * Administrator
 */
public class ICAT_S_1 {

    private static Logger log = Logger.getLogger(ICAT_S_1.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;
    private static java.lang.String sessionId = null;

    public ICAT_S_1() {
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
    public void searchAdvancedByExperimentNumberNonPublic() {
        try {

            log.info("ICAT_S_1 #1 searchAdvancedByExperimentNumber");

            //set up search criteria                        
            log.info("ICAT_S_1 #1 Criteria [EXPERIMENT_NUMBER: '" + ICAT_F_3_EXPERIMENT_NUMBER + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setExperimentNumber(ICAT_F_3_EXPERIMENT_NUMBER);
            
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_1 #1 Searching, found #" + investigations.size() + " results");

            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);

            //loop through investigations for more detail
            log.info("ICAT_S_1 #1 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_1 #1 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
                assertTrue("Experiment number ('" + i.getInvNumber() + "') for investigation #" + i.getId() + " does not match request '" + ICAT_F_3_EXPERIMENT_NUMBER + "'", i.getInvNumber().equalsIgnoreCase(ICAT_F_3_EXPERIMENT_NUMBER));

                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), ALL);

                //get access to publications
                List<uk.icat3.client.Publication> publications = _inv.getPublicationCollection();
                assertTrue("Could not access publications", publications.size() > 0);

                //get access to investigators
                List<uk.icat3.client.Investigator> investigators = _inv.getInvestigatorCollection();
                assertTrue("Could not access investigators", investigators.size() > 0);

                //get access to samples
                List<uk.icat3.client.Sample> samples = _inv.getSampleCollection();
                assertTrue("Could not access samples", samples.size() > 0);

                //get access to a parameter 
                List<uk.icat3.client.Dataset> datasets = _inv.getDatasetCollection();
                boolean foundParam = false;
                for (uk.icat3.client.Dataset dataset : datasets) {
                    List<uk.icat3.client.Datafile> datafiles = dataset.getDatafileCollection();
                    for (uk.icat3.client.Datafile datafile : datafiles) {                        
                        List<uk.icat3.client.DatafileParameter> parameters = datafile.getDatafileParameterCollection();
                        for (uk.icat3.client.DatafileParameter parameter : parameters) {                           
                            if (parameter.getDatafileParameterPK().getName().length() > 0) {
                                foundParam = true;
                            }//end if                                
                        }//end if                            
                    }//end if
                }//end if

                assertTrue("Could not access parameters in investigation #" + i.getId(), foundParam);
            }//end for

            //if we get here then all is ok
            log.info("ICAT_S_1 #1 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void searchAdvancedByDatafileNameCommercial() {
        try {

            log.info("ICAT_S_1 #2 searchAdvancedByDatafileNameCommercial");

            //set up search criteria                        
            log.info("ICAT_S_1 #2 Criteria [DATAFILE_NAME: '" + ICAT_S_1_DATAFILE_NAME + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_S_1_DATAFILE_NAME);
            
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_1 #2 Searching, found #" + investigations.size() + " results");

            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);

            //loop through investigations for more detail
            log.info("ICAT_S_1 #2 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_1 #2 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());

                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), ALL);

                //get access to a parameter 
                List<uk.icat3.client.Dataset> datasets = _inv.getDatasetCollection();
                boolean foundParam = false;
                for (uk.icat3.client.Dataset dataset : datasets) {
                    List<uk.icat3.client.Datafile> datafiles = dataset.getDatafileCollection();
                    for (uk.icat3.client.Datafile datafile : datafiles) {
                        List<uk.icat3.client.DatafileParameter> parameters = datafile.getDatafileParameterCollection();
                        for (uk.icat3.client.DatafileParameter parameter : parameters) {
                            if (parameter.getDatafileParameterPK().getName().length() > 0) {
                                foundParam = true;
                            }//end if                                
                        }//end if                            
                    }//end if
                }//end if

                assertTrue("Could not access parameters in investigation #" + i.getId(), foundParam);
            }//end for

            //if we get here then all is ok
            log.info("ICAT_S_1 #2 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}