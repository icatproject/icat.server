/*
 * DownloadManagerLocal.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.data;

import uk.icat3.data.exceptions.TotalSizeExceededException;
import java.net.MalformedURLException;
import javax.activation.DataHandler;
import javax.ejb.Local;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;

/**
 *
 * @author gjd37
 */
@Local
public interface DownloadManagerLocal {
    
    public DataHandler downloadDatafile(String sessionId, Long datafileId)  throws SessionException, NoSuchObjectFoundException, TotalSizeExceededException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException ;
    
    public DataHandler downloadDataset(String sessionId, Long datasetId)  throws SessionException, NoSuchObjectFoundException, TotalSizeExceededException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException ;
        
}
