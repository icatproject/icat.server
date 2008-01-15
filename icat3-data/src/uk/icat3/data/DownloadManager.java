/*
 * Download.java
 *
 * Created on 17-Oct-2007, 13:05:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.DatasetInclude;

/**
 * All methods for downloading file (ie getting the URL of the download service and
 * giving it to the user of ICAT so they can invoke it, normally a HTTP GET).  The user
 * needs READ and DOWNLOAD access for this.
 * 
 * Also a method for the download service to check if the user has access to download what they 
 * have requested through the HTTP GET
 * 
 * @author gjd37
 */
public class DownloadManager {

    static Logger log = Logger.getLogger(DownloadManager.class);

    /**
     * Generates the download URL for the download of a single file.  The method checks
     * if the users has permission to download the file first
     *
     * @param userId federalId of the user.
     * @param datafileId datafile id
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the data file
     */
    public static String downloadDatafile(String userId, String sessionId, Long datafileId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        Collection<Long> datafileIds = new ArrayList<Long>();
        datafileIds.add(datafileId);
        return downloadDatafiles(userId, sessionId, datafileIds, manager);
    }

    /**
     * Generates the download URL for the download of a collection of files. The method checks
     * if the users has permission to download the files first
     *
     * @param userId federalId of the user.
     * @param datafileIds collection of datafile ids
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the collection of data files
     */
    public static String downloadDatafiles(String userId, String sessionId, Collection<Long> datafileIds, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("downloadDatafiles(" + userId + ", " + sessionId + ", " + datafileIds + ", EntityManager)");

        //get the datafiles.
        Collection<Datafile> datafiles = DataFileManager.getDataFiles(userId, datafileIds, manager);

        //collection of readable and downloadable files
        Collection<Datafile> validDatafiles = new ArrayList<Datafile>();

        //loop and add downloadable to list
        for (Datafile datafile : datafiles) {
            if (datafile.getIcatRole().isActionDownload()) {
                validDatafiles.add(datafile);
                log.trace("User: " + userId + " granted access to 'DOWNLOAD' " + datafile);
            } else {
                log.trace("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafile);
            }
        }
        //check download
        if (!validDatafiles.isEmpty()) {
            return generateDownloadUrl(validDatafiles, sessionId);
        } else {
            if (datafileIds.size() == 1) {
                throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafiles.iterator().next());
            } else {
                throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafiles);
            }
        }
    }

    /**
     * Generates the download URL for the download of a data set. The method checks
     * if the users has permission to download the dataset files first
     *
     * @param userId federalId of the user.
     * @param datasetId dataset id
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the dataset
     */
    public static String downloadDataset(String userId, String sessionId, Long datasetId, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("downloadDataset(" + userId + ", " + sessionId + ", " + datasetId + ", EntityManager)");

        //get the dataset and its files.
        Dataset dataset = DataSetManager.getDataSet(userId, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY, manager);
        Collection<Datafile> datafiles = dataset.getDatafileCollection();

        //collection of readable and downloadable files
        Collection<Datafile> validDatafiles = new ArrayList<Datafile>();

        //loop and add downloadable to list
        for (Datafile datafile : datafiles) {
            if (datafile.getIcatRole().isActionDownload()) {
                validDatafiles.add(datafile);
                log.trace("User: " + userId + " had access to 'DOWNLOAD' " + datafile);
            } else {
                log.trace("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafile);
            }
        }

        //check download
        if (dataset.getIcatRole().isActionDownload() && !validDatafiles.isEmpty()) {
            return generateDownloadUrl(validDatafiles, sessionId);
        } else {
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + dataset);
        }
    }

    /**
     * Generates the URL from the given datafiles.
     *  
     * @param datafiles collection of the data files that are to be downloaded
     * @param sessionId session id of the user for ICAT
     * @return URL that will be used to download the datafiles
     */
    private static String generateDownloadUrl(Collection<Datafile> datafiles, String sessionId) {

        StringBuilder builder = new StringBuilder();
        builder.append("http://data.isis/downloadFiles?sid=" + sessionId);
        for (Datafile datafile : datafiles) {
            builder.append("&name=" + datafile.getName());
        }
        return builder.toString();
    }

    /**
     * Checks if user has access to download the files.
     *      
     * @param userId federalId of the user.
     * @param fileNames names of the files that are to be downloaded 
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object             
     */
    public static boolean checkFileDownloadAccess(String userId, Collection<String> fileNames, EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        log.trace("checkFileDownloadAccess(" + userId + ", " + fileNames + ", EntityManager)");

        //collection of readable and downloadable files
        Collection<Long> validDatafilesIds = new ArrayList<Long>();

        for (String fileName : fileNames) {
            try {
                Datafile datafile = (Datafile) manager.createNamedQuery("Datafile.findByName").setParameter("name", fileName).getSingleResult();
                //check if deleted, if so act as if the file has not been found
                if(datafile.isDeleted()){
                    log.trace(datafile+" is deleted and therefore does not exist.");
                    throw new NoResultException();
                }
                validDatafilesIds.add(datafile.getId());
            } catch (NoResultException nre) {
                log.warn("User: " + userId + " trying to download datafile: " + fileName + " that does not exist");
                throw new NoSuchObjectFoundException(fileName + " does not exist.");
            } catch (NonUniqueResultException nure) {
                log.warn("User: " + userId + " trying to download datafile: " + fileName + " that is not unique");
                throw new NoSuchObjectFoundException(fileName + " is not unique.");
            }
        }

        //now check that the user has access to read, download    
        Collection<Datafile> datafiles = DataFileManager.getDataFiles(userId, validDatafilesIds, manager);

        for (Datafile datafile : datafiles) {
            if (datafile.getIcatRole().isActionDownload()) {
                log.trace("User: " + userId + " granted access to 'DOWNLOAD' " + datafile);
            } else {
                log.trace("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafile);
                throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafile);
            }
        }
        
        //user had access
        return true;
    }
}
