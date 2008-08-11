/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.acctests.functional;

import java.util.List;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static uk.icat3.acctests.util.Constants.*;
import org.junit.Test;
import uk.icat3.client.AdvancedSearchDetails;
import static org.junit.Assert.*;
import static uk.icat3.client.InvestigationInclude.*;

/**
 *  
 * @author df01
 * The following unit tests are designed to ensure that correct data is
 * returned for the various combinations of advanced search criteria
 * 
 */
public class ICAT_F_3 {

    private static Logger log = Logger.getLogger(ICAT_F_3.class);    
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;
    private static java.lang.String sessionId = null;

    public ICAT_F_3() {
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
    public void searchAdvancedByInstrumentAndRunNumber() {
        try {
            
            log.info("ICAT_F_3 #1 searchAdvancedByInstrumentAndRunNumber");
            log.info("ICAT_F_3 #1 Criteria [RUN_START: '" + ICAT_F_3_RUN_START + "'], [RUN_END: '" + ICAT_F_3_RUN_END + "'], [INSTRUMENT: '" + ICAT_F_3_INSTRUMENT + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setRunStart(ICAT_F_3_RUN_START);
            asd.setRunEnd(ICAT_F_3_RUN_END);
            asd.getInstruments().add(ICAT_F_3_INSTRUMENT);
                                                
            //do search
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #1 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #1 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                
                //if instrument different from what we asked for --> fail
                if (!i.getInstrument().equalsIgnoreCase(ICAT_F_3_INSTRUMENT)) assertTrue(false);
                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), DATASETS_DATAFILES_AND_PARAMETERS);
                                
                //make sure dataset includes at least one datafile within range
                boolean found = false;
                List<uk.icat3.client.Dataset> datasets = _inv.getDatasetCollection();
                for (uk.icat3.client.Dataset dataset: datasets) {
                    List<uk.icat3.client.Datafile> datafiles = dataset.getDatafileCollection();
                    for (uk.icat3.client.Datafile datafile : datafiles) {
                        List<uk.icat3.client.DatafileParameter> parameters = datafile.getDatafileParameterCollection();
                        for (uk.icat3.client.DatafileParameter parameter : parameters) {
                            //if parameter is run number
                            if (parameter.getDatafileParameterPK().getName().equalsIgnoreCase(ICAT_F_3_RUN_NUMBER)) {
                                if ((parameter.getNumericValue() >= ICAT_F_3_RUN_START) && (parameter.getNumericValue() <= ICAT_F_3_RUN_END)) {
                                    found = true;                                    
                                }                                    
                            }                                
                        }                            
                    }
                }
                
                assertTrue("No Run Numbers between " + ICAT_F_3_RUN_START + " and " + ICAT_F_3_RUN_END + " found within any datasets in investigation #" + i.getId(), found);                
            }
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #1 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    @Test
    public void searchAdvancedBySample() {
        try {
            
            log.info("ICAT_F_3 #2 searchAdvancedBySample");
            log.info("ICAT_F_3 #2 Criteria [SAMPLE_NAME: '" + ICAT_F_3_SAMPLE + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setSampleName(ICAT_F_3_SAMPLE);
            asd.setCaseSensitive(true); //GD
                                                
            //do search
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #2 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #2 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), SAMPLES_ONLY);
                log.info("ICAT_F_3 #2 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
                
                boolean found = false;
                List<uk.icat3.client.Sample> samples = _inv.getSampleCollection();
                for (uk.icat3.client.Sample sample : samples) {
                    if (sample.getName().indexOf(ICAT_F_3_SAMPLE.replace("*", "")) != -1) found = true; //GD
                }//end for
                
                //make sure that sample included in at least one sample of each result returned
                assertTrue(found);
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #2 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
   @Test
    public void searchAdvancedByInstrumentAndDateRange() {
        try {
            
            log.info("ICAT_F_3 #3 searchAdvancedByInstrumentAndDateRange");

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
            
            log.info("ICAT_F_3 #3 Criteria [INSTRUMENT: '" + ICAT_F_3_INSTRUMENT + "'], [START_DATE: '" + startDate +"'], [END_DATE: '" + endDate + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.getInstruments().add(ICAT_F_3_INSTRUMENT);
            asd.setDateRangeStart(startDate);
            asd.setDateRangeEnd(endDate);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #3 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #3 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), DATASETS_AND_DATAFILES);
                log.info("ICAT_F_3 #3 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
                
                //make sure dataset includes at least one datafile within range
                boolean found = false;
                List<uk.icat3.client.Dataset> datasets = _inv.getDatasetCollection();
                for (uk.icat3.client.Dataset dataset: datasets) {
                    List<uk.icat3.client.Datafile> datafiles = dataset.getDatafileCollection();
                    for (uk.icat3.client.Datafile datafile : datafiles) {                        
                        if ((datafile.getDatafileCreateTime().compare(startDate) == DatatypeConstants.GREATER) && (datafile.getDatafileCreateTime().compare(endDate) == DatatypeConstants.LESSER))                           
                            found = true;
                    }//end for
                }//end for
                
                assertTrue("No Run datafiles between dates " + startDate + " and " + endDate + " found within any datasets in investigation #" + i.getId(), found);                                                
                
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #3 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByExperimentNumber() {
        try {
            
            log.info("ICAT_F_3 #4 searchAdvancedByExperimentNumber");

            //set up search criteria                        
            log.info("ICAT_F_3 #4 Criteria [EXPERIMENT_NUMBER: '" + ICAT_F_3_EXPERIMENT_NUMBER + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setExperimentNumber(ICAT_F_3_EXPERIMENT_NUMBER);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #4 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #4 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #4 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                  
                assertTrue("Experiment number ('" + i.getInvNumber() + "') for investigation #" + i.getId() + " does not match request '" + ICAT_F_3_EXPERIMENT_NUMBER + "'", i.getInvNumber().equalsIgnoreCase(ICAT_F_3_EXPERIMENT_NUMBER));                                                                
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #4 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByAbstractKeyword() {
        try {
            
            log.info("ICAT_F_3 #5 searchAdvancedByAbstractKeyword");

            //set up search criteria                        
            log.info("ICAT_F_3 #5 Criteria [ABSTRACT KEYWORD: '" + ICAT_F_3_ABSTRACT_KEYWORD_1 + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setInvestigationAbstract(ICAT_F_3_ABSTRACT_KEYWORD_1);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #5 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #5 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #5 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                  
                assertTrue("Abstract Keyword '" + ICAT_F_3_ABSTRACT_KEYWORD_1 + "' not found in investigation #" + i.getId(), i.getInvAbstract().indexOf(ICAT_F_3_ABSTRACT_KEYWORD_1.replace("*", "")) != -1);  //GD                                            
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #5 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByAbstractKeywords() {
        try {
            
            log.info("ICAT_F_3 #6 searchAdvancedByAbstractKeywords");

            //set up search criteria                        
            log.info("ICAT_F_3 #6 Criteria [ABSTRACT KEYWORDS: '" + ICAT_F_3_ABSTRACT_KEYWORD_2 + "', '" + ICAT_F_3_ABSTRACT_KEYWORD_3 + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setInvestigationAbstract(ICAT_F_3_ABSTRACT_KEYWORD_2 + " " + ICAT_F_3_ABSTRACT_KEYWORD_3);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #6 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #6 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #6 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                  
                assertTrue("Abstract Keyword '" + ICAT_F_3_ABSTRACT_KEYWORD_2 + "' not found in investigation #" + i.getId(), i.getInvAbstract().indexOf(ICAT_F_3_ABSTRACT_KEYWORD_2.replace("*", "")) != -1);     //GD                                                           
                assertTrue("Abstract Keyword '" + ICAT_F_3_ABSTRACT_KEYWORD_3 + "' not found in investigation #" + i.getId(), i.getInvAbstract().indexOf(ICAT_F_3_ABSTRACT_KEYWORD_3.replace("*", "")) != -1);     //GD                                                           
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #6 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByInvestigator() {
        try {
            
            log.info("ICAT_F_3 #7 searchAdvancedByInvestigator");

            //set up search criteria                        
            log.info("ICAT_F_3 #7 Criteria [INVESTIGATOR: '" + ICAT_F_3_INVESTIGATOR_1 + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.getInvestigators().add(ICAT_F_3_INVESTIGATOR_1);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #7 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #7 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #7 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                  
                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), INVESTIGATORS_ONLY);                                
                List<uk.icat3.client.Investigator> investigators = _inv.getInvestigatorCollection();
                boolean found = false;
                for (uk.icat3.client.Investigator investigator : investigators) {
                    if (investigator.getFacilityUser().getLastName().equalsIgnoreCase(ICAT_F_3_INVESTIGATOR_1)) found = true;
                }//end for
                
                assertTrue("Investigator '" + ICAT_F_3_INVESTIGATOR_1 + "' not found in investigation #" + i.getId(), found);                                                                                
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #7 PASSED");            
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByInvestigators() {
        try {
            
            log.info("ICAT_F_3 #8 searchAdvancedByInvestigators");

            //set up search criteria                        
            log.info("ICAT_F_3 #8 Criteria [INVESTIGATORS: '" + ICAT_F_3_INVESTIGATOR_1 + "', '" + ICAT_F_3_INVESTIGATOR_2 + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.getInvestigators().add(ICAT_F_3_INVESTIGATOR_1);
            asd.getInvestigators().add(ICAT_F_3_INVESTIGATOR_2);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #8 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #8 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #8 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                  
                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), INVESTIGATORS_ONLY);                                
                List<uk.icat3.client.Investigator> investigators = _inv.getInvestigatorCollection();
                boolean found1 = false;
                boolean found2 = false;
                for (uk.icat3.client.Investigator investigator : investigators) {
                    if (investigator.getFacilityUser().getLastName().equalsIgnoreCase(ICAT_F_3_INVESTIGATOR_1)) found1 = true;
                    if (investigator.getFacilityUser().getLastName().equalsIgnoreCase(ICAT_F_3_INVESTIGATOR_1)) found2 = true;
                }//end for
                
                assertTrue("Investigator '" + ICAT_F_3_INVESTIGATOR_1 + "' not found in investigation #" + i.getId(), found1);                                                                                
                assertTrue("Investigator '" + ICAT_F_3_INVESTIGATOR_2 + "' not found in investigation #" + i.getId(), found2);                                                                                
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #8 PASSED");            
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByBackCatalogueInvestigatorString() {
        try {
            
            log.info("ICAT_F_3 #9 searchAdvancedByBackCatalogueInvestigatorString");

            //set up search criteria                        
            log.info("ICAT_F_3 #9 Criteria [BCAT_INV_STR: '" + ICAT_F_3_BCAT_INV_STR + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setBackCatalogueInvestigatorString(ICAT_F_3_BCAT_INV_STR);
                          
            //do search 
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #9 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue("No results returned", investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #9 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {                                                                
                log.info("ICAT_F_3 #9 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "', Experiment Number: '" + i.getInvNumber());                                                                                                  
                assertTrue("Investigator '" + ICAT_F_3_BCAT_INV_STR + "' not found in investigation #" + i.getId(), i.getBcatInvStr().indexOf(ICAT_F_3_BCAT_INV_STR.replace("*", "")) != -1);    //GD                                                                            
            }//end for
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #9 PASSED");            
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void searchAdvancedByDatafileName() {
        try {
            
            log.info("ICAT_F_3 #10 searchAdvancedByDatafileName");
            log.info("ICAT_F_3 #10 Criteria [DATAFILE_NAME: '" + ICAT_F_3_DATAFILE_NAME + "']");
                       
            AdvancedSearchDetails asd = new AdvancedSearchDetails();
            asd.setDatafileName(ICAT_F_3_DATAFILE_NAME);
                                                
            //do search
            List<uk.icat3.client.Investigation> investigations = port.searchByAdvanced(sessionId, asd);
            log.info("ICAT_F_3 #10 Searching, found #" + investigations.size() + " results");
            
            //if no results returned --> fail                     
            assertTrue(investigations.size() > 0);
                        
            //loop through investigations for more detail
            log.info("ICAT_F_3 #10 Looping through results to ensure correctness...");
            for (uk.icat3.client.Investigation i : investigations) {
                                
                //get dependent objects
                uk.icat3.client.Investigation _inv = port.getInvestigationIncludes(sessionId, i.getId(), DATASETS_AND_DATAFILES);
                                
                //make sure dataset includes at least one datafile within range
                boolean found = false;
                List<uk.icat3.client.Dataset> datasets = _inv.getDatasetCollection();
                for (uk.icat3.client.Dataset dataset: datasets) {
                    List<uk.icat3.client.Datafile> datafiles = dataset.getDatafileCollection();
                    for (uk.icat3.client.Datafile datafile : datafiles) {
                        if (datafile.getName().equalsIgnoreCase(ICAT_F_3_DATAFILE_NAME)) found = true;                           
                    }//end for
                }//end for
                
                assertTrue("No Datafile name '" + ICAT_F_3_DATAFILE_NAME + "' found within any datasets in investigation #" + i.getId(), found);                
            }
            
            //if we get here then all is ok
            log.info("ICAT_F_3 #10 PASSED");            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    
}