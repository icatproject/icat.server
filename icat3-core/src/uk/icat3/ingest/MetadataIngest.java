package uk.icat3.ingest;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.KeywordPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.jaxb.MetadataParser;
import uk.icat3.jaxb.gen.*;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.Util;

/**
 * MetadataIngest.java
 * 
 * Created on 23-May-2007, 12:15:23
 * 
 * This class implements a service that enables a user/facility to submit an xml document that declaratively 
 * describes data that is to be ingested into ICAT.  This is designed as an alternative to manipulating api 
 * directly which would necessitate familiarity with the ICAT API and would require integration of the ICAT
 * API...
 * 
 * uses the icat3-jaxb package to read an icat compliant xml file for ingestion 
 * into ICAT.  The database is queried to decipher where 
 * 
 
 * isis will use this service at the end of each experimental run to submit metadata for a resulting raw / log / nexus file
 * read xml into object tree
 * find related investigation
 * if related investigation found then place datafile within that investigation
 * if no related investigation found then a new one must be created along with all other related artifacts e.g. datasets, samples etc.
 * 
 * 
 * @author df01
 */
public class MetadataIngest {

    // Global class logger
    static Logger log = Logger.getLogger(MetadataIngest.class);
    
    /**
     * Method that accepts XML document in the form of a String for ingestion into ICAT
     * Spawns insert off to asynchronous MessageDrivenBean for efficiency
     * 
     * @param userId 
     * @param xml 
     * @param manager 
     * @throws ValidationException 
     * 
     */ 
    public static void ingestMetadata(String userId, String xml, EntityManager manager) throws ValidationException {                
        Icat icat = null;        
        Long investigationId = null;
        
        //read xml file
        try {
            icat = MetadataParser.parseMetadata(userId, xml);
        } catch (Exception e) {
            throw new ValidationException("An error occurred while trying to parse metadata for ingestion into ICAT ", e);
        }//end try/catch                                                  
        
        //After successful parsing of xml, check to see if datafile belongs to existing investigation
        investigationId = findMatchingInvestigation(userId, icat, manager);
        
        //Extract all keywords from metadata entry
        //String[] keywords = getKeywords(icat);
        
        //If datafile does not belong to existing investigation then ingest everything
        if (investigationId == null) {           
            try {
                
                uk.icat3.entity.Keyword k = new uk.icat3.entity.Keyword();
                KeywordPK kpk = new KeywordPK();
                
                
                uk.icat3.entity.Investigation investigation = new uk.icat3.entity.Investigation();
                InvestigationManager.createInvestigation(userId, investigation, manager);
            } catch (NoSuchObjectFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
            } catch (InsufficientPrivilegesException ex) {
                java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
            } catch (ValidationException ex) {
                java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
        } //end if
        
        //If datafile belongs to existing investigation then ingest just the datafile
        if (investigationId != null) {           
            //ingest datafile            
        }//end if
    }
    
    
    /**
     * Method searches database in order to find matching investigation.
     * If a match is found then the Experiment Number is returned otherwise null is returned.
     */
    private static Long findMatchingInvestigation(String userId, Icat icat, EntityManager manager) {
        String experimentNumber = null;
        boolean trusted = false;
        Long investigationId = null;
        
        //retrieve experiment number from jaxb structure
        List<Study> studies = icat.getStudy();
        for (Study study : studies) {                               
               List<Investigation> investigations = study.getInvestigation();               
               for (Investigation inv : investigations) {                                      
                   experimentNumber = inv.getInvNumber();
                   trusted = inv.isTrusted();
               }//end for
        }//end for
        
        //if experiment number found, try to find a match in the database
        if ((!Util.isEmpty(experimentNumber)) && (trusted)) {
            AdvancedSearchDetails advanDTO = new AdvancedSearchDetails();
            advanDTO.setExperimentNumber(experimentNumber);
            Collection<uk.icat3.entity.Investigation> investigations = InvestigationSearch.searchByAdvanced(userId, advanDTO, manager);
            
            //if there is no match return null
            if ((investigations == null) || (investigations.size() == 0)) return null;
            
            //if there is a match then return investigationId
            for (uk.icat3.entity.Investigation inv : investigations) {
                investigationId = inv.getId();
            }//end for        
            return investigationId;
        }//end if
        
        
        //if experiment number not found or not trusted then we need to do some detective work
        //...
        
        
        return null;
    }
    
    /**
     * 
     */ 
    private static String[] getKeywords(Icat icat) {
        
        List<Study> studies = icat.getStudy();
        for (Study study : studies) {               
               System.out.println("study " + study.getName());               
               List<Investigation> investigations = study.getInvestigation();
               
               for (Investigation inv : investigations) {                   
                   System.out.println("inv " + inv.getTitle());    System.out.println("Trusted: " + inv.isTrusted());    
                   Keyword key = inv.getKeyword();                 
                   
                   List<Dataset> datasets = inv.getDataset();                                       
                   
                   for (Dataset dataset : datasets) {                       
                       System.out.println("dataset " + dataset.getName());
                       List<Datafile> datafiles = dataset.getDatafile();
                       List<Parameter> dsParams = dataset.getParameter();
                       Sample sample = dataset.getSample();
                 
                               
                       for (Datafile datafile : datafiles) {                           
                           System.out.println("datafile " + datafile.getName());
                           List<Parameter> dfParams = datafile.getParameter();
                           
                           for (Parameter dfParam : dfParams) {                               
                               System.out.println("data file param " + dfParam.getName());                               
                           }//end for df parameter
                           
                       }//end for datafile
                   
                       
                       for (Parameter dsParam : dsParams) {                               
                           System.out.println("dataset param " + dsParam.getName());                               
                       }//end for df parameter
                       
                       
                   }
               }
           
           }
        
        return null;
    }

}
