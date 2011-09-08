package uk.icat3.sessionbeans.manager;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;

import org.apache.log4j.Logger;

import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DatafileManager;
import uk.icat3.sessionbeans.EJBObject;

@Stateless()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DatafileManagerBean extends EJBObject implements DatafileManagerLocal {

	static Logger log = Logger.getLogger(DatafileManagerBean.class);

	public DatafileManagerBean() {
	}


	/**
	 * Gets a data file object from a data file id, depending if the user has access to read the
	 * data file
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datafileId
	 *            Id of data file
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return {@link Datafile}
	 */
	@WebMethod()
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Datafile getDatafile(String sessionId, Long datafileId) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		return DatafileManager.getDataFile(userId, datafileId, manager);
	}

	/**
	 * Gets a collection of data file object from a collection of data file ids, depending if the
	 * user has access to read the data file
	 * 
	 * @param sessionId
	 *            session id of the user. @WebMethod() public void deleteDatafile(String sessionId,
	 *            Datafile datafile) throws SessionException, NoSuchObjectFoundException,
	 *            InsufficientPrivilegesException, ValidationException { String userId =
	 *            user.getUserIdFromSessionId(sessionId); DataFileManager.deleteDatafile(userId,
	 *            datafile, manager); }
	 * @param datafileIds
	 *            collection of data file ids
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return collection of {@link Datafile} objects
	 */
	@WebMethod()
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		return DatafileManager.getDataFiles(userId, datafileIds, manager);
	}




}
