/*
 * DownloadManagerBean.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.data;


/*import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.xml.ws.soap.MTOM;
import org.apache.log4j.Logger;
import uk.icat3.data.DownloadManager;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserDetails;
import uk.ac.dl.srbapi.srb.Url;
import uk.ac.dl.srbapi.util.IOTools;
import uk.icat3.exceptions.NoSuchObjectFoundException;*/

/**
 *
 * @author gjd37
 */
//@Stateless
//@Interceptors(ArgumentValidator.class)
//@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DownloadManagerBean /*extends EJBObject implements DownloadManagerLocal*/ {
    
    //static Logger log = Logger.getLogger(DownloadManagerBean.class);
    
    
   /* public String downloadDatafile(String sessionId, Long datafileId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get users credential
        UserDetails userDetails = user.getUserDetails(sessionId, userId);
        
        File file = DownloadManager.downloadDatafile(userId, datafileId, userDetails.getCredential(), FACILITY, manager);
        
        return  generateDownloadUrl(file, sessionId);
    }
    
    public String downloadDataset(String sessionId, Long datasetId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        //get users credential
        UserDetails userDetails = user.getUserDetails(sessionId, userId);
        
        File file = DownloadManager.downloadDataset(userId, datasetId, userDetails.getCredential(), FACILITY,  manager);
        
        return generateDownloadUrl(file, sessionId);
    }
    
    private String generateDownloadUrl(File file, String sessionId) throws DownloadException{
        String hostUrl = facilityProps.getProperty("facility.host");
        if(hostUrl == null) throw new DownloadException("Icat not configured correctly for download");
                
        int index = file.getAbsolutePath().lastIndexOf(FACILITY);
        String fileReturned = file.getAbsolutePath().substring(index+FACILITY.length()+1, file.getAbsolutePath().length());
        index = fileReturned.indexOf(File.separator);
        fileReturned = fileReturned.substring(index +1 , fileReturned.length());
                
        StringBuilder builder = new StringBuilder(hostUrl);
        builder.append("?sid="+sessionId+"&name="+file.getName()+"&file="+fileReturned);
        return builder.toString();
    }*/
}
