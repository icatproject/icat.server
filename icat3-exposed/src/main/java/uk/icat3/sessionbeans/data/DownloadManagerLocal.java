/*
 * DownloadManagerLocal.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.data;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.data.DownloadInfo;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;

/**
 *
 * @author gjd37
 */
@Local
public interface DownloadManagerLocal {

    String downloadDatafile(String sessionId, Long datafileId) throws SessionException, NoSuchObjectFoundException,  InsufficientPrivilegesException;

    String downloadDataset(String sessionId, Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    String downloadDatafiles(String sessionId, Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    DownloadInfo checkDatasetDownloadAccess(String sessionId, Long datasetId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    DownloadInfo checkDatafileDownloadAccess(String sessionId, Collection<Long> datafileIds) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;
}
