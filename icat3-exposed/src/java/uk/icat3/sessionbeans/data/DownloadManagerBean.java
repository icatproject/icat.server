/*
 * DownloadManagerBean.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.data;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import uk.icat3.data.DownloadInfo;
import uk.icat3.data.DownloadManager;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.exceptions.NoSuchObjectFoundException;

/**
 *
 * @author gjd37
 */
@Stateless
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DownloadManagerBean extends EJBObject implements DownloadManagerLocal {

    static Logger log = Logger.getLogger(DownloadManagerBean.class);

    /**
     * Generates the download URL for the download of a single file.  The method checks
     * if the users has permission to download the file first
     *
     * @param sessionId session Id of the user.
     * @param datafileId datafile id
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the data file
     */
    public String downloadDatafile(String sessionId, Long datafileId) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DownloadManager.downloadDatafile(userId, sessionId, datafileId, manager);
    }

    /**
     * Generates the download URL for the download of a collection of files. The method checks
     * if the users has permission to download the files first
     *
     * @param sessionId session Id of the user.
     * @param datafileIds collection of datafile ids
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the collection of data files
     */
    public String downloadDatafiles(String sessionId, Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DownloadManager.downloadDatafiles(userId, sessionId, datafileIds, manager);
    }

    /**
     * Generates the download URL for the download of a data set. The method checks
     * if the users has permission to download the dataset files first
     *
     * @param sessionId session Id of the user.
     * @param datasetId dataset id
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object     
     * @return URL that will be used to download the dataset
     */
    public String downloadDataset(String sessionId, Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DownloadManager.downloadDataset(userId, sessionId, datasetId, manager);
    }

    /**
     * Checks if user has access to download the files.  Returns a DownloadInfo object with
     * the federal Id if the user and the filenames of the download.
     *      
     * @param sessionId session Id of the user.
     * @param datafileIds ids of the files that are to be downloaded 
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object             
     * @return DownloadInfo download info
     */
    public DownloadInfo checkDatafileDownloadAccess(String sessionId, Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        DownloadInfo downloadInfo = DownloadManager.checkDatafileDownloadAccess(userId, datafileIds, manager);

        //if here, user has access, return downloadInfo
        return downloadInfo;
    }

    /**
     * Checks if user has access to download the dataset.  Returns a DownloadInfo object with
     * the federal Id if the user and the filenames of the download.
     *      
     * @param sessionId session Id of the user.
     * @param datafileIds ids of the files that are to be downloaded 
     * @param manager Entity manager object that will facilitate interaction with underlying database
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object             
     * @return DownloadInfo download info
     */
    public DownloadInfo checkDatasetDownloadAccess(String sessionId, Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        DownloadInfo downloadInfo = DownloadManager.checkDatasetDownloadAccess(userId, datasetId, manager);

        //if here, user has access, return downloadInfo
        return downloadInfo;
    }
}
