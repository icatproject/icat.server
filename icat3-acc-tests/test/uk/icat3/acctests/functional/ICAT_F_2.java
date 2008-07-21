/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.functional;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.icat3.acctests.util.Helper;
import static uk.icat3.acctests.util.Constants.*;
import static org.junit.Assert.*;
import static uk.icat3.client.InvestigationInclude.*;


/**
 *  
 * @author df01
 * This following unit tests are to ensure that all requested objects
 * are returned as part of a keyword search
 * 
 */
public class ICAT_F_2 {

    private static Logger log = Logger.getLogger(ICAT_F_2.class);
    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    
    private static java.lang.String sessionId = null;    
    private static List<String> keywords = null;
    
    public ICAT_F_2() {
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
            keywords.add(ICAT_F_2_KEYWORD1);
            //keywords.add(ICAT_F_2_KEYWORD2);
            //keywords.add(ICAT_F_2_KEYWORD3);
            
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
    public void keywordSearchAll() {    
        try {  
                                                
            log.info("ICAT_F_2 #1 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + ALL + " dependent objects...");
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);
            
            //get more detail
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), ALL);            
            log.info("ICAT_F_2 #1 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, ALL);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #1 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
    @Test
    public void keywordSearchAllExceptDatasetsAndDatafiles() {    
        try {              
                                    
            log.info("ICAT_F_2 #2 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + ALL_EXCEPT_DATASETS_AND_DATAFILES + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);            
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), ALL_EXCEPT_DATASETS_AND_DATAFILES);            
            log.info("ICAT_F_2 #2 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, ALL_EXCEPT_DATASETS_AND_DATAFILES);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #2 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsAndDatafiles() {    
        try {  
            
            log.info("ICAT_F_2 #3 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + DATASETS_AND_DATAFILES + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);            
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_AND_DATAFILES);            
            log.info("ICAT_F_2 #3 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_AND_DATAFILES);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #3 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsDatafilesAndParameters() {    
        try {  
                                                
            log.info("ICAT_F_2 #4 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + DATASETS_DATAFILES_AND_PARAMETERS + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);                        
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_DATAFILES_AND_PARAMETERS);            
            log.info("ICAT_F_2 #4 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_DATAFILES_AND_PARAMETERS);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #4 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsOnly() {    
        try {  
                                                
            log.info("ICAT_F_2 #5 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + DATASETS_ONLY + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);  
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_ONLY);            
            log.info("ICAT_F_2 #5 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_ONLY);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #5 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsAndKeywords() {    
        try {  
            
            log.info("ICAT_F_2 #6 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + INVESTIGATORS_AND_KEYWORDS + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);  
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_AND_KEYWORDS);            
            log.info("ICAT_F_2 #6 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");                        
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_AND_KEYWORDS);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #6 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsOnly() {    
        try {  
                                                
            log.info("ICAT_F_2 #7 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + INVESTIGATORS_ONLY + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0);  
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_ONLY);            
            log.info("ICAT_F_2 #7 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");                        
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_ONLY);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #7 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    @Test
    public void keywordSearchInvestigatorsShiftsAndSamples() {    
        try {  
                                                
            log.info("ICAT_F_2 #8 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + INVESTIGATORS_SHIFTS_AND_SAMPLES + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0); 
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_SHIFTS_AND_SAMPLES);            
            log.info("ICAT_F_2 #8 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");                        
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_SHIFTS_AND_SAMPLES);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #8 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsShiftsSamplesAndPublications() {    
        try {             
                                    
            log.info("ICAT_F_2 #9 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS + " dependent objects...");            
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0); 
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS);            
            log.info("ICAT_F_2 #9 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #9 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    @Test
    public void keywordSearchKeywordsOnly() {    
        try {  
           
            log.info("ICAT_F_2 #10 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + KEYWORDS_ONLY + " dependent objects...");                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0); 
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), KEYWORDS_ONLY);            
            log.info("ICAT_F_2 #10 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, KEYWORDS_ONLY);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #10 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchPublicationsOnly() {    
        try {  
            
            log.info("ICAT_F_2 #11 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + PUBLICATIONS_ONLY + " dependent objects...");                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0); 
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), PUBLICATIONS_ONLY);            
            log.info("ICAT_F_2 #11 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, PUBLICATIONS_ONLY);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #11 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchSamplesOnly() {    
        try {  
                                                            
            log.info("ICAT_F_2 #12 Testing Keyword Search (as " + ISIS_GUARDIAN + ") returning " + SAMPLES_ONLY + " dependent objects...");                        
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned --> fail
            assertTrue(investigations.size() > 0); 
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), SAMPLES_ONLY);            
            log.info("ICAT_F_2 #12 Investigation#" + i.getId() + ", Title: '" + i.getTitle() + "'");  
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, SAMPLES_ONLY);
            
            //if we get here without exception then all is OK!
            log.info("ICAT_F_2 #12 PASSED");
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}