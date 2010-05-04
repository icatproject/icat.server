/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import org.apache.log4j.Logger;
import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.FacilityUser;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.FacilityManager;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 *
 * @author tang76
 */
@Stateless
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class FacilityManagerBean extends EJBObject implements FacilityManagerLocal {

    static Logger log = Logger.getLogger(InvestigationManagerBean.class);

    /** Creates a new instance of InvestigationManagerBean */
    public FacilityManagerBean() {}
    
    /**
     *  Lists all the FacilityCycles in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of FacilityCycles
     */
    @WebMethod()
    public Collection<FacilityCycle> listAllFacilityCycles(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return FacilityManager.listAllFacilityCycles(manager);
    }

    public FacilityUser getFacilityUserByFacilityUserId(String sessionId, String facilityUserId) throws SessionException {
        String userId = user.getUserIdFromSessionId(sessionId);
        return FacilityManager.getFacilityUserByFacilityUserId(facilityUserId, manager);
    }

     public FacilityUser getFacilityUserByFederalId(String sessionId, String federalId) throws SessionException, NoSuchObjectFoundException {
        String userId = user.getUserIdFromSessionId(sessionId);
        return FacilityManager.getFacilityUserByFederalId(federalId, manager);
    }
}
