/*
 * XMLIngestionManagerLocal.java
 *
 * Created on 15-Aug-2007, 10:10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;

/**
 *
 * @author gjd37
 */
@Local
public interface XMLIngestionManagerLocal {

    Long[] ingestMetadata(String sessionId, String xml) throws SessionException, ValidationException, InsufficientPrivilegesException, ICATAPIException;
}
