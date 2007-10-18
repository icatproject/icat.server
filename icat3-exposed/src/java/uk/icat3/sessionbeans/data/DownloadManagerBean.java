/*
 * DownloadManagerBean.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.data;


import uk.icat3.data.exceptions.TotalSizeExceededException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.xml.ws.soap.MTOM;
import org.apache.log4j.Logger;
import uk.icat3.data.DownloadManager;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.manager.DatafileManagerLocal;
import uk.icat3.sessionbeans.manager.DatasetManagerLocal;
import uk.icat3.sessionbeans.manager.InvestigationManagerLocal;
import uk.icat3.user.UserDetails;
import uk.ac.dl.srbapi.srb.Url;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@MTOM
@Stateless
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DownloadManagerBean extends EJBObject implements DownloadManagerLocal {
    
   static Logger log = Logger.getLogger(DownloadManagerBean.class);
       
        
    public DataHandler downloadDatafile(String sessionId, Long datafileId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException, TotalSizeExceededException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get users credential
        UserDetails userDetails = user.getUserDetails(sessionId, userId);
        
       return DownloadManager.downloadDatafile(userId, datafileId, userDetails.getCredential(), manager);
        
    }
    
    public DataHandler downloadDataset(String sessionId, Long datasetId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException, TotalSizeExceededException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get users credential
        UserDetails userDetails = user.getUserDetails(sessionId, userId);
        
        return DownloadManager.downloadDataset(userId, datasetId, userDetails.getCredential(), manager);
            
    }
    
}
