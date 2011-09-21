/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.web;

import java.util.List;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import uk.icat3.logging.entity.LoggedDownload;
import uk.icat3.logging.util.LoggingBeanRemote;

/**
 *
 * @author scb24683
 */
@WebService(serviceName="ICATLoggingService")
@Stateless()
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class ICATLogging {

    @EJB
    private LoggingBeanRemote logging;

    @WebMethod
    public List<LoggedDownload> getDataDownloadsByInvestigation(@WebParam(name="sessionId") String sessionId, @WebParam(name="investigationId") Long investigationId) {
        return this.logging.getDataDownloadsByInvestigation(sessionId, investigationId);
    }

    @WebMethod
    public List<LoggedDownload> getDataDownloadsByDatafile(@WebParam(name="sessionId") String sessionId, @WebParam(name="datafileId") Long datafileId) {
        return this.logging.getDataDownloadsByDatafile(sessionId, datafileId);
    }

    @WebMethod
    public List<LoggedDownload> getWhoDownloadedMyData(@WebParam(name="sessionId") String sessionId) {
        return this.logging.getWhoDownloadedMyData(sessionId);
    }
}
