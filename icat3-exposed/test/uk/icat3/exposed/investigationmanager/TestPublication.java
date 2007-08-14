/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.investigationmanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Publication;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.AccessType;
import uk.icat3.exposed.util.BaseTestClassTX;
import uk.icat3.exposed.util.TestUserLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerBean;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import static uk.icat3.exposed.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestPublication extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestPublication.class);
    private Random random = new Random();
    
    private static InvestigationManagerBean icat = new InvestigationManagerBean();
    private static UserSessionLocal tul = new TestUserLocal();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addPublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for adding publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        Publication insertedPublication = icat.addPublication(VALID_SESSION, validPublication, VALID_INVESTIGATION_ID);
        
        Publication modified = em.find(Publication.class, insertedPublication.getId() );
        
        checkPublication(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void modifyPublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ "  for modifying publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedURL = "http://"+random.nextInt();
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        
        duplicatePublication.setUrl(modifiedURL);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.modifyPublication(VALID_SESSION, duplicatePublication);
        
        Publication modified = em.find(Publication.class,duplicatePublication.getId() );
        
        assertEquals("url must be "+modifiedURL+" and not "+modified.getUrl(), modified.getUrl().toString(), modifiedURL);
        
        checkPublication(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicatePublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ "  for adding invalid publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
             try {
            icat.addPublication(VALID_SESSION, duplicatePublication, VALID_INVESTIGATION_ID);
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
    public void deletePublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for rmeoving publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublicationDuplicate(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.deletePublication(VALID_SESSION, validPublication.getId());
        
        Publication modified = em.find(Publication.class, validPublication.getId() );
        
        checkPublication(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedPublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for adding deleted publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
         try{
             icat.addPublication(VALID_SESSION, duplicatePublication, VALID_INVESTIGATION_ID);
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
    public void removePublication() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION_ICAT_ADMIN+ "  for rmeoving publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        duplicatePublication.setDeleted(false);
        duplicatePublication.setCreateId(VALID_ICAT_ADMIN_FOR_INVESTIGATION);
                
        //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        icat.removePublication(VALID_SESSION_ICAT_ADMIN, duplicatePublication.getId());
        
        Publication modified = em.find(Publication.class,duplicatePublication.getId() );
        
        assertNull("Publication must not be found in DB "+duplicatePublication, modified);
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addPublicationInvalidUser() throws ICATAPIException {
        log.info("Testing  session: "+INVALID_SESSION+ "  for adding publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.addPublication(INVALID_SESSION, validPublication, VALID_INVESTIGATION_ID);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void addPublicationInvalidInvestigation() throws ICATAPIException {
        log.info("Testing session: "+VALID_SESSION+ " for adding publication to invalid investigation Id");
        
        Publication validPublication  = getPublication(true);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication, random.nextLong(), em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addInvalidPublication() throws ICATAPIException {
        log.info("Testing session: "+VALID_SESSION+ " for adding invalid publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication invalidPublication = getPublication(false);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.addPublication(VALID_SESSION, invalidPublication, VALID_INVESTIGATION_ID);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'cannot be null'", ex.getMessage().contains("cannot be null"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void deletePublicationNoId() throws ICATAPIException {
        log.info("Testing session: "+VALID_SESSION+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deletePublication(VALID_SESSION, validPublication.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void removePublicationNoId() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION_ICAT_ADMIN+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        validPublication.setCreateId(VALID_ICAT_ADMIN_FOR_INVESTIGATION);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deletePublication(VALID_SESSION_ICAT_ADMIN, validPublication.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests update a file
     */
    @Test(expected=NoSuchObjectFoundException.class)
    public void updatePublicationNoId() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
           icat.modifyPublication(VALID_SESSION, validPublication);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //  @Test(expected=InsufficientPrivilegesException.class)
    public void modifyPublicationProps() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for modifying a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.modifyPublication(VALID_SESSION, propsPublication);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
           assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
             throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    // @Test(expected=InsufficientPrivilegesException.class)
    public void deletePublicationProps() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for deleting a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.deletePublication(VALID_SESSION, propsPublication.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
          assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
              throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    //   @Test(expected=InsufficientPrivilegesException.class)
    public void removePublicationProps() throws ICATAPIException {
        log.info("Testing  session: "+VALID_SESSION+ " for removing a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
          //set entitymanager for each new method
        icat.setEntityManager(em);
        icat.setUserSession(tul);
        
        try {
            icat.removePublication(VALID_SESSION, propsPublication.getId());
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
           assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    
    private Publication getPublicationDuplicate(boolean last){
        Publication publication = null;
        if(!last){
            Collection<Publication> publications = (Collection<Publication>)executeListResultCmd("select d from Publication d where d.createId LIKE '%PROP%'");
            if(publications.size() == 0) throw new RuntimeException("No propergated publications");
            publication = publications.iterator().next();
        } else {
            Collection<Publication> publications = (Collection<Publication>)executeListResultCmd("select d from Publication d where d.createId NOT LIKE '%PROP%' order by d.modTime desc");
            publication = publications.iterator().next();
        }
        log.trace(publication);
        return publication;
    }
    
    private Publication getPublication(boolean valid){
        if(valid){
            //create valid publication
            Publication publication = new Publication();
            
            publication.setUrl("http://publication.com");
            
            publication.setFullReference("full");
            return publication;
        } else {
            //create invalid publication
            Publication publication = new Publication();
            return publication;
        }
    }
    
    private void checkPublication(Publication publication){
        assertNotNull("getId cannot be null", publication.getId());
        assertNotNull("getInvestigationId cannot be null", publication.getInvestigationId());
        assertNotNull("full ref cannot be null", publication.getFullReference());
        assertEquals("Create id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, publication.getCreateId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_FACILITY_USER_FOR_INVESTIGATION, publication.getModId(), VALID_FACILITY_USER_FOR_INVESTIGATION);
        assertEquals("Investigation id must be "+VALID_INVESTIGATION_ID, publication.getInvestigationId().getId(), VALID_INVESTIGATION_ID);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestPublication.class);
    }
}
