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

    //implement method here
    public void ingestXML() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 
}
