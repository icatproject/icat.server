/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;

import org.apache.log4j.Logger;

import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.FacilityUser;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.FacilityManager;
import uk.icat3.sessionbeans.EJBObject;

/**
 * 
 * @author tang76
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class FacilityManagerBean extends EJBObject implements FacilityManagerLocal {

	static Logger log = Logger.getLogger(InvestigationManagerBean.class);

	/** Creates a new instance of InvestigationManagerBean */
	public FacilityManagerBean() {
	}

	@Override
	public FacilityUser getFacilityUserByFacilityUserId(String sessionId, String facilityUserId)
			throws SessionException {
		return FacilityManager.getFacilityUserByFacilityUserId(facilityUserId, this.manager);
	}

	@Override
	public FacilityUser getFacilityUserByFederalId(String sessionId, String federalId) throws SessionException,
			NoSuchObjectFoundException {
		return FacilityManager.getFacilityUserByFederalId(federalId, this.manager);
	}

	/**
	 * Lists all the FacilityCycles in the DB
	 * 
	 * @param sessionId
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return collection of FacilityCycles
	 */
	@Override
	@WebMethod()
	public Collection<FacilityCycle> listAllFacilityCycles(String sessionId) throws SessionException {
		return FacilityManager.listAllFacilityCycles(this.manager);
	}
}
