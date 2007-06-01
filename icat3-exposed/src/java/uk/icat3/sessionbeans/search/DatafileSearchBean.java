/*
 * DatafileSearchBean.java
 *
 * Created on 27 March 2007, 10:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.DatafileSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author gjd37
 */
@Stateless
@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DatafileSearchBean extends EJBObject implements DatafileSearchLocal {

    /** Creates a new instance of DatafileSearchBean */
    public DatafileSearchBean() {
    }

    @WebMethod()
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, Long startRun, Long endRun) throws SessionException {

        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DatafileSearch.searchByRunNumber(userId, instruments, startRun, endRun, manager);
    }
}
