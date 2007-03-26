/*
 * DatasetManagerBean.java
 *
 * Created on 26 March 2007, 15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionLocal;

/**
 *
 * @author gjd37
 */
@Stateless()
@WebService()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatafileManagerBean extends EJBObject implements DatafileManagerLocal {
    
    static Logger log = Logger.getLogger(DatafileManagerBean.class);
    
    @EJB
    UserSessionLocal user;
    
    /** Creates a new instance of DatasetManagerBean */
    public DatafileManagerBean() {}
    
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DataFileManager.getDataFile(userId, datafileId, manager);        
    }
    
    
}
