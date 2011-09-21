/*
 * InvestigationManagerBean.java
 *
 * Created on 26 March 2007, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.log4j.Logger;

import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.InvestigationInclude;

/**
 * This web service exposes the functions that are needed on investigation
 * 
 * @author gjd37
 */
@Stateless()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InvestigationManagerBean extends EJBObject implements InvestigationManagerLocal {

	static Logger log = Logger.getLogger(InvestigationManagerBean.class);

	/** Creates a new instance of InvestigationManagerBean */
	public InvestigationManagerBean() {
	}

	/**
	 * Returns a {@link Investigation} investigation from a {@link Investigation} id if the user has
	 * access to the investigation.
	 * 
	 * @param sessionId
	 *            sessionid of the user.
	 * @param investigationId
	 *            Id of investigations
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return {@link Investigation} object
	 */
	@WebMethod(operationName = "getInvestigation")
	@RequestWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationDefault")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationDefaultResponse")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		return InvestigationManager.getInvestigation(userId, investigationId, manager);
	}

	/**
	 * Returns a {@link Investigation} from a {@link Investigation} id if the user has access to the
	 * investigation. Also gets extra information regarding the investigation. See
	 * {@link InvestigationInclude}
	 * 
	 * @param sessionId
	 *            sessionid of the user.
	 * @param investigationId
	 *            Id of investigations
	 * @param includes
	 *            information that is needed to be returned with the investigation
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return {@link Investigation} object
	 */
	@WebMethod(operationName = "getInvestigationIncludes")
	@RequestWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationIncludes")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationIncludesResponse")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes)
			throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		Investigation investigation = InvestigationManager.getInvestigation(userId, investigationId, manager);

		// now set the investigation includes for JAXB web service
		Collection<Investigation> investigations = new ArrayList<Investigation>();
		investigations.add(investigation);
		InvestigationManager.getInvestigationInformation(userId, investigations, includes, manager);

		return investigation;
	}

	/**
	 * Returns a Collection of {@link Investigation}s from a Collection of {@link Investigation} ids
	 * if the user has access to the investigations. Also gets extra information regarding the
	 * investigations. See {@link InvestigationInclude}
	 * 
	 * @param sessionId
	 *            sessionid of the user.
	 * @param investigationIds
	 *            Id of investigations
	 * @param includes
	 *            information that is needed to be returned with the investigation
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return Collection of {@link Investigation} objects
	 */
	@WebMethod(operationName = "getInvestigationsIncludes")
	@RequestWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationsIncludes")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.manager.getInvestigationsIncludesResponse")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Collection<Investigation> getInvestigations(String sessionId, Collection<Long> investigationIds,
			InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		Collection<Investigation> investigations = InvestigationManager.getInvestigations(userId, investigationIds,
				manager);

		// now set the investigation includes for JAXB web service
		InvestigationManager.getInvestigationInformation(userId, investigations, includes, manager);

		return investigations;
	}

}
