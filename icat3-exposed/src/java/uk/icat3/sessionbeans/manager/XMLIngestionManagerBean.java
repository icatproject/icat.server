/*
 * XMLIngestionManagerBean.java
 *
 * Created on 15-Aug-2007, 10:10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;


import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.jaxb.MetadataIngest;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 * This web service exposes the functions that are needed on investigation
 *
 * @author gjd37
 */
@Stateless()
//@WebService(/*name="XMLIngestionManagerService",*/targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XMLIngestionManagerBean extends EJBObject implements XMLIngestionManagerLocal {
    
    static Logger log = Logger.getLogger(XMLIngestionManagerBean.class);

    /**
     * Method that accepts XML document in the form of a String for ingestion into ICAT
     * Spawns insert off to asynchronous MessageDrivenBean for efficiency
     *
     * @param sessionId
     * @param xml   
     * @throws java.lang.Exception    
     */
    public Long[] ingestMetadata(String sessionId, String xml) throws SessionException, ValidationException, InsufficientPrivilegesException, ICATAPIException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return MetadataIngest.ingestMetadata(userId, xml, manager);
    }
}
