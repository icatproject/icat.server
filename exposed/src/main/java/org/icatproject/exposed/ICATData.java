package org.icatproject.exposed;


import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.ws.soap.MTOM;

import org.apache.log4j.Logger;
import org.icatproject.exposed.data.DownloadManagerLocal;


@MTOM(threshold=10000) //10KB (threshold to move to MTOM)
@Stateless()
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ICATData extends EJBObject /*implements ICATLocal*/ {
    
    static Logger log = Logger.getLogger(ICATData.class);
    
    static {
     //   HttpAdapter.dump=false; //dont log SOAP
    }
      
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
    //@WebMethod
    /*public @WebResult(name = "URL") String downloadDatafile(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "datafileId") Long datafileId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException{
        return  downloadManagerLocal.downloadDatafile(sessionId, datafileId);
    }*/
    
    /**
     * Downloads a dataset
     *
     * @param sessionId
     * @param datasetId
     * @return DataHandler of the zipped dataset
     */
    //@WebMethod
  /*  public @WebResult(name = "URL") String downloadDataset(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "datasetId") Long datasetId)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        return downloadManagerLocal.downloadDataset(sessionId, datasetId);
    }*/
}
