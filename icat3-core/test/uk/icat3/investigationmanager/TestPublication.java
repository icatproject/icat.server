/*
 * TestDatafileManager.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.investigationmanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Publication;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestPublication extends BaseTestClassTX {
    
    private static Logger log = Logger.getLogger(TestPublication.class);
    private Random random = new Random();
    
    /**
     * Tests creating a file
     */
    @Test
    public void addPublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        
        Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication, VALID_INVESTIGATION_ID, em);
        
        Publication modified = em.find(Publication.class,publicationInserted.getId() );
        
        checkPublication(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void modifyPublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        String modifiedURL = "http://"+random.nextInt();
        //create invalid publication, no name
        Publication modifiedInvestigation = getPublication(true);
        modifiedInvestigation.setInvestigationId(null);
        
        Publication duplicatePubilication = getPublicationDuplicate(true);
        //set investigation id
        Investigation in = em.find(Investigation.class, VALID_INVESTIGATION_ID);
        modifiedInvestigation.setInvestigationId(in);
        modifiedInvestigation.setId(duplicatePubilication.getId());
        
        log.trace("modifiedInvestigation url is  "+modifiedInvestigation.getUrl());
        modifiedInvestigation.setUrl(modifiedURL);
        log.trace("Modifies URl is "+modifiedURL);
        
        InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, modifiedInvestigation, em);
        
        Publication modified = em.find(Publication.class,modifiedInvestigation.getId() );
        
        assertEquals("url must be "+modifiedURL+" and not "+modified.getUrl(), modified.getUrl().toString(), modifiedURL);
        
        checkPublication(modified);
        assertFalse("Deleted must be false", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDuplicatePublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        
        try {
            Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicatePublication, VALID_INVESTIGATION_ID, em);
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublicationDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication,  AccessType.DELETE, em);
        
        Publication modified = em.find(Publication.class,validPublication.getId() );
        
        checkPublication(modified);
        assertTrue("Deleted must be true", modified.isDeleted());
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void undeletePublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for undeleting publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublicationDuplicate(true);
        
        
        InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication,  AccessType.DELETE, em);
        
        Publication modified = em.find(Publication.class,validPublication.getId() );
        
        checkPublication(modified);
        assertTrue("Deleted must be false", !modified.isDeleted());
        
        deletePublication();
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=ValidationException.class)
    public void addDeletedPublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding deleted publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        try{
            Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicatePublication, VALID_INVESTIGATION_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'unique'", ex.getMessage().contains("unique"));
            
            throw ex;
        }
       /* Publication modified = em.find(Publication.class,publicationInserted.getId() );
        
        checkPublication(modified);
        assertFalse("Deleted must be false", modified.isDeleted());*/
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void removePublication() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for rmeoving publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        duplicatePublication.setDeleted(false);
        
        try{
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, duplicatePublication, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test
    public void removeActualPublication() throws ICATAPIException {
        log.info("Testing  user: "+ICAT_ADMIN_USER+ " for rmeoving publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication duplicatePublication = getPublicationDuplicate(true);
        duplicatePublication.setDeleted(false);
        duplicatePublication.setCreateId(ICAT_ADMIN_USER);
        
        InvestigationManager.deleteInvestigationObject(ICAT_ADMIN_USER, duplicatePublication, AccessType.REMOVE, em);
        
        Publication modified = em.find(Publication.class,duplicatePublication.getId() );
        
        assertNull("Publication must not be found in DB "+duplicatePublication, modified);
    }
    
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void addPublicationInvalidUser() throws ICATAPIException {
        log.info("Testing  user: "+INVALID_USER+ " for adding publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        
        try {
            Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(INVALID_USER, validPublication, VALID_INVESTIGATION_ID, em);
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding publication to invalid investigation Id");
        
        Publication validPublication  = getPublication(true);
        
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for adding invalid publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication invalidPublication = getPublication(false);
        
        try {
            Publication publicationInserted = (Publication)InvestigationManager.addInvestigationObject(VALID_USER_FOR_INVESTIGATION, invalidPublication, VALID_INVESTIGATION_ID, em);
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication,  AccessType.DELETE, em);
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication,  AccessType.REMOVE, em);
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
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for delete publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        Publication validPublication  = getPublication(true);
        validPublication.setId(null);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, validPublication, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'not found'", ex.getMessage().contains("not found"));
            throw ex;
        }
    }
    
    /**
     * Tests creating a file
     */
    @Test(expected=InsufficientPrivilegesException.class)
    public void modifyPublicationProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for modifying a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
        try {
            InvestigationManager.updateInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsPublication, em);
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
    public void deletePublicationProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for deleting a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsPublication, AccessType.DELETE, em);
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
    public void removePublicationProps() throws ICATAPIException {
        log.info("Testing  user: "+VALID_USER_FOR_INVESTIGATION+ " for removing a props publication to investigation Id: "+VALID_INVESTIGATION_ID);
        
        //create invalid publication, no name
        Publication propsPublication = getPublicationDuplicate(false);
        
        try {
            InvestigationManager.deleteInvestigationObject(VALID_USER_FOR_INVESTIGATION, propsPublication, AccessType.REMOVE, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: "+ex.getClass()+" "+ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            throw ex;
        }
    }
    
    
    
    private Publication getPublicationDuplicate(boolean last){
        Publication publication = null;
        if(!last){
            Collection<Publication> publications = (Collection<Publication>)executeListResultCmd("select d from Publication d where d.facilityAcquired = 'Y' AND d.markedDeleted = 'N'");
            if(publications.size() == 0) throw new RuntimeException("No propergated publications");
            publication = publications.iterator().next();
        } else {
            Collection<Publication> publications = (Collection<Publication>)executeListResultCmd("select d from Publication d where d.facilityAcquired = 'N' order by d.modTime desc");
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
        assertEquals("Create id must be "+VALID_USER_FOR_INVESTIGATION, publication.getCreateId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Mod id must be "+VALID_USER_FOR_INVESTIGATION, publication.getModId(), VALID_USER_FOR_INVESTIGATION);
        assertEquals("Investigation id must be "+VALID_INVESTIGATION_ID, publication.getInvestigationId().getId(), VALID_INVESTIGATION_ID);
        
    }
    
    public static junit.framework.Test suite(){
        return new JUnit4TestAdapter(TestPublication.class);
    }
}
