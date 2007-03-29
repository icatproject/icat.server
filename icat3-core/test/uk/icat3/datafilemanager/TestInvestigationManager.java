/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.datafilemanager;

import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.EntityNotModifiableError;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestInvestigationManager extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvestigationManager.class);
    
    /**
     * Tests creating a file
     */
   // @Test
    public void testAddKeyword() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding keyword to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid keyword
        Random ram = new Random();
        Keyword keyword = new Keyword("keyword "+ram.nextLong(),VALID_INVESTIGATION_ID );
        
        
        InvestigationManager.addKeyword(VALID_USER_FOR_INVESTIGATION, keyword, VALID_DATASET_ID_FOR_INVESTIGATION, em);
    }
    
    //@Test
    public void testDeleteKeyword() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting keyword from investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid keyword
        Keyword keywordToDelete  = null;
        Collection<Keyword> keywords = (Collection<Keyword>)executeListResultCmd("select k from Keyword k where k.deleted = 'N'");
        for(Keyword keyword : keywords){
            if(!keyword.getCreateId().contains("SPREAD") && !keyword.getCreateId().contains("PROP")){
                keywordToDelete = keyword;
                log.info("Deleting : "+keywordToDelete);
                break;
            }
        }
        
        
        if(keywordToDelete == null) throw new ICATAPIException("No keywords to delete");
        
        InvestigationManager.deleteKeyword(VALID_USER_FOR_INVESTIGATION, keywordToDelete, em);
    }
    
    @Test(expected=InsufficientPrivilegesException.class)
    public void testDeleteKeywordPropagated() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting keyword from investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        
        Collection<Keyword> keywords = (Collection<Keyword>)executeListResultCmd("select k from Keyword k where k.createId LIKE '%PROP%'");
        
        if(keywords.size() == 0) throw new ICATAPIException("No keywords to delete");
        Keyword keywordToDelete  = keywords.iterator().next();
        
        log.info("Trying to delete "+keywordToDelete);
        
        try {
            InvestigationManager.deleteKeyword(VALID_USER_FOR_INVESTIGATION, keywordToDelete, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
     //@Test
    public void testRemoveKeyword() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing keyword from investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid keyword
        Keyword keywordToDelete  = null;
        Collection<Keyword> keywords = (Collection<Keyword>)executeListResultCmd("select k from Keyword k where k.deleted = 'N'");
        for(Keyword keyword : keywords){
            if(!keyword.getCreateId().contains("SPREAD") && !keyword.getCreateId().contains("PROP")){
                keywordToDelete = keyword;
                log.info("Deleting : "+keywordToDelete);
                break;
            }
        }
        if(keywordToDelete == null) throw new ICATAPIException("No keywords to delete");
        
        InvestigationManager.removeKeyword(VALID_USER_FOR_INVESTIGATION, keywordToDelete, em);
    }
    
    // @Test(expected=InsufficientPrivilegesException.class)
    public void testAddKeywordInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding keyword to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid keyword
        Random ram = new Random();
        Keyword keyword = new Keyword("keyword "+ram.nextLong(),VALID_INVESTIGATION_ID );
        
        try {
            InvestigationManager.addKeyword(INVALID_USER, keyword, VALID_DATASET_ID_FOR_INVESTIGATION, em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    @Test
    public void testAddInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding investigator to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        //create valid keyword
        Random ram = new Random();
        Investigator investigator = new Investigator("9932",VALID_INVESTIGATION_ID );             
        
        InvestigationManager.addInvestigator(VALID_USER_FOR_INVESTIGATION, investigator, VALID_DATASET_ID_FOR_INVESTIGATION, em);
    }
    
    // @Test
    public void testDeleteInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing investigator to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        
        Investigator investigator = new Investigator("9932",VALID_INVESTIGATION_ID );
        
        InvestigationManager.deleteInvestigator(VALID_USER_FOR_INVESTIGATION, investigator,  em);
    }
    
    //@Test
    public void testRemoveInvestigator() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing investigator to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        
        Investigator investigator = new Investigator("9932",VALID_INVESTIGATION_ID );
        
        InvestigationManager.removeInvestigator(VALID_USER_FOR_INVESTIGATION, investigator,  em);
    }
    
    //@Test(expected=InsufficientPrivilegesException.class)
    public void testRemoveInvestigatorPropagated() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing investigator to investigation Id: "+VALID_DATASET_ID_FOR_INVESTIGATION);
        
        
        Investigator investigator = new Investigator("JAMES",VALID_INVESTIGATION_ID );
        
        try {
            InvestigationManager.removeInvestigator(VALID_USER_FOR_INVESTIGATION, investigator,  em);
        }  catch (ICATAPIException ex) {
            log.info("Caught : " +ex.getClass()+" : "+ex.getMessage());
            throw ex;
        }
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigationManager.class);
    }
    
}
