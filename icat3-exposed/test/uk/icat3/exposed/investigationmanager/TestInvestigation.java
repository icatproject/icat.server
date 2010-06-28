/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.investigationmanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.Logger;
import uk.icat3.entity.InvestigationType;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.exposed.util.BaseTestClassTX;
import uk.icat3.exposed.util.TestUserLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerBean;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import uk.icat3.util.InvestigationInclude;
import static uk.icat3.exposed.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestInvestigation extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestInvestigation.class);
    private static Random random = new Random();
    
    private static InvestigationManagerBean icat = new InvestigationManagerBean();
    private static UserSessionLocal tul = new TestUserLocal();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for adding investigation");
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setInvAbstract("Valid length");
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Investigation investigationInserted = icat.createInvestigation(VALID_SESSION, validInvestigation);
        
        Investigation modified = em.find(Investigation.class,investigationInserted.getId());
        
        checkInvestigation(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
        
        //check that investigator is there
        for(Investigator investigator : modified.getInvestigatorCollection()){
            log.trace(investigator.getInvestigation());
            log.trace(investigator.getInvestigatorPK().getFacilityUserId());
            assertFalse("investigator must not be deleted", investigator.isDeleted());
            assertEquals("investigator must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, VALID_FACILITY_USER_FOR_INVESTIGATION, investigator.getInvestigatorPK().getFacilityUserId());
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicateInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for adding invalid investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigation(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Investigation investigationInserted = icat.createInvestigation(VALID_SESSION, duplicateInvestigation);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void deleteInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigationDuplicate(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.deleteInvestigation(VALID_SESSION, validInvestigation.getId());
        
        Investigation modified = em.find(Investigation.class,validInvestigation.getId() );
        
        checkInvestigation(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
        
        //check deep delete
        for(Investigator investigator : modified.getInvestigatorCollection()){
            assertTrue("investigator must be deleted", investigator.isDeleted());
        }
    }
    
    
    /**
     * Tests creating a file
     */
    @Test
    public void removeInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION_ICAT_ADMIN +"  for rmeoving investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation duplicateInvestigation = getInvestigationDuplicate(true);
        duplicateInvestigation.setDeleted(false);
        duplicateInvestigation.setCreateId(VALID_ICAT_ADMIN_FOR_INVESTIGATION);
        
        Collection<Long> longs =  addAuthorisation(duplicateInvestigation.getId(), null, VALID_ICAT_ADMIN_FOR_INVESTIGATION, ElementType.INVESTIGATION, IcatRoles.ICAT_ADMIN);
        Iterator it = longs.iterator();
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.removeInvestigation(VALID_SESSION_ICAT_ADMIN, duplicateInvestigation.getId());
        
        Investigation modified = em.find(Investigation.class,duplicateInvestigation.getId() );
        assertNull("Investigation must not be found in DB "+duplicateInvestigation, modified);
        
        IcatAuthorisation icatAuth = em.find(IcatAuthorisation.class,it.next());
        IcatAuthorisation childIcatAuth = em.find(IcatAuthorisation.class,it.next());
                
        assertNull("IcatAuthorisation[main] must not be found in DB ", icatAuth);
        assertNull("IcatAuthorisation[child] must not be found in DB ", childIcatAuth);
    }
    
    /**
     * Tests creating a file
     */
    //@Test(expected=InsufficientPrivilegesException.class)
    public void addInvestigationInvalidUser() throws ICATAPIException {
        log.info("Testing  session: "+INVALID_SESSION+ " for adding investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Investigation investigationInserted = icat.createInvestigation(INVALID_SESSION, validInvestigation);
            
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for adding invalid investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation invalidInvestigation = getInvestigation(false);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Investigation investigationInserted = icat.createInvestigation(VALID_SESSION, invalidInvestigation);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addValidInvestigationInvalidAbstract() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for adding valid investigation with too big abstract to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation invalidInvestigation = getInvestigation(true);
        
        StringBuilder builder  = new StringBuilder();
        for(int i = 0; i  < 4001; i ++ ){
            builder.append(i);
        }
        invalidInvestigation.setInvAbstract(builder.toString());
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Investigation investigationInserted = icat.createInvestigation(VALID_SESSION, invalidInvestigation);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be more than'", ex.getMessage().contains("cannot be more than"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void deleteInvestigationProps() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting a props investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation propsInvestigation = getInvestigationDuplicate(false);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deleteInvestigation(VALID_SESSION, propsInvestigation.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
          assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
               throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removeInvestigationProps() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for removing a props investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid investigation, no name
        Investigation propsInvestigation = getInvestigationDuplicate(false);
        log.trace(propsInvestigation);
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.removeInvestigation(VALID_SESSION, propsInvestigation.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
           assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
   /* @Test
    public void getInvestigations() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for gettings investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        Collection<Long> investigations = new ArrayList<Long>();
        investigations.add(VALID_INVESTIGATION_ID);
    
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
    
        Collection<Investigation> investigationsFound = icat.getInvestigations(VALID_SESSION, investigations);
    
        for(Investigation investigation : investigationsFound){
            assertNotNull("investigation title cannot be null", investigation.getTitle());
            assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
            assertNotNull("investigation id cannot be null", investigation.getId());
            assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
            //    assertFalse("Deleted must be false", investigation.isDeleted());
            //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
        }
    }*/
    
    /**
     * Tests creating a file
     */
    @Test
    public void getInvestigation() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for gettings investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Investigation investigation = icat.getInvestigation(VALID_SESSION, VALID_INVESTIGATION_ID);
        
        //  checkInvestigation(investigation);
        assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
        // assertFalse("Deleted must be false", investigation.isDeleted());
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void getInvestigationIncludes() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for gettings investigation includes to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Investigation investigation = icat.getInvestigation(VALID_SESSION, VALID_INVESTIGATION_ID, InvestigationInclude.DATASETS_ONLY);
        
        //close em
        //em.getTransaction().commit();
        //em.close();
        
        //  checkInvestigation(investigation);
        assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
        // assertFalse("Deleted must be false", investigation.isDeleted());
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
        assertNotNull("number of datasets cannot be null", investigation.getDatasetCollection());
        
        for (Dataset ds : investigation.getDatasetCollection()) {
            log.info("Number of datafile is "+ds.getDatafileCollection().size());
            //cannot check this as it fetches them even if i close the em on line 329???
            //assertNull("number of datafiles must be null", ds.getDatafileCollection());
        }
        
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void getInvestigationIncludes2() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for gettings investigation includes to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Investigation investigation = icat.getInvestigation(VALID_SESSION, VALID_INVESTIGATION_ID, InvestigationInclude.DATASETS_AND_DATAFILES);
        
        //close em
        //em.close();
        
        //   checkInvestigation(investigation);
        assertEquals("investigation id is "+VALID_INVESTIGATION_ID, VALID_INVESTIGATION_ID, investigation.getId());
        // assertFalse("Deleted must be false", investigation.isDeleted());
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        //assertEquals("Number datasets must be 2", investigation.getDatasetCollection().size(),2);
        assertNotNull("number of datasets cannot be null", investigation.getDatasetCollection());
        for (Dataset ds : investigation.getDatasetCollection()) {
            assertNotNull("number of datafiles cannot be null", ds.getDatafileCollection());
        }
        
    }
    
    
    /**
     * Tests deleting a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deleteInvestigationNoId() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deleteInvestigation(VALID_SESSION, validInvestigation.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage(),ex);
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests remove a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removeInvestigationNoId() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for deleting investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.removeInvestigation(VALID_SESSION, validInvestigation.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a investigation, no id
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updateInvestigationNoId() throws ICATAPIException {
        log.info("Testing  session: "+ VALID_SESSION +"  for updating investigation to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Investigation validInvestigation  = getInvestigation(true);
        validInvestigation.setId(null);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.modifyInvestigation(VALID_SESSION, validInvestigation);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    private Investigation getInvestigationDuplicate(boolean last){
        Investigation investigation = null;
        if(!last){
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.createId LIKE '%PROP%'");
            investigation = investigations.iterator().next();
        } else {
            Collection<Investigation> investigations = (Collection<Investigation>)executeListResultCmd("select d from Investigation d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
            investigation = investigations.iterator().next();
            if(investigation == null) throw new RuntimeException("No investigation found");
        }
        log.trace(investigation);
        return investigation;
    }
    
    private Investigation getInvestigation(boolean valid){
        if(valid){
            //create valid investigation
            Investigation investigation = new Investigation();
            
            investigation.setTitle("investigation "+random.nextInt());
            investigation.setInvNumber("11");
            investigation.setInvType("experiment");
            
            return investigation;
        } else {
            //create invalid investigation
            Investigation investigation = new Investigation();
            return investigation;
        }
    }
    
    private void checkInvestigation(Investigation investigation){
        assertNotNull("investigation title cannot be null", investigation.getTitle());
        assertNotNull("investigation InvNumber cannot be null", investigation.getInvNumber());
        assertNotNull("investigation id cannot be null", investigation.getId());
        assertEquals("Create id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, investigation.getCreateId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, investigation.getModId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestInvestigation.class);
    }
}
