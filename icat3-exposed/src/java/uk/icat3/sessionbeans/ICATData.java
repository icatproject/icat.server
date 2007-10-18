/*
 * AllOperationsBean.java
 *
 * Created on 17 May 2007, 10:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans;

import java.net.MalformedURLException;
import javax.activation.DataHandler;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import org.apache.log4j.Logger;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.data.DownloadManagerLocal;
import uk.icat3.data.exceptions.TotalSizeExceededException;
import uk.icat3.sessionbeans.search.DatasetSearchLocal;

/**
 * This adds all the Admin methods to the ICAT methods and protects them with the role 'admin'
 *
 * @author gjd37
 */
@MTOM
@Stateless()
@WebService(serviceName="ICATDataService", targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ICATData extends EJBObject /*implements ICATLocal*/ {
    
    static Logger log = Logger.getLogger(ICATData.class);
    
    @EJB
    protected DownloadManagerLocal downloadManagerLocal;
    
    /** Creates a new instance of AllOperationsBean */
    public ICATData() {
    }
    
    /**
     * Downloads a datafile
     *
     * @param sessionId
     * @param datafileId
     * @return DataHandler of the file
     */
    @WebMethod
    public @XmlMimeType(value="application/octet-stream") DataHandler downloadDatafile(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "datafileId") Long datafileId)  throws SessionException, NoSuchObjectFoundException, TotalSizeExceededException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException{
       return  downloadManagerLocal.downloadDatafile(sessionId, datafileId);
    }
    
    /**
     * Downloads a dataset
     *
     * @param sessionId
     * @param datasetId
     * @return DataHandler of the zipped dataset
     */
    @WebMethod
    public @XmlMimeType(value="application/octet-stream") DataHandler downloadDataset(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "datasetId") Long datasetId)  throws SessionException, NoSuchObjectFoundException, TotalSizeExceededException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        return downloadManagerLocal.downloadDataset(sessionId, datasetId);
    }
}
