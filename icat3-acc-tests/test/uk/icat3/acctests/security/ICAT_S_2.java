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
 * Instrument Scientist
 */
public class ICAT_S_2 {

    private static Logger log = Logger.getLogger(ICAT_S_2.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;
    private static java.lang.String sessionId = null;

    public ICAT_S_2() {
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

    /**
     * Search for experiment that is not public but that belongs to instrument
     * of instrument scientist. Inst Sci should have access to all elements
     */
    @Test
    public void searchAdvancedByExperimentNumberNonPublicAllowed() {
        try {

            log.info("ICAT_S_2 #1 searchAdvancedByExperimentNumberNonPublicAllowed");
            sessionId = adminPort.loginAdmin(USER5);

            //make sure session id not null
            log.info("ICAT_S_2 #1 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);

            //set up search criteria                        
            log.info("ICAT_S_2 #1 Criteria [EXPERIMENT_NUMBER: '" + ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_1 + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setExperimentNumber(ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_1);
          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_2 #1 Searching, found #" + investigations.size() + " results");

            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);

            //loop through investigations for more detail
            log.info("ICAT_S_2 #1 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #1 Investigation#" + i.getId() + ", Instrument: '" + i.getInstrument() + "', Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
                assertTrue("Experiment number ('" + i.getInvNumber() + "') for investigation #" + i.getId() + " does not match request '" + ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_1 + "'", i.getInvNumber().equalsIgnoreCase(ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_1));

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
            log.info("ICAT_S_2 #1 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Search for non public data from a different instrument than instrument scientist is
     * responsible for.  Should not be allowed access.
     */
    //GD DOWNLOADER for #24003358 set by set_icat_auth_data_entities
    @Test
    public void searchAdvancedByExperimentNumberNonPublicNotAllowed() {
        try {

            log.info("ICAT_S_2 #2 searchAdvancedByExperimentNumberNonPublicNotAllowed");
            sessionId = adminPort.loginAdmin(USER5);

            //make sure session id not null
            log.info("ICAT_S_2 #2 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);

            //set up search criteria                        
            log.info("ICAT_S_2 #2 Criteria [EXPERIMENT_NUMBER: '" + ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_2 + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setExperimentNumber(ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_2);
          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_2 #2 Searching, found #" + investigations.size() + " results");

            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #2 Investigation#" + i.getId() + ", Instrument: " + i.getInstrument() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
            }//end for                                                              

            //should not return any results
            assertTrue("No results should be returned for this search as this inst sci does not have permission to view non public data from other instruments", investigations.size() == 0);

            //if we get here then all is ok
            log.info("ICAT_S_2 #2 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Search for commercial data on instrument that instrument scientist is responsible for.
     * Access should be allowed.
     */    
    @Test
    public void searchAdvancedByDatafileNameCommercialInstSciAllowed() {
        try {

            log.info("ICAT_S_2 #3 searchAdvancedByDatafileNameCommercialInstSciAllowed");
            sessionId = adminPort.loginAdmin(USER5);

            //make sure session id not null
            log.info("ICAT_S_2 #3 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);

            //set up search criteria                        
            log.info("ICAT_S_2 #3 Criteria [DATAFILE_NAME: '" + ICAT_S_1_DATAFILE_NAME + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_S_1_DATAFILE_NAME);
         
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_2 #3 Searching, found #" + investigations.size() + " results");

            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #3 Investigation#" + i.getId() + ", Instrument: " + i.getInstrument() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
            }//end for  

            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);

            //loop through investigations for more detail
            log.info("ICAT_S_2 #3 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #3 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());

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
            log.info("ICAT_S_2 #3 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Search for commercial data on instrument that instrument scientist is NOT responsible for.
     * Access should NOT be allowed.
     */    //GD DOWNLOADER for #24003359 set by set_icat_auth_data_entities
    @Test
    public void searchAdvancedByDatafileNameCommercialInstSciNotAllowed() {
        try {

            log.info("ICAT_S_2 #4 searchAdvancedByDatafileNameCommercialInstSciNotAllowed");
            sessionId = adminPort.loginAdmin(USER4);

            //make sure session id not null
            log.info("ICAT_S_2 #4 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);

            //set up search criteria                        
            log.info("ICAT_S_2 #4 Criteria [DATAFILE_NAME: '" + ICAT_S_1_DATAFILE_NAME + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_S_1_DATAFILE_NAME);
         
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_2 #4 Searching, found #" + investigations.size() + " results");

            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #4 Investigation#" + i.getId() + ", Instrument: " + i.getInstrument() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
            }//end for  

            //should not return any results
            assertTrue("Should not return any results as INST SCI for MAPS should not be able to view commercial data for LOQ!", investigations.size() == 0);

            //if we get here then all is ok
            log.info("ICAT_S_2 #4 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Search for public data on instrument that instrument scientist is NOT responsible for.
     * Access should be allowed.
     */    
    @Test
    public void searchAdvancedByDatafileNamePublicDataAllowed() {
        try {

            log.info("ICAT_S_2 #5 searchAdvancedByDatafileNamePublicDataAllowed");
            sessionId = adminPort.loginAdmin(USER4);

            //make sure session id not null
            log.info("ICAT_S_2 #5 SessionId is '" + sessionId + "'");
            assertTrue(sessionId != null);

            //set up search criteria                        
            log.info("ICAT_S_2 #5 Criteria [DATAFILE_NAME: '" + ICAT_F_3_DATAFILE_NAME + "']");

            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_F_3_DATAFILE_NAME_2);
                    
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_S_2 #5 Searching, found #" + investigations.size() + " results");

            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #5 Investigation#" + i.getId() + ", Instrument: " + i.getInstrument() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());
            }//end for  

            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);

            //loop through investigations for more detail
            log.info("ICAT_S_2 #5 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                log.info("ICAT_S_2 #5 Investigation#" + i.getId() + ", Instrument: '" + i.getInstrument() + "', Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());

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
            log.info("ICAT_S_2 #5 PASSED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}