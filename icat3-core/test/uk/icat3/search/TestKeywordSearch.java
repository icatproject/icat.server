/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.util.BaseTestClass;
import static org.junit.Assert.*;
import uk.icat3.util.KeywordType;
import static uk.icat3.util.TestConstants.*;
import uk.icat3.util.TestConstants;
import uk.icat3.entity.Investigation;
/**
 *
 * @author gjd37
 */
public class TestKeywordSearch extends BaseTestClass {
    
    private static Logger log = Logger.getLogger(TestKeywordSearch.class);
    
    
    /**
     * Tests whether the user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testValidUserKeyword(){
        log.info("Testing valid user for all keywords: "+VALID_USER_FOR_INVESTIGATION);
        testKeywords(VALID_USER_FOR_INVESTIGATION);
    }
    
     /**
     * Tests whether the SUPER user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testValidSuperUserKeyword(){
        log.info("Testing super user for all keywords: "+SUPER_USER);
        testKeywords(SUPER_USER);
    }
    
    /**
     * Tests whether the user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testInvalidUserKeyword(){
        log.info("Testing invalid user for all keywords: "+INVALID_USER);
        testKeywords(INVALID_USER);
    }
    
    /**
     * Tests whether the user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testValidUsersKeywords(){
        log.info("Testing valid user for their keywords: "+VALID_USER_FOR_INVESTIGATION);
        testUsersKeywords(VALID_USER_FOR_INVESTIGATION, true);
    }
    
    /**
     * Tests whether the user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testInvalidUsersKeywords(){
        log.info("Testing invalid user for their keywords: "+INVALID_USER);
        testUsersKeywords(INVALID_USER, false);
    }
    
    private void testKeywords(String user){
        log.info("Testing user: "+user);
        
        //get the number of keywords
        Collection<String> keywordsInDB = (Collection<String>)executeNativeListResultCmd("SELECT DISTINCT NAME FROM keyword WHERE regexp_like(NAME,'^[[:alpha:]]*$')", String.class);
        log.trace("Number keywords (ALPHA) in DB is : "+keywordsInDB.size());
        
        //clear manager, because of alot of keywords
        em.clear();
        
        //search all keywords
        Collection<String> keywords = KeywordSearch.getAllKeywords(user, KeywordType.ALPHA, em);
        log.trace("Number keywords (ALPHA) from search is : "+keywords.size());
        
        assertNotNull("Must not be an null collection of keywords (ALPHA)", keywords);
        assertEquals("Number of keywords (ALPHA) searched is different to number in DB",keywordsInDB.size(),keywords.size());
        
        //clear manager
        em.clear();
        
        //get keywords for user
        keywordsInDB = (Collection<String>)executeNativeListResultCmd("SELECT DISTINCT NAME FROM keyword WHERE regexp_like(NAME,'^[[:alnum:]]*$')", String.class);
        log.trace("Number keywords (ALPHA_NUMERIC) in DB is : "+keywordsInDB.size());
        
        //clear manager, because of alot of keywords
        em.clear();
        
        //search all keywords
        keywords = KeywordSearch.getAllKeywords(user, KeywordType.ALPHA_NUMERIC, em);
        log.trace("Number keywords (ALPHA_NUMERIC) from search is : "+keywords.size());
        
        assertNotNull("Must not be an null collection of keywords (ALPHA_NUMERIC)", keywords);
        assertEquals("Number of keywords (ALPHA_NUMERIC) searched is different to number in DB",keywordsInDB.size(),keywords.size());
        
        //clear manager
        em.clear();
    }
    
    //TODO make this method better
    private void testUsersKeywords(String user, boolean valid){
        
        //get number of keywords user can see starting with isis
       /* Collection<String> keywordsUserInDB = (Collection<String>)executeListResultCmd("SE");
        
        //clear manager, because of alot of keywords
        em.clear();*/
        
        //search all keywords
        Collection<String> keywordsForUser = KeywordSearch.getKeywordsForUser(user, "", em);
        log.trace("Number keywords for user "+user+" from search is : "+keywordsForUser.size());
        
        assertNotNull("Must not be an null collection of user keywords", keywordsForUser);
        if(valid){
            assertTrue("Number of user keywords searched for user is different to number in DB", keywordsForUser.size() > 0);
        } else {
            assertSame("Number of user keywords searched for user is different to number in DB", 0 , keywordsForUser.size());
            
        }
        
        
        //clear manager
        em.clear();
    }
    
    /**
     * Tests whether the user has any investigations and that he has access to the number of investigations
     * with no investigator
     */
    @Test
    public void testKeywordSearch(){
        log.info("Testing invalid user: "+INVALID_USER+ ", should be associated with no investigations");
        
        //get the number of keywords
        Collection<Investigation> investigations = (Collection<Investigation> )executeListResultCmd("SELECT i FROM Investigation i where i.investigatorCollection IS EMPTY ");
        log.trace("Investigations for user in DB for "+INVALID_USER+" is "+investigations.size());
        
        
        //search for users own investigations
        Collection<Investigation> searchedInvestigations = InvestigationSearch.searchByKeyword(INVALID_USER,"l", 0, 100000, em);
        log.trace("Investigations for user "+INVALID_USER+" is "+searchedInvestigations.size());
        
        assertNotNull("Must not be an null collection", searchedInvestigations);
        assertEquals("Collection 'searchByKeyword()' should be zero", 0 , searchedInvestigations.size());
        
        //search by user id
        /*Collection<Investigation> searchedUserIdInvestigations = InvestigationSearch.searchByUserID(INVALID_USER,"JAMES-JAMES", em);
        log.trace("Investigations for user "+INVALID_USER+" is "+searchedUserIdInvestigations.size());
         
        assertNotNull("Must not be an null collection", searchedUserIdInvestigations);
        assertEquals("Collection 'searchByKeyword()' should be zero size", 0 , searchedUserIdInvestigations.size());
         
         
        //search by surname
        Collection<Investigation> searchedSurnameInvestigations = InvestigationSearch.searchByUserSurname(INVALID_USER,"HEALY", em);
        log.trace("Investigations for user "+INVALID_USER+" is "+searchedSurnameInvestigations.size());
         
        assertNotNull("Must not be an null collection", searchedSurnameInvestigations);
        assertEquals("Collection 'searchByKeyword()' should be zero size", 0 , searchedSurnameInvestigations.size());*/
    }
    
    
    
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestKeywordSearch.class);
    }
    
}
