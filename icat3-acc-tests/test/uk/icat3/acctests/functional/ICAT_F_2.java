/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.functional;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
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
 */
public class ICAT_F_2 {

    private static uk.icat3.client.admin.ICATAdminService adminService = null;
    private static uk.icat3.client.admin.ICATAdmin adminPort = null;
    private static uk.icat3.client.ICATService service = null;
    private static uk.icat3.client.ICAT port = null;    
    
    private java.lang.String sessionId = null;
    private static String USER1 = "ISIS_GUARDIAN";
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
            keywords.add(ICAT_F_2_KEYWORD2);
            keywords.add(ICAT_F_2_KEYWORD3);
            
                        
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
    public void keywordSearchAll() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), ALL);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, ALL);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
    @Test
    public void keywordSearchAllExceptDatasetsAndDatafiles() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), ALL_EXCEPT_DATASETS_AND_DATAFILES);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, ALL_EXCEPT_DATASETS_AND_DATAFILES);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsAndDatafiles() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_AND_DATAFILES);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_AND_DATAFILES);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsDatafilesAndParameters() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_DATAFILES_AND_PARAMETERS);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_DATAFILES_AND_PARAMETERS);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchDatasetsOnly() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), DATASETS_ONLY);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, DATASETS_ONLY);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsAndKeywords() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_AND_KEYWORDS);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_AND_KEYWORDS);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsOnly() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_ONLY);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_ONLY);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    @Test
    public void keywordSearchInvestigatorsShiftsAndSamples() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_SHIFTS_AND_SAMPLES);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_SHIFTS_AND_SAMPLES);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchInvestigatorsShiftsSamplesAndPublications() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    @Test
    public void keywordSearchKeywordsOnly() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), KEYWORDS_ONLY);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, KEYWORDS_ONLY);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchPublicationsOnly() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), PUBLICATIONS_ONLY);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, PUBLICATIONS_ONLY);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void keywordSearchSamplesOnly() {    
        try {  
            // TODO process result here
            sessionId = adminPort.loginAdmin(USER1);            
            if (sessionId == null) assertTrue(false);                        
            System.out.println("sessionId: " + sessionId);
                                    
            //get armstrong investigation
            List<uk.icat3.client.Investigation> investigations = port.searchByKeywords(sessionId, keywords);        
            
            //if no results returned, don't bother do anything else
            if (investigations.size() == 0) assertTrue(false);
            
            uk.icat3.client.Investigation i = port.getInvestigationIncludes(sessionId, investigations.get(0).getId(), SAMPLES_ONLY);            
            System.out.println("name: " + i.getTitle());            
            
            //check that all requested objects are contained within search result
            Helper.checkIncluded(i, SAMPLES_ONLY);
            
            //if we get here without exception then all is OK!
            assertTrue(true);
                                  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}